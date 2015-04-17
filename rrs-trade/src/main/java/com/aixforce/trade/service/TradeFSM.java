package com.aixforce.trade.service;

import com.aixforce.trade.model.Order;
import com.googlecode.stateless4j.StateMachine;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-07
 */
public class TradeFSM {
    private final StateMachine<Order.Status, Trigger> stateMachine;

    private static enum Trigger {
        BuyerCancel,
        CodBuyerCancel,
        SellerCancel,
        CodSellerCancel,
        Pay,
        Deliver,
        Confirm
    }

    public TradeFSM(Order.Status state) throws Exception {
        this.stateMachine = new StateMachine<Order.Status, Trigger>(state);

        stateMachine.Configure(Order.Status.WAIT_FOR_PAY)
                .Permit(Trigger.BuyerCancel, Order.Status.CANCELED_BY_BUYER)
                .Permit(Trigger.SellerCancel, Order.Status.CANCELED_BY_SELLER);

        stateMachine.Configure(Order.Status.WAIT_FOR_PAY)
                .Permit(Trigger.Pay, Order.Status.PAID);

        stateMachine.Configure(Order.Status.PAID)
                .Permit(Trigger.Deliver, Order.Status.DELIVERED)
                .Permit(Trigger.CodBuyerCancel, Order.Status.CANCELED_BY_BUYER)
                .Permit(Trigger.CodSellerCancel, Order.Status.CANCELED_BY_SELLER);

        stateMachine.Configure(Order.Status.DELIVERED)
                .Permit(Trigger.Confirm, Order.Status.DONE);
    }

    public Order.Status getCurrentState() {
        return stateMachine.getState();
    }

    /**
     * 买家已付款
     *
     * @throws Exception 异常
     */
    public void buyerPaid() throws Exception {
        stateMachine.Fire(Trigger.Pay);
    }

    /**
     * 卖家已发货
     *
     * @throws Exception 异常
     */
    public void sellerDelivered() throws Exception {
        stateMachine.Fire(Trigger.Deliver);
    }

    public void confirmed() throws Exception {
        stateMachine.Fire(Trigger.Confirm);
    }


    /**
     * 买家关闭交易
     *
     * @throws Exception 异常
     */
    public void buyerCanceled() throws Exception {
        stateMachine.Fire(Trigger.BuyerCancel);
    }

    /**
     * 买家关闭货到付款的订单
     */
    public void codBuyerCanceled() throws Exception {
        stateMachine.Fire(Trigger.CodBuyerCancel);
    }


    /**
     * 卖家关闭交易
     *
     * @throws Exception 异常
     */
    public void sellerCanceled() throws Exception {
        stateMachine.Fire(Trigger.SellerCancel);
    }

    /**
     * 卖家关闭货到付款的订单
     */
    public void codSellerCanceled() throws Exception {
        stateMachine.Fire(Trigger.CodSellerCancel);
    }
}
