package com.aixforce.admin.web.controller;

import com.aixforce.admin.service.OrderCommentJobService;
import com.aixforce.common.model.Response;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopExtraService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.aixforce.user.util.UserVerification.isNotAdmin;

/**
 * Date: 5/14/14
 * Time: 18:58
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Slf4j
@Controller
@RequestMapping("/api/order-comment/job")
public class CommentJobController {
    @Autowired
    OrderCommentJobService orderCommentJobService;
    @Autowired
    private OrderCommentService orderCommentService;
    @Autowired
    MessageSources messageSources;

    @Autowired
    ShopExtraService shopExtraService;

    @Autowired
    ShopService shopService;


    @RequestMapping(value = "/delta-dump", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deltaDump() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (!isAdmin(baseUser)) {
            return "not a admin!";
        }

        Response<Boolean> tryDeltaDump = orderCommentJobService.calcShopExtraScore();
        if (tryDeltaDump.isSuccess()) {
            return "ok";
        } else {
            return messageSources.get(tryDeltaDump.getError());
        }
    }

    @RequestMapping(value = "/full-dump", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String fullDump() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (!isAdmin(baseUser)) {
            return "not a admin!";
        }

        Response<Boolean> tryDeltaDump = orderCommentJobService.fullDumpShopExtraScore();
        if (tryDeltaDump.isSuccess()) {
            return "ok";
        } else {
            return messageSources.get(tryDeltaDump.getError());
        }
    }

    @RequestMapping(value = "/shops/{shopId}/comments/dump", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String shopDump(@PathVariable("shopId") Long shopId) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (isNotAdmin(baseUser)) {
            return "not a admin!";
        }
        Response<Boolean> result = orderCommentService.shopDump(shopId);
        if(!result.isSuccess()) {
            log.error("fail to full dump shop score by shopId={}, error code:{}",shopId, result.getError());
            return "fail";
        }
        return "succeed";
    }

    @RequestMapping(value = "/expire-comment", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String expire() {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (isNotAdmin(baseUser)) {
            return "not a admin!";
        }

        Response<Boolean> tryExpire = orderCommentJobService.expireOrderComment();
        if (tryExpire.isSuccess()) {
            return "ok";
        } else {
            return messageSources.get(tryExpire.getError());
        }
    }

    //测试商铺评分累加
    @RequestMapping(value = "/sum/score", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean sumShopScore() {
        Response<Boolean> result = new Response<Boolean>();

        try {
            Response<Long> maxIdGet = shopService.maxId();
            if (!maxIdGet.isSuccess()) {
                log.error("get max id of shop fail, error code:{}", maxIdGet.getError());
                result.setError("shop.get.max.id.fail");
                return result.getResult();
            }
            Long lastId = maxIdGet.getResult();
            Integer returnSize = 200;

            while (Objects.equal(returnSize, 200)) {
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
                final List<Long> ids= Lists.transform(shops, new Function<Shop, Long>() {
                    @Override
                    public Long apply(Shop shop) {
                        return shop.getId();
                    }
                });
                orderCommentService.sumUpCommentScoreYestDayByShopIds(ids);

                lastId = shops.get(shops.size()-1).getId();
                returnSize = shops.size();
                verificationShopExtra(ids);
            }
            result.setResult(true);
            return result.getResult();
        } catch (Exception e) {
            log.error("`fullDumpShopExtraScore` invoke fail. e:{}", e);
            result.setError("shop.extra.full.dump.fail");
            return result.getResult();
        }
    }


    //验证店铺积分
    public void verificationShopExtra(List<Long> ids){
        for(Long id : ids) {
            try {
                ShopExtra exist = shopExtraService.findByShopId(id).getResult();
                if(!isCorrect(exist)){
//                    Response<Boolean> isUpdate=  orderCommentService.sumUpCommentScoreByShopId(id);
//                    if (!isUpdate.isSuccess()) {
//                        log.error("fail to full update shop score shopId={}, error code:{}, skip it", id, isUpdate.getError());
//                    }else {
//                        log.warn("success to full update shop score shopId={}",id);
//                    }
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


}
