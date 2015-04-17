package com.aixforce.admin.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopExtraService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.trade.service.OrderQueryService;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.aixforce.common.utils.Arguments.notNull;

/**
 * Date: 14-2-26
 * Time: PM1:38
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Service
@Slf4j
public class OrderCommentJobService {

    @Autowired
    OrderCommentService orderCommentService;

    @Autowired
    ShopService shopService;

    @Autowired
    OrderQueryService orderQueryService;

    @Autowired
    ShopExtraService shopExtraService;

    private final static Integer PAGE_SIZE = 200;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * 定时执行的任务，由 aix－admin 调用，根据用户评价计算 shop 的平均分数
     * @return 是否成功修改
     */
    public Response<Boolean> calcShopExtraScore() {
        Response<Boolean> result = new Response<Boolean>();

        Response<Long> maxIdGet = shopService.maxId();
        if (!maxIdGet.isSuccess()) {
            log.error("get max id of shop fail, error code:{}", maxIdGet.getError());
            result.setError("shop.get.max.id.fail");
            return result;
        }
        Long lastId = maxIdGet.getResult();
        Integer returnSize = PAGE_SIZE;

        while (Objects.equal(returnSize, PAGE_SIZE)) {
            try {
                Response<List<Shop>> shopsGet = shopService.forDump(lastId, returnSize);
                if (!shopsGet.isSuccess()) {
                    log.error("get shops for dump by lastId={}, returnSize{} fail, error code:{}",
                            lastId, returnSize, shopsGet.getError());
                    //如果一次查找失败，直接跳过
                    continue;
                }
                List<Shop> shops = shopsGet.getResult();
                if (shops.isEmpty()) {
                    break;
                }

                List<ShopExtra> extras = Lists.newArrayList();
                for (Shop s : shops) {
                    Response<List<OrderComment>> OCLGet = orderCommentService.getYesterdayCommentForShop(s.getId());
                    if (!OCLGet.isSuccess()) {
                        log.error("get order comment for shop fail, shop:(id={}), error code:{}", s.getId(), OCLGet.getError());
                        continue;
                    }
                    List<OrderComment> ocl = OCLGet.getResult();
                    if (ocl.isEmpty()) {
                        //log.warn("not new comment for shop at yesterday, shop:{}", s);
                        continue;
                    }

                    ShopExtra extra = new ShopExtra();
                    extra.setShopId(s.getId());
                    for (OrderComment c : ocl) {
                        extra.addRDescribe((long) c.getRDescribe());
                        extra.addRExpress((long) c.getRExpress());
                        extra.addRQuality((long) c.getRQuality());
                        extra.addRService((long) c.getRService());
                    }
                    extra.addTradeQuantity((long) ocl.size());
                    extras.add(extra);
                }


                for (ShopExtra extra : extras) {
                    log.warn("extra -> id:{}, shopId:{}, rService:{}, rExpress:{}, rDescribe:{}, rQuality:{}, quantity:{}", extra.getId(),
                            extra.getShopId(), extra.getRService(), extra.getRExpress(), extra.getRDescribe(), extra.getRQuality(), extra.getTradeQuantity());

                    if (notNull(extra.getRService()) && notNull(extra.getTradeQuantity()) && (extra.getRService() / extra.getTradeQuantity() > 5.0)) {
                        log.warn("extra -> id:{} rService:{} quantity:{}  overflow!!!", extra.getId(), extra.getRService(), extra.getTradeQuantity());
                    }
                }



//                Response<List<Long>> tryUpdate = shopService.bulkUpdateShopExtraScore(extras);
//                if (!tryUpdate.isSuccess()) {
//                    log.warn("bulk update shop extra fail, error code:{}", tryUpdate.getError());
//                }
                lastId = shops.get(shops.size() - 1).getId();
                returnSize = shops.size();
            } catch (Exception e) {
                log.error("calc avg score for shops by lastId={},size={}. fail, ",
                        lastId, returnSize, e);
                result.setError("shop.extra.delta.dump.fail");
                return result;
            }
        }

        result.setResult(true);
        return result;
    }

    /**
     * 计划任务 动态对shop进行分页 并对每个shop下昨天的订单评论进行一次统计，并更新到shopExtras中
     * @return 是否操作成功
     */
    public Response<Boolean> statisticsShopExtraScore(){

        Response<Boolean> result = new Response<Boolean>();

        try {
            //得到shops表最大的id
            Response<Long> maxIdGet = shopService.maxId();
            if (!maxIdGet.isSuccess()) {
                log.error("get max id of shop fail, error code:{}", maxIdGet.getError());
                result.setError("shop.get.max.id.fail");
                return result;
            }
            Long lastId = maxIdGet.getResult();
            Integer returnSize = PAGE_SIZE;
            //动态分页 每次查出PAGE_SIZE条数据 当returnSize不等于PAGE_SIZE 说明已经查到最后一页了
            while (Objects.equal(returnSize, PAGE_SIZE)) {
                Response<List<Shop>> shopsGet = shopService.forDump(lastId, returnSize);
                if (!shopsGet.isSuccess()) {
                    log.error("get shops for dump fail, lastId={}, size={}, error code:{}",
                            lastId, returnSize, shopsGet.getError());
                    continue;
                }
                final List<Shop> shops = shopsGet.getResult();
                if (shops.isEmpty()) {
                    break;
                }

                final List<Long> ids=Lists.transform(shops, new Function<Shop, Long>() {
                    @Override
                    public Long apply(Shop shop) {
                        return shop.getId();
                    }
                });

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        orderCommentService.sumUpCommentScoreYestDayByShopIds(ids);
                    }
                });

                lastId = shops.get(shops.size()-1).getId();
                returnSize = shops.size();
                //验证店铺评分
                verificationShopExtra(ids);
            }
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`fullDumpShopExtraScore` invoke fail. e:{}", e);
            result.setError("shop.extra.full.dump.fail");
            return result;
        }

    }
    //验证店铺积分
    public void verificationShopExtra(List<Long> ids){
        for(Long id : ids) {
            try {
                ShopExtra exist = shopExtraService.findByShopId(id).getResult();
                if(!isCorrect(exist)) {
                    //如果存在错误数据就进行一次全量统计
                    Response<Boolean> isUpdate=  orderCommentService.sumUpCommentScoreByShopId(id);
                    if (!isUpdate.isSuccess()) {
                        log.error("fail to full update shop score shopId={}, error code:{}, skip it", id, isUpdate.getError());
                    }else {
                    log.warn("success to full update shop score shopId={}", id);
                }
                }
            } catch (Exception e) {
                log.error("`verificationShopExtra` invoke fail, shopId={}, cause:{}, skip it",
                        id, Throwables.getStackTraceAsString(e));
            }
        }
    }


    /**
     * 判断该店铺4种评分是否都正确 <=5
     * @param extra 店铺评分记录
     * @return 是否正确
     */
    public Boolean isCorrect(ShopExtra extra){

        Double tradeQuantity=0.0;
        if(notNull(extra.getTradeQuantity())){
            tradeQuantity=extra.getTradeQuantity().doubleValue();
        }

        if(tradeQuantity>0.0&&notNull(extra.getRService())&&notNull(extra.getRDescribe()) &&notNull(extra.getRQuality()) &&notNull(extra.getRExpress())
                &&(extra.getRDescribe()/tradeQuantity<=5)&&(extra.getRQuality()/tradeQuantity<=5)&&(extra.getRExpress()/tradeQuantity<=5)&&
                (extra.getRService()/tradeQuantity<=5)){
            return Boolean.TRUE;
        }
        if(tradeQuantity==0.0){
            //说明该店铺目前还没有一条评论
            return Boolean.TRUE;
        }

        log.warn("full_shop_extra ->id:{}, shopId:{}, rService:{}, rExpress:{}, rDescribe:{}, rQuality:{}, quantity:{} unqualified!!!",
                extra.getId(),extra.getShopId(), extra.getRService(), extra.getRExpress(), extra.getRDescribe(), extra.getRQuality(), extra.getTradeQuantity());


        return Boolean.FALSE;

    }

    public Response<Boolean> fullDumpShopExtraScore() {
        Response<Boolean> result = new Response<Boolean>();

        try {
            Response<Long> maxIdGet = shopService.maxId();
            if (!maxIdGet.isSuccess()) {
                log.error("get max id of shop fail, error code:{}", maxIdGet.getError());
                result.setError("shop.get.max.id.fail");
                return result;
            }
            Long lastId = maxIdGet.getResult();
            Integer returnSize = PAGE_SIZE;

            while (Objects.equal(returnSize, PAGE_SIZE)) {
                Response<List<Shop>> shopsGet = shopService.forDump(lastId, returnSize);
                if (!shopsGet.isSuccess()) {
                    log.error("get shops for dump fail, lastId={}, size={}, error code:{}",
                            lastId, returnSize, shopsGet.getError());
                    continue;
                }
                final List<Shop> shops = shopsGet.getResult();
                if (shops.isEmpty()) {
                    break;
                }

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        orderCommentService.sumUpCommentScoreByShopIds(Lists.transform(shops, new Function<Shop, Long>() {
                            @Override
                            public Long apply(Shop shop) {
                                return shop.getId();
                            }
                        }));
                    }
                });

                lastId = shops.get(shops.size()-1).getId();
                returnSize = shops.size();
            }
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`fullDumpShopExtraScore` invoke fail. e:{}", e);
            result.setError("shop.extra.full.dump.fail");
            return result;
        }
    }

    public Response<Boolean> expireOrderComment() {
        Response<Boolean> result = new Response<Boolean>();


        try {
            // 关闭：15天前，未评论，交易成功的订单
            Response<List<OrderItem>> itemGet = orderQueryService.findExpiredUncommentedOrderItemId(15);
            if (!itemGet.isSuccess()) {
                log.error("method 'expireOrderComment' cannot find any orderItem");
                result.setError("comment.job.query.order.fail");
                return result;
            }
            List<OrderItem> avaliableOIList = itemGet.getResult();
            if (avaliableOIList.isEmpty()) {
                result.setResult(true);
                return result;
            }
            Iterator<OrderItem> avaliableOI = avaliableOIList.iterator();

            Response<Long> lastId = orderCommentService.maxId();
            if (!lastId.isSuccess()) {
                log.error("find max id for order comment fail, error code:{}", lastId.getError());
                result.setError(lastId.getError());
                return result;
            }
            Long maxId = lastId.getResult()+1;
            Integer returnSize = PAGE_SIZE;

            while (Objects.equal(returnSize, PAGE_SIZE)) {
                Response<List<Long>> idR = orderCommentService.forExpire(maxId, PAGE_SIZE);
                final List<Long> ids = idR.getResult();
                if (!idR.isSuccess()) {
                    log.error("method 'expireOrderComment' cannot find any Comment");
                    result.setError("comment.job.query.comment.fail");
                    return result;
                }

                if (!ids.isEmpty()) {
                    avaliableOI = Iterators.filter(avaliableOI, new Predicate<OrderItem>() {
                        @Override
                        public boolean apply(@Nullable OrderItem input) {
                            return !ids.contains(input.getId());
                        }
                    });

                    maxId = ids.get(ids.size()-1);
                    returnSize = ids.size();
                } else {
                    break;
                }
            }

            // 做差集得到未评论，已过可评论期的子订单
            List<OrderItem> eAvaliableIO = Lists.newArrayList(avaliableOI);
            if (eAvaliableIO.isEmpty()) {
                result.setResult(true);
                return result;
            }

            orderCommentService.createCommentForExpiredOrderItem(eAvaliableIO);
            result.setResult(true);
        } catch (Exception e) {
            log.error("fail to expire order 15 days ago,", e);
            result.setError("expire.order.fail");
        }

        return result;
    }
}
