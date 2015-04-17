package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-24 3:24 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class OrderAlipayCash implements Serializable {

    private static final long serialVersionUID = 6274315375916464713L;

    @Getter
    @Setter
    private Long id;            // 主键

    @Getter
    @Setter
    private Long orderId;       // 订单id

    @Getter
    @Setter
    private Long orderItemId;   // 子订单id

    @Getter
    @Setter
    private Integer type;       // 订单类型

    @Getter
    @Setter
    private Long buyerId;       // 买家id

    @Getter
    @Setter
    private String buyerName;   // 买家账户

    @Getter
    @Setter
    private Long sellerId;      // 卖家id

    @Getter
    @Setter
    private String sellerName;  // 卖家账户

    @Getter
    @Setter
    private Long shopId;        // 店铺id

    @Getter
    @Setter
    private String shopName;    // 店铺名称

    @Getter
    @Setter
    private Long totalFee;      // 收入

    @Getter
    @Setter
    private Long alipayFee;     // 手续费

    @Getter
    @Setter
    private Long cashFee;       // 提现金额

    @Getter
    @Setter
    private Long refundFee;     // 支出(退款)

    @Getter
    @Setter
    private Integer status;     // 提现状态

    @Getter
    @Setter
    private Boolean fixed;      // 是否补帐

    @Getter
    @Setter
    private String voucher;     // 凭证

    @Getter
    @Setter
    private String operator;    // 提现人

    @Getter
    @Setter
    private Date tradedAt;      // 帐务日期

    @Getter
    @Setter
    private Date cashedAt;      // 提现日期

    @Getter
    @Setter
    private Integer cashType;       // 提现类型 1:普通订单提现-1:普通订单退款提现,2 预售定金提现 -2 预售定金退款 3尾款-3尾款退款提现'

    @Getter
    @Setter
    private Date createdAt;     // 创建日期

    @Getter
    @Setter
    private Date updatedAt;     // 更新日期


    public static enum CashType {
        PLAIN(1, "普通交易"),
        PRESELL_DEPOSIT(2,"预售定金"),
        PRESELL_REST(3,"预售尾款"),
        PLAIN_REFUND(-1, "普通交易退款"),
        PRESELL_DEPOSIT_REFUND(-2,"预售定金退款"),
        PRESELL_REST_REFUND(-3,"预售尾款退款");

        private final int value;

        private final String description;

        private CashType(int value, String description) {
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


    public static enum Status {
        NOT(0, "未提现"),
        DONE(1, "已提现");

        private final int value;

        private final String description;

        private Status(int value, String description) {
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

    public static enum Type {
        PLAIN(1, "普通"),
        PRE_SELL(2, "预售");

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

}
