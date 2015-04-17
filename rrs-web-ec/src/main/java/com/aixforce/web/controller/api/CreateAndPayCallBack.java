package com.aixforce.web.controller.api;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.agreements.service.PreAuthorizationDepositOrderService;
import com.aixforce.alipay.request.Request;
import com.aixforce.alipay.request.Token;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.web.controller.api.userEvent.PaySuccessEvent;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: dong-jie@neusoft.com <br>
 * Date: 2014-12-18<br>
 * Author: dong-jie
 */
@Slf4j
@Controller
@RequestMapping("/api/alipay")
public class CreateAndPayCallBack {

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PreAuthorizationDepositOrderService preAuthorizationDepositOrderService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private Token token;

    @Autowired
    UserEventBus paySuccessEventBus;

    /**
     * alipay.acquire.createandpay 后台通知
     */
    @RequestMapping(value = "/createAndPayNotify", produces = MediaType.APPLICATION_JSON_VALUE)
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
        log.info("pay request param map: {}", request.getParameterMap());

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
        // 通知动作类型
        // TODO 不确定此字段的内容 payByAccountAction？
        String notifyActionType = request.getParameter("notify_action_type");
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
            log.error("fail to get paidAt, cause:{}", Throwables.getStackTraceAsString(e));
            return DateTime.now().toDate();
        }
    }

    private void updatePaymentSucceed(HttpServletRequest request) {
        String tradeNos = request.getParameter("out_trade_no");     // rrs商城订单号
        String paymentCode = request.getParameter("trade_no");       // 支付宝订单号

        List<String> identities = Splitter.on(",").splitToList(tradeNos);
        int orderNum = identities.size();
        checkArgument(orderNum > 0, "alipay.notify.trade.no.format.incorrect");

        Iterator<String> it = identities.iterator();
        Long firstOrderId = Long.valueOf(it.next());
        // firstOrderId rrs商城订单ID
        // TODO 修改订单状态 / 结算 ......

        log.info("start update type.");

        //押金订单表：失联（-4）
        PreAuthorizationDepositOrder preAuthorizationDepositOrder=new PreAuthorizationDepositOrder();
        preAuthorizationDepositOrder.setOrderId(firstOrderId);
        preAuthorizationDepositOrder.setStatus(-4);
        preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);

        //订单表：交易成功（3）
        Order order = new Order();
        order.setId(firstOrderId);
        order.setStatus(Order.Status.DONE.value());
        orderWriteService.updateOrder(order);

        //订单商品表状态表(定金订单 )：交易成功（3）
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(firstOrderId);
        orderItem.setStatus(OrderItem.Status.DONE.value());
        orderItem.setType(OrderItem.Type.PRESELL_DEPOSIT.value());
        orderWriteService.updateOrderItemType(orderItem);

        //订单商品表状态表(尾款订单 )：等待买家付款（0）
        orderItem.setStatus(OrderItem.Status.WAIT_FOR_PAY.value());
        orderItem.setType(OrderItem.Type.PRESELL_REST.value());
        orderWriteService.updateOrderItemType(orderItem);

        log.info("end update type.");
    }
}
