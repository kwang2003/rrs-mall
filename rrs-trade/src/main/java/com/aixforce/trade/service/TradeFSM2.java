package com.aixforce.trade.service;

import com.aixforce.trade.model.Order;
import com.googlecode.stateless4j.StateMachine;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-05-20
 */
public class TradeFSM2 {
    private final StateMachine<Order.Status2, Trigger> stateMachine;

    public static enum Status2 {

        WAIT_FOR_PAY(0, "等待买家付款"), //在线支付和预售业务都可以用这个状态
        BUYER_CONFIRMED(3, "买家已确认收货"),
        BUYER_CANCELED_BEFORE_PAY(-1, "买家未付款, 买家取消交易"),
        SELLER_CANCELED_BEFORE_PAY(-2, "买家未付款, 卖家取消交易"),
        BUYER_REQUEST_REFUND_WAIT_FOR_AGREE(-3, "买家申请退款, 等待卖家审核"),
        SELLER_REFUND_SUCCESS(-4, "卖家退款成功"),
        SELLER_REFUSED_RETURN_GOODS(-97, "卖家拒绝退货, 等待平台介入"), //在线支付和货到付款共用
        SELLER_REFUSED_REFUND(-98, "卖家拒绝退款, 等待平台介入"),
        RETURN_GOODS_ABNORMAL(-99, "退货有问题, 等待平台介入"),
        DONE(100, "交易完成"),

        /**
         * 在线支付业务 OP stands for Online Pay *
         */
        OP_PAID_WAIT_FOR_SHIP(1, "买家已付款, 等待卖家发货"), //预售订单付完尾款后, 也进入这个状态
        OP_SHIPPED_WAIT_FOR_CONFIRM(2, "卖家已发货, 等待买家确认收货"),
        OP_BUYER_REQUEST_RETURN_GOODS(-5, "买家申请退货,等待卖家审核"),
        OP_SELLER_WAIT_BUYER_SHIP(-6, "卖家同意退货申请, 等待买家发货"),
        OP_BUYER_WAIT_SELLER_REFUND(-7, "买家已发货, 等待卖家退款"),
        //卖家退款成功可以使用SELLER_REFUND_SUCCESS 这个状态

        /**
         * 货到付款业务 COD stands for Cash On Deliver *
         */
        COD_WAIT_FOR_SHIP(4, "买家已下单, 等待卖家发货"),
        COD_SHIPPED_WAIT_FOR_CONFIRM(5, "卖家已发货, 等待买家确认"),
        COD_BUYER_REQUEST_RETURN_GOODS(-8, "买家申请退货, 等待卖家审核"),
        COD_SELLER_AGREE_CANCEL(-9, "买家退货, 交易关闭"),
        //卖家拒绝退货, 等待平台介入可以使用 SELLER_REFUSED_RETURN_GOODS

        /**
         * 预售业务, 在线支付 PS stands for PreSale *
         */
        PS_BUYER_PAID_EARNEST(6, "买家已付定金, 等待预售结束"),
        PS_WAIT_FOR_REMAIN_MONEY(7, "预售结束, 等待买家付尾款"),
        PS_BUYER_NOT_PAY_REMAIN_MONEY(-10, "买家未付尾款, 定金扣除, 交易关闭"),
        //付完尾款后的状态可以用 OP_PAID_WAIT_FOR_SHIP

        /**
         * 预售业务, 货到付款 *
         */
        PS_COD_WAIT_FOR_SHIP(8, "买家已付定金, 等待卖家发货"),
        //买家取消交易, 定金扣除, 可以用 PS_BUYER_NOT_PAY_REMAIN_MONEY
        PS_COD_SHIPPED_WAIT_FOR_CONFIRM(9, "卖家已发货, 等待买家确认收货"),
        PS_COD_BUYER_REFUSED_GOODS(-11, "买家拒收, 等待平台介入");
        //接下来的状态同 COD_SHIPPED_WAIT_FOR_CONFIRM


        private final int value;

        private final String description;

        private Status2(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static Status2 from(int value) {
            for (Status2 ta : Status2.values()) {
                if (ta.value == value) {
                    return ta;
                }
            }

            return null;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }


    private static enum Trigger {
        PAY, //在线支付
        BUYER_CANCEL_BEFORE_PAY, //买家在付款前取消订单
        SELLER_CANCEL_BEFORE_PAY, //卖家在付款前取消订单
        SELLER_SHIPPED, //卖家发货
        BUYER_SHIPPED, //买家退货发货
        BUYER_REQUEST_REFUND, //买家申请退款
        BUYER_CONFIRM_RECEIVE_GOODS, //买家确认收货
        BUYER_REQUEST_RETURN_GOODS, //买家申请退货
        BUYER_CANCELED_REQUEST, //买家撤销申请
        BUYER_REFUSED_RECEIVE_GOODS, //买家拒绝收货
        SELLER_AGREE, //卖家同意
        SELLER_DISAGREE, //卖家不同意
        TIME_ELAPSED, //时间流逝
        PAY_EARNEST, //付定金
        BUYER_GIVE_UP_EARNEST, //买家放弃定金, 取消交易
    }

    public TradeFSM2(Order.Status2 state) throws Exception {
        this.stateMachine = new StateMachine<Order.Status2, Trigger>(state);

        /**** 在线支付状态转移图 begin ***/

        //在买家未付款前,买家可以付款, 买家(手动或者超时自动)和卖家都可以关闭交易
        stateMachine.Configure(Order.Status2.WAIT_FOR_PAY)
                .Permit(Trigger.PAY, Order.Status2.OP_PAID_WAIT_FOR_SHIP)
                .Permit(Trigger.BUYER_CANCEL_BEFORE_PAY, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_CANCEL_BEFORE_PAY, Order.Status2.SELLER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.BUYER_CANCELED_BEFORE_PAY);

        //在买家已付款, 等待卖家发货时,  卖家可以发货 , 买家可以申请退款
        stateMachine.Configure(Order.Status2.OP_PAID_WAIT_FOR_SHIP)
                .Permit(Trigger.SELLER_SHIPPED, Order.Status2.OP_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_REQUEST_REFUND, Order.Status2.BUYER_REQUEST_REFUND_WAIT_FOR_AGREE);

        //在卖家已发货, 等待买家确认收货时,买家可以确认收货(手动或者超时自动), 买家也可以申请退货
        stateMachine.Configure(Order.Status2.OP_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_CONFIRM_RECEIVE_GOODS, Order.Status2.BUYER_CONFIRMED)
                .Permit(Trigger.BUYER_REQUEST_RETURN_GOODS, Order.Status2.OP_BUYER_REQUEST_RETURN_GOODS);

        //在买家申请退款, 等待卖家审核时,卖家可以同意(手动或者超时自动), 卖家也可以拒绝申请, 让平台介入
        stateMachine.Configure(Order.Status2.BUYER_REQUEST_REFUND_WAIT_FOR_AGREE)
                .Permit(Trigger.SELLER_AGREE, Order.Status2.SELLER_REFUND_SUCCESS)
                .Permit(Trigger.SELLER_DISAGREE, Order.Status2.SELLER_REFUSED_REFUND);

        //在买家已确认收货后, 只有等待时间流逝, 才会进入DONE状态, 同时买家也可以申请退货
        stateMachine.Configure(Order.Status2.BUYER_CONFIRMED)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.DONE)
                .Permit(Trigger.BUYER_REQUEST_RETURN_GOODS, Order.Status2.OP_BUYER_REQUEST_RETURN_GOODS);

        //在买家申请退货, 等待卖家审核时, 买家可以撤销申请, 卖家可以同意申请, 也可以拒绝退货(手动或者超时自动), 让平台介入
        stateMachine.Configure(Order.Status2.OP_BUYER_REQUEST_RETURN_GOODS)
                .Permit(Trigger.BUYER_CANCELED_REQUEST, Order.Status2.OP_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.SELLER_AGREE, Order.Status2.OP_SELLER_WAIT_BUYER_SHIP)
                .Permit(Trigger.SELLER_DISAGREE, Order.Status2.SELLER_REFUSED_RETURN_GOODS)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.SELLER_REFUSED_RETURN_GOODS);

        //在卖家同意退货申请, 等待买家发货时, 买家可以撤销, 也可以寄回退货
        stateMachine.Configure(Order.Status2.OP_SELLER_WAIT_BUYER_SHIP)
                .Permit(Trigger.BUYER_CANCELED_REQUEST, Order.Status2.OP_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_SHIPPED, Order.Status2.OP_BUYER_WAIT_SELLER_REFUND);

        //买家已经退货发货了, 等待卖家退款时, 卖家可以确认收货, 如果认为退货有问题, 让平台介入
        stateMachine.Configure(Order.Status2.OP_BUYER_WAIT_SELLER_REFUND)
                .Permit(Trigger.SELLER_AGREE, Order.Status2.SELLER_REFUND_SUCCESS)
                .Permit(Trigger.SELLER_DISAGREE, Order.Status2.RETURN_GOODS_ABNORMAL);

        /**** 在线支付状态转移图 end ***/

        /**** 货到付款状态转移图 begin ***/
        //在买家已下单, 未付款状态时, 买卖双方都可以取消订单, 卖家可以发货
        stateMachine.Configure(Order.Status2.COD_WAIT_FOR_SHIP)
                .Permit(Trigger.BUYER_CANCEL_BEFORE_PAY, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_CANCEL_BEFORE_PAY, Order.Status2.SELLER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_SHIPPED, Order.Status2.COD_SHIPPED_WAIT_FOR_CONFIRM);

        //在卖家已发货, 等待买家确认时, 买家可以确认收货, 也可以拒绝收货, 也可以申请退货, 或者都不处理,当超出规定时间后, 将处于订单交易成功状态
        stateMachine.Configure(Order.Status2.COD_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_CONFIRM_RECEIVE_GOODS, Order.Status2.COD_BUYER_CONFIRMED)
                .Permit(Trigger.BUYER_REQUEST_RETURN_GOODS, Order.Status2.COD_BUYER_REQUEST_RETURN_GOODS)
                .Permit(Trigger.BUYER_REFUSED_RECEIVE_GOODS, Order.Status2.SELLER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.DONE);

        //在买家已经确认收货时, 买家可以申请退货, 也可以当时间流逝时, 处于订单交易成功状态
        stateMachine.Configure(Order.Status2.COD_BUYER_CONFIRMED)
                .Permit(Trigger.BUYER_REQUEST_RETURN_GOODS, Order.Status2.COD_BUYER_REQUEST_RETURN_GOODS)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.DONE);

        //在买家申请退货时,卖家可以同意, 也可以不同意(手动或者超时自动),让平台介入
        stateMachine.Configure(Order.Status2.COD_BUYER_REQUEST_RETURN_GOODS)
                .Permit(Trigger.SELLER_AGREE, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_DISAGREE, Order.Status2.SELLER_REFUSED_RETURN_GOODS)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.SELLER_REFUSED_RETURN_GOODS);
        /**** 货到付款状态转移图 end ***/

        /**** 预售尾款在线支付业务状态图 begin ***/
        //对于预售尾款的在线支付业务, 其初始状态同普通在线支付业务, 只是额外允许付定金
        stateMachine.Configure(Order.Status2.PS_BUYER_NOT_PAY_EARNEST)
                .Permit(Trigger.PAY_EARNEST, Order.Status2.PS_BUYER_PAID_EARNEST)
                .Permit(Trigger.BUYER_CANCEL_BEFORE_PAY, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_CANCEL_BEFORE_PAY, Order.Status2.SELLER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.BUYER_CANCELED_BEFORE_PAY);

        //对于已付定金的预售订单, 只有等待时间流逝, 预售结束, 买家才能付尾款
        stateMachine.Configure(Order.Status2.PS_BUYER_PAID_EARNEST)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.PS_WAIT_FOR_REMAIN_MONEY);

        //对于等待买家付尾款的订单,买家可以付尾款, 如果买家超时未付尾款,则扣除订金,订单关闭
        stateMachine.Configure(Order.Status2.PS_WAIT_FOR_REMAIN_MONEY)
                .Permit(Trigger.PAY, Order.Status2.OP_PAID_WAIT_FOR_SHIP)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.PS_BUYER_NOT_PAY_REMAIN_MONEY);

        //对于买家已付尾款的预售在线支付业务, 其之后的状态同普通订单在线支付的等待卖家发货状态
        /**** 预售尾款业务在线支付状态图 end ***/

        /**** 预售尾款货到付款支付业务状态图 begin ***/
        //对于预售尾款货到付款支付业务, 当买家已下单, 未付定金时, 买(手动或者自动超时)卖双方都可以取消订单, 买家也可以付定金
        stateMachine.Configure(Order.Status2.PS_COD_BUYER_NOT_PAY_EARNEST)
                .Permit(Trigger.PAY_EARNEST, Order.Status2.PS_COD_WAIT_FOR_SHIP)
                .Permit(Trigger.BUYER_CANCEL_BEFORE_PAY, Order.Status2.BUYER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.SELLER_CANCEL_BEFORE_PAY, Order.Status2.SELLER_CANCELED_BEFORE_PAY)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.BUYER_CANCELED_BEFORE_PAY);

        //买家已付定金, 等待卖家发货时, 卖家可以发货, 买家也可以放弃定金, 取消交易
        stateMachine.Configure(Order.Status2.PS_COD_WAIT_FOR_SHIP)
                .Permit(Trigger.SELLER_SHIPPED, Order.Status2.PS_COD_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_GIVE_UP_EARNEST, Order.Status2.PS_BUYER_NOT_PAY_REMAIN_MONEY);

        //卖家已经发货, 等待买家确认时, 买家可以确认, 也可以超时自动交易成功, 买家也可以拒收,等待平台介入
        stateMachine.Configure(Order.Status2.PS_COD_SHIPPED_WAIT_FOR_CONFIRM)
                .Permit(Trigger.BUYER_CONFIRM_RECEIVE_GOODS, Order.Status2.COD_BUYER_CONFIRMED)
                .Permit(Trigger.TIME_ELAPSED, Order.Status2.DONE)
                .Permit(Trigger.BUYER_REFUSED_RECEIVE_GOODS, Order.Status2.PS_COD_BUYER_REFUSED_GOODS);

        /**** 预售尾款货到付款支付业务状态图 end ***/
    }

    public Order.Status2 getCurrentState() {
        return stateMachine.getState();
    }

    /**
     * 买家已付款
     *
     * @throws Exception 异常
     */
    public void buyerPaid() throws Exception {
        stateMachine.Fire(Trigger.PAY);
    }

    /**
     * 买家在付款前取消订单
     *
     * @throws Exception 异常
     */
    public void buyCancelBeforePay() throws Exception {
        stateMachine.Fire(Trigger.BUYER_CANCEL_BEFORE_PAY);
    }

    /**
     * 卖家在买家付款前取消订单
     *
     * @throws Exception 异常
     */
    public void sellerCancelBeforePay() throws Exception {
        stateMachine.Fire(Trigger.SELLER_CANCEL_BEFORE_PAY);
    }

    /**
     * 卖家已发货
     * throws Exception 异常
     */
    public void sellerShipped() throws Exception {
        stateMachine.Fire(Trigger.SELLER_SHIPPED);
    }

    /**
     * 买家退货已发货
     *
     * @throws Exception 异常
     */
    public void buyerShipped() throws Exception {
        stateMachine.Fire(Trigger.BUYER_SHIPPED);
    }

    /**
     * 买家申请退款
     *
     * @throws Exception 异常
     */
    public void buyerRequestRefund() throws Exception {
        stateMachine.Fire(Trigger.BUYER_REQUEST_REFUND);
    }

    /**
     * 卖家确认收货
     * @throws Exception 异常
     */
    public void buyerConfirmReceiveGoods() throws Exception {
        stateMachine.Fire(Trigger.BUYER_CONFIRM_RECEIVE_GOODS);
    }


    /**
     * 买家请求退货
     * @throws Exception 异常
     */
    public void buyerRequestReturnGoods() throws Exception{
        stateMachine.Fire(Trigger.BUYER_REQUEST_RETURN_GOODS);
    }

    /**
     * 买家撤销申请
     * @throws Exception 异常
     */
    public void buyerCancelRequest() throws Exception{
        stateMachine.Fire(Trigger.BUYER_CANCELED_REQUEST);
    }

    /**
     * 买家拒绝收货
     * @throws Exception  异常
     */
    public void buyerRefusedReceiveGoods() throws Exception{
        stateMachine.Fire(Trigger.BUYER_REFUSED_RECEIVE_GOODS);
    }

    /**
     * 卖家同意买家的(退货或者退款)申请
     * @throws Exception  异常
     */
    public void sellerAgree() throws Exception{
        stateMachine.Fire(Trigger.SELLER_AGREE);
    }

    /**
     * 卖家不同意买家的 (退货或者退款)申请
     * @throws Exception 异常
     */
    public void sellerDisagree() throws Exception{
        stateMachine.Fire(Trigger.SELLER_DISAGREE);
    }

    /**
     * 超时自动处理
     * @throws Exception 异常
     */
    public void  timeElaspsed() throws Exception{
        stateMachine.Fire(Trigger.TIME_ELAPSED);
    }

    /**
     * 买家付定金
     * @throws Exception 异常
     */
    public void payEarnest() throws Exception{
        stateMachine.Fire(Trigger.PAY_EARNEST);
    }
}
