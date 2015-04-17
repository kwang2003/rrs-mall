package com.aixforce.rrs.settle.model;

import com.google.common.base.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 商家保证金
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@ToString
@EqualsAndHashCode
public class DepositFee implements Serializable, Voucher, Receipt {
    private static final long serialVersionUID = 8300497280483410784L;


    @Getter
    @Setter
    private Long id;                // 自增主键

    @Getter
    @Setter
    private Long sellerId;          // 商家id

    @Getter
    @Setter
    private String sellerName;      // 商家名

    @Getter
    @Setter
    private Long shopId;            // 店铺id

    @Getter
    @Setter
    private String shopName;        // 店铺名称

    @Getter
    @Setter
    private String outerCode;       // 海尔 88 码

    @Getter
    @Setter
    private Long business;          // 行业id

    @Getter
    @Setter
    private Long deposit;           // 存入金额, 以分为单位

    @Getter
    @Setter
    private Integer type;           // 类型: 1、新增保证金 2、扣除保证金 3、技术服务费 4、退保证金

    @Getter
    @Setter
    private Integer paymentType;    // 付款方式

    @Getter
    @Setter
    private String voucher;         // 凭据号(由 技术服务费（对账）回写)

    @Getter
    @Setter
    private String receipt;         // 发票号(由 技术服务费（订单）回写)

    @Getter
    @Setter
    private String description;     // 备注

    @Getter
    @Setter
    private Boolean auto = false;   // 是否自动扣除

    @Getter
    @Setter
    private Integer ordered;        // 技术服务费JDE订单创建同完成 0:未完成, 1:已完成 , 当前仅当 type = 3 时

    @Getter
    @Setter
    private Integer synced;         // 同步JDE完成 0:未完成, 1:已完成

    @Getter
    @Setter
    private Integer vouched;        // 单据更新完成 0:未完成, 1:已完成

    @Getter
    @Setter
    private Integer receipted;      // 是否已打印发票

    @Getter
    @Setter
    private Date orderedAt;         // 技术服务费JDE订单创建同完成时间

    @Getter
    @Setter
    private Date syncedAt;          // 同步完成时间

    @Getter
    @Setter
    private Date vouchedAt;         // 凭证打印时间

    @Getter
    @Setter
    private Date receiptedAt;       // 发票打印时间

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新


    public DepositFee() {}

    public DepositFee(String sellerName, Integer type,  Long deposit, String description) {
        this.sellerName = sellerName;
        this.type = type;
        this.deposit = deposit;
        this.description = description;
    }


    public static enum Type {
        INCREMENT(1, "新增保证金"),
        DEDUCTION(2, "扣除保证金"),
        TECH_SERVICE(3, "技术服务费"),
        REFUND(4, "退保证金");

        private Type(int value, String description) {
            this.value = value;
            this.description = description;
        }

        private final int value;
        private final String description;

        public final Integer value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.description;
        }

        public static final List<Integer> values = Arrays.asList(1, 2, 3, 4);
        public static final List<Integer> deposits = Arrays.asList(1, 2, 4);
        public static final List<Integer> techs = Arrays.asList(3);
    }

    public static enum PaymentType {
        ALIPAY(1, "支付宝"),
        CBC(2, "建行电汇");


        private PaymentType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        private final int value;
        private final String description;

        public final Integer value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.description;
        }

        public static final List<Integer> values = Arrays.asList(1, 2, 3, 4);
    }

    public static enum Synced {
        NOT(0, "未同步"),
        DONE(1,"已同步");


        private final int value;

        private final String description;

        private Synced(int value, String description) {
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

    public static enum Vouched {
        NOT(0, "未更新"),
        DONE(1,"已更新");


        private final int value;

        private final String description;

        private Vouched(int value, String description) {
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


    public static enum Receipted {
        NOT(0, "未更新"),
        DONE(1,"已更新");


        private final int value;

        private final String description;

        private Receipted(int value, String description) {
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


    public static boolean isTechService(DepositFee depositFee) {
        return Objects.equal(depositFee.getType(), Type.TECH_SERVICE.value());
    }

    public static boolean isIncrement(DepositFee depositFee) {
        return Objects.equal(depositFee.getType(), Type.INCREMENT.value());
    }

    public static boolean isDeduction(DepositFee depositFee) {
        return Objects.equal(depositFee.getType(), Type.DEDUCTION.value());
    }

    public static boolean isRefund(DepositFee depositFee) {
        return Objects.equal(depositFee.getType(), Type.REFUND.value());
    }

    public static boolean ofDeposit(DepositFee depositFee) {
        return depositFee.getType() == null || Type.deposits.contains(depositFee.getType());
    }

    public static boolean ofTech(DepositFee depositFee) {
        return depositFee.getType() == null || Type.techs.contains(depositFee.getType());
    }

    public static boolean isNotSynced(DepositFee depositFee) {
        return Objects.equal(depositFee.getSynced(), Synced.NOT.value());
    }
}
