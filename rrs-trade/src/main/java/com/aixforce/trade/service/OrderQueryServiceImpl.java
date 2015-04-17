package com.aixforce.trade.service;

import com.aixforce.agreements.dao.PreAuthorizationDao;
import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemBundle;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemBundleService;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.*;
import com.aixforce.trade.dto.*;
import com.aixforce.trade.model.*;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-10
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    private final static Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PreAuthorizationDao preAuthorizationDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    OrderCommentDao commentDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private UserTradeInfoDao userTradeInfoDao;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderExtraDao orderExtraDao;

    @Autowired
    private ItemBundleService itemBundleService;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private LogisticsInfoDao logisticsInfoDao;

    private static final List<Order> EMPTY = Collections.emptyList();
    private static final List<OrderItem> EMPTY_ITMES = Collections.emptyList();
    private static final List<HaierOrder> EMPTY_HAIER_LIST = Collections.emptyList();
    private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();

    private final static String ITEM_SEPARATOR = ";";

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;

    /**
     * 根据id查找订单
     *
     * @param id 订单id
     * @return 订单
     */
    @Override
    public Response<Order> findById(Long id) {
        Response<Order> result = new Response<Order>();
        if (id == null) {
            log.error("order id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            Order order = orderDao.findById(id);
            if (order == null) {
                log.error("no order(id={}) found", id);
                result.setError("order.not.found");
                return result;
            }
            result.setResult(order);
            return result;
        } catch (Exception e) {
            log.error("failed to find order where id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     * 根据原始id查找订单
     *
     * @param id 订单原始id
     * @return 订单
     */
    @Override
    public Response<Order> findByOriginId(Long id) {
        Response<Order> result = new Response<Order>();
        if (id == null) {
            log.error("origin order id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            Order order = orderDao.findByOriginId(id);
            if (order == null) {
                log.error("no order(origin id={}) found", id);
                result.setError("order.not.found");
                return result;
            }
            result.setResult(order);
            return result;
        } catch (Exception e) {
            log.error("failed to find order where origin id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }


    /**
     * 根据id列表查找订单列表
     *
     * @param ids id列表
     * @return 订单列表
     */
    @Override
    public Response<List<Order>> findByIds(List<Long> ids) {
        Response<List<Order>> result = new Response<List<Order>>();
        if (ids.isEmpty()) {
            log.warn("ids is empty, return directly");
            result.setResult(Collections.<Order>emptyList());
            return result;
        }
        try {
            List<Order> orders = orderDao.findByIds(ids);
            result.setResult(orders);
            return result;
        } catch (Exception e) {
            log.error("failed to find order by ids, cause:", e);
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     * 根据 criteria 查找订单,目前支持的查询条件为:sellerId, buyerId,status,这是管理员使用的功能
     *
     * @param criteria           查询条件
     * @param pageNo             页码
     * @param size               返回条数
     * @param createdStartAt     创建起始时间
     * @param createdEndAt       创建截止时间
     * @return 订单列表
     */
    @Override
    public Response<Paging<Order>> findBy(Order criteria, Integer pageNo, Integer size, Date createdStartAt, Date createdEndAt) {
        Response<Paging<Order>> result = new Response<Paging<Order>>();
        try {
            PageInfo pageInfo = new PageInfo(pageNo, size);

            Map<String, Object> params = Maps.newHashMap();
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);
            params.put("criteria", criteria);
            params.put("createdStartAt", createdStartAt);
            params.put("createdEndAt", createdEndAt);
            Paging<Order> orders = orderDao.findBy(params);
            result.setResult(orders);
            return result;
        } catch (Exception e) {
            log.error("failed to query orders by {},cause:{}", criteria, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     * 运营查询订单，按订单类型、行业（频道）、商家帐号、订单状态
     * important: admin 不能按照子订单状态筛选
     *
     * @param type       订单类型 可选
     * @param business   行业 可选
     * @param sellerName 商家帐号 可选
     * @param status     主订单状态 可选
     * @param pageNo     起始页码 可选，默认 1
     * @param size       数量 可选，默认 20
     * @return 查询结果
     */
    @Override
    public Response<Paging<RichOrderSellerView>> adminFind(Integer type,
                                                           Long business,
                                                           String sellerName,
                                                           Long orderId,
                                                           Long itemId,
                                                           Integer status,
                                                           Integer pageNo,
                                                           Integer size) {

        Response<Paging<RichOrderSellerView>> result = new Response<Paging<RichOrderSellerView>>();

        PageInfo pageInfo = new PageInfo(pageNo, size);

        Map<String, String> params = Maps.newHashMap();

        try {
            if(orderId != null) {
                Order order = orderDao.findById(orderId);
                if(order == null) {
                    result.setResult(new Paging<RichOrderSellerView>(0L, Collections.<RichOrderSellerView>emptyList()));
                    return result;
                }
                List<RichOrderSellerView> richOrderSellerViews = buildOrdersFromSellerView(Lists.newArrayList(order),true);
                result.setResult(new Paging<RichOrderSellerView>(1L, richOrderSellerViews));
                return result;
            }

            Long sellerId = null;
            if (!Strings.isNullOrEmpty(sellerName)) {
                Response<User> sellerR = accountService.findUserBy(sellerName, LoginType.NAME);
                if (!sellerR.isSuccess()) {
                    log.error("failed to find seller(name={}), error code:{}", sellerName, sellerR.getError());
                    result.setError(sellerR.getError());
                    return result;
                }
                sellerId = sellerR.getResult().getId();
            }

            if(type != null) {
                params.put("type", String.valueOf(type));
            }
            if(sellerId != null) {
                params.put("sellerId", String.valueOf(sellerId));
            }
            if(business != null) {
                params.put("businessId", String.valueOf(business));
            }
            if(itemId != null) {
                params.put("itemId", String.valueOf(itemId));
            }
            if(status != null) {
                params.put("status", String.valueOf(status));
            }
            Paging<Long> orderIdsP = orderItemDao.findOrderIdsBy(params, pageInfo.getOffset(),pageInfo.getLimit());
            if (orderIdsP.getTotal() > 0) {
                List<Order> orders = orderDao.findByIds(orderIdsP.getData());
                List<RichOrderSellerView> richOrderSellerViews = buildOrdersFromSellerView(orders, true);
                result.setResult(new Paging<RichOrderSellerView>(orderIdsP.getTotal(), richOrderSellerViews));
                return result;
            } else {
                result.setResult(new Paging<RichOrderSellerView>(0L, Collections.<RichOrderSellerView>emptyList()));
                return result;
            }
        } catch (Exception e) {
            log.error("failed to query orders by admin ", e);
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     * 卖家查看已卖出的宝贝
     *
     * @param baseUser 卖家,系统自动注入
     * @param pageNo   起始页码
     * @param size     数量
     * @param status   订单状态
     * @return 订单列表
     */
    @Override
    public Response<Paging<RichOrderSellerView>> findBySellerId(BaseUser baseUser, Integer pageNo,
                                                                Integer size, Integer status, Long orderId) {
        Response<Paging<RichOrderSellerView>> result = new Response<Paging<RichOrderSellerView>>();

        PageInfo pageInfo = new PageInfo(pageNo, size);

        try {
            if(orderId != null) {
                Order order = orderDao.findById(orderId);
                if(order != null && Objects.equal(order.getSellerId(), baseUser.getId())) {
                    List<RichOrderSellerView> richOrderSellerViews = buildOrdersFromSellerView(Lists.newArrayList(order), false);
                    result.setResult(new Paging<RichOrderSellerView>(1L, richOrderSellerViews));
                    return result;
                }else {
                    result.setResult(new Paging<RichOrderSellerView>(0L, Collections.<RichOrderSellerView>emptyList()));
                    return result;
                }
            }

            Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
            params.put("sellerId", baseUser.getId().toString());
            if (status != null) {
                params.put("status", status.toString());
            }

            Paging<Long> orderIds = orderItemDao.findOrderIdsBy(params, pageInfo.getOffset(), pageInfo.getLimit());
            if (orderIds.getTotal() > 0) {
                List<Order> orders = orderDao.findByIds(orderIds.getData());
                List<RichOrderSellerView> richOrderSellerViews = buildOrdersFromSellerView(orders, false);
                result.setResult(new Paging<RichOrderSellerView>(orderIds.getTotal(), richOrderSellerViews));
                return result;
            } else {
                result.setResult(new Paging<RichOrderSellerView>(0L, Collections.<RichOrderSellerView>emptyList()));
                return result;
            }
        } catch (Exception e) {
            log.error("fail to find orders by seller={},pageNo={},count={},status={},cause:{}",
                    baseUser, pageNo, size, status, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }

    @Override
    public Response<UserTradeInfo> findUserTradeInfoByOrderId(BaseUser baseUser, Long orderId) {
        Response<UserTradeInfo> result = new Response<UserTradeInfo>();
        if (orderId == null) {
            log.error("orderId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                log.error("order(id={}) not found", orderId);
                result.setError("order.not.found");
                return result;
            }
            //只有订单的买家和卖家才能看到订单的收货信息
            if (Objects.equal(baseUser.getId(), order.getBuyerId())
                    || Objects.equal(baseUser.getId(), order.getSellerId())) {
                UserTradeInfo info = userTradeInfoDao.findById(order.getTradeInfoId());
                result.setResult(info);
                return result;
            }
            log.error("only seller(id={}) or buyer(id={}) can see this,but you are {}",
                    order.getSellerId(), order.getBuyerId(), baseUser.getId());
            result.setError("authorized.fail");
            return result;
        } catch (Exception e) {
            log.error("failed to query userTradeInfo for order (id={}),cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("tradeinfo.query.fail");
            return result;
        }
    }

    /**
     * 卖家查看订单详情,包括发票和买家留言等信息
     *
     * @param seller 卖家
     * @param id     订单id
     * @return 订单详情
     */
    @Override
    public Response<RichOrderWithDetail> sellerFindOrderById(@ParamInfo("seller") BaseUser seller, @ParamInfo("id") Long id) {
        Response<RichOrderWithDetail> result = new Response<RichOrderWithDetail>();
        if (id == null) {
            log.error("order id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        Response<Order> or = findById(id);
        if (!or.isSuccess()) {
            result.setError(or.getError());
            return result;
        }
        Order order = or.getResult();
        if (!Objects.equal(order.getSellerId(), seller.getId())) { //只有订单的卖家才能查看此订单
            log.error("only seller(id={}) can view this order,but actual sellerId is:{}", seller.getId());
            result.setError("authorized.fail");
            return result;
        }
        try {
            RichOrderSellerView rosv = makeRichOrderSellerView(order, false);
            RichOrderWithDetail<RichOrderSellerView> rowd = new RichOrderWithDetail<RichOrderSellerView>();
            OrderExtra orderExtra = orderExtraDao.findByOrderId(id);
            rowd.setRichOrder(rosv);
            rowd.setOrderExtra(orderExtra);
            result.setResult(rowd);
            return result;

        } catch (Exception e) {
            log.error("seller(id={}) failed to find rich order(id={}) with details,cause:{} ",
                    seller.getId(), id, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }


    }

    /**
     * 买家查看订单详情,包括发票和买家留言等信息
     *
     * @param buyer 买家
     * @param id    订单id
     * @return 订单详情
     */
    @Override
    public Response<RichOrderWithDetail> buyerFindOrderById(@ParamInfo("buyer") BaseUser buyer, @ParamInfo("id") Long id) {
        Response<RichOrderWithDetail> result = new Response<RichOrderWithDetail>();
        if (id == null) {
            log.error("order id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        Response<Order> or = findById(id);
        if (!or.isSuccess()) {
            result.setError(or.getError());
            return result;
        }
        Order order = or.getResult();
        if (!Objects.equal(order.getBuyerId(), buyer.getId())) { //只有订单的买家才能查看此订单
            log.error("only buyer(id={}) can view this order,but actual buyerId is:{}", buyer.getId());
            result.setError("authorized.fail");
            return result;
        }
        try {
            RichOrderBuyerView robv = makeRichOrderBuyerView(order);
            RichOrderWithDetail<RichOrderBuyerView> rowd = new RichOrderWithDetail<RichOrderBuyerView>();
            OrderExtra orderExtra = orderExtraDao.findByOrderId(id);
            rowd.setRichOrder(robv);
            rowd.setOrderExtra(orderExtra);
            result.setResult(rowd);
            return result;
        } catch (Exception e) {
            log.error("buyer(id={}) failed to find rich order(id={}) with details,cause:{} ",
                    buyer.getId(), id, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }

    @Override
    public Response<OrderItemTotalFee> findExtraByOrderItemId(@ParamInfo("orderItemId") Long orderItemId) {
        Response<OrderItemTotalFee> result = new Response<OrderItemTotalFee>();
        try {
            OrderItemTotalFee oitf = new OrderItemTotalFee();
            OrderItem orderItem = orderItemDao.findById(orderItemId);
            BeanMapper.copy(orderItem, oitf);
            //如果是预售订单，返回的价格就要加上定金的金额
            if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())) {
                Order order = orderDao.findById(orderItem.getOrderId());
                oitf.setTotalFee(order.getFee());
                oitf.setEarnestFee(order.getFee() - orderItem.getFee());
                List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
                Integer totalRefundAmount = 0;
                for (OrderItem oi : orderItems) {
                    if (oi.getRefundAmount() != null)
                        totalRefundAmount += oi.getRefundAmount();
                }
                oitf.setTotalRefundAmount(totalRefundAmount);
            }

            result.setResult(oitf);
            return result;
        } catch (Exception e) {
            log.error("fail to find orderItem extra by orderItemId={}, cause:{}", orderItemId, Throwables.getStackTraceAsString(e));
            result.setError("extra.query.fail");
            return result;
        }
    }

    /**
     * 押金订单退款申请数据获取
     * 押金订单目前只支持在试用中，购买之前的状态下进行退货，购买后不允许退货
     * @param orderId 订单id
     * @return
     */
    @Override
    public Response<OrderItemTotalFee> findDepositExtraByOrderItemId(@ParamInfo("orderId") Long orderId) {
        Response<OrderItemTotalFee> result = new Response<OrderItemTotalFee>();

        //判断传过来参数是OrderId还是OrderItemId
        log.debug("加载退货组件，传入参数:OrderId={}",orderId);
        OrderItem orderItem = new OrderItem();
        Order order = orderDao.findById(orderId);
        if(isNull(order)){
            orderItem = orderItemDao.findById(orderId);
        }else {
            orderItem = orderItemDao.findByMap(order.getId(), 2);
        }
        try {
            PreAuthorizationDepositOrder preAuthorizationDepositOrder = preAuthorizationDao.findOneByOrderId(orderItem.getOrderId());
            log.debug("获取押金表信息:{}",preAuthorizationDepositOrder);
            OrderItemTotalFee oitf = new OrderItemTotalFee();
            BeanMapper.copy(orderItem, oitf);

            oitf.setPreState(preAuthorizationDepositOrder.getStatus());
            oitf.setTotalRefundAmount(orderItem.getFee());//退款总金额（押金订单，退款发生在购买之前，所以只需要退押金）
            log.debug("OrderItemTotalFee:返回信息{}",oitf);
            result.setResult(oitf);
            return result;
        } catch (Exception e) {
            log.error("fail to find orderItem extra by orderId={}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("extra.query.fail");
            return result;
        }
    }

    @Override
    public OrderItem findByMap(@ParamInfo("orderId") Long orderId,@ParamInfo("type") int type) {
        return orderItemDao.findByMap(orderId,type);
    }

    /**
     * 根据订单号获取子订单号信息
     *
     * @param orderId 订单号
     * @return 子订单号列表
     */
    @Override
    public Response<List<OrderItem>> findSubsByOrderId(@ParamInfo("orderId") Long orderId) {
        Response<List<OrderItem>> result = new Response<List<OrderItem>>();

        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);

        result.setResult(orderItems);
        return result;
    }


    /**
     * 根据订单列表获取所有子订单信息
     *
     * @param ids 订单列表
     * @return 子订单列表
     */
    @Override
    public Response<List<OrderItem>> findSubsInOrderIds(Long... ids) {
        Response<List<OrderItem>> result = new Response<List<OrderItem>>();
        List<OrderItem> orderItems = orderItemDao.findInOrderIds(ids);
        if (ids.length == 0) {
            result.setResult(EMPTY_ITMES);
            return result;
        }

        result.setResult(orderItems);
        return result;
    }

    /**
     * 根据订单获取子订单详情消息
     *
     * @param orderId 订单id
     * @return 子订单详情
     */
    @Override
    public Response<List<RichOrderItem>> findOrderItemsByOrderIdForComment(
            @ParamInfo("orderId") Long orderId, BaseUser baseuser) {
        Response<List<RichOrderItem>> result = new Response<List<RichOrderItem>>();

        Order order = orderDao.findById(orderId);
        if (order == null) {
            log.error("fail to find order by orderId={}", orderId);
            result.setError("order.query.fail");
            return result;
        }
        if (!Objects.equal(order.getBuyerId(), baseuser.getId())) {
            log.error("user cannot leave comment on other's order, order:{}, user:{}", orderId, baseuser);
            result.setError("order.comment.not.own.order");
            return result;
        }

        RichOrderBuyerView buyerView = makeRichOrderBuyerView(order);
        List<RichOrderItem> orderItems = buyerView.getOrderItems();

        for (Iterator<RichOrderItem> it = orderItems.iterator(); it.hasNext(); ) {
            RichOrderItem oi = it.next();
            if (Objects.equal(
                    oi.getOrderItemType(), OrderItem.Type.PRESELL_DEPOSIT.value()) ||
                    !Objects.equal(oi.getStatus(), OrderItem.Status.DONE.value())) {
                it.remove();
            }
        }
        result.setResult(orderItems);
        return result;
    }

    /**
     * 获取在指定日期内完成的订单数量
     *
     * @param finishedAt 订单完成日期
     * @return 符合条件的订单数量，若没有则返回0
     */
    @Override
    public Response<Long> countOfFinishedOrder(Date finishedAt) {
        Response<Long> result = new Response<Long>();

        if (finishedAt == null) {
            log.error("method 'countOfFinishedOrder' args 'finishedAt' cannot be null");
            result.setError("order.count.finish.at.null");
            return result;
        }

        try {
            DateTime finishDate = new DateTime(finishedAt);

            Date startAt = finishDate.withTimeAtStartOfDay().toDate();
            Date endAt = finishDate.withTimeAtStartOfDay().plusDays(1).toDate();

            Long count = orderDao.countOfFinished(startAt, endAt);
            result.setResult(count);
            return result;
        } catch (Exception e) {
            log.error("fail to invoke method with finishedAt={}", finishedAt, e);
            result.setError("order.count.finish.at.fail");
            return result;
        }

    }

    /**
     * 按买家将订单进行归组
     *
     * @param orders  订单
     * @param isAdmin 如果是admin视角则额外增加sellerId和sellerName
     * @return 归组后的结果
     */
    private List<RichOrderSellerView> buildOrdersFromSellerView(List<Order> orders, boolean isAdmin) {
        List<RichOrderSellerView> result = Lists.newArrayListWithCapacity(orders.size());
        for (Order order : orders) {
            try {
                RichOrderSellerView richOrderSellerView = makeRichOrderSellerView(order, isAdmin);
                List<RichOrderItem> orderItems = richOrderSellerView.getOrderItems();
                boolean canDeliver = true;
                for(RichOrderItem oi : orderItems) {
                    if(!Objects.equal(oi.getStatus(), OrderItem.Status.PAYED.value())
                            && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())) {
                        canDeliver = false;
                        break;
                    }
                }
                richOrderSellerView.setCanDeliver(canDeliver);
                result.add(richOrderSellerView);
            } catch (Exception e) {
                // 为什么这里不抛出？
                log.error("failed to handle {} seller view,cause:{},skip",
                        order, Throwables.getStackTraceAsString(e));
            }
        }
        return result;
    }

    private RichOrderSellerView makeRichOrderSellerView(Order order, boolean isAdmin) {
        RichOrderSellerView richOrderSellerView = new RichOrderSellerView();
        try {
            richOrderSellerView.setOrderId(order.getId());
            buildRichOrder(order, richOrderSellerView);
            richOrderSellerView.setBuyerId(order.getBuyerId());
            Response<User> br = accountService.findUserById(order.getBuyerId());
            if (!br.isSuccess()) {
                log.error("failed to find user(id={}) for order(id={}),error code:{}", order.getBuyerId(),order.getId(), br.getError());
                throw new IllegalStateException("buyer(id=" + order.getBuyerId() + ") not found");
            }
            BaseUser user = br.getResult();
            richOrderSellerView.setBuyerName(user.getName());
            richOrderSellerView.setMobile(user.getMobile());
            // 如果是admin就再查一下seller信息
            if (isAdmin) {
                richOrderSellerView.setSellerId(order.getSellerId());
                Response<User> sellerR = accountService.findUserById(order.getSellerId());
                if (!sellerR.isSuccess()) {
                    log.error("failed to find user(id={}),error code:{}", order.getSellerId(), br.getError());
                    throw new IllegalStateException("seller(id=" + order.getSellerId() + ") not found");
                }
                richOrderSellerView.setSellerName(sellerR.getResult().getName());
            }
            OrderExtra orderExtra = orderExtraDao.findByOrderId(order.getId());

            //配送方式：0 物流配送 1 到店自提
            if(notNull(orderExtra) && isNull(orderExtra.getDeliverType())){
                orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
            }else if(isNull(orderExtra)){
                orderExtra = new OrderExtra();
                orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
            }

            richOrderSellerView.setOrderExtra(orderExtra);
            richOrderSellerView.setUserTradeInfoId(order.getTradeInfoId());

            PreAuthorizationDepositOrder preAuthorizationDepositOrder = preAuthorizationDao.findOneByOrderId(order.getId());
            if(preAuthorizationDepositOrder!=null){
//                if(preAuthorizationDepositOrder.getType()==1){
//                    order.setPaymentType(4);
//                }else if(preAuthorizationDepositOrder.getType()==2){
//                    order.setPaymentType(5);
//                }
                richOrderSellerView.setDepositType(1);
            } else {
                richOrderSellerView.setDepositType(0);
            }


            return richOrderSellerView;
        } catch (Exception e) {
            log.error("failed to make rich order seller view for order(id={}), cause:{}", order.getId(), Throwables.getStackTraceAsString(e));
            return richOrderSellerView;
        }
    }

    /**
     * 买家查看已买到的宝贝
     *
     * @param baseUser 买家,系统自动注入
     * @param pageNo   起始页码
     * @param size     数量
     * @return 订单列表
     */
    @Override
    public Response<Paging<RichOrderBuyerView>> findByBuyerId(BaseUser baseUser, Integer pageNo,
                                                              Integer size, Integer status, Long orderId) {
        Response<Paging<RichOrderBuyerView>> result = new Response<Paging<RichOrderBuyerView>>();

        if(baseUser == null){
            log.error("user is null");
            result.setError("user.not.login.yet");
            return result;
        }

        PageInfo pageInfo = new PageInfo(pageNo, size);

        try {

            if(orderId != null) {
                Order order = orderDao.findById(orderId);
                if(order != null && Objects.equal(order.getBuyerId(), baseUser.getId())) {

                    List<RichOrderBuyerView> richOrderBuyerViews = buildRichOrdersForBuyerView(Lists.newArrayList(order));
                    result.setResult(new Paging<RichOrderBuyerView>(1L, richOrderBuyerViews));
                    return result;
                }else {
                    result.setResult(new Paging<RichOrderBuyerView>(0L, Collections.<RichOrderBuyerView>emptyList()));
                    return result;
                }
            }

            Map<String, String> params = Maps.newHashMapWithExpectedSize(2);
            params.put("buyerId", baseUser.getId().toString());
            if (status != null) {
                params.put("status", status.toString());
            }

            Paging<Long> orderIds = orderItemDao.findOrderIdsBy(params, pageInfo.getOffset(), pageInfo.getLimit());
            if (orderIds.getTotal() > 0) {
                List<Order> orders = orderDao.findByIds(orderIds.getData());
                List<RichOrderBuyerView> richOrderBuyerViews = buildRichOrdersForBuyerView(orders);
                result.setResult(new Paging<RichOrderBuyerView>(orderIds.getTotal(), richOrderBuyerViews));
                return result;
            } else {
                result.setResult(new Paging<RichOrderBuyerView>(0L, Collections.<RichOrderBuyerView>emptyList()));
                return result;
            }

        } catch (Exception e) {
            log.error("fail to find orders by baseUser={},pageNo={},size={},status={},cause:{}",
                    baseUser, pageNo, size, status, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            return result;
        }
    }


    private List<RichOrderBuyerView> buildRichOrdersForBuyerView(List<Order> orders) {
        List<RichOrderBuyerView> result = Lists.newArrayListWithCapacity(orders.size());
        for (Order order : orders) {
            try {

                RichOrderBuyerView richOrderBuyerView = makeRichOrderBuyerView(order);
                List<RichOrderItem> orderItems = richOrderBuyerView.getOrderItems();
                boolean canConfirm = true;
                boolean canComment = false;

                List<String> oiIDs = Lists.newArrayList();
                for (RichOrderItem oi : orderItems) {
                    if (Objects.equal(oi.getStatus(), OrderItem.Status.DONE.value()) &&
                            !Objects.equal(oi.getOrderItemType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        oiIDs.add(oi.getOrderItemId().toString());
                    }

                    if (canConfirm &&
                            !Objects.equal(oi.getStatus(), OrderItem.Status.DELIVERED.value())
                            && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                            && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())) {
                        canConfirm = false;
                    }
                }
                if (order.getStatus() == Order.Status.DONE.value() && oiIDs.size() > 0) {
                    List<OrderComment> comments = commentDao.findAnyByOrderItemId(oiIDs);
                    if (comments.size() == 0) {
                        canComment = true;
                    }
                }
                richOrderBuyerView.setCanConfirm(canConfirm);
                richOrderBuyerView.setCanComment(canComment);
                result.add(richOrderBuyerView);
            } catch (Exception e) {
                log.error("failed to build {} buyer view, cause:{}", order, Throwables.getStackTraceAsString(e));
            }
        }
        return result;
    }

    private RichOrderBuyerView makeRichOrderBuyerView(Order order) {
        RichOrderBuyerView richOrderBuyerView = new RichOrderBuyerView();
        try {
            richOrderBuyerView.setOrderId(order.getId());
            buildRichOrder(order, richOrderBuyerView);
//        RichOrderItem richOrderItem = richOrderBuyerView.getOrderItems().get(0);
//        Item item = itemService.findById(richOrderItem.getSku().getItemId()).getResult();
            Shop shop = shopService.findByUserId(order.getSellerId()).getResult();
            richOrderBuyerView.setSiteId(order.getSellerId());
            richOrderBuyerView.setSiteName(shop.getName());
            richOrderBuyerView.setShopImage(shop.getImageUrl());
            richOrderBuyerView.setUserTradeInfoId(order.getTradeInfoId());
            OrderExtra orderExtra = orderExtraDao.findByOrderId(order.getId());

            //配送方式：0 物流配送 1 到店自提
            if(notNull(orderExtra) && isNull(orderExtra.getDeliverType())){
                orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
            }else if(isNull(orderExtra)){
                orderExtra = new OrderExtra();
                orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
            }

            richOrderBuyerView.setOrderExtra(orderExtra);
            DateTime sysDate = new DateTime(new Date());
            richOrderBuyerView.setSystemDate(DFT.print(sysDate));


            PreAuthorizationDepositOrder preAuthorizationDepositOrder = preAuthorizationDao.findOneByOrderId(order.getId());
            if(preAuthorizationDepositOrder!=null){
//                if(preAuthorizationDepositOrder.getType()==1){
//                    order.setPaymentType(4);
//                }else if(preAuthorizationDepositOrder.getType()==2){
//                    order.setPaymentType(5);
//                }
                richOrderBuyerView.setDepositType(1);
            } else {
                richOrderBuyerView.setDepositType(0);
            }

            return richOrderBuyerView;
        } catch (Exception e) {
            log.error("failed to make rich order buyer view for order(id={}), cause:{} ", order.getId(),
                    Throwables.getStackTraceAsString(e));
            return richOrderBuyerView;
        }
    }

    private void buildRichOrder(Order order, RichOrder richOrder) {
        richOrder.setOrderId(order.getId());
        richOrder.setStatus(order.getStatus());
        richOrder.setOrderType(order.getType());
        richOrder.setCreatedAt(order.getCreatedAt());
        //是否是ehaier商家
        richOrder.setIsEhaier(Objects.equal(eHaierSellerId,order.getSellerId().toString()));
        //Site shop = findShopByUserId(order);

        boolean isRecordLogistics = false;
        LogisticsInfo existed = logisticsInfoDao.findByOrderId(order.getId());
        if (existed != null) {
            isRecordLogistics = true;
        }

        richOrder.setIsRecordLogistics(isRecordLogistics);//订单是否已录入物流信息

        List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());
        List<RichOrderItem> richOrderItems = Lists.newArrayListWithCapacity(orderItems.size());

        int discount = 0;

        boolean hasComment = false;
        boolean canBaskOrder =false;//是否可以晒单
        for (OrderItem orderItem : orderItems) {
            if (Objects.equal(orderItem.getHasComment(), Boolean.TRUE)) {
                hasComment = true;
            }
            if (!orderItem.getType().equals(3)&&(orderItem.getIsBaskOrder()==null||Objects.equal(orderItem.getIsBaskOrder(), Boolean.FALSE))) {
                canBaskOrder = true;
            }
            Response<Sku> sr = itemService.findSkuById(orderItem.getSkuId());
            if (!sr.isSuccess()) {
                log.error("failed to find sku(id={}),skip", orderItem.getSkuId());
                continue;
            }
            Sku sku = sr.getResult();
            Response<Item> ir = itemService.findById(sku.getItemId());
            if (!ir.isSuccess()) {
                log.error("failed to find item(id={}), error code:{} ,skip", sku.getItemId(), ir.getError());
                continue;
            }
            Item item = ir.getResult();
            RichOrderItem roi = new RichOrderItem();
            roi.setOrderItemId(orderItem.getId());
            roi.setItemImage(item.getMainImage());
            roi.setItemName(item.getName());
            roi.setCount(orderItem.getQuantity());
            roi.setDeliverFee(Objects.firstNonNull(orderItem.getDeliverFee(), 0));
            roi.setFee(orderItem.getFee());
            // 商品的单价
            roi.setUnitFee((roi.getFee() - roi.getDeliverFee()) / roi.getCount());
            roi.setDiscount(Objects.firstNonNull(orderItem.getDiscount(), 0)); // discount 暂时还没用
            roi.setStatus(orderItem.getStatus());
            roi.setSku(sku);
            roi.setRequestRefundAt(orderItem.getRequestRefundAt());
            roi.setReason(orderItem.getReason());
            roi.setRefundAmount(orderItem.getRefundAmount());
            roi.setOrderItemType(orderItem.getType());
            roi.setCreatedAt(orderItem.getCreatedAt());

            roi.setPaymentType(orderItem.getPayType());
            discount = discount + roi.getDiscount();
            richOrderItems.add(roi);
        }
        richOrder.setHasComment(hasComment);
        richOrder.setCanBaskOrder(canBaskOrder);//是否可晒单
        richOrder.setDiscount(discount);
        richOrder.setTotalFee(order.getFee());
        richOrder.setDeliverFee(order.getDeliverFee());
        richOrder.setPaymentType(order.getPaymentType());
        richOrder.setIsBuying(order.getIsBuying());
        richOrder.setOrderItems(richOrderItems);
    }

    /**
     * 获取在指定日期内完成的订单数量
     *
     * @param startAt 查询开始时间
     * @param endAt   查询截止时间
     * @param pageNo  页码
     * @param size    返回条数
     * @return 已完成订单列表
     */
    @Override
    public Response<Paging<Order>> findByFinishAt(Date startAt, Date endAt, Integer pageNo, Integer size) {
        Response<Paging<Order>> result = new Response<Paging<Order>>();
        if (startAt == null || endAt == null) {
            log.error("begin nor end cannot be null, beginAt={}, endAt={}", startAt, endAt);
            Paging<Order> paging = new Paging<Order>(0L, EMPTY);
            result.setResult(paging);
            return result;
        }

        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;

        try {
            Paging<Order> paging = orderDao.findFinished(startAt, endAt, offset, size);
            result.setResult(paging);
            return result;
        } catch (Exception e) {
            log.error("fail to invoke findByFinishAt with beginAt={}, endAt={}", startAt, endAt, e);
            Paging<Order> paging = new Paging<Order>(0L, EMPTY);
            result.setResult(paging);
            return result;
        }
    }


    /**
     *
     * 获取指定日期内更新的订单列表，封装成Page<HaierOrder>对象返回
     *
     * @param beginAt       查询开始时间
     * @param endAt         查询截止时间
     * @param businesses    行业范围筛选
     * @param pageNo        页码
     * @param size          返回条数
     * @return  更新的订单列表
     */
    public Response<Paging<HaierOrder>> findHaierOrderByUpdatedAt(Date beginAt,
                                                                  Date endAt,
                                                                  List<Long> businesses,
                                                                  Integer pageNo,
                                                                  Integer size) {
        Response<Paging<HaierOrder>> result = new Response<Paging<HaierOrder>>();

        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;

        try {
            Paging<Order> paging = orderDao.findUpdated(businesses, beginAt, endAt, offset, size);

            List<Order> orders = paging.getData();
            int len = orders.size();

            if (len == 0) {
                Paging<HaierOrder> haierOrderPaging = new Paging<HaierOrder>(0L, EMPTY_HAIER_LIST);
                result.setResult(haierOrderPaging);
                return result;
            }


            Long[] orderIds = new Long[len];
            Long[] tradeInfoIds = new Long[len];
            List<Long> userIds = Lists.newArrayListWithCapacity(len);

            for (int i = 0; i < len; i++) {
                orderIds[i] = orders.get(i).getId();
                tradeInfoIds[i] = orders.get(i).getTradeInfoId();
                userIds.add(orders.get(i).getBuyerId());
            }

            List<OrderItem> orderItems = orderItemDao.findInOrderIds(orderIds);
            int itemLen = orderItems.size();
            List<Long> skuIds = Lists.newArrayListWithCapacity(itemLen);

            for (OrderItem orderItem : orderItems) {
                skuIds.add(orderItem.getSkuId());
            }




            Map<Long, Sku> mappedSku = getMappedSku(skuIds);
            Map<Long, UserTradeInfo> mappedUserInfo = getMappedTradeInfo(tradeInfoIds);
            Map<Long, OrderExtra> mappedOrderExtra = getMappedOrderExtra(orderIds);
            Map<Long, User> mappedBuyer = getMappedBuyer(userIds);


            List<HaierOrder> haierOrders = getHaierOrders(paging, orderItems, mappedUserInfo, mappedOrderExtra, mappedSku, mappedBuyer);

            Paging<HaierOrder> haierOrderPaging = new Paging<HaierOrder>(paging.getTotal(), haierOrders);
            result.setResult(haierOrderPaging);
            return result;

        } catch (ServiceException e) {
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to query haier orders page with beginAt={}, endAt={}, pageNo={}, size={}",
                    beginAt, endAt, pageNo, size, e);
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     *
     * 获取指定卖家ID并且在指定日期内更新的订单列表，封装成Page<HaierOrder>对象返回
     *
     * @param beginAt       查询开始时间
     * @param endAt         查询截止时间
     * @param businesses    行业范围筛选
     * @param pageNo        页码
     * @param size          返回条数
     * @return  更新的订单列表
     */
    public Response<Paging<HaierOrder>> findHaierOrderByUpdatedAtAndSellerIds(Date beginAt,
                                                                              Date endAt,
                                                                              List<Long> businesses,
                                                                              Integer pageNo,
                                                                              Integer size,
                                                                              List<Long> sellerIds) {
        Response<Paging<HaierOrder>> result = new Response<Paging<HaierOrder>>();

        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;

        try {
            Paging<Order> paging = orderDao.findUpdatedAndSellerIds(businesses, beginAt, endAt, offset, size, sellerIds);

            List<Order> orders = paging.getData();
            int len = orders.size();

            if (len == 0) {
                Paging<HaierOrder> haierOrderPaging = new Paging<HaierOrder>(0L, EMPTY_HAIER_LIST);
                result.setResult(haierOrderPaging);
                return result;
            }


            Long[] orderIds = new Long[len];
            Long[] tradeInfoIds = new Long[len];
            List<Long> userIds = Lists.newArrayListWithCapacity(len);

            for (int i = 0; i < len; i++) {
                orderIds[i] = orders.get(i).getId();
                tradeInfoIds[i] = orders.get(i).getTradeInfoId();
                userIds.add(orders.get(i).getBuyerId());
            }

            List<OrderItem> orderItems = orderItemDao.findInOrderIds(orderIds);
            int itemLen = orderItems.size();
            List<Long> skuIds = Lists.newArrayListWithCapacity(itemLen);

            for (OrderItem orderItem : orderItems) {
                skuIds.add(orderItem.getSkuId());
            }




            Map<Long, Sku> mappedSku = getMappedSku(skuIds);
            Map<Long, UserTradeInfo> mappedUserInfo = getMappedTradeInfo(tradeInfoIds);
            Map<Long, OrderExtra> mappedOrderExtra = getMappedOrderExtra(orderIds);
            Map<Long, User> mappedBuyer = getMappedBuyer(userIds);


            List<HaierOrder> haierOrders = getHaierOrders(paging, orderItems, mappedUserInfo, mappedOrderExtra, mappedSku, mappedBuyer);

            Paging<HaierOrder> haierOrderPaging = new Paging<HaierOrder>(paging.getTotal(), haierOrders);
            result.setResult(haierOrderPaging);
            return result;

        } catch (ServiceException e) {
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to query haier orders page with beginAt={}, endAt={}, pageNo={}, size={}",
                    beginAt, endAt, pageNo, size, e);
            result.setError("order.query.fail");
            return result;
        }
    }

    private Map<Long, User> getMappedBuyer(List<Long> userIds) {
        Response<List<User>> result = accountService.findByIds(userIds);
        checkState(result.isSuccess(), result.getError());

        return Maps.uniqueIndex(result.getResult(), new Function<User, Long>() {
            public Long apply(User entry) {
                return entry.getId();
            }
        });
    }


    private Map<Long, Sku> getMappedSku(List<Long> skuIds) {
        Response<List<Sku>> result = itemService.findSkuByIds(skuIds);
        checkState(result.isSuccess(), result.getError());

        return Maps.uniqueIndex(result.getResult(), new Function<Sku, Long>() {
            public Long apply(Sku entry) {
                return entry.getId();
            }
        });
    }

    /**
     * 获取交易信息映射
     *
     * @param tradeInfoIds 交易信息id
     * @return id与实体的map对象
     */
    private Map<Long, UserTradeInfo> getMappedTradeInfo(Long... tradeInfoIds) {
        List<UserTradeInfo> tradeInfos = userTradeInfoDao.findInIds(tradeInfoIds);
        return Maps.uniqueIndex(tradeInfos, new Function<UserTradeInfo, Long>() {
            public Long apply(UserTradeInfo entry) {
                return entry.getId();
            }
        });
    }

    /**
     * 获取交易扩展信息映射
     *
     * @param orderIds 交易信息id
     * @return orderId与实体的map对象
     */
    private Map<Long, OrderExtra> getMappedOrderExtra(Long... orderIds) {
        List<OrderExtra> extras = orderExtraDao.findInOrderIds(orderIds);
        return Maps.uniqueIndex(extras, new Function<OrderExtra, Long>() {
            public Long apply(OrderExtra entry) {
                return entry.getOrderId();
            }
        });
    }


    /**
     * 海尔订单列表
     *
     * @param orderPaging     订单分页信息
     * @param items           所有的子订单信息
     * @param mappedTradeInfo 交易信息
     * @return 封装后的海尔订单，包含子订单信息及交易信息和发票信息
     */
    private List<HaierOrder> getHaierOrders(Paging<Order> orderPaging,
                                            List<OrderItem> items,
                                            Map<Long, UserTradeInfo> mappedTradeInfo,
                                            Map<Long, OrderExtra> mappedOrderExtra,
                                            Map<Long, Sku> mappedItem,
                                            Map<Long, User> mappedBuyer) {

        List<HaierOrder> haierOrders = Lists.newLinkedList();
        Map<Long, HaierOrder> maps = Maps.newTreeMap();

        for (Order order : orderPaging.getData()) {
            try {

                Response<ShopExtra> shopExtraResult = shopService.getExtra(order.getSellerId());
                checkState(shopExtraResult.isSuccess(), shopExtraResult.getError());
                ShopExtra shopExtra = shopExtraResult.getResult();

                // 填充商家8码
                HaierOrder dto = HaierOrder.transform(order, shopExtra.getShopId(), shopExtra.getOuterCode());
                UserTradeInfo tradeInfo = mappedTradeInfo.get(order.getTradeInfoId());
                if (tradeInfo != null)
                    fillWithTradeInfo(dto, tradeInfo);  // 填入交易信息

                OrderExtra extra = mappedOrderExtra.get(order.getId());
                if (extra != null)
                    fillWithOrderExtraAndTradeInfo(dto, extra, tradeInfo);  // 填入发票信息
                maps.put(dto.getId(), dto);
                User buyer = mappedBuyer.get(dto.getBuyerId());
                dto.setBuyerName(buyer.getName());

                // 抢券ID
                dto.setRusherId(1L);
                // 填充卖家id
                dto.setSellerId(order.getSellerId());

                haierOrders.add(dto);
            } catch (IllegalStateException e) {
                log.error("fail to deal with order:{}, error:{}", order, e.getMessage());
            }
        }

        for (OrderItem orderItem : items) {
            HaierOrderItem itemDto = HaierOrderItem.transform(orderItem);
            HaierOrder orderDto = maps.get(orderItem.getOrderId());
            List<HaierOrderItem> orderItems = orderDto.getItems();
            orderItems.add(itemDto);
            Sku sku = mappedItem.get(orderItem.getSkuId());
            if (sku != null)
                fillWithSku(itemDto, sku);
            itemDto.setDeliveryPromise(orderItem.getDeliveryPromise());
        }
        return haierOrders;
    }


    /**
     * 填入商品信息
     *
     * @param itemDto 海尔子订单对象
     * @param sku     库存
     */
    private void fillWithSku(HaierOrderItem itemDto, Sku sku) {
        int originPrice = sku.getPrice();  // 由于暂无营销活动，现在的原价即是实际价格
        int realPrice = itemDto.getFee() / itemDto.getQuantity(); //商品实际价格 = 子订单总价 / 商品数
        int discount = originPrice - realPrice;

        itemDto.setOriginPrice(originPrice);  // 原价
        itemDto.setDiscount(discount); // 折扣 = 原价 - 实际价
        itemDto.setPrice(realPrice); // 商品实际价格
        itemDto.setOuterId(sku.getOuterId());

    }

    /**
     * 填入买家交易信息（包含配送地址、电话、联系人等）
     *
     * @param dto       海尔订单对象
     * @param tradeInfo 交易信息
     */
    private void fillWithTradeInfo(HaierOrder dto, UserTradeInfo tradeInfo) {
        // 电话号码头尾不允许空格
        if (notEmpty(tradeInfo.getPhone())) {
            dto.setPhone(tradeInfo.getPhone().trim());
        } else {
            dto.setPhone(tradeInfo.getPhone());
        }

        dto.setProvince(tradeInfo.getProvince());
        dto.setCity(tradeInfo.getCity());
        dto.setDistrict(tradeInfo.getDistrict());
        dto.setProvinceCode(tradeInfo.getProvinceCode());
        dto.setCityCode(tradeInfo.getCityCode());

        // 地区编码这里要做特殊处理,如果是东莞市或者中山市的特殊编码，则置空地区
        dto.setDistrictCode(tradeInfo.getDistrictCode());
        if (equalWith(dto.getDistrictCode(), 999998) || equalWith(dto.getDistrictCode(), 999999)) {
            dto.setDistrictCode(null);
//            dto.setDistrictCode(tradeInfo.getCityCode());
        }

        dto.setStreet(tradeInfo.getStreet());
        dto.setZip(tradeInfo.getZip());
        dto.setName(tradeInfo.getName());
    }

    /**
     * 填入订单的补充信息以及 收件人信息(增值税）
     *
     * @param dto   海尔订单对象
     * @param extra 订单补充信息
     */
    private void fillWithOrderExtraAndTradeInfo(HaierOrder dto, OrderExtra extra, UserTradeInfo userTradeInfo) {
        dto.setInvoice(extra.getInvoice());

        //  转发票信息
        if (notEmpty(extra.getInvoice())) {
            try {
                Map mappedInvoice = new ObjectMapper().readValue(extra.getInvoice(), Map.class);

                String type = getStringValue(mappedInvoice.get("type"));
                dto.setInvoiceType(type);
                if (equalWith(type, OrderExtra.Type.VAT.value())) {
                    Map vat = (Map) mappedInvoice.get("vat");
                    dto.setCompanyName(getStringValue(vat.get("companyName")));
                    dto.setTaxRegisterNo(getStringValue(vat.get("taxRegisterNo")));
                    dto.setRegisterAddress(getStringValue(vat.get("registerAddress")));
                    dto.setRegisterPhone(getStringValue(vat.get("registerPhone")));
                    dto.setRegisterBank(getStringValue(vat.get("registerBank")));
                    dto.setBankAccount(getStringValue(vat.get("bankAccount")));
                    dto.setTaxCertificate(getStringValue(vat.get("taxCertificate")));
                    dto.setTaxpayerCertificate(getStringValue(vat.get("taxpayerCertificate")));


                    // 用配送信息来填充增税发票的收件人相关信息
                    dto.setReceiveName(userTradeInfo.getName());
                    dto.setReceivePhone(userTradeInfo.getPhone());
                    dto.setReceiveZip(userTradeInfo.getZip());
                    // 地址需要拼装省市区
                    dto.setRegisterAddress(Joiner.on(",").skipNulls().join(dto.getProvince(),
                            dto.getCity(), dto.getDistrict(), dto.getStreet()));

                } else {
                    dto.setInvoiceTitle(getStringValue(mappedInvoice.get("title")));
                }


            } catch (Exception e) {
                log.error("fail to covert json:{}, cause:{}", extra.getInvoice(), Throwables.getStackTraceAsString(e));

            }
        }

        dto.setBuyerNotes(extra.getBuyerNotes());
        // 送达时段
        dto.setDeliverTime(extra.getDeliverTime());
    }

    private String getStringValue(Object o) {
        return String.valueOf(Objects.firstNonNull(o, ""));
    }

    /**
     * 获取指定id的订单，封装成HaierOrder返回
     *
     * @param id 订单号
     * @return 订单
     */
    public Response<HaierOrder> findHaierOrderById(Long id) {
        Response<HaierOrder> result = new Response<HaierOrder>();
        try {
            Order order = orderDao.findById(id);

            Response<ShopExtra> shopExtraResult = shopService.getExtra(order.getSellerId());
            checkState(shopExtraResult.isSuccess(), shopExtraResult.getError());
            ShopExtra shopExtra = shopExtraResult.getResult();

            List<OrderItem> items = orderItemDao.findByOrderId(order.getId());
            HaierOrder haierOrder = HaierOrder.transform(order, items, shopExtra.getShopId(), shopExtra.getOuterCode());
            OrderExtra extra = orderExtraDao.findByOrderId(order.getId());
            UserTradeInfo tradeInfo = userTradeInfoDao.findById(order.getTradeInfoId());

            if (tradeInfo != null)
                fillWithTradeInfo(haierOrder, tradeInfo);

            if (extra != null)
                fillWithOrderExtraAndTradeInfo(haierOrder, extra, tradeInfo);

            List<HaierOrderItem> haierOrderItems = haierOrder.getItems();

            int itemLen = haierOrderItems.size();
            List<Long> skuIds = Lists.newArrayListWithCapacity(itemLen);

            for (HaierOrderItem haierOrderItem : haierOrderItems) {
                skuIds.add(haierOrderItem.getSkuId());
            }

            Map<Long, Sku> mappedSku = getMappedSku(skuIds);

            for (HaierOrderItem haierOrderItem : haierOrderItems) {
                Sku sku = mappedSku.get(haierOrderItem.getSkuId());
                if (sku != null)
                    fillWithSku(haierOrderItem, sku);
            }

            Response<User> buyerQueryResult = accountService.findUserById(order.getSellerId());
            checkState(buyerQueryResult.isSuccess(), buyerQueryResult.getError());
            User buyer = buyerQueryResult.getResult();

            haierOrder.setBuyerName(buyer.getName());
            result.setResult(haierOrder);
            return result;
        } catch (IllegalStateException e) {
            log.error("fail to query haier order with id={}, error:{}", id, e.getMessage());
            result.setError("order.get.fail");
            return result;
        } catch (Exception e) {
            log.error("fail to query haier order with id={}", id, e);
            result.setError("order.get.fail");
            return result;
        }
    }

    @Override
    public Response<List<OrderItem>> findOrderItemByOrderId(Long orderId) {
        Response<List<OrderItem>> result = new Response<List<OrderItem>>();
        if (orderId == null) {
            log.error("order id can not be null");
            result.setError("illegal.params");
            return result;
        }
        try {
            List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
            result.setResult(orderItems);
            return result;
        } catch (Exception e) {
            log.error("failed to find orderItem by orderId, cause:{}", e);
            result.setError("orderItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<OrderItem>> findExpiredUncommentedOrderItemId(Integer expireDays) {
        Response<List<OrderItem>> result = new Response<List<OrderItem>>();
        try {
            List<OrderItem> orderItems = orderItemDao.findOutOfCommentDeadLineOrderItem(15);

            result.setResult(orderItems);
            return result;
        } catch (Exception e) {
            log.error("failed to find out of comment date order item, e:{}", e);
            result.setError("orderItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<OrderItem> findOrderItemById(Long orderItemId) {
        Response<OrderItem> result = new Response<OrderItem>();
        try {
            OrderItem orderItem = orderItemDao.findById(orderItemId);
            result.setResult(orderItem);
            return result;
        } catch (Exception e) {
            log.error("failed to find orderItem by id={},cause:{}", orderItemId,
                    Throwables.getStackTraceAsString(e));
            result.setError("orderItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<OrderItem> findOrderItemByOriginId(Long orderItemId) {
        Response<OrderItem> result = new Response<OrderItem>();
        try {
            OrderItem orderItem = orderItemDao.findByOriginId(orderItemId);
            result.setResult(orderItem);
            return result;
        } catch (Exception e) {
            log.error("failed to find orderItem by origin id={},cause:{}", orderItemId,
                    Throwables.getStackTraceAsString(e));
            result.setError("orderItem.query.fail");
            return result;
        }
    }


    @Override
    public Response<ItemBundlePreOrder> findItemBundlePreOrder(Long id, String skus) {
        Response<ItemBundlePreOrder> result = new Response<ItemBundlePreOrder>();
        if (id == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            Map<Long, Integer> skuIdAndQuantity = mapper.fromJson(skus,
                    mapper.createCollectionType(HashMap.class, Long.class, Integer.class));
            List<RichOrderItem> richOrderItems = getRichOrderItem(skuIdAndQuantity);
            Response<ItemBundle> itemBundleR = itemBundleService.findById(id);
            if (!itemBundleR.isSuccess()) {
                log.error("fail to find itemBundle by id={}, error code:{}", id, itemBundleR.getError());
                result.setError("item.bundle.query.fail");
                return result;
            }
            ItemBundle itemBundle = itemBundleR.getResult();

            Long userId = itemBundle.getSellerId();
            Response<Shop> shopR = shopService.findByUserId(userId);
            if (!shopR.isSuccess()) {
                log.error("fail to find shop by userId={}, error code:{}", userId, shopR.getError());
                result.setError(shopR.getError());
                return result;
            }
            Shop shop = shopR.getResult();
            ItemBundlePreOrder ibpo = new ItemBundlePreOrder();
            ibpo.setItemBundle(itemBundle);
            ibpo.setIsCod(shop.getIsCod());
            ibpo.setShopName(shop.getName());
            ibpo.setRichOrderItems(richOrderItems);
            result.setResult(ibpo);
            return result;
        } catch (Exception e) {
            log.error("fail to find item bundle preOrder by id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("item.bundle.pre.order.query.fail");
            return result;
        }
    }

    /**
     * 汇总指定商户某一天的退款金额
     *
     * @param sellerId 商户id
     * @param refundAt 退款时间
     * @return 退款总金额
     */
    @Override
    public Response<Long> sumRefundAmountOfSellerInDate(Long sellerId, Date refundAt) {
        Response<Long> result = new Response<Long>();

        try {
            checkArgument(notNull(sellerId), "seller.id.empty");
            checkArgument(notNull(refundAt), "refund.at.empty");

            Long sum = orderItemDao.sumRefundAmountOfShopInDate(sellerId, refundAt);
            result.setResult(sum);

        } catch (IllegalArgumentException e) {
            log.error("fail to sum refundAmount of shop(userId:{}), refundAt:{}, error:{}", sellerId, refundAt, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to sum refundAmount of shop(userId:{}), refundAt:{}, error:{}", sellerId, refundAt, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to sum refundAmount of shop(userId:{}), refundAt:{}, cause:{}", sellerId, refundAt, Throwables.getStackTraceAsString(e));
            result.setError("order.refund.sum.fail");
        }

        return result;
    }


    public Response<Paging<OrderItem>> findRefundedOrderItemInDate(Date refundAt, Integer pageNo, Integer size) {
        Response<Paging<OrderItem>> result = new Response<Paging<OrderItem>>();
        try {
            checkArgument(notNull(refundAt), "refund.at.can.not.be.empty");
            checkArgument(notNull(pageNo), "page.no.can.not.be.empty");
            checkArgument(notNull(size), "size.can.not.be.empty");

            OrderItem criteria = new OrderItem();
            criteria.setRefundAt(refundAt);


            PageInfo pageInfo = new PageInfo(pageNo, size);
            Paging<OrderItem> paging = orderItemDao.findBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.error("fail to find refunded orderItems of refundAt:{}, pageNo:{}, size:{}, error:{}",
                    refundAt, pageNo, size, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to find refunded orderItems of refundAt:{}, pageNo:{}, size:{}, error:{}",
                    refundAt, pageNo, size, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to find refunded orderItems of refundAt:{}, pageNo:{}, size:{}, cause:{}",
                    refundAt, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setError("order.item.query.fail");
        }

        return result;
    }



    @Override
    public Response<OrderDescription> getDescriptionOfOrders(List<Long> ids) {
        Response<OrderDescription> result = new Response<OrderDescription>();

        try {
            checkArgument(!CollectionUtils.isEmpty(ids), "order.ids.can.not.be.empty");
            List<String> nameOfOrders = orderItemDao.findItemNameOfOrders(ids.toArray(new Long[ids.size()]));
            checkState(!CollectionUtils.isEmpty(nameOfOrders), "order.items.not.found");

            // 若只有一个商品，则第一个商品作为标题
            String title = nameOfOrders.get(0);

            if (title.length() > 200) { // 标题不允许超过200个字符
                title = title.substring(0, 200);
            } else if (nameOfOrders.size() > 1) { // 未超过200个字符的情况下有多个商品，则显示xxx等字样
                title += "等..";
            }

            String content = Joiner.on(ITEM_SEPARATOR).join(nameOfOrders);

            if (content.length() > 900) {  // 内容不允许超过900个字符
                content = content.substring(0, 900);
            }

            result.setResult(new OrderDescription(title, content));
        } catch (IllegalArgumentException e) {
            log.warn("fail to get desc of orders of {}, error:{}", ids, e.getMessage());
            result.setResult(new OrderDescription());
        } catch (IllegalStateException e) {
            log.warn("fail to get desc of orders of {}, error:{}", ids, e.getMessage());
            result.setResult(new OrderDescription());
        } catch (Exception e) {
            log.warn("fail to get desc of orders of {}, cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setResult(new OrderDescription());
        }

        return result;
    }

    @Override
    public Response<Long> maxId() {
        Response<Long> result = new Response<Long>();

        try {
            Long maxId = orderDao.maxId();
            result.setResult(maxId);
            return result;
        }catch (Exception e) {
            log.error("fail to find maxId of order, cause:{}", e);
            result.setError("order.max.id.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> maxIdOfOrderItem() {
        Response<Long> result = new Response<Long>();

        try {
            Long maxId = orderItemDao.maxId();
            result.setResult(maxId);
            return result;
        }catch (Exception e) {
            log.error("fail to find max orderItem id, cause:{}",e);
            result.setError("orderItem.max.id.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Order>> findNotConfirmDeliverOrder(Long lastId, String startAt, String endAt, Integer size) {
        Response<List<Order>> result = new Response<List<Order>>();

        if(lastId == null || startAt == null || endAt == null) {
            log.error("params illegal when find not confirm deliver order");
            result.setError("illegal.param");
            return result;
        }

        size = Objects.firstNonNull(size, 200);

        try {
            List<Order> orders = orderDao.findNotConfirmDeliver(lastId, startAt, endAt, size);
            result.setResult(orders);
            return result;
        }catch (Exception e) {
            log.error("fail to find not confirm deliver order by lastId={}, startAt={}, endAt={}, size={}, cause:{}",
                    lastId, startAt, endAt, size, Throwables.getStackTraceAsString(e));
            result.setError("not.confirm.deliver.order.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<OrderItem>> findNotConfirmRefund(Long lastId, String startAt, String endAt, Integer size) {
        Response<List<OrderItem>> result = new Response<List<OrderItem>>();

        if(lastId == null || startAt == null || endAt == null) {
            log.error("params illegal when find not confirm refund orderItem");
            result.setError("illegal.param");
            return result;
        }

        size = Objects.firstNonNull(size, 200);

        try {
            List<OrderItem> orderItems = orderItemDao.findNotConfirmRefund(lastId, startAt, endAt, size);
            result.setResult(orderItems);
            return result;
        }catch (Exception e) {
            log.error("fail to find not confirm refund orderItem by lastId={}, startAt={}, endAt={}, size={}, cause:{}",
                    lastId, startAt, endAt, size, Throwables.getStackTraceAsString(e));
            result.setError("not.confirm.refund.orderItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<OrderExtra> getOrderExtraByOrderId(Long orderId) {
        Response<OrderExtra> result = new Response<OrderExtra>();
        try {
            checkArgument(!isNull(orderId),"illegal.param");
            OrderExtra orderExtra = orderExtraDao.findByOrderId(orderId);
            result.setResult(orderExtra);
        } catch (IllegalArgumentException e){
            log.error("failed to find orderExtra order(id={}),error:{} ",
                    orderId, e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e) {
            log.error("failed to find orderExtra order(id={}),cause:{} ",
                    orderId, Throwables.getStackTraceAsString(e));
            result.setError("orderExtra.query.fail");
        }
        return result;
    }

    @Override
    public Response<Long> createOrderExtra(OrderExtra orderExtra) {
        Response<Long> result = new Response<Long>();
        try {
            checkArgument(!isNull(orderExtra),"illegal.param");
            Long id = orderExtraDao.create(orderExtra);
            result.setResult(id);
        } catch (IllegalArgumentException e){
            log.error("failed to create orderExtra {},error:{} ",
                    orderExtra, e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e) {
            log.error("failed to find orderExtra {},cause:{} ",
                    orderExtra, Throwables.getStackTraceAsString(e));
            result.setError("orderExtra.create.fail");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateOrderExtra(OrderExtra orderExtra) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(orderExtra),"illegal.param");
            checkArgument(!isNull(orderExtra.getId()),"illegal.param");
            checkArgument(!isNull(orderExtra.getOrderId()),"illegal.param");
            Boolean isUpdate = orderExtraDao.updateByOrderId(orderExtra);
            result.setResult(isUpdate);
        } catch (IllegalArgumentException e){
            log.error("failed to update orderExtra {},error:{} ",
                    orderExtra, e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e) {
            log.error("failed to update orderExtra {},cause:{} ",
                    orderExtra, Throwables.getStackTraceAsString(e));
            result.setError("orderExtra.update.fail");
        }
        return result;
    }

    private List<RichOrderItem> getRichOrderItem(Map<Long, Integer> skuIdAndQuantity) {
        List<RichOrderItem> result = Lists.newArrayList();
        for (Long skuId : skuIdAndQuantity.keySet()) {
            Integer quantity = skuIdAndQuantity.get(skuId);
            if (quantity <= 0) {
                log.error("sku quantity can not litter than 1");
                continue;
            }
            Response<Sku> sr = itemService.findSkuById(skuId);
            if (!sr.isSuccess()) {
                log.error("failed to find sku where id = {},error code:{}", skuId, sr.getError());
                continue;
            }
            Sku sku = sr.getResult();
            if (sku.getStock() < quantity) {
                log.warn("no enough stock for sku where id={} (required:{},stock:{})", skuId, quantity, sku.getStock());
                continue;
            }
            Response<Item> ir = itemService.findById(sku.getItemId());
            if (!ir.isSuccess()) {
                log.error("failed to find item(id={}),error code:{}", sku.getItemId(), ir.getError());
                continue;
            }
            Item item = ir.getResult();
            if (!Objects.equal(item.getStatus(), Item.Status.ON_SHELF.toNumber())) {
                log.warn("item(id={}) is not onShelf,so skip this {}", item.getId(), sku);
                continue;
            }
            RichOrderItem roi = new RichOrderItem();
            roi.setSku(sku);
            roi.setItemName(item.getName());
            roi.setItemImage(item.getMainImage());
            roi.setFee(sku.getPrice() * quantity);
            roi.setCount(quantity);

            //是否是ehaier商家
            roi.setIsEhaier(Objects.equal(eHaierSellerId,item.getUserId().toString()));

            Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(item.getDeliveryMethodId());
            if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
                log.error("fail to find delivery method by id={}, error code={}",
                        item.getDeliveryMethodId(), deliveryMethodR.getError());
            }else {
                roi.setDeliveryPromise(deliveryMethodR.getResult().getName());
            }
            result.add(roi);
        }
        return result;
    }
    @Override
	public boolean isUserStatus(Map<String, Object> map) {
		String flog=orderItemDao.smsUserStatus(map);
		if(flog==null || flog.equals("") || flog.equals("1")){
			return true;
		}
		return false;
	}

	@Override
	public List<Map<String, Object>> getMoblieList() {
		List<Long> itemIdList=orderItemDao.itmeIdList();
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("itemIdList", itemIdList);
		List<Map<String, Object>> buyerIdList=orderItemDao.buyerIdList(map);
		List<Map<String, Object>> moblieList=new ArrayList<Map<String, Object>>();
	    for (int i = 0; i < buyerIdList.size(); i++) {
	    	Map<String, Object> buyerMap=new HashMap<String, Object>();
	    	Order order=orderDao.findById(Long.parseLong(buyerIdList.get(i).get("order_id").toString()));
	    	buyerMap.put("moblie", userTradeInfoDao.findById(order.getTradeInfoId()).getPhone());
	    	buyerMap.put("name", accountService.findUserById(Long.parseLong(buyerIdList.get(i).get("buyer_id").toString())).getResult().getName());
	    	//buyerMap.put("moblie", accountService.findUserById(Long.parseLong(buyerIdList.get(i).get("buyer_id").toString())).getResult().getMobile());
	    	buyerMap.put("itemName", buyerIdList.get(i).get("item_name"));
	    	buyerMap.put("orderDate", buyerIdList.get(i).get("orderDate"));
	    	buyerMap.put("itemId", buyerIdList.get(i).get("item_id"));
	    	moblieList.add(buyerMap);
	    }		
		return moblieList;
	}

	@Override
	public Integer updateSmsFloag(Map<String, Object> map) {
		return orderItemDao.updateSmsFloag(map);
	}

    /**
     * 取得押金试用失联（>=45天）的订单
     * @param ids id列表
     * @return  订单列表
     */
    @Override
    public Response<List<Long>> findOnTrialTimeOutOrder(List<Long> ids) {
        Response<List<Long>> result = new Response<List<Long>>();
        if (ids.isEmpty()) {
            log.warn("ids is empty, return directly");
            result.setResult(Collections.<Long>emptyList());
            return result;
        }
        try {
            List<Long> orders = orderDao.findOnTrialTimeOutOrder(ids);
            result.setResult(orders);
            return result;
        } catch (Exception e) {
            log.error("failed to find on tria time out order by ids, cause:", e);
            result.setError("order.query.fail");
            return result;
        }
    }

    /**
     * 根据优惠券Id 查询优惠券对应的订单信息
     * **/
    @Override
    public Map<String, Object> queryOrderCouponsByCouponsId(@ParamInfo("baseUser") BaseUser baseUser, @ParamInfo("pageNo") Integer pageNo, @ParamInfo("count") Integer size, @ParamInfo("couponsId") Long couponsId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("couponsId",couponsId);
        Response<Paging<RichOrderSellerView>> result = new Response<Paging<RichOrderSellerView>>();

        PageInfo pageInfo = new PageInfo(pageNo, size);

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("sellerId", baseUser.getId().toString());
            params.put("couponsId", couponsId.toString());

            Paging<Long> orderIds = orderItemDao.queryOrderCouponsByCouponsId(params, pageInfo.getOffset(), pageInfo.getLimit());
            if (orderIds.getTotal() > 0) {
                List<Order> orders = orderDao.findByIds(orderIds.getData());
                List<RichOrderSellerView> richOrderSellerViews = buildOrdersFromSellerView(orders, false);
                result.setResult(new Paging<RichOrderSellerView>(orderIds.getTotal(), richOrderSellerViews));
                map.put("lists",result.getResult());
                return map;
            } else {
                result.setResult(new Paging<RichOrderSellerView>(0L, Collections.<RichOrderSellerView>emptyList()));
                map.put("lists",result.getResult());
                return map;
            }
        } catch (Exception e) {
            log.error("fail to find orders by seller pageNo={},count={},status={},cause:{} ,couponsId:{}",
                    pageNo, size, couponsId, Throwables.getStackTraceAsString(e));
            result.setError("order.query.fail");
            map.put("lists",result.getResult());
            return map;
        }
    }

    @Override
    public Response<Boolean> containByTradeInfoId(Long userId, Long tradeInfoId) {

        Response<Boolean> result = new Response<Boolean>();
        result.setResult(false);
        try {

//            UserTradeInfo userTradeInfo = userTradeInfoDao.findById(tradeInfoId);
//            if (!isNull(userTradeInfo)) {
//                preAuthorizationDao.findPredepositOrderByUserId(userId);
//            }

            Paging<Long> orderIdsR = preAuthorizationDao.findPredepositOrderByUserId(userId);

            if (orderIdsR.getData().size() > 0) {
                List<Order> orderList = orderDao.findByIds(orderIdsR.getData());
                for (Order order : orderList) {
                    if (tradeInfoId.equals(order.getTradeInfoId())) {
                        result.setResult(true);
                        break;
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("failed to find on tria time out order by ids, cause:", e);
            result.setResult(true);
            result.setError("order.query.fail");
            return result;
        }
    }

}
