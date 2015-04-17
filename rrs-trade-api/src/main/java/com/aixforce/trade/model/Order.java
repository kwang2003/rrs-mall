/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
public class Order implements Serializable {


    public static enum Status2 {

        WAIT_FOR_PAY(0, "等待买家付款"), //在线支付和预售业务都可以用这个状态
        BUYER_CONFIRMED(3,"买家已确认收货"),
        BUYER_CANCELED_BEFORE_PAY(-1, "买家未付款, 买家取消交易"),
        SELLER_CANCELED_BEFORE_PAY(-2, "买家未付款, 卖家取消交易"),
        BUYER_REQUEST_REFUND_WAIT_FOR_AGREE(-3, "买家申请退款, 等待卖家审核"),
        SELLER_REFUND_SUCCESS(-4, "卖家退款成功"),
        SELLER_REFUSED_RETURN_GOODS(-97, "卖家拒绝退货, 等待平台介入"), //在线支付和货到付款共用
        SELLER_REFUSED_REFUND(-98, "卖家拒绝退款, 等待平台介入"),
        RETURN_GOODS_ABNORMAL(-99, "退货有问题, 等待平台介入"),
        DONE(100, "交易完成"),

        /** 在线支付业务 OP stands for Online Pay **/
        OP_PAID_WAIT_FOR_SHIP(1, "买家已付款, 等待卖家发货"), //预售订单付完尾款后, 也进入这个状态
        OP_SHIPPED_WAIT_FOR_CONFIRM(2, "卖家已发货, 等待买家确认收货"),
        OP_BUYER_REQUEST_RETURN_GOODS(-5, "买家申请退货,等待卖家审核"),
        OP_SELLER_WAIT_BUYER_SHIP(-6, "卖家同意退货申请, 等待买家发货"),
        OP_BUYER_WAIT_SELLER_REFUND(-7, "买家已发货, 等待卖家退款"),
        //卖家退款成功可以使用SELLER_REFUND_SUCCESS 这个状态

        /** 货到付款业务 COD stands for Cash On Deliver **/
        COD_WAIT_FOR_SHIP(4, "买家已下单, 等待卖家发货"),
        COD_SHIPPED_WAIT_FOR_CONFIRM(5, "卖家已发货, 等待买家确认"),
        COD_BUYER_CONFIRMED(6, "买家确认收货"),
        COD_BUYER_REQUEST_RETURN_GOODS(-8, "买家申请退货, 等待卖家审核"),
        COD_SELLER_AGREE_CANCEL(-9, "买家退货, 交易关闭"),
        //卖家拒绝退货, 等待平台介入可以使用 SELLER_REFUSED_RETURN_GOODS
        //买家拒绝收货, 可以使用SELLER_CANCELED_BEFORE_PAY 这个状态

        /** 预售业务, 在线支付 PS stands for PreSale **/
        PS_BUYER_NOT_PAY_EARNEST(7, "买家已下单, 等待买家付定金"),
        PS_BUYER_PAID_EARNEST(8, "买家已付定金, 等待预售结束"),
        PS_WAIT_FOR_REMAIN_MONEY(9, "预售结束, 等待买家付尾款"),
        PS_BUYER_NOT_PAY_REMAIN_MONEY(-10, "买家未付尾款, 定金扣除, 交易关闭"),
        //付完尾款后的状态可以用 OP_PAID_WAIT_FOR_SHIP

        /** 预售业务, 货到付款 **/
        PS_COD_BUYER_NOT_PAY_EARNEST(10, "买家已下单, 等待买家付定金"),
        PS_COD_WAIT_FOR_SHIP(11, "买家已付定金, 等待卖家发货"),
        //买家取消交易, 定金扣除, 可以用 PS_BUYER_NOT_PAY_REMAIN_MONEY
        PS_COD_SHIPPED_WAIT_FOR_CONFIRM(12, "卖家已发货, 等待买家确认收货"),
        PS_COD_BUYER_REFUSED_GOODS(-11, "买家拒收, 等待平台介入");
        //从等待买家确认收货之后的状态可以用BUYER_CONFIRM_RECEIVE_GOODS和 BUYER_REFUSED_RECEIVE_GOODS




        private final int value;

        @SuppressWarnings("unused")
        private final String description;

        private Status2(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static com.aixforce.trade.model.Order.Status from(int value) {
            for(com.aixforce.trade.model.Order.Status ta: Order.Status.values()) {
                if(ta.value==value) {
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

    public static enum Status {
        WAIT_FOR_PAY(0, "等待买家付款"),
        PAID(1, "买家已付款,等待卖家发货"),
        DELIVERED(2, "卖家已发货"),
        DONE(3, "交易成功"),
        WAIT_FOR_REFUND(4, "等待卖家退款"),
        APPLY_FOR_RETURN_GOODS(5, "买家申请退货"), // 现在 6 就是等待卖家确认收货
        AGREE_RETURN_GOODS(6, "卖家同意退货申请"),
        RETURN_GOODS(7, "买家已退货,等待卖家确认"),
        CANCELED_BY_BUYER(-1, "买家关闭交易"),
        CANCELED_BY_SELLER(-2, "卖家关闭交易"),
        CANCELED_BY_REFUND(-3, "卖家已退款,关闭交易"),
        CANCELED_BY_RETURN_GOODS(-4, "卖家确认收到退货,关闭交易"),
        CANCELED_BY_EARNEST_EXPIRE(-5, "付定金超时,关闭交易"),
        CANCELED_BY_REMAIN_EXPIRE(-6, "付尾款超时,关闭交易"),
        CANCELED_PRESALE_DEPOSIT_BY_BUYER(-7, "买家关闭已付定金的预售订单");

        private final int value;

        @SuppressWarnings("unused")
        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static Status from(int value) {
            for(Status ta: Status.values()) {
                if(ta.value==value) {
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

    public static enum Type {
        PLAIN(1, "普通"),
        PRE_SELL(2,"预售");

        private final int value;

        private final String description;

        private Type(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

    public static enum PayType {
        ONLINE(1, "在线支付"),
        COD(2,"货到付款"),
        SCORE(3,"积分"),
        STORE_PAY(4,"到店支付");


        private final int value;

        private final String description;

        private PayType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

    private static final long serialVersionUID = -941450597558964476L;

    @Getter
    @Setter
    private Long id;                // 订单id

    @Getter
    @Setter
    private Long buyerId;           // 买家id

    @Getter
    @Setter
    private Long sellerId;          // 卖家id

    @Getter
    @Setter
    private Integer status;         // 订单状态

    @Getter
    @Setter
    private Integer type;           // 订单类型

    @Getter
    @Setter
    private Long business;          // 订单所属行业, 从shop所属行业得到

    @Getter
    @Setter
    private Long tradeInfoId;       // 买家配送id

    @Getter
    @Setter
    private Integer deliverFee;     // 运费

    @Getter
    @Setter
    private Integer paymentType;    // 支付类型

    @Getter
    @Setter
    private String paymentCode;     // 支付宝交易流水

    @Getter
    @Setter
    private Boolean isBuying;       //是否抢购订单

    @Getter
    @Setter
    private Date paidAt;            // 支付时间

    @Getter
    @Setter
    private Date deliveredAt;       // 到货时间

    @Getter
    @Setter
    private Date doneAt;            // 订单完成时间

    @Getter
    @Setter
    private Date canceledAt;        // 订单关闭时间(主动关闭)

    @Getter
    @Setter
    private Date finishedAt;        // 订单结束时间(完成或关闭)

    @Getter
    @Setter
    private String channel;         // 支付渠道

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新时间

    @Getter
    @Setter
    private Integer fee;            // 货款


    @Getter
    @Setter
    private Long originId;                // 原始订单id

    @Getter
    @Setter
    private String paymentPlatform;      // 支付平台 1：支付宝 2：微信 3：其他

    @Override
    public int hashCode() {
        return Objects.hashCode(buyerId, sellerId, status);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Order)) {
            return false;
        }
        Order that = (Order) o;
        return Objects.equal(this.buyerId, that.buyerId) && Objects.equal(this.sellerId, that.sellerId)
                && Objects.equal(this.status, that.status);
    }
}
