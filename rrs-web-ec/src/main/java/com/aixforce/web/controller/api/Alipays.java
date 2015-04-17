package com.aixforce.web.controller.api;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.agreements.service.PreAuthorizationDepositOrderService;
import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.alipay.mpiPay.MPIPayCallBack;
import com.aixforce.alipay.request.*;
import com.aixforce.alipay.wxPay.WXPay;
import com.aixforce.alipay.wxPay.WXPayRefund;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.web.controller.api.userEvent.PaySuccessEvent;
import com.aixforce.web.controller.api.userEvent.SmsEvent;
import com.aixforce.web.controller.api.userEvent.SmsEventBus;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

import static com.aixforce.rrs.settle.util.SettlementVerification.needSettlementAfterPaid;
import static com.google.common.base.Preconditions.checkArgument;
import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-24 2:43 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/alipay")
public class Alipays {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private PreDepositService preDepositService;

    @Value("#{app.alipayReturnSuffix}")
    private String returnUrl;

    @Value("#{app.mpipayReturnSuffixFailed}")
    private String mpipayReturnSuffixFailed;

    @Value("#{app.alipayFreezeNotifySuffix}")
    private String freezeNotifyUrl;

    @Value("#{app.alipayRefundSuffix}")
    private String refundNotifyUrl;

    @Value("#{app.wxKey}")
    private String wxKey;

    @Value("#{app.wxMchId}")
    private String wxMchId;

    @Value("#{app.wxAppSecret}")
    private String wxAppSecret;

    @Value("#{app.wxRefundGateway}")
    private String wxRefundGateway;

    @Value("#{app.wxAppID}")
    private String wxAppId;

    @Value("#{app.wxPfxPath}")
    private String wxPfxPath;

    @Autowired
    SmsEventBus smsEventBus;

    private static final Splitter tradeSplitter = Splitter.on("#");
    private static final Splitter infoSplitter = Splitter.on("^");

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DET_WX = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Autowired
    private Token token;

    @Autowired
    UserEventBus paySuccessEventBus;

    @Autowired
    private PreAuthorizationDepositOrderService preAuthorizationDepositOrderService;

    /**
     * 支付宝的后台通知
     */
    @RequestMapping(value = "/notify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void payNotify(HttpServletRequest request) {
        try {
            checkPayNotifyArguments(request);
            validateRequestSign(request);
            checkTradeStatusIfSucceed(request);
            updatePaymentSucceed(request);

            // 向亿起发推送订单状态
            paySuccessEventBus.post(new PaySuccessEvent(request));

        } catch (IllegalArgumentException e) {
            log.error("Pay notify raise error params:{}, error:{} ", request.getParameterMap(), e.getMessage());

        } catch (IllegalStateException e) {
            log.error("Pay notify raise error params:{}, error:{} ", request.getParameterMap(), e.getMessage());

        } catch (Exception e) {
            log.error("Pay notify raise error params:{}, cause:{}", request.getParameterMap(), Throwables.getStackTraceAsString(e));
        }
    }


    private void checkPayNotifyArguments(HttpServletRequest request) {
        log.debug("pay request param map: {}", request.getParameterMap());

        checkArgument(!Strings.isNullOrEmpty(request.getParameter("trade_status")), "alipay.notify.trade.status.empty");
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("out_trade_no")), "alipay.notify.out.trade.no.empty");
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("trade_no")), "alipay.notify.trade.no.empty");
    }


    private void validateRequestSign(HttpServletRequest request) {
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("sign")), "alipay.notify.sign.empty");
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("sign_type")), "alipay.notify.sign.type.empty");

        String sign = request.getParameter("sign");
        Map<String, String> params = Maps.newTreeMap();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (isValueEmptyOrSignRelatedKey(key, value)) {
                continue;
            }
            params.put(key, value);
        }

        boolean valid = Request.verify(params, sign, token);
        checkState(valid, "alipay.notify.sign.not.valid");
    }

    private boolean isValueEmptyOrSignRelatedKey(String key, String value) {
        return Strings.isNullOrEmpty(value) || StringUtils.equalsIgnoreCase(key, "sign")
                || StringUtils.equalsIgnoreCase(key, "sign_type");
    }

    private void checkTradeStatusIfSucceed(HttpServletRequest request) {
        String tradeStatus = request.getParameter("trade_status");
        checkState(isTradeSucceed(tradeStatus), "alipay.notify.trade.status.incorrect");
    }

    private boolean isTradeSucceed(String tradeStatus) {
        return Objects.equal(tradeStatus, "TRADE_SUCCESS")
                || Objects.equal(tradeStatus,"TRADE_FINISHED");
    }

    private Date getPaidAt(HttpServletRequest request) {
        try {

            String paidTime = request.getParameter("gmt_payment");
            return DFT.parseDateTime(paidTime).toDate();
        } catch (Exception e) {
            log.warn("fail to get paidAt, cause:{}", e.getMessage());
            return DateTime.now().toDate();
        }
    }


    private void updatePaymentSucceed(HttpServletRequest request) {
        String tradeNos = request.getParameter("out_trade_no");
        String paymentCode = request.getParameter("trade_no");

        List<String> identities = Splitter.on(",").splitToList(tradeNos);
        int orderNum = identities.size();
        checkArgument(orderNum > 0, "alipay.notify.trade.no.format.incorrect");


        Iterator<String> it = identities.iterator();
        Long firstOrderId = Long.valueOf(it.next());
        Order order = getOrder(firstOrderId);
        Date paidAt = getPaidAt(request);

        if (isPlainOrder(order) && isSingleOrder(orderNum)) {
            if (isEmpty(order.getPaymentCode())) {
            	//Order order1=orderQueryService.findById(order.getId()).getResult();
            	int type=order.getType();
            	if(type==1){
            		int ststus=order.getStatus();
		            if(ststus==0){
		             	smsEventBus.post(new SmsEvent(order.getId(), order.getId(),"1"));
		            } 
    			}
                // 更新支付平台
                this.setPaymentPlatform(convertToLong(identities), null, null, "1", 0);
                updateOrderAsPaid(order, paymentCode, paidAt);

            }

            // 创建结算信息
            order.setStatus(Order.Status.PAID.value());
            if (needSettlementAfterPaid(order)) {
                // 更新支付平台
                this.setPaymentPlatform(convertToLong(identities), null, null, "1", 0);
                createOrderSettlement(order);
            }
            return;
        }

        if (isPlainOrder(order) && isMultiOrder(orderNum)) {
            List<Long> ids = convertToLong(identities);
            updateMultiOrderAsPaid(ids, paymentCode, paidAt);
            return;
        }

        if (isPreSaleOrder(order) && isPreSaleOrderNum(orderNum)) {   // 预售订单约定为 "订单号,子订单号" 的形式

            order.setStatus(Order.Status.PAID.value());
            Long orderItemId = Long.valueOf(identities.get(1));

            // 标记预售订单支付方式
            List<Long> orderIds = new ArrayList<Long>();
            orderIds.add(order.getId());
            List<Long> orderItemIds = new ArrayList<Long>();
            orderItemIds.add(orderItemId);
            this.setPaymentPlatform(orderIds, orderItemIds, null, "1", 1);

            //  更新押金订单状态
            updateDeposit(order, orderItemId);
            updatePreSaleOrderAsPaid(orderItemId, paymentCode,paidAt,order);

            return;
        }

        throw new IllegalStateException("impossible.exception.raised");
    }


    private void updateMultiOrderAsPaid(List<Long> ids, String paymentCode, Date paidAt) {
        Response<Boolean> batchUpdatePaid = orderWriteService.batchNormalOrderPaid(ids, paymentCode, paidAt);
        checkState(batchUpdatePaid.isSuccess(), batchUpdatePaid.getError());


        for (Long id : ids) {
            try {
                Order order = getOrder(id);
                if (isEmpty(order.getPaymentCode())) {
                    updateOrderAsPaid(order, paymentCode, paidAt);                    
                }

                // 创建结算信息
                order.setStatus(Order.Status.PAID.value());
                if (needSettlementAfterPaid(order)) {
                    createMultiOrderSettlement(order);
                }

            } catch (IllegalStateException e) {
                log.error("fail to handle order:(id={}), error:{}", id, e.getMessage());
            }
        }

    }

    private void updateOrderAsPaid(Order order, String paymentCode, Date paidAt) {
        Response<Boolean> updatePaid = orderWriteService.normalOrderPaid(order.getId(), paymentCode, paidAt);
        checkState(updatePaid.isSuccess(), updatePaid.getError());
    }

    private void updatePreSaleOrderAsPaid(Long orderItemId, String paymentCode,Date paidAt,Order order) {
        // 标记支付定金或尾款成功 如果是支付尾款 则要更新尾款子结算中得paid_at字段
        Response<Boolean> updatePaid = orderWriteService.preSalePay(orderItemId, paymentCode,paidAt);
        checkState(updatePaid.isSuccess(), updatePaid.getError());

        Response<OrderItem> orderItemRes = orderQueryService.findOrderItemById(orderItemId);
        if(orderItemRes.isSuccess()){
            OrderItem orderItem = orderItemRes.getResult();
            //如果是预售定金 则产生总结算记录和定金结算记录和尾款结算
            if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                createOrderSettlementForPresale(order,paidAt);
            }

            ItemSettlement itemSettlement = new ItemSettlement();
            //如果是预售尾款 更新尾款子结算中得paid_at字段 TradeStatus  PaymentCode
            if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())) {
                Response<ItemSettlement> settlmentRes = settlementService.findByOrderIdAndType(order.getId(),ItemSettlement.Type.PRESELL_REST.value());
                if(settlmentRes.isSuccess()){
                  //如果存在 判断子订单号和当前订单号是否一致 如果不一致同时也把子订单号更新过来
                  if(!Objects.equal(orderItemId,settlmentRes.getResult().getOrderItemId())){
                      itemSettlement.setOrderItemId(orderItemId);
                  }
                    itemSettlement.setId(settlmentRes.getResult().getId());
                    itemSettlement.setTradeStatus(order.getStatus());
                    itemSettlement.setPaymentCode(paymentCode);
                    itemSettlement.setPaidAt(paidAt);
                   Response<Boolean> updateRes = settlementService.updateItemSettlement(itemSettlement);
                    if(!updateRes.getResult()){
                        log.error("fail to update item settlement error:{}",updateRes.getError());
                    }

              }else {
                  log.error("fail to get item settlement error:{}",settlmentRes.getError());
              }
                //更新总结算的订单状态 paid_at
                Response<Settlement> settlementResponse =settlementService.findByOrderId(order.getId());
                if(settlementResponse.isSuccess()){
                    Settlement settlement = new Settlement();
                    settlement.setId(settlementResponse.getResult().getId());
                    settlement.setTradeStatus(order.getStatus());
                    settlement.setPaidAt(paidAt);
                    Response<Boolean> updateRes = settlementService.updateSettlement(settlement);
                    if(!updateRes.isSuccess()){
                        log.error("update settlement fail error:{}",updateRes.getError());
                    }
                }else {
                    log.error("query settlement by order(id={}) fail error:{}",order.getId(),settlementResponse.getError());
                }
            }

        }
    }

    private List<Long> convertToLong(List<String> identities) {
        List<Long> ids = Lists.newArrayListWithCapacity(identities.size());
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }

    private boolean isPlainOrder(Order order) {
        return Objects.equal(order.getType(), Order.Type.PLAIN.value());
    }

    private boolean isPreSaleOrder(Order order) {
        return Objects.equal(order.getType(), Order.Type.PRE_SELL.value());
    }

    private boolean isSingleOrder(int orderNum) {
        return orderNum == 1;
    }

    private boolean isMultiOrder(int orderNum) {
        return !isSingleOrder(orderNum);
    }

    private boolean isPreSaleOrderNum(int orderNum) {
        return orderNum == 2;
    }


    private Order getOrder(Long orderId) {
        Response<Order> getOrder = orderQueryService.findById(orderId);
        checkState(getOrder.isSuccess(), getOrder.getError());
        return getOrder.getResult();
    }


    @RequestMapping(value = "/refund/notify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void refundNotify(HttpServletRequest request) {
        try {
            log.debug("refund request param map:{}", request.getParameterMap());
            checkRefundNotifyArguments(request);
            validateRequestSign(request);
            updateRefundSucceed(request);

        } catch (IllegalArgumentException e) {
            log.error("Refund notify raise error params:{}, error:{}", request.getParameterMap(),  e.getMessage());

        } catch (IllegalStateException e) {
            log.error("Refund notify raise error params:{}, error:{}", request.getParameterMap(),  e.getMessage());

        } catch (Exception e) {
            log.error("Refund notify raise error params:{}, cause:{}", request.getParameterMap(), Throwables.getStackTraceAsString(e));
        }

    }


    private void checkRefundNotifyArguments(HttpServletRequest request) {
        log.debug("pay request param map: {}", request.getParameterMap());
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("batch_no")), "alipay.refund.batch.no.empty");
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("result_details")), "alipay.refund.result.detail.empty");
    }

    private void updateRefundSucceed(HttpServletRequest request) {
        String refundDetail = request.getParameter("result_details");
        List<String> details = tradeSplitter.splitToList(refundDetail);
        checkState(details.size() == 1 || details.size() == 2, "alipay.refund.detail.num.incorrect");
        String detail = details.get(0);

        // 详细信息中包含 原付款支付宝交易号^退款总金额^退款状态
        //List<String> fields =infoSplitter.splitToList(detail);
        //checkState(fields.size() >= 3, "alipay.refund.detail.field.num.incorrect");
        //String result = fields.get(fields.size()-1);        // 获取处理结果
        //log.debug("====result = {}.", result);
        // 只要 detail 包含 SUCCESS 就判定成功
        log.debug("detail = {}", detail);
        checkState(StringUtils.containsIgnoreCase(detail, "SUCCESS"), "alipay.refund.fail");

        String batchNo = request.getParameter("batch_no");
        Long orderItemId = RefundRequest.fromBatchNo(batchNo);

        // 结算
        OrderItem orderItem = getOrderItemById(orderItemId);

        // 订单已退款或退货成功, 则不进行后续步骤
        if(Objects.equal(orderItem.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                || Objects.equal(orderItem.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())) {

            return;
        }

        //试金押金退款回调
        if(checkPreDeposit(orderItem.getOrderId())){
            //判断购买回调库存
            Boolean bool = orderWriteService.checkPreDepositPayOrBack(orderItem.getOrderId());
            Response<Boolean> response = new Response<Boolean>();
            if(bool==null){
                log.error("fail to preOrder  statue{}, error code={}", bool, response.getError());
            }else{
                if(bool){
                    //对于分仓的预售还要恢复库存
                    response  = preDepositService.recoverPreDepositStorageIfNecessary(orderItem.getOrderId());
                    if(!response.isSuccess()){
                        log.error("fail to update Storage data{}, error code={}", orderItem.getOrderId(), response.getError());
                    }
                }

                response = orderWriteService.updateOrderCallBack(orderItem.getOrderId());
                if (!response.isSuccess()) {
                    log.error("fail to update order data{}, error code={}", orderItem.getOrderId(), response.getError());
                }
            }

        }else{

            //对于分仓的预售还要恢复库存
            Response<Boolean> storageR = preSaleService.recoverPreSaleStorageIfNecessary(orderItem.getOrderId());
            if(!storageR.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", orderItem.getOrderId(), storageR.getError());
            }

            // 更新订单退款状态
            updateRefundOrderItemAsCanceled(orderItemId);

       /* Order order = getOrderByOrderItem(orderItem);
        if (needSettlementAfterRefund(order)) {
            createOrderSettlement(order);
        }*/
        }
    }

    private void updateRefundOrderItemAsCanceled(Long orderItemId) {
        Response<Boolean> cancel = orderWriteService.cancelOrderItem(orderItemId);   // 标记退款或退货成功
        checkState(cancel.isSuccess(), cancel.getError());
    }

    private OrderItem getOrderItemById(Long orderItemId) {
        Response<OrderItem> getOrderItem = orderQueryService.findOrderItemById(orderItemId);
        checkState(getOrderItem.isSuccess(), getOrderItem.getError());
        return getOrderItem.getResult();
    }

    private void createOrderSettlementForPresale(Order order,Date paidAt) {
        Response<Long> created = settlementService.generateForPresale(order.getId(), paidAt);
        checkState(created.isSuccess(), created.getError());
    }

    private void createOrderSettlement(Order order) {
        Response<Long> created = settlementService.generate(order.getId());
        checkState(created.isSuccess(), created.getError());
    }


    private void createMultiOrderSettlement(Order order) {
        Response<Long> created = settlementService.generateMulti(order.getId());
        checkState(created.isSuccess(), created.getError());
    }

    @RequestMapping(value = "/refund/record", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void refundRecord(HttpServletRequest request) {
        try {
            log.debug("Finance refund request param map:{}", request.getParameterMap());
            checkRefundNotifyArguments(request);
            validateRequestSign(request);
            log.info("Finance direct refund:{}", request.getParameterMap());

        } catch (IllegalArgumentException e) {
            log.error("Finance direct refund raise error params:{}, error:{}", request.getParameterMap(),  e.getMessage());

        } catch (IllegalStateException e) {
            log.error("Finance direct refund error params:{}, error:{}", request.getParameterMap(),  e.getMessage());

        } catch (Exception e) {
            log.error("Finance direct refund error params:{}, cause:{}", request.getParameterMap(), Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 电子钱包后台通知
     */
    @RequestMapping(value = "/mpiNotify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void mpiNotify (HttpServletRequest request, HttpServletResponse response) {
        try {
            MPIPayCallBack mpiPayCallBack = new MPIPayCallBack();
            String mpiRes = request.getParameter("MPIRes");
            String payResult = mpiPayCallBack.checkPaySuccess(mpiRes);
            log.info("payResult = " + payResult);
            if (!"failed".equals(payResult)) {
                // 支付成功
                if (!"".equals(payResult)) {
                    Date date = new Date();
                    Order order = getOrder(Long.parseLong(payResult));
                    updateOrderAsPaid(order, "", date);
                    // 浏览器重定向
                    response.sendRedirect(returnUrl);
                }
            } else {
                // 支付失败
                response.sendRedirect(mpipayReturnSuffixFailed);
            }
        } catch (Exception e) {
            log.error("failed to pay cause : {}", Throwables.getStackTraceAsString(e));
            try {
                response.sendRedirect(mpipayReturnSuffixFailed);
            } catch (IOException e1) {
                // ignore
            }
        }
    }

    /**
     * 更新押金表收款
     * */
    private void updateDeposit(Order order, Long orderItemId) {

        try {

            // 非押金订单返回
            Response<PreAuthorizationDepositOrder> authDepositR = preAuthorizationDepositOrderService.findPreDepositByOrderId(order.getId());
            if (!authDepositR.isSuccess()) {
                log.error("fail to find preAuthorizationDepositOrder orderId{}, error code={}", order.getId(), authDepositR.getError());
                return;
            }
            if (authDepositR.getResult() == null) {
                return;
            }

            // 判断子订单是尾款子订单还是定金子订单
            Response<OrderItem> orderItemRes = orderQueryService.findOrderItemById(orderItemId);
            Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(order.getId());
            if (!orderItemsR.isSuccess()) {
                log.error("fail to findOrderItemByOrderId preAuthorizationDepositOrder orderId{}, error code={}", order.getId(), orderItemsR.getError());
                return;
            }

            if(orderItemRes.isSuccess()){

                OrderItem orderItem = orderItemRes.getResult();
                if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {

                    // 如果不是未支付状态的订单不会再次支付
                    if (!Objects.equal(PreAuthorizationDepositOrder.DepositPayType.NOPAY.value(), authDepositR.getResult().getStatus())) {
                        log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", "已付押金的订单不让付押金");
                        return;
                    }

                    // 首款子订单
                    PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();

                    preAuthorizationDepositOrder.setOrderId(order.getId());
                    preAuthorizationDepositOrder.setStatus(PreAuthorizationDepositOrder.DepositPayType.PAYED.value());

                    Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
                    if (!response.isSuccess()) {
                        log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
                        throw new JsonResponseException(500, response.getError());
                    }
                } else if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())) {

                    log.info("update preAuthor deposit begin");

                    // 如果不是已付押金的订单不让付尾款
                    if (!Objects.equal(PreAuthorizationDepositOrder.DepositPayType.PAYED.value(), authDepositR.getResult().getStatus())) {
                        log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", "未付押金的订单不让付尾款");
                        return;
                    }

                    log.info("update preAuthor deposit begin success");

                    PreAuthorizationDepositOrder preAuthorizationDeposit = new PreAuthorizationDepositOrder();

                    preAuthorizationDeposit.setOrderId(order.getId());
                    preAuthorizationDeposit.setStatus(PreAuthorizationDepositOrder.DepositPayType.PAYFINNSH.value());

                    Response<Boolean> preDepositResponse = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDeposit);
                    if (!preDepositResponse.isSuccess()) {
                        log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDeposit, preDepositResponse.getError());
                        throw new JsonResponseException(500, preDepositResponse.getError());
                    }

                    // 尾款子订单
                    // 退押金
                    PreAuthorizationDepositOrder authDeposit = authDepositR.getResult();

                    OrderItem OrderItemDeposit = null;
                    for (OrderItem orderI : orderItemsR.getResult()) {
                        if (Objects.equal(orderI.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                            OrderItemDeposit = orderI;
                        }
                    }

                    log.info("updateing preAuthor deposit PRESELL");

                    OrderItem setPaymentCode = new OrderItem();
                    setPaymentCode.setId(orderItemId);
                    setPaymentCode.setPaymentCode(order.getPaymentCode());
                    Response<Boolean> pRes = orderWriteService.updateOrderItem(setPaymentCode);
                    if (!pRes.isSuccess()) {
                        log.error("update paymentCode fail cause {}", pRes.getError());
                    }
                    // 判断押金是否是微信支付，如果是微信支付，则通过微信退押金
                    if (OrderItemDeposit != null && Objects.equal(OrderItemDeposit.getPaymentPlatform(), "2")) {
                        Response<Boolean> wxRefundRes = orderWriteService.wxPayRefund(OrderItemDeposit);
                        if (!wxRefundRes.isSuccess()) {
                            log.error(wxRefundRes.getError());
                        }
                        return;
                    }

                    // 向支付宝发出退款请求
                    if (authDeposit.getType().equals(1)) {

                        log.info("refund freeze preAuthor deposit begin");
                        // 预授权

                        String batchNo = authDeposit.getTradeNo();
//                        AlipayRefundData refund = new AlipayRefundData(OrderItemDeposit.getPaymentCode(),
//                                OrderItemDeposit.getFee(), "押金商品自动退押金");

                        CallBack notify = new CallBack(freezeNotifyUrl);
//                        Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
//                                .detail(Lists.newArrayList(refund)).notify(notify).refund();
                        String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
                        String remark = "日日顺净水预授权订单";
                        Response<Boolean> refundByAlipay = UnFreezeRequest.build(token)
                                .authNo(batchNo)
                                .outRequestNo(outRequestNo)
                                .amount(OrderItemDeposit.getFee())
                                .remark(remark)
                                .notify(notify).refund();

                        log.info("refund freeze preAuthor deposit sucess");

                        if (!refundByAlipay.isSuccess()) {
                            log.error("fail to refund to {}, error code:{}", OrderItemDeposit, refundByAlipay.getError());
                            return;
                        }

                    } else if (authDeposit.getType().equals(2)) {
                        log.info("refund depositAmount preAuthor deposit begin");
                        // 押金
                        Long orderItemForDeposit = 0L;
                        for (OrderItem orderI : orderItemsR.getResult()) {
                            if (Objects.equal(orderI.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                                orderItemForDeposit = orderI.getId();
                            }
                        }
                        Date refundAt = new Date();
                        String batchNo = RefundRequest.toBatchNo(refundAt, OrderItemDeposit.getId());
                        AlipayRefundData refund = new AlipayRefundData(OrderItemDeposit.getPaymentCode(),
                                OrderItemDeposit.getFee(), "押金商品自动退押金");

                        CallBack notify = new CallBack(refundNotifyUrl);
                        Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
                                .detail(Lists.newArrayList(refund)).notify(notify).refund();

                        log.info("refund depositAmount preAuthor deposit sucess");

                        if (!refundByAlipay.isSuccess()) {
                            log.error("fail to refund to {}, error code:{}", OrderItemDeposit, refundByAlipay.getError());
                            return;
                        }
                    }

                    log.info("update preAuthor deposit begin");

                }
            }

        } catch (IllegalArgumentException e) {
            log.error("Finance direct refund raise error orderId:{}, error:{}", order.getId(),  e.getMessage());

        } catch (IllegalStateException e) {
            log.error("Finance direct refund error orderId:{}, error:{}", order.getId(),  e.getMessage());

        } catch (Exception e) {
            log.error("Finance direct refund error orderId:{}, cause:{}", order.getId(), Throwables.getStackTraceAsString(e));
        }

    }

    //是否试金订单
    public boolean checkPreDeposit(Long orderId){
        return orderWriteService.checkPreDeposit(orderId);
    }

    @RequestMapping(value = "/wxPayNotify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HashMap<String, String> wxPayNotify (HttpServletRequest request) {
        log.info("wx pay request param map : {}.", request.getParameterMap());
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            // check the sign
            if (!this.validateRequestSignWX(request)) {
                log.error("wx pay sign error");
                throw new JsonResponseException(500, "wx pay sign error");
            }
            // check sign success , update order status
            String tradeState = request.getParameter("trade_state").toString();
            // 0 支付成功
            if (!Objects.equal("0", tradeState)) {
                log.info("wx pay trade_state is {} expect 0", tradeState);
                throw new JsonResponseException(500, "wx pay trade_state is "+ tradeState +" expect 0");
            }
            this.updatePaymentSucceedWX(request);
        } catch (Exception e) {
            log.error("wx pay notify raise error params:{}, cause:{}", request.getParameterMap(), Throwables.getStackTraceAsString(e));
            result.put("result", "fail");
            result.put("errMsg", e.getMessage());
            return result;
        }
        result.put("result", "success");
        return result;
    }

    /**
     * 验签(微信支付)
     * @param request
     * @return
     */
    public boolean validateRequestSignWX (HttpServletRequest request) {
        // 带验证的签名
        String sign = request.getParameter("sign");
        // 构造 map ，把 request 中的参数按照字典顺序排序
        Map<String, String> params = Maps.newTreeMap();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (this.isValueEmptyOrSignRelatedKeyWX(key, value)) {
                continue;
            }
            params.put(key, value);
        }
        // 验签
        return WXPay.verify(params, sign, wxKey);
    }

    /**
     * 剔除空值、sign不参与签名
     * @param key
     * @param value
     * @return
     */
    private boolean isValueEmptyOrSignRelatedKeyWX(String key, String value) {
        return Strings.isNullOrEmpty(value) || StringUtils.equalsIgnoreCase(key, "sign");
    }

    /**
     * 保存支付平台
     * @param channel
     * @param paymentPlatform
     */
    private void setPaymentPlatform (List<Long> orderIds, List<Long> orderItemIds, String channel, String paymentPlatform, int isPreSale) {
        List<Order> orderList = new ArrayList<Order>();
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        if (null != orderIds && !orderIds.isEmpty()) {
            for (long id : orderIds) {
                Order order = new Order();
                order.setId(id);
                order.setChannel(channel);
                order.setPaymentPlatform(paymentPlatform);
                orderList.add(order);
            }
        }
        if (null != orderItemIds && !orderItemIds.isEmpty()) {
            for (long id : orderItemIds) {
                OrderItem orderItem = new OrderItem();
                orderItem.setId(id);
                orderItem.setChannel(channel);
                orderItem.setPaymentPlatform(paymentPlatform);
                orderItemList.add(orderItem);
            }
        }
        Response<Boolean> updateResult = orderWriteService.setPaymentPlatform(orderList, orderItemList, isPreSale);
        checkState(updateResult.isSuccess(), updateResult.getError());
    }

    /**
     * 更新订单状态 （微信支付）
     * @param request
     */
    private void updatePaymentSucceedWX(HttpServletRequest request) {
        String paymentCode = request.getParameter("transaction_id");    // 交易号
        String tradeNos = request.getParameter("out_trade_no");         // 商户订单号
        String attach = request.getParameter("attach");                  // json字符串
        String bankType = request.getParameter("bank_type");
        log.info("attach = {}", attach);
        // 解析json
        JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();
        Map<String, String> map = new HashMap<String, String>();
        try {
            map = JSON_MAPPER.fromJson(attach, JSON_MAPPER.createCollectionType(HashMap.class, String.class, String.class));
        } catch (Exception e) {
            throw new IllegalStateException("orders id null");
        }

        if (map == null) {
            throw new JsonResponseException(500, "params error");
        }

        String putong = map.get("putong");
        String other = map.get("other");
        List<String> identities = new ArrayList<String>();
        if (null != putong && !"".equals(putong)) {
            identities = Splitter.on(",").splitToList(putong);
            // 普通订单，标记支付平台和支付方式
            this.setPaymentPlatform(convertToLong(identities), null, bankType, "2", 0);
        } else if (null != other && !"".equals(other)) {
            identities = Splitter.on(",").splitToList(other);
        } else {
            throw new JsonResponseException(500, "order id null");
        }

        int orderNum = identities.size();
        checkArgument(orderNum > 0, "alipay.notify.trade.no.format.incorrect");

        Iterator<String> it = identities.iterator();
        Long firstOrderId = Long.valueOf(it.next());
        Order order = getOrder(firstOrderId);

        if (order.getStatus() != 0) {
            log.info("wx pay trade finish");
            return;
        }

        order.setChannel(bankType);
        order.setPaymentPlatform("2");

        // 日期格式 2009年12月27日9点10分10秒表示为20091227091010
        String paidTime = request.getParameter("time_end");
        Date paidAt = DET_WX.parseDateTime(paidTime).toDate();

        if (isPlainOrder(order) && isSingleOrder(orderNum)) {
            if (isEmpty(order.getPaymentCode())) {
                //Order order1=orderQueryService.findById(order.getId()).getResult();
                int type=order.getType();
                if(type==1){
                    int ststus=order.getStatus();
                    if(ststus==0){
                        smsEventBus.post(new SmsEvent(order.getId(), order.getId(),"1"));
                    }
                }

                updateOrderAsPaid(order, paymentCode, paidAt);

            }

            // 创建结算信息
            order.setStatus(Order.Status.PAID.value());
            if (needSettlementAfterPaid(order)) {
                createOrderSettlement(order);
            }
            return;
        }

        if (isPlainOrder(order) && isMultiOrder(orderNum)) {
            List<Long> ids = convertToLong(identities);
            updateMultiOrderAsPaid(ids, paymentCode, paidAt);
            return;
        }

        if (isPreSaleOrder(order) && isPreSaleOrderNum(orderNum)) {   // 预售订单约定为 "订单号,子订单号" 的形式

            order.setStatus(Order.Status.PAID.value());
            Long orderItemId = Long.valueOf(identities.get(1));

            // 标记预售订单支付方式
            List<Long> orderIds = new ArrayList<Long>();
            orderIds.add(order.getId());
            List<Long> orderItemIds = new ArrayList<Long>();
            orderItemIds.add(orderItemId);
            this.setPaymentPlatform(orderIds, orderItemIds, bankType, "2", 1);

            //  更新押金订单状态
            order.setPaymentCode(paymentCode);
            updateDeposit(order, orderItemId);
            updatePreSaleOrderAsPaid(orderItemId, paymentCode, paidAt, order);

            return;
        }

        throw new IllegalStateException("impossible.exception.raised");
    }

}
