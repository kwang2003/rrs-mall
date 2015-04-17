package com.aixforce.rrs.settle.util;

import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;

import static com.aixforce.common.utils.Arguments.notNull;
import static org.elasticsearch.common.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-11 11:58 AM  <br>
 * Author: xiao
 */
@Slf4j
public class SettlementVerification {

    public static boolean isPlain(Order order) {
        return Objects.equal(Order.Type.PLAIN.value(), order.getType());
    }

    public static boolean isPlain(Settlement settlement) {
        return Objects.equal(Settlement.Type.PLAIN.value(), settlement.getType());
    }

    public static boolean isPreSale(Order order) {
        return Objects.equal(Order.Type.PRE_SELL.value(), order.getType());
    }

    public static boolean isPreSale(Settlement settlement) {
        return Objects.equal(Settlement.Type.PRE_SELL.value(), settlement.getType());
    }

    public static boolean isCod(Order order) {
        return Objects.equal(Order.PayType.COD.value(), order.getPaymentType());
    }

    public static boolean isCod(Settlement settlement) {
        return Objects.equal(Settlement.PayType.COD.value(), settlement.getPayType());
    }

    public static boolean isOnline(Order order) {
        return Objects.equal(Order.PayType.ONLINE.value(), order.getPaymentType());
    }

    public static boolean isOnline(Settlement settlement) {
        return Objects.equal(Settlement.PayType.ONLINE.value(), settlement.getPayType());
    }

    public static boolean canceledByBuyer(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }

    public static boolean canceledByBuyer(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }

    public static boolean canceledByExpire(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.CANCELED_BY_REMAIN_EXPIRE.value());
    }

    public static boolean canceledByExpire(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value());
    }

    public static boolean canceledByRefund(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.CANCELED_BY_REFUND.value());
    }

    public static boolean canceledByRefund(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REFUND.value());
    }

    public static boolean canceledByReturnGoods(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.CANCELED_BY_RETURN_GOODS.value());
    }

    public static boolean canceledByReturnGoods(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS.value());
    }

    public static boolean isDeposit(ItemSettlement itemSettlement) {
        return Objects.equal(itemSettlement.getType(), ItemSettlement.Type.PRESELL_DEPOSIT.value());
    }

    public static boolean isDeposit(OrderItem orderItem) {
        return Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value());
    }

    public static boolean isRest(ItemSettlement itemSettlement) {
        return Objects.equal(itemSettlement.getType(), ItemSettlement.Type.PRESELL_REST.value());
    }

    public static boolean isRest(OrderItem orderItem) {
        return Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value());
    }


    public static boolean paid(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.PAID.value());
    }

    public static boolean paid(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.PAID.value());
    }

    public static boolean done(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.DONE.value());
    }

    public static boolean done(Settlement settlement) {
        return Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.DONE.value());
    }


    public static boolean depositExpired(Order order) {
        return Objects.equal(order.getStatus(), Order.Status.CANCELED_BY_EARNEST_EXPIRE.value());
    }



    public static boolean checkOrderStatus(Order order) {
        if (needSettlementAfterPaid(order)) {
            return Boolean.TRUE;
        }

        if (needSettlementAfterRefund(order)) {
            return Boolean.TRUE;
        }

        if (needSettlementAfterCancel(order)) {
            return Boolean.TRUE;
        }

        if (needSettlementAfterSuccess(order)) {
            return Boolean.TRUE;
        }

        log.error("not valid orderHasPaid:{}", order);
        return Boolean.FALSE;
    }




    /**
     * 校验订单是否可以生成结算记录(用于取消订单之后)
     *
     * @param canceled  已经取消的订单
     * @return  是否可以进行结算
     */
    public static boolean needSettlementAfterCancel(Order canceled) {
        checkArguments(canceled);

        // 预售订单买家主动关闭交易不记账
        if (isPreSale(canceled) && isOnline(canceled) && canceledByBuyer(canceled)) { // 预售订单-在线支付-买家关闭订单(支付定金后)
            return Boolean.TRUE;
        }

        if (isPreSale(canceled) && isCod(canceled) && canceledByExpire(canceled)) { // 预售订单-货到付款-尾款超时
            return Boolean.TRUE;
        }

        if (isPreSale(canceled) && isOnline(canceled) && canceledByExpire(canceled)) { // 预售订单-在线付款-尾款超时
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 校验订单是否可以生成结算记录(用于更新支付成功状态之后)
     *
     * @param refunded  已经退款的订单
     * @return  是否可以进行结算
     */
    public static boolean needSettlementAfterRefund(Order refunded) {

        checkArguments(refunded);

        if (isPreSale(refunded) && isOnline(refunded) && canceledByRefund(refunded)) { // 预售订单-在线支付-退款成功
            return Boolean.TRUE;
        }

        if (isPreSale(refunded) && isOnline(refunded) && canceledByReturnGoods(refunded)) { // 预售订单-在线支付-退货成功
            return Boolean.TRUE;
        }

        if (isPreSale(refunded) && isCod(refunded) && canceledByReturnGoods(refunded)) {  // 预售订单-货到付款-退货成功
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 校验订单是否可以生成结算记录(用于更新支付成功状态之后)
     *
     * @param paid  已经支付的订单
     * @return  是否可以进行结算
     */
    public static boolean needSettlementAfterPaid(Order paid) {
        checkArguments(paid);

        if (isPlain(paid) && isOnline(paid) && paid(paid)) {  // 普通订单-在线支付-支付完成
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * 校验订单是否可以生成结算记录(用于买家确认收货)
     *
     * @param succeed  交易成功的订单
     * @return  是否可以进行结算
     */
    public static boolean needSettlementAfterSuccess(Order succeed) {
        checkArguments(succeed);

        if (isPlain(succeed) && isCod(succeed) && done(succeed)) {  // 普通订单-货到付款-交易成功
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 校验订单是否可以生成结算记录(用于买家确认收货) for 预售
     *
     * @param succeed  交易成功的订单
     * @return  是否可以进行结算
     */
    public static boolean needSettlementAfterSuccessForPresale(Order succeed) {
        checkArguments(succeed);

        //remove valid
       /* if (isPreSale(succeed) && isOnline(succeed) && done(succeed)) {  // 预售订单-在线支付-交易成功(尾款)
            return Boolean.TRUE;
        }*/

        if (isPreSale(succeed) && isCod(succeed) && done(succeed)) {  // 预售订单-货到付款-交易成功(尾款)
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private static void checkArguments(Order checking) {
        checkState(notNull(checking.getType()), "order.type.null");
        checkState(notNull(checking.getPaymentType()), "order.payment.type.null");
        checkState(notNull(checking.getStatus()), "order.status.null");
    }

}
