package com.aixforce.trade.service;

import com.aixforce.trade.model.OrderItem;
import com.googlecode.stateless4j.StateMachine;

/**
 * Created by yangzefeng on 14-1-23
 */
public class OrderItemTradeFSM {

    private final StateMachine<OrderItem.Status, Trigger> stateMachine;

    private static enum Trigger {
        Refund,
        ReturnGoods,
        RequestRefund,
        RequestReturnGoods,
        undoRequestRefund,
        undoRequestReturnGoods,
        RefuseRefund,
        RefuseReturnGoods,
        BuyerReturnGoods,
        ConfirmReturnGoods
    }

    public OrderItemTradeFSM(OrderItem.Status state) throws Exception {
        this.stateMachine = new StateMachine<OrderItem.Status, Trigger>(state);

        stateMachine.Configure(OrderItem.Status.PAYED)
                .Permit(Trigger.RequestRefund, OrderItem.Status.WAIT_FOR_REFUND);

        stateMachine.Configure(OrderItem.Status.DELIVERED)
                .Permit(Trigger.RequestReturnGoods, OrderItem.Status.APPLY_FOR_RETURNGOODS);

        stateMachine.Configure(OrderItem.Status.WAIT_FOR_REFUND)
                .Permit(Trigger.undoRequestRefund, OrderItem.Status.PAYED)
                .Permit(Trigger.Refund, OrderItem.Status.CANCELED_BY_REFUND)
                .Permit(Trigger.RefuseRefund, OrderItem.Status.PAYED);

        stateMachine.Configure(OrderItem.Status.APPLY_FOR_RETURNGOODS)
                .Permit(Trigger.undoRequestReturnGoods, OrderItem.Status.DELIVERED)
                .Permit(Trigger.ReturnGoods, OrderItem.Status.AGREE_RETURNGOODS)
                .Permit(Trigger.RefuseReturnGoods, OrderItem.Status.DELIVERED);

        stateMachine.Configure(OrderItem.Status.AGREE_RETURNGOODS)
                .Permit(Trigger.ConfirmReturnGoods, OrderItem.Status.CANCELED_BY_RETURNGOODS);

    }

    public OrderItem.Status getCurrentState() {
        return stateMachine.getState();
    }

    /**
     * 卖家已退款
     *
     * @throws Exception 异常
     */
    public void sellerRefund() throws Exception {
        stateMachine.Fire(Trigger.Refund);
    }

    /**
     * 买家要求退款
     *
     * @throws Exception
     */
    public void requestRefund() throws Exception {
        stateMachine.Fire(Trigger.RequestRefund);
    }

    /**
     * 买家要求退货
     *
     * @throws Exception
     */
    public void requestReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.RequestReturnGoods);
    }

    /**
     * 卖家同意退货
     *
     * @throws Exception
     */
    public void agreeReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.ReturnGoods);
    }

    /**
     * 卖家拒绝退款,订单状态返回为已付款，买家还能继续申请退款
     *
     * @throws Exception
     */
    public void refuseRefund() throws Exception {
        stateMachine.Fire(Trigger.RefuseRefund);
    }

    /**
     * 卖家拒绝退货，订单状态返回为已发货，买家还能继续申请退货
     *
     * @throws Exception
     */
    public void refuseReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.RefuseReturnGoods);
    }

    /**
     * 买家退货，在卖家同意退货之后
     * @throws Exception
     */
    public void buyerReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.BuyerReturnGoods);
    }

    /**
     * 卖家确认收货，在买家退货之后
     * @throws Exception
     */
    public void confirmReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.ConfirmReturnGoods);
    }

    /**
     * 买家撤销退款申请
     * @throws Exception
     */
    public void undoRequestRefund() throws Exception {
        stateMachine.Fire(Trigger.undoRequestRefund);
    }

    /**
     * 买家撤销退货申请
     * @throws Exception
     */
    public void undoRequestReturnGoods() throws Exception {
        stateMachine.Fire(Trigger.undoRequestReturnGoods);
    }
}
