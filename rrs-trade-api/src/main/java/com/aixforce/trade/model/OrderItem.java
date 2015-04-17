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
public class OrderItem implements Serializable {

    private static final long serialVersionUID = -214645381661688590L;

    @Getter
    @Setter
    private Long id;                    // 主键

    @Getter
    @Setter
    private Long originId;                    // 原始子订单id


    @Getter
    @Setter
    private Long orderId;               // 订单id

    @Getter
    @Setter
    private Long buyerId;               // 买家id

    @Getter
    @Setter
    private Long sellerId;              // 卖家id

    @Getter
    @Setter
    private Integer deliverFee;         // 子订单的运费（在用户未付款前,商家可以更改运费, 也可用于针对组合商品情况）

    @Getter
    @Setter
    private Integer fee;                // 总费用, 包括运费

    @Getter
    @Setter
    private Long skuId;                 // 库存id

    @Getter
    @Setter
    private Long itemId;                // 商品id

    @Getter
    @Setter
    private String itemName;            // 商品名称

    @Getter
    @Setter
    private Long brandId;               // 品牌id

    @Getter
    @Setter
    private Long businessId;            //行业id

    @Getter
    @Setter
    private Integer quantity;           // 数量

    @Getter
    @Setter
    private Integer discount;           // 折扣

    @Getter
    @Setter
    private Integer type;               // 子订单类型 1:普通交易, 2:预售定金, 3:预售尾款

    @Getter
    @Setter
    private Integer status;             // 状态


    @Getter
    @Setter
    private Boolean hasComment;         // 是否已评价

    @Getter
    @Setter
    private String reason;              // 退货款理由

    @Getter
    @Setter
    private Integer refundAmount;       // 退货款金额

    @Getter
    @Setter
    private Integer payType;            // 支付类型

    @Getter
    @Setter
    private String paymentCode;         // 支付宝交易流水

    @Getter
    @Setter
    private String deliveryPromise;     //送达承诺

    @Getter
    @Setter
    private Date paidAt;                // 支付时间

    @Getter
    @Setter
    private Date requestRefundAt;       // 申请退款时间

    @Getter
    @Setter
    private Date refundAt;              // 卖家同意退款时间

    @Getter
    @Setter
    private Date returnGoodsAt;         // 卖家收到退货时间

    @Getter
    @Setter
    private String channel;

    @Getter
    @Setter
    private Boolean isBaskOrder;        //是否已晒单

    @Getter
    @Setter
    private Date createdAt;             // 创建日期

    @Getter
    @Setter
    private Date updatedAt;             // 更新日期

    @Getter
    @Setter
    private String paymentPlatform;      // 支付平台 1：支付宝 2：微信 3：其他

    @Override
    public int hashCode() {
        return Objects.hashCode(buyerId, skuId, orderId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof OrderItem)) {
            return false;
        }
        OrderItem that = (OrderItem) o;
        return Objects.equal(buyerId, that.buyerId) && Objects.equal(skuId, that.skuId) && Objects.equal(orderId, that.getOrderId());
    }


    public static enum Type {
        PLAIN(1, "普通交易"),
        PRESELL_DEPOSIT(2,"预售定金"),
        PRESELL_REST(3,"预售尾款");

        private final int value;

        private final String description;

        private Type(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }

        public String description() {
            return description;
        }


        @Override
        public String toString() {
            return description;
        }
    }

    public static enum Status {
        WAIT_FOR_PAY(0, "等待买家付款"),
        PAYED(1, "买家已付款,等待卖家发货"),
        DELIVERED(2, "卖家已发货"),
        DONE(3, "交易成功"),
        WAIT_FOR_REFUND(4, "等待卖家退款"),
        APPLY_FOR_RETURNGOODS(5, "买家申请退货"),
        AGREE_RETURNGOODS(6, "卖家同意退货申请"),
        RETURNGOODS(7, "买家已退货,等待卖家确认"),
        CANCELED_BY_BUYER(-1, "买家关闭交易"),
        CANCELED_BY_SELLER(-2, "卖家关闭交易"),
        CANCELED_BY_REFUND(-3, "卖家已退款,关闭交易"),
        CANCELED_BY_RETURNGOODS(-4, "卖家确认收到退货,关闭交易"),
        CANCELED_BY_EARNEST_EXPIRE(-5, "付定金超时,关闭交易"),
        CANCELED_BY_REMAIN_EXPIRE(-6, "付尾款超时,关闭交易"),
        CANCELED_PRESALE_DEPOSIT_BY_BUYER(-7, "买家关闭已付定金的预售订单");

        private final int value;

        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static Status from(final int value) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.value, value)) {
                    return status;
                }
            }
            return null;
        }

        public String description() {
            return description;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
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
}

