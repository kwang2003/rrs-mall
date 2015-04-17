/**
 * 支付宝
 * 授权交易回调处理类
 */
package com.aixforce.web.controller.api;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.agreements.service.PreAuthorizationDepositOrderService;
import com.aixforce.alipay.request.Request;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.web.controller.api.userEvent.PaySuccessEvent;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by dong-jie@neusoft.com on 14-12-2.
 */
@Controller
@RequestMapping("/api/freezePayCallBack")
@Slf4j
public class freezePayCallBack {

    @Autowired
    private Token token;

    @Autowired
    UserEventBus paySuccessEventBus;

    @Autowired
    PreAuthorizationDepositOrderService preAuthorizationDepositOrderService;

    @Autowired
    OrderQueryService orderQueryService;

    @Autowired
    OrderWriteService orderWriteService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private PreDepositService preDepositService;
    /**
     *  支付宝 资金授权 冻结资金 回调方法
     * @param request
     */
    @RequestMapping(value = "/freezeNotify",  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void freezeCallBack (HttpServletRequest request, HttpServletResponse response) {
        log.info("freezeCallBack access {}" , request.getParameterMap());
        try {
            checkPayNotifyArguments(request);
            validateRequestSign(request);
            checkTradeStatusIfSucceed(request);
            updatePaymentSucceed(request);
            // TODO 不知道要不要
            // 向亿起发推送订单状态
//            paySuccessEventBus.post(new PaySuccessEvent(request));
            response.getWriter().write("success");
            response.getWriter().flush();
            response.getWriter().close();
            log.info("freezeNotify end!");
        } catch (IllegalArgumentException e) {
            log.error("Pay notify raise error params:{}, error:{} ", request.getParameterMap(), e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Pay notify raise error params:{}, error:{} ", request.getParameterMap(), e.getMessage());
        } catch (Exception e) {
            log.error("Pay notify raise error params:{}, cause:{}", request.getParameterMap(), Throwables.getStackTraceAsString(e));
        }
    }

    private void updatePaymentSucceed(HttpServletRequest request) {
        // 支付宝资金授权订单号
        String authNo = request.getParameter("auth_no");                // 此次资金授权订单号（支付宝） 此订单号必须保存下来，退款的时候必要
        String outOrderNo = request.getParameter("out_order_no");      // 此次资金授权订单号（RRS）
        // 付款方账号信息
        String payerLogonId = request.getParameter("payer_logon_id");  // 付款方支付宝账号（不一定有）
        String payerUserId = request.getParameter("payer_user_id");    // 付款方支付宝用户号（不一定有）
        log.info("authNo = " + authNo);
        log.info("outOrderNo = " + outOrderNo);
        log.info("payerLogonId = " + payerLogonId);
        log.info("payerUserId = " + payerUserId);

        // 可能有多个out_order_no，支付宝用 , 分割
        // List<String> identities = Splitter.on(",").splitToList(outOrderNo);
        // Iterator<String> it = identities.iterator();

        // operation_type FREEZE / UNFREEZE
        String operationType = request.getParameter("operation_type");
        log.info("operationType = " + operationType);
        if ("FREEZE".equals(operationType)) {
            // 资金冻结回调
            List<String> identities = Splitter.on(",").splitToList(outOrderNo);
            int orderNum = identities.size();
            checkArgument(orderNum > 0, "alipay.notify.trade.no.format.incorrect");

            Iterator<String> it = identities.iterator();
            Long firstOrderId = Long.valueOf(it.next());
            Order order = getOrder(firstOrderId);

            if (isPreSaleOrder(order) && isPreSaleOrderNum(orderNum)) {   // 预售订单约定为 "订单号,子订单号" 的形式
                Long orderItemId = Long.valueOf(identities.get(1));
                updatePreSaleOrderAsPaid(orderItemId, authNo);
            }

            PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();

            preAuthorizationDepositOrder.setPayerLogonId(payerLogonId);
            preAuthorizationDepositOrder.setPayerUserId(payerUserId);
            preAuthorizationDepositOrder.setOrderId(firstOrderId);
            preAuthorizationDepositOrder.setTradeNo(authNo);
            preAuthorizationDepositOrder.setStatus(PreAuthorizationDepositOrder.DepositPayType.PAYED.value());

            Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
            if (!response.isSuccess()) {
                log.error("fail to create preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
                throw new JsonResponseException(500, messageSources.get(response.getError()));
            }

        } else if ("UNFREEZE".equals(operationType)) {
            // TODO update order status  资金解冻回调
            List<String> identities = Splitter.on(",").splitToList(outOrderNo);
            int orderNum = identities.size();
            checkArgument(orderNum > 0, "alipay.notify.trade.no.format.incorrect");
            Iterator<String> it = identities.iterator();
            Long firstOrderId = Long.valueOf(it.next());

            //判断购买回调库存
            Boolean bool = orderWriteService.checkPreDepositPayOrBack(firstOrderId);
            Response<Boolean> response = new Response<Boolean>();
            if(bool==null){
                log.error("fail to preOrder  statue{}, error code={}", bool, response.getError());
                throw new JsonResponseException(500, messageSources.get(response.getError()));
            }
            if(bool){
                //对于分仓的预售还要恢复库存
                response  = preDepositService.recoverPreDepositStorageIfNecessary(firstOrderId);
                if(!response.isSuccess()){
                    log.error("fail to update Storage data{}, error code={}", firstOrderId, response.getError());
                    throw new JsonResponseException(500, messageSources.get(response.getError()));
                }
            }

            response = orderWriteService.updateOrderCallBack(firstOrderId);
            if (!response.isSuccess()) {
                log.error("fail to update order data{}, error code={}", firstOrderId, response.getError());
                throw new JsonResponseException(500, messageSources.get(response.getError()));
            }
        }
    }

    private void checkPayNotifyArguments(HttpServletRequest request) {
        log.debug("pay request param map: {}", request.getParameterMap());
        // 支付宝资金授权订单号
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("auth_no")), "alipay.notify.trade.status.empty");
        // RRS内部订单号
        checkArgument(!Strings.isNullOrEmpty(request.getParameter("out_order_no")), "alipay.notify.out.trade.no.empty");
    }

    /**
     * 验签方法
     * @param request
     */
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
        // TODO 文档上描述得比较模糊，使用 order_status 还是 status 有待测试确认
        // order_status 支付宝订单状态
        String orderStatus = request.getParameter("order_status");
        // status 操作流水状态
        String status = request.getParameter("status");
        // order_status有4种状态
        // INIT：初始
        // AUTHORIZED：已授权
        // FINISH：完成
        // CLOSE：关闭
        log.info("alipay order_status = " + orderStatus);
        // status有5种状态
        // INIT：初始
        // PROCESSING：处理中
        // SUCCESS：成功
        // FAIL：失败
        // CLOSED：关闭
        log.info("alipay status = " + status);
        // 目前不知道是用 order_status 还是用 status 待测试
        checkState(isTradeSucceed(status, orderStatus), "alipay.notify.trade.status.incorrect");
    }

    private boolean isTradeSucceed(String status, String orderStatus) {
        // 操作流水状态是成功 并且 订单状态是 已授权
        //return Objects.equal(status, "SUCCESS") && Objects.equal(orderStatus, "AUTHORIZED");
        //修改原因预授权解冻回调orderStatus【closed】 判断问题
        log.info("boolean = " + Objects.equal(status, "SUCCESS"));
        return Objects.equal(status, "SUCCESS");
    }

    private Order getOrder(Long orderId) {
        Response<Order> getOrder = orderQueryService.findById(orderId);
        checkState(getOrder.isSuccess(), getOrder.getError());
        return getOrder.getResult();
    }

    private boolean isPreSaleOrder(Order order) {
        return Objects.equal(order.getType(), Order.Type.PRE_SELL.value());
    }

    private boolean isPreSaleOrderNum(int orderNum) {
        return orderNum == 2;
    }

    private void updatePreSaleOrderAsPaid(Long orderItemId, String paymentCode) {
        // 标记支付定金或尾款成功
        Response<Boolean> updatePaid = orderWriteService.preSalePay(orderItemId, paymentCode, new Date());
        checkState(updatePaid.isSuccess(), updatePaid.getError());
    }

}
