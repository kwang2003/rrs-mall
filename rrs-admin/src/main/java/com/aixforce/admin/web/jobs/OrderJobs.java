package com.aixforce.admin.web.jobs;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.agreements.service.PreAuthorizationDepositOrderService;
import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.alipay.request.CallBack;
import com.aixforce.alipay.request.CreateAndPay;
import com.aixforce.alipay.request.RefundRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.constant.ExpireTimes;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.buying.service.BuyingActivityDefinitionService;
import com.aixforce.rrs.buying.service.BuyingTempOrderService;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.OrderJobOverDay;
import com.aixforce.trade.service.OrderJobOverDayService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.rrs.settle.util.SettlementVerification.needSettlementAfterSuccess;
import static com.aixforce.rrs.settle.util.SettlementVerification.needSettlementAfterSuccessForPresale;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yangzefeng on 14-2-22
 */
@Component
public class OrderJobs {


    private final AdminLeader adminLeader;

    private final OrderWriteService orderWriteService;

    private final PreSaleService preSaleService;

    private final OrderQueryService orderQueryService;

    private final SettlementService settlementService;

    private final BuyingActivityDefinitionService buyingActivityDefinitionService;

    private final BuyingTempOrderService buyingTempOrderService;

    private final PreAuthorizationDepositOrderService preAuthorizationDepositOrderService;

    private final static Logger log = LoggerFactory.getLogger(OrderJobs.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final int PAGE_SIZE = 200;

    private static final String productCode = "FUND_TRADE_FAST_PAY";

    private final Token token;

    @Value("#{app.alipayRefundSuffix}")
    private String notifyUrl;

    @Value("#{app.alipayOnTrialTimeOutSuffix}")
    private String alipayOnTrialTimeOutSuffix;

    @Autowired
    private OrderJobOverDayService orderJobOverDayService;

    private final PreDepositService preDepositService;

    @Autowired
    public OrderJobs(AdminLeader adminLeader,
                     OrderWriteService orderWriteService,
                     PreSaleService preSaleService,
                     OrderQueryService orderQueryService,
                     SettlementService settlementService,
                     BuyingActivityDefinitionService buyingActivityDefinitionService,
                     BuyingTempOrderService buyingTempOrderService,
                     PreAuthorizationDepositOrderService preAuthorizationDepositOrderService,
                     Token token, PreDepositService preDepositService) {
        this.adminLeader = adminLeader;
        this.orderWriteService = orderWriteService;
        this.preSaleService = preSaleService;
        this.orderQueryService = orderQueryService;
        this.settlementService = settlementService;
        this.buyingActivityDefinitionService=buyingActivityDefinitionService;
        this.buyingTempOrderService = buyingTempOrderService;
        this.preAuthorizationDepositOrderService = preAuthorizationDepositOrderService;
        this.token = token;
        this.preDepositService = preDepositService;
    }

    /**
     * run every 1:00
     */
//    @Scheduled(cron = "0 0 1 * * *")
    public void orderExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }


        log.info("[ORDER_EXPIRE_VERIFICATION] verify order expire job begin");
        orderWriteService.verifyOrderExpire(new Date());
        log.info("[ORDER_EXPIRE_VERIFICATION] verify order expire job end");

    }

    @Scheduled(cron = "0 0 1 * * *")
    public void orderNotPaidExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[ORDER_EXPIRE_VERIFICATION] verify order not paid expire job begin");
        orderWriteService.verifyOrderNotPaidExpire(new Date());
        log.info("[ORDER_EXPIRE_VERIFICATION] verify order not paid expire job end");
    }

    /**
     * 这里同时也处理预售订单，预售订单和普通订单的超时处理只有超时未付款分开
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void orderNotConfirmDeliverExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[ORDER_EXPIRE_VERIFICATION] verify order not confirm deliver expire job begin");

        Date date = new Date();

        String endAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_CONFIRM_EXPIRE_DAY));
        String startAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_CONFIRM_EXPIRE_DAY+ExpireTimes.MONTH_DAY));

        Response<Long> maxIdR = orderQueryService.maxId();
        if(!maxIdR.isSuccess()) {
            log.error("fail to find max id of order, error code:{}", maxIdR.getError());
        }

        Long lastId = maxIdR.getResult() + 1;
        log.info("begin to verify order create after {}, before", startAt, endAt);
        int returnSize = PAGE_SIZE;
        int handled = 0;

        // ADD START
        // 查找未执行job的试金行动净水订单（下单时根据sku配置表插入此表中）
        OrderJobOverDay orderJobOverDay = new OrderJobOverDay();
        orderJobOverDay.setStatus(0);
        orderJobOverDay.setOverDayStart(new Date());

        Map map = Maps.newLinkedHashMap();

        Response<Paging<OrderJobOverDay>> orderJobOverDayResponse = orderJobOverDayService.findBy(orderJobOverDay);
        if(!orderJobOverDayResponse.isSuccess()) {
            log.error("fail to find orderJobOverDay id of orderJobOverDay, error code:{}", orderJobOverDayResponse.getError());
        } else {
            for (OrderJobOverDay overDay : orderJobOverDayResponse.getResult().getData()) {
                map.put(overDay.getOrderId(), (Object) overDay.getOverDay());
            }
        }

        List<Order> orders_temp = null;
        // ADD END

        while(returnSize == PAGE_SIZE) {
            Response<List<Order>> ordersR = orderQueryService.findNotConfirmDeliverOrder(lastId, startAt, endAt, PAGE_SIZE);

            if(!ordersR.isSuccess()) {
                log.error("fail to find not confirm deliver order by lastId={}, startAt={}, endAt={}, size={}, error code={}",
                        lastId, startAt, endAt, PAGE_SIZE, ordersR.getError());
                continue;
            }
            List<Order> orders = ordersR.getResult();

            // ADD START
            // 试金行动净水订单过滤
            orders_temp = new ArrayList<Order>();

            for (Order order : orders) {
                if (map.get(order.getId()) == null) {
                    orders_temp.add(order);
                }
            }
            orders = orders_temp;
            // ADD END

            if(orders.isEmpty()) {
                log.info("no more order, lastId={}", lastId);
                break;
            }else {
                handled += notConfirmDeliverExpire(orders);
            }

            returnSize = orders.size();
            handled += orders.size();
            lastId = orders.get(orders.size()-1).getId();
        }

        // ADD START
        // 试金行动净水订单job执行

        // 查找未执行job的试金行动净水订单（下单时根据sku配置表插入此表中）
        OrderJobOverDay orderJobOverDay2 = new OrderJobOverDay();
        orderJobOverDay2.setStatus(0);
        orderJobOverDay2.setOverDayEnd(new Date());

        List<Long> orderLists = new ArrayList<Long>();

        Response<Paging<OrderJobOverDay>> orderJobOverDayResponse2 = orderJobOverDayService.findBy(orderJobOverDay2);
        if(!orderJobOverDayResponse2.isSuccess()) {
            log.error("fail to find orderJobOverDay id of orderJobOverDay, error code:{}", orderJobOverDayResponse2.getError());
        } else {
            for (OrderJobOverDay overDay : orderJobOverDayResponse2.getResult().getData()) {
                orderLists.add(overDay.getOrderId());
            }
        }

        Response<List<Order>> orderJobList = orderQueryService.findByIds(orderLists);
        if(!orderJobList.isSuccess()) {
            log.error("fail to find order by ids={}",
                    orderLists, orderJobList.getError());
        } else {
            handled += notConfirmDeliverExpire(orderJobList.getResult());

            orderJobOverDayService.updateStatusByOrderIds(orderLists);
        }
        // ADD END

        log.info("[ORDER_EXPIRE_VERIFICATION] verify order not confirm deliver expire job end handled {} orders", handled);
    }

    /**
     * 这里同时也处理预售订单，预售订单和普通订单的超时处理只有超时未付款分开
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void orderItemNotConfirmRefundExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[ORDER_EXPIRE_VERIFICATION] verify orderItem not confirm refund expire job begin");

        Date date = new Date();

        String endAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_CONFIRM_EXPIRE_DAY));
        String startAt = DATE_TIME_FORMAT.print(new DateTime(date).withTimeAtStartOfDay().minusDays(ExpireTimes.NOT_CONFIRM_EXPIRE_DAY+ExpireTimes.MONTH_DAY));

        Response<Long> maxIdR = orderQueryService.maxIdOfOrderItem();
        if(!maxIdR.isSuccess()) {
            log.error("fail to find maxId of orderItem, error code={}", maxIdR.getError());
        }

        Long lastId = maxIdR.getResult() + 1;
        log.info("begin to verify order create after {}, before", startAt, endAt);
        int returnSize = PAGE_SIZE;
        int handled = 0;

        while(returnSize == PAGE_SIZE) {
            Response<List<OrderItem>> orderItemsR = orderQueryService.findNotConfirmRefund(lastId, startAt, endAt, PAGE_SIZE);

            if(!orderItemsR.isSuccess()) {
                log.error("fail to find not confirm refund order by lastId={}, startAt={}, endAt={}, size={}, error code={}",
                        lastId, startAt, endAt, PAGE_SIZE, orderItemsR.getError());
                continue;
            }
            List<OrderItem> orderItems = orderItemsR.getResult();

            if(orderItems.isEmpty()) {
                log.info("no more order, lastId={}", lastId);
                break;
            }else {
                handled += notConfirmRefundExpire(orderItems);
            }

            returnSize = orderItems.size();
            handled += orderItems.size();
            lastId = orderItems.get(orderItems.size()-1).getId();
        }

        log.info("[ORDER_EXPIRE_VERIFICATION] verify orderItem not confirm refund expire job end handled {} orders", handled);
    }

    private int notConfirmDeliverExpire(List<Order> orders) {
        int handled = 0;
        for(Order order : orders) {
            try {
                Date now = new Date();

                //更新总订单和子订单状态
                Order updated = new Order();
                updated.setId(order.getId());
                updated.setStatus(Order.Status.DONE.value());
                updated.setFinishedAt(now);
                updated.setDoneAt(now);
                if(Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
                    updated.setPaidAt(now);
                }
                orderWriteService.updateOrder(updated);
                orderWriteService.bathUpdateOrderItemStatusByOrderId(Order.Status.DELIVERED.value(),
                        Order.Status.DONE.value(), order.getId(), null);

                order = getOrder(order.getId());
                createSettlementAfterConfirm(order);

                handled ++;
            } catch (IllegalStateException e) {
                log.error("fail to expire order id={}, error:{}, skip it", order.getId(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to expire order id={}, cause:{}, skip it", order.getId(), Throwables.getStackTraceAsString(e));
            }
        }
        return handled;
    }


    private Order getOrder(Long orderId) {
        Response<Order> orderR = orderQueryService.findById(orderId);
        checkState(orderR.isSuccess(), orderR.getError());
        return orderR.getResult();
    }

    private void createSettlementAfterConfirm(Order order) {
        //普通订单-货到付款-交易成功
        if (needSettlementAfterSuccess(order)) {
            Response<Long> createResult = settlementService.generate(order.getId());
            checkState(createResult.isSuccess(), createResult.getError());
        }
        //预售订单-货到付款-交易成功(尾款) 只生成尾款子结算(定金子结算在支付成功后已生成)
        /*if(needSettlementAfterSuccessForPresale(order)){
            Response<Long> created = settlementService.generateForPresaleRest(order.getId());
            checkState(created.isSuccess(), created.getError());

        }*/
    }


    private int notConfirmRefundExpire(List<OrderItem> orderItems) {
        int handled = 0;
        for(OrderItem oi : orderItems) {
            try {
                DateTime startAtNow = DateTime.now();

                DateTime requestRefundTime = new DateTime(oi.getRequestRefundAt()).withTimeAtStartOfDay();
                //退款申请超时，自动同意退款
                if (Days.daysBetween(requestRefundTime, startAtNow).getDays() >= ExpireTimes.NOT_CONFIRM_EXPIRE_DAY) {

                    String batchNo = RefundRequest.toBatchNo(DateTime.now().toDate(), oi.getId());
                    AlipayRefundData refund = new AlipayRefundData(oi.getPaymentCode(),
                            oi.getRefundAmount(), oi.getReason());

                    CallBack notify = new CallBack(notifyUrl);
                    Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
                            .detail(Lists.newArrayList(refund)).notify(notify).refund();
                    checkState(refundByAlipay.isSuccess(), refundByAlipay.getError());
                }

                handled ++;
            } catch (Exception e) {
                log.error("fail to expire orderItem id={}, cause:{}, skip it",
                        oi.getId(), Throwables.getStackTraceAsString(e));
            }
        }
        return handled;
    }


    /**
     * run every 15 minuter
     * 判断预售订单付款是否超时，付款时还会最终判断一发
     */
    @Scheduled(cron = "0 0/15 * * * *")
    public void verifyPreSaleOrderExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[PRESALE_ORDER_EXPIRE_VERIFICATION] verify presale order expire job begin");
        preSaleService.verifyPreSaleOrderExpire();
        log.info("[PRESALE_ORDER_EXPIRE_VERIFICATION] verify presale order expire job end");

    }

    /**
     * run every 30s
     * 判断预售是否已结束,在下单的时候还会最终判断一发
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void verifyPreSaleExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }


        log.info("[PRESALE_EXPIRE_VERIFICATION] verify presale expire job begin");
        preSaleService.verifyPreSaleExpire();
        log.info("[PRESALE_EXPIRE_VERIFICATION] verify presale expire job end");

    }


    /**
     * run every 1 minuter
     * 判断抢购活动状态
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void verifyBuyingActivityExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[BUYING_EXPIRE_VERIFICATION] verify buying expire job begin");
        buyingActivityDefinitionService.batchUpdateStatus();
        log.info("[BUYING_EXPIRE_VERIFICATION] verify buying expire job end");

    }

    /**
     * run every 1 minuter
     * 判断虚拟订单状态 如果当前时间大于订单下单结束时间 将该订单改为已过期
     */
    @Scheduled(cron = "0 0/1 * * * *")
    public void verifyBuyingTempOrderExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[BUYING_EXPIRE_OUT_DATE] verify buying expire out date job begin");
        buyingTempOrderService.batchUpdateStatus();
        log.info("[BUYING_EXPIRE_OUT_DATE] verify buying expire out date job end");

    }

    /**
     * 押金失联订单处理（大于45天的已付押金并且已发货的订单）
     */
    @Scheduled(cron = "0 0 1-5 * * *")
    public void orderOnTrialTimeOut() {

        log.info("start orderOnTrialTimeOut at " + org.elasticsearch.common.joda.time.DateTime.now().toLocalTime());

        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        //取得试用中（已支付且已发货）的最大订单号
        Response<Long> maxIdR = preAuthorizationDepositOrderService.maxOnTrialId();
        if (!maxIdR.isSuccess()) {
            log.error("fail to find max id of on trialId order, error code:{}", maxIdR.getError());
            return;
        }

        //没有试用中订单
        if (maxIdR.getResult() == null) {
            log.info("not find on trialId order.");
            return;
        }

        Long lastId = maxIdR.getResult();
        int returnSize = PAGE_SIZE;

        //取得失联订单集合
        List<Long> timeOutList = new ArrayList<Long>();
        while (returnSize == PAGE_SIZE) {
            returnSize=0;
            //取得正在试用中的订单号(已付押金且已发货)
            Response<List<Long>> ordersR = preAuthorizationDepositOrderService.findOnTrialOrderId(lastId, PAGE_SIZE);
            if (!ordersR.isSuccess()) {
                log.error("fail to find id of order, error code:{}", ordersR.getError());
                break;
            }
            List<Long> ids = ordersR.getResult();
            if (ids == null || ids.size() == 0) {
                //没有使用中的订单
                log.info("not find on trialId order.");
                break;
            }

            //取得使用中的订单是否失联（>=45天）
            Response<List<Long>> timeOutOrders = orderQueryService.findOnTrialTimeOutOrder(ids);
            if (!timeOutOrders.isSuccess()) {
                log.error("fail to find id of order, error code:{}", timeOutOrders.getError());
                break;
            }
            List<Long> orderList = timeOutOrders.getResult();
            if (orderList == null || orderList.size() == 0) {
                //没有失联订单
                break;
            }

            timeOutList.addAll(orderList);
            returnSize = ids.size();
            lastId = ids.get(ids.size() - 1) - 1;
        }

        if(timeOutList==null || timeOutList.size()==0)
        {
            //没有失联订单
            log.info("not find time out order.");
            return;
        }
        //扣款操作
        callAlipay(timeOutList);
    }

    /**
     * 失联订单扣款操作
     */
    private void callAlipay(List<Long> orderList) {

        //支付宝回调方式
        CallBack notify = new CallBack(alipayOnTrialTimeOutSuffix);
        for (Long orderId : orderList) {
            //取得支付信息
            Response<PreAuthorizationDepositOrder> preResponse = preAuthorizationDepositOrderService.findOneByOrderId(orderId);
            if (!preResponse.isSuccess()) {
                log.error("fail to find order info, error code:{}", preResponse.getError());
                continue;
            }
            PreAuthorizationDepositOrder preDto = preResponse.getResult();
            if (preDto == null) {
                log.info("not find pre_authorization_deposit_order info.");
                continue;
            }

            //押金订单直接更新状态，不调用支付宝扣钱
            if (preDto.getType() == 2) {
                log.info("pre_authorization_deposit_order get type is 2");
                updateType(orderId);
                continue;
            }

            //预授权订单需要调用支付宝扣钱
            //取得扣款金额
            log.info("pre_authorization_deposit_order get type is 1");
            OrderItem itemDto = orderQueryService.findByMap(orderId, 2);
            if (itemDto == null) {
                log.info("not find order item.");
                continue;
            }

            String strName = itemDto.getItemName();
            if(strName.length()>128)
            {
                strName= strName.substring(0,127);
            }

            log.info("start call CreateAndPay. notify:{},outTradeNo:{},totalFee:{}," +
                    "productCode:{},subject:{},authNo:{},buyerId:{}",
                    notify,orderId.toString(),itemDto.getFee(),productCode,strName,
                    preDto.getTradeNo(),preDto.getPayerUserId());

            //调用支付宝
            CreateAndPay payRequest = CreateAndPay.build(token)
                    .notify(notify)                    //回调方法
                    .outTradeNo(orderId.toString())    //订单号
                    .totalFee(itemDto.getFee())        //扣款金额
                    .productCode(productCode)          //预授权产品:FUND_TRADE_FAST_PAY
                    .subject(strName)                  //商品名称
                    .authNo(preDto.getTradeNo())       //付宝支交易流水号
                    .buyerId(preDto.getPayerUserId()); //付款方支付宝用户号
            Response<Boolean> response = payRequest.payAuto();

            if (response.isSuccess()) {
                log.info("call CreateAndPay success. info code:{}", response.toString());
            }

            if (!response.isSuccess()) {
                log.error("fail to call CreateAndPay. error code:{}", response.getError());
            }
        }
    }

    /**
     * 更新失联押金订单状态
     */
    private void updateType(Long orderId) {
        //押金订单表：失联（-4）
        PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
        preAuthorizationDepositOrder.setOrderId(orderId);
        preAuthorizationDepositOrder.setStatus(-4);
        preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);

        //订单表：交易成功（3）
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(Order.Status.DONE.value());
        orderWriteService.updateOrder(order);

        //订单商品表状态表(定金订单 )：交易成功（3）
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setStatus(OrderItem.Status.DONE.value());
        orderItem.setType(OrderItem.Type.PRESELL_DEPOSIT.value());
        orderWriteService.updateOrderItemType(orderItem);

        //订单商品表状态表(尾款订单 )：等待买家付款（0）
        orderItem.setStatus(OrderItem.Status.WAIT_FOR_PAY.value());
        orderItem.setType(OrderItem.Type.PRESELL_REST.value());
        orderWriteService.updateOrderItemType(orderItem);
    }

    /**
     * run every 30s
     * 判断押金商品是否已结束,在下单的时候还会最终判断一发
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void verifyPreDepositExpire() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }


        log.info("[PRESALE_EXPIRE_VERIFICATION] verify predeposit expire job begin");
        preDepositService.verifyPreDepositExpire();
        log.info("[PRESALE_EXPIRE_VERIFICATION] verify predeposit expire job end");

    }

}
