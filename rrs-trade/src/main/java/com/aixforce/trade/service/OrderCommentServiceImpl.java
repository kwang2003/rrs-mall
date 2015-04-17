package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.OrderCommentDao;
import com.aixforce.trade.dao.OrderDao;
import com.aixforce.trade.dao.OrderItemDao;
import com.aixforce.trade.dto.RichOrderComment;
import com.aixforce.trade.dto.RichOrderCommentForBuyer;
import com.aixforce.trade.dto.RichOrderCommentForSeller;
import com.aixforce.trade.manager.OrderCommentManager;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.aixforce.common.utils.Arguments.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.aixforce.common.utils.Arguments.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Date: 14-2-12
 * Time: PM3:55
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Slf4j
@Service
public class OrderCommentServiceImpl implements OrderCommentService {

    private static final Integer PAGE_SIZE = 200;

    @Autowired
    OrderCommentDao commentDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    OrderDao orderDao;

    @Autowired
    ShopService shopService;

    @Autowired
    AccountService<User> accountService;

    @Autowired
    OrderCommentManager commentManager;

    @Autowired
    ItemService itemService;

    @Autowired
    OrderWriteService orderWriteService;


    /**
     * 用户创建订单的评价
     *
     * @param orderId  主订单id
     * @param comments 评价对象列表
     * @param   userId 评论者id
     * @return 成功创建的评价 id
     */
    @Override
    public Response<List<Long>> create(Long orderId, List<OrderComment> comments, Long userId) {
        Response<List<Long>> result = new Response<List<Long>>();

        if (orderId == null) {
            log.error("order id can not be null");
            result.setError("order.comment.invalid.argument");
            return result;
        }

        for (OrderComment comment : comments) {
            if (!comment.ifValid()) {
                // 若评价数据不合法
                log.error("bad argument: comment: {}", comment);
                result.setError("order.comment.invalid.argument");
                return result;
            }
        }

        Order order = orderDao.findById(orderId);
        if (order == null) {
            log.error("no order found for id {}", orderId);
            result.setError("order.comment.find.order.fail");
            return result;
        }

        if (!Objects.equal(order.getBuyerId(), userId)) {//只有买家才能评论

            log.error("buyer mismatch,  expected buyer id: {}, but actual user id: {}, ",
                    order.getBuyerId(), userId);
            result.setError("order.comment.not.own.order");
            return result;
        }

        //只有已经确认收货的订单才能被评论
        if (order.getStatus() != Order.Status.DONE.value()) {
            log.error("failed to create comment, order not done yet ,  order(id={}) wrong status: {}",orderId, order.getStatus() );
            result.setError("order.comment.wrong.order.status");
            return result;
        }

        final List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            log.error("no order items found for orderId ({})", orderId);
            result.setError("order.comment.find.order.item.fail");
            return result;
        }

        //检查是否有子订单已经被评论
        for (OrderItem oi : orderItems) {
            if (Objects.equal(oi.getStatus(), OrderItem.Status.DONE.value())
                    && Objects.equal(oi.getHasComment(), Boolean.TRUE)) {
                log.error("orderItem(id={}) has been comment, it can not be comment twice", oi.getId());
                result.setError("order.has.commented");
                return result;
            }
        }

        //查找并设置评论的shopId
        Response<Shop> shopR = shopService.findByUserId(order.getSellerId());
        if (!shopR.isSuccess()) {
            log.error("failed to  find shop by seller id: {}", order.getSellerId());
            result.setError(shopR.getError());
            return result;
        }

        Set<OrderComment> refined = Sets.newHashSetWithExpectedSize(comments.size());
        for (OrderComment comment : comments) {
            boolean orderItemExist = false;
            for (OrderItem orderItem : orderItems) {
                //忽略定金子订单
                if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                    continue;
                }
                if(Objects.equal(orderItem.getId(),comment.getOrderItemId())){
                    orderItemExist = true;
                    comment.setOrderType(order.getType());
                    comment.setItemId(orderItem.getItemId());
                    comment.setShopId(shopR.getResult().getId());
                    comment.setBuyerId(userId);
                    refined.add(comment);
                    break;
                }
            }
            if(!orderItemExist){
                log.error("orderItem(id={}) doesn't belong to order(id={})", comment.getOrderItemId(), orderId);
                result.setError("order.comment.invalid.argument");
                return result;
            }
        }

        List<Long> idList = commentManager.create(refined);
        result.setResult(idList);
        return result;
    }

    /**
     * 根据评价对象的 id，返回评价具体内容
     *
     * @param id 评价对象的 id
     * @return 指定 ID 的评价对象
     */
    @Override
    public Response<OrderComment> findById(Long id) {
        Response<OrderComment> result = new Response<OrderComment>();

        if (id == null) {
            log.error("method 'findById' cannot invoke with null id");
            result.setError("order.comment.invalid.argument");
            return result;
        }

        OrderComment comment = commentDao.findById(id);
        if (comment == null) {
            log.error("method 'findById' find comment fail with id:{}", id);
            result.setError("order.comment.find.comment.fail");
            return result;
        }
        result.setResult(comment);
        return result;
    }

    /**
     * 根据子订单对象的 ID 返回评价
     *
     * @param id 子订单ID
     * @return OrderComment 对象
     */
    public Response<OrderComment> findByOrderItemId(Long id) {
        Response<OrderComment> result = new Response<OrderComment>();

        if (id == null) {
            log.error("method 'findById' cannot invoke with null id");
            result.setError("order.comment.invalid.argument");
            return result;
        }

        OrderComment comment = commentDao.findByOrderItemId(id);
        if (comment == null) {
            log.error("method 'findById' find comment fail with id:{}", id);
            result.setError("order.comment.find.comment.fail");
            return result;
        }
        return result;
    }

    /**
     * @param ids    店铺的ID列表
     * @return      对应店铺Id的对象
     */
    @Override
    public Response<Boolean> sumUpCommentScoreByShopIds(List<Long> ids) {
        Response<Boolean> result = new Response<Boolean>();

        for(Long id : ids) {
            try {
                // OrderComment 的分数都是同级的总分，id是交易总笔数
                // !!!TODO: OrderComment 的分数是int，现在没有考虑溢出的问题
                OrderComment sumUp = commentDao.sumUpShopScore(id);

                // 更新店铺评分
                // 如果一个店铺没下有评价，他的分数默认为0，交易笔数为0，由前端处理显示为默认5分
                ShopExtra extra = extraFromSumUp(id, sumUp);
                Response<Boolean> isUpdate = shopService.fullUpdateShopExtraScore(extra);
                if (!isUpdate.isSuccess()) {
                    log.error("fail to full update shop score shopId={}, error code:{}, skip it", id, isUpdate.getError());
                }

            } catch (Exception e) {
                log.error("`sumUpCommentScoreByShopIds` invoke fail, shopId={}, cause:{}, skip it",
                        id, Throwables.getStackTraceAsString(e));
            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }


    /**
     * 根据店铺的ID查找店铺昨天所有评价
     *
     * @param ids    店铺的ID列表
     * @return      店铺积分是否更新成功
     */
    @Override
    public Response<Boolean> sumUpCommentScoreYestDayByShopIds(List<Long> ids) {
        Response<Boolean> result = new Response<Boolean>();

        for(Long id : ids) {
            try {
                // OrderComment 的分数都是同级的总分，id是交易总笔数
                // !!!TODO: OrderComment 的分数是int，现在没有考虑溢出的问题

                OrderComment sumUp = commentDao.sumUpShopScoreYesterday(id);
                log.warn("sumUp -> shopId:{}, rService:{}, rExpress:{}, rDescribe:{}, rQuality:{},tradQuality:{}",
                        id, sumUp.getRService(), sumUp.getRExpress(), sumUp.getRDescribe(), sumUp.getRQuality(),sumUp.getTradeQuantity());

                // 更新店铺评分
                // 如果一个店铺没下有评价，他的分数默认为0，交易笔数为0，由前端处理显示为默认5分
                ShopExtra extra = extraFromSumUp(id, sumUp);
                //打印店铺昨天评价积分日志
                isCorrectYesterday(extra);

                Response<Boolean> isUpdate = shopService.updateShopExtraScore(extra);
                if (!isUpdate.isSuccess()) {
                    log.error("fail to full update shop score shopId={}, error code:{}, skip it", id, isUpdate.getError());
                }

            } catch (Exception e) {
                log.error("`sumUpCommentScoreYestDayByShopIds` invoke fail, shopId={}, cause:{}, skip it",
                        id, Throwables.getStackTraceAsString(e));
            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }


    /**
     * 判断该店铺昨天累计4种评分是否都正确 <=5
     * @param extra 店铺评分记录
     * @return 是否正确
     */
    public Boolean isCorrectYesterday(ShopExtra extra){

        Double tradeQuantity=0.0;
        if(notNull(extra.getTradeQuantity())){
            tradeQuantity=extra.getTradeQuantity().doubleValue();
        }

        log.warn("yesterday_shop_extra -> shopId:{}, rService:{}, rExpress:{}, rDescribe:{}, rQuality:{}, quantity:{}",
               extra.getShopId(), extra.getRService(), extra.getRExpress(), extra.getRDescribe(), extra.getRQuality(), extra.getTradeQuantity());

        if(tradeQuantity>0.0&&notNull(extra.getRService())&&notNull(extra.getRDescribe()) &&notNull(extra.getRQuality()) &&notNull(extra.getRExpress())
                &&(extra.getRDescribe()/tradeQuantity<=5)&&(extra.getRQuality()/tradeQuantity<=5)&&(extra.getRExpress()/tradeQuantity<=5)&&
                (extra.getRService()/tradeQuantity<=5)){
            return Boolean.TRUE;
        }

        if(tradeQuantity==0.0){
            //说明该店铺目前还没有一条评论
            return Boolean.TRUE;
        }

        log.warn("yesterday_shop_extra -> shopId:{}, rService:{}, rExpress:{}, rDescribe:{}, rQuality:{}, quantity:{} unqualified!!!",
                extra.getShopId(), extra.getRService(), extra.getRExpress(), extra.getRDescribe(), extra.getRQuality(), extra.getTradeQuantity());


        return Boolean.FALSE;

    }



    /**
     * @param id    店铺的ID
     * @return      对应店铺评价积分更新是否成功
     */
    @Override
    public Response<Boolean> sumUpCommentScoreByShopId(Long id) {
        Response<Boolean> result = new Response<Boolean>();

            try {
                OrderComment sumUp = commentDao.sumUpShopScore(id);

                // 更新店铺评分
                // 如果一个店铺没下有评价，他的分数默认为0，交易笔数为0，由前端处理显示为默认5分
                ShopExtra extra = extraFromSumUp(id, sumUp);
                Response<Boolean> isUpdate = shopService.fullUpdateShopExtraScore(extra);
                if (!isUpdate.isSuccess()) {
                    log.error("fail to full update shop score shopId={}, error code:{}, skip it", id, isUpdate.getError());
                }

            } catch (Exception e) {
                log.error("`sumUpCommentScoreByShopIds` invoke fail, shopId={}, cause:{}, skip it",
                        id, Throwables.getStackTraceAsString(e));
            }
        result.setResult(Boolean.TRUE);
        return result;
    }

    private ShopExtra extraFromSumUp(Long id, OrderComment sumUp) {
        ShopExtra extra = new ShopExtra();
        extra.setShopId(id);
        // if trade quantity less then one, set all score to 0
        int switchOff = sumUp.getTradeQuantity()>=1 ? 1 : 0;
        extra.setRQuality((long)sumUp.getRQuality() * switchOff);
        extra.setRDescribe((long) sumUp.getRDescribe() * switchOff);
        extra.setRService((long) sumUp.getRService() * switchOff);
        extra.setRExpress((long) sumUp.getRExpress() * switchOff);
        extra.setTradeQuantity(
            sumUp.getTradeQuantity() >= 1 ? sumUp.getTradeQuantity() : 0l);
        return extra;
    }

    /**
     * 根据卖家 ID 返回分页的评介
     *
     * @param pageNo        顾名思义
     * @param size          顾名思义
     * @param currentSeller 自动注入当前用户
     * @return 分页的订单评价
     */
    @Override
    public Response<Paging<RichOrderCommentForSeller>> findBySellerId(Integer pageNo, Integer size,
                                                                      @ParamInfo("baseUser") BaseUser currentSeller) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<RichOrderCommentForSeller>> result = new Response<Paging<RichOrderCommentForSeller>>();

        if (!Objects.equal(currentSeller.getTypeEnum(), BaseUser.TYPE.SELLER) &&
                !Objects.equal(currentSeller.getTypeEnum(), BaseUser.TYPE.ADMIN)) {
            log.error("method 'findBySellerId' invoke with wrong user type, user:{}", currentSeller);
            result.setError("order.comment.query.without.auth");
            return result;
        }

        Response<Shop> shopGet = shopService.findByUserId(currentSeller.getId());
        if (!shopGet.isSuccess()) {
            log.error("failed to find shop by gaven user:{}", currentSeller);
            result.setError(shopGet.getError());
            return result;
        }

        Paging<OrderComment> comments = commentDao.findByShopId(shopGet.getResult().getId(), page.offset, page.limit);


        if (comments.getTotal() == 0) {
            result.setResult(new Paging<RichOrderCommentForSeller>(0l, Collections.<RichOrderCommentForSeller>emptyList()));
        } else {
            result.setResult(buildRichOrderCommentForSeller(comments.getTotal(),comments.getData()));
        }
        return result;
    }

    @Override
    public Response<Paging<RichOrderCommentForBuyer>> findByBuyerId(Integer pageNo, Integer size, BaseUser currentBuyer) {
        PageInfo page = new PageInfo(pageNo, size);
        Response<Paging<RichOrderCommentForBuyer>> result = new Response<Paging<RichOrderCommentForBuyer>>();

        Paging<OrderComment> comments = commentDao.findByBuyerId(currentBuyer.getId(), page.offset, page.limit);

        if (comments.getTotal() == 0) {
            result.setResult(new Paging<RichOrderCommentForBuyer>(0l, new ArrayList<RichOrderCommentForBuyer>()));
        } else {
            result.setResult(buildRichOrderCommentForBuyer(comments.getTotal(),comments.getData()));
        }

        return result;
    }

    /**
     * 分页查看评价列表
     *
     * @param pageNo 顾名思义
     * @param size   顾名思义
     * @return 分页后的评价列表
     */
    @Override
    public Response<Paging<OrderComment>> viewItemComments(Long itemId, Integer pageNo, Integer size) {
        Response<Paging<OrderComment>> result = new Response<Paging<OrderComment>>();
        PageInfo page = new PageInfo(pageNo, size);

        // empty list
        if (Objects.equal(null, itemId)) {
            result.setResult(new Paging<OrderComment>(0l, Collections.<OrderComment>emptyList()));
            return result;
        }
        Paging<OrderComment> list = commentDao.viewItemComments(itemId, page.offset, page.limit);
        List<OrderComment> comments = list.getData();
        List<OrderComment> compiledComments = new ArrayList<OrderComment>();
        if (list.getTotal() > 0) {
            Response<User> user;
            for (OrderComment comment : comments) {

                // buyer id equals -1, user comments anonymously
                if (Objects.equal(-1l, comment.getBuyerId())) {
                    continue;
                }

                user = accountService.findUserById(comment.getBuyerId());
                if (user.isSuccess()) {
                    comment.setBuyerName(user.getResult().getName());
                } else {
                    continue;
                }
                compiledComments.add(comment);
            }
        }

        list.setData(compiledComments);
        result.setResult(list);
        return result;
    }


    @Override
    public Response<List<Long>> createCommentForExpiredOrderItem(List<OrderItem> avaliableOI) {
        Response<List<Long>> result = new Response<List<Long>>();

        List<OrderComment> compiledList = new ArrayList<OrderComment>();


        List<OrderItem> commentableOI = Lists.newArrayList();
        for (OrderItem oi : avaliableOI) {
            // compile the object with: item_id, shop_id, buyer_id
            List<OrderItem> orderItems = orderItemDao.findByOrderId(oi.getOrderId());
            Order order = orderDao.findById(oi.getOrderId());
            commentableOI.addAll(orderItems);

            Response<Shop> shopR = shopService.findByUserId(order.getSellerId());
            if (!shopR.isSuccess()) {
                log.error("failed to  find shop by seller id: {}, error code:{}", order.getSellerId(), shopR.getError());
                result.setError("order.comment.create.find.shop.fail");
                return result;
            }

            for (OrderItem orderItem : orderItems) {
                OrderComment comment = generateDefaultOrderComment(orderItem.getId(), orderItem.getBuyerId());
                comment.setOrderType(order.getType());
                comment.setItemId(orderItem.getItemId());
                comment.setShopId(shopR.getResult().getId());
                compiledList.add(comment);
            }
        }
        List<Long> idList = commentManager.create(compiledList);
        result.setResult(idList);
        return result;
    }

    @Override
    public Response<List<OrderComment>> getYesterdayCommentForShop(Long shopId) {
        Response<List<OrderComment>> result = new Response<List<OrderComment>>();

        try {
            Paging<OrderComment> page = commentDao.getYesterdayCommentForShopId(shopId, 0, PAGE_SIZE);
            Long total = page.getTotal();
            if (total == 0 || total < PAGE_SIZE) {
                result.setResult(page.getData());
                return result;
            }

            List<OrderComment> ocList = page.getData();
            result.setResult(ocList);
            for(int i=1;i<=total/PAGE_SIZE;i++) {
                page = commentDao.getYesterdayCommentForShopId(shopId, PAGE_SIZE*i, PAGE_SIZE);
                if (isNullOrEmpty(page.getData())) {
                    return result;
                }

                ocList.addAll(page.getData());
            }
            return result;
        } catch (Exception e) {
            log.error("find shop's yesterday order comment fail, shop id:{}, e:{}", shopId, e);
            result.setError("order.comment.find.comment.fail");
            return result;
        }
    }

    @Override
    public Response<List<OrderComment>> getYesterdayComment() {
        Response<List<OrderComment>> result = new Response<List<OrderComment>>();

        try {
                List<OrderComment> orderCommentList = commentDao.getYesterdayComment();
                result.setResult(orderCommentList);
                return result;
        } catch (Exception e) {
            log.error("find  yesterday order comment fail e:{}", e);
            result.setError("order.comment.find.comment.fail");
            return result;
        }
    }

    @Override
    public Response<List<OrderComment>> sumUpForYesterdayGroupByShop() {
        Response<List<OrderComment>> result = new Response<List<OrderComment>>();

        try {
            List<OrderComment> orderCommentList = commentDao.sumUpForYesterdayGroupByShop();
            result.setResult(orderCommentList);
            return result;
        } catch (Exception e) {
            log.error("find  yesterday order comment fail e:{}", e);
            result.setError("order.comment.find.comment.fail");
            return result;
        }
    }


    /**
     * @param lastId    上次获取的最后一个评论的id
     * @param limit     每次获取评论的数量
     * @return          限定大小的评论列表
     */
    @Override
    public Response<List<OrderComment>> forDump(Long lastId, Integer limit) {
        Response<List<OrderComment>> result = new Response<List<OrderComment>>();

        try {
            List<OrderComment> list = commentDao.forDump(lastId, limit);
            result.setResult(list);
            return result;

        } catch (Exception e) {
            log.error("failed to find comments where id<{} and limit={}. cause:{}",
                    lastId, limit, Throwables.getStackTraceAsString(e));
            result.setError("order.comment.find.for.dump.fail");
            return result;
        }
    }

    /**
     * @param lastId    上次获取的最后一个评论的id
     * @param limit     每次获取评论的数量
     * @return          限定大小的订单评论的订单id列表
     */
    @Override
    public Response<List<Long>> forExpire(Long lastId, Integer limit) {
        Response<List<Long>> result = new Response<List<Long>>();

        try {
            List<Long> list = commentDao.forExpire(lastId, limit);
            result.setResult(list);
            return result;

        } catch (Exception e) {
            log.error("failed to find comments where id<{} and limit={}. cause:{}",
                    lastId, limit, Throwables.getStackTraceAsString(e));
            result.setError("order.comment.find.for.expire.fail");
            return result;
        }
    }

    /**
     * 获取最后一个评论的id
     * @return  最后一个评论id
     */
    @Override
    public Response<Long> maxId() {
        Response<Long> result = new Response<Long>();

        try {
            // means no comment at all
            Long max = commentDao.maxId();
            if (max==null) {
                max = Long.MAX_VALUE-1;
            }

            result.setResult(max);
            return result;

        } catch (Exception e) {
            log.error("`maxId` invoke fail. e:{}", e);
            result.setError("order.comment.max.id.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> shopDump(Long shopId) {
        Response<Boolean> result = new Response<Boolean>();

        if(shopId == null) {
            log.error("shopId can not be null when dump shop score");
            result.setError("illegal.param");
            return result;
        }

        try {
            OrderComment comment = commentDao.sumUpShopScore(shopId);
            //更新店铺评分
            ShopExtra extra = extraFromSumUp(shopId, comment);
            Response<Boolean> isUpdate = shopService.fullUpdateShopExtraScore(extra);
            if (!isUpdate.isSuccess()) {
                log.error("fail to full update shop score shopId={}, error code:{}", shopId, isUpdate.getError());
                result.setError(isUpdate.getError());
                return result;
            }
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to dump shop score by shopId={}, cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("shop.score.dump.fail");
            return result;
        }
    }

    //-- ---------------------------
    //-- 私有帮助方法
    //-- ---------------------------

    private OrderComment generateDefaultOrderComment(Long id, Long buyerId) {
        OrderComment comment = new OrderComment();
        comment.setOrderItemId(id);
        comment.setBuyerId(buyerId);
        comment.setRDescribe(5);
        comment.setRQuality(5);
        comment.setRService(5);
        comment.setRExpress(5);
        comment.setComment("好评。");
        return comment;
    }

    private Paging<RichOrderCommentForSeller> buildRichOrderCommentForSeller(Long total, List<OrderComment> comments) {
        List<RichOrderCommentForSeller> commentForSellers = Lists.newArrayListWithCapacity(comments.size());
        total = 0l;
        for (OrderComment comment : comments) {
            try {
                RichOrderCommentForSeller commentForSeller = new RichOrderCommentForSeller();
                compileRichOrderComment(commentForSeller, comment);

                commentForSeller.setBuyerId(comment.getBuyerId());
                Response<User> userGet = accountService.findUserById(comment.getBuyerId());
                if (!userGet.isSuccess()) {
                    log.warn("failed to  find user by given id:{}, error code :{}", comment.getBuyerId(), userGet.getError());
                    continue;
                }
                commentForSeller.setBuyerName(userGet.getResult().getName());

                commentForSellers.add(commentForSeller);
                total ++;
            } catch (Exception e) {
                log.error("failed to build rich order comment({}) for seller, cause:{}, skip it",
                        comment, Throwables.getStackTraceAsString(e));
            }
        }

        return new Paging<RichOrderCommentForSeller>(total, commentForSellers);
    }

    private Paging<RichOrderCommentForBuyer> buildRichOrderCommentForBuyer(Long total, List<OrderComment> comments) {
        List<RichOrderCommentForBuyer> commentForBuyers = Lists.newArrayList();
        for (OrderComment comment : comments) {
            try {
                RichOrderCommentForBuyer commentForBuyer = new RichOrderCommentForBuyer();
                compileRichOrderComment(commentForBuyer, comment);

                Response<Shop> shopR = shopService.findById(comment.getShopId());
                if (!shopR.isSuccess()) {
                    log.error("failed to find shop by id:{}, error code:{}, skip", comment.getShopId(), shopR.getError());
                    continue;
                }
                Shop shop = shopR.getResult();
                commentForBuyer.setSellerId(comment.getBuyerId());
                commentForBuyer.setSellerName(shop.getUserName());
                commentForBuyer.setShopId(shop.getId());
                commentForBuyer.setShopName(shop.getName());

                commentForBuyers.add(commentForBuyer);
            } catch (Exception e) {
                log.error("failed to build rich order comment({}) for buyer, cause:{}, skip it",
                        comment, Throwables.getStackTraceAsString(e));
            }
        }

        return new Paging<RichOrderCommentForBuyer>(total, commentForBuyers);
    }


    private void compileRichOrderComment(
            RichOrderComment commentForBuyer, OrderComment comment) throws Exception {
        commentForBuyer.setItemId(comment.getItemId());

        Response<Item> itemGet = itemService.findById(comment.getItemId());
        if (!itemGet.isSuccess()) {
            log.error("method 'makeRichOrderCommentForBuyer' cannot find item by given id:{}", comment.getItemId());
            throw new Exception("cannot find item by given sku id:" + comment.getItemId().toString());
        }
        Item item = itemGet.getResult();

        commentForBuyer.setFee(item.getPrice());

        commentForBuyer.setItemImg(item.getMainImage());
        commentForBuyer.setItemName(item.getName());
        commentForBuyer.setOrderComment(comment.getComment());
        commentForBuyer.setOrderCommentReply(comment.getCommentReply());
        commentForBuyer.setOrderCommentId(comment.getId());

        commentForBuyer.setRQuality(comment.getRQuality());
        commentForBuyer.setRDescribe(comment.getRDescribe());
        commentForBuyer.setRService(comment.getRService());
        commentForBuyer.setRExpress(comment.getRExpress());
        commentForBuyer.setUpdatedAt(comment.getUpdatedAt());
        commentForBuyer.setOrderType(comment.getOrderType());
    }

    /**
     * 全量sum店铺的评价积分
     * @param shopId 店铺id
     * @return sum后OrderComment
     */
    @Override
    public Response<OrderComment> sumUpShopScore(Long shopId){
        Response<OrderComment> result = new Response<OrderComment>();
        OrderComment sumUp =null;
        try {
            sumUp = commentDao.sumUpShopScore(shopId);
            if(sumUp==null){
                log.error("not find OrderComment by shopId={}",shopId);
                result.setError("not find OrderComment by shopId");
                return  result;
            }

        } catch (Exception e) {
            log.error("find OrderComment by shopId={} fail, cause:{}, skip it",
                    shopId, Throwables.getStackTraceAsString(e));
        }
        result.setResult(sumUp);
        return result;

    }

    /**
     * sum店铺昨天的评价积分
     * @param shopId 店铺id
     * @return sum后OrderComment
     */
    @Override
    public Response<OrderComment> sumUpShopScoreYesterday(Long shopId){
        Response<OrderComment> result = new Response<OrderComment>();
        OrderComment sumUp =null;
        try {
            sumUp = commentDao.sumUpShopScoreYesterday(shopId);
            if(sumUp==null){
                log.error("not find yesterday OrderComment by shopId={}",shopId);
                result.setError("not find yesterday OrderComment by shopId");
                return  result;
            }

        } catch (Exception e) {
            log.error("find OrderComment by shopId={} fail, cause:{}, skip it",
                    shopId, Throwables.getStackTraceAsString(e));
        }
        result.setResult(sumUp);
        return result;

    }

    @Override
    public Response<Boolean> updateCommentReply(Long id, String commentReply) {

        Response<Boolean> result = new Response<Boolean>();
        try {

            checkArgument(notNull(id),"illegal.param");
            checkArgument(notNull(commentReply),"illegal.param");

            Boolean isUpdate = commentDao.commentReply(id,commentReply);
            if(isUpdate){
                result.setResult(isUpdate);
                return result;
            }
        }catch (IllegalArgumentException e){
            log.error("fail to update order comment by id={},commentReply={},error:{}",
                    id, commentReply, e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e){
            log.error("fail to update order comment by id={},commentReply={},cause:{}",
                    id, commentReply, Throwables.getStackTraceAsString(e));
            result.setError("order.comment.reply.update.fail");
            return result;

        }
        return result;
    }

}
