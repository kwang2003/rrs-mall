package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-20 11:49 AM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class DepositFeeCash implements Serializable, Voucher {

    private static final long serialVersionUID = -1229655780652831737L;

    @Getter
    @Setter
    private Long id;                // 主键

    @Getter
    @Setter
    private Long depositId;         // 关联保证金id

    @Getter
    @Setter
    private Long sellerId;          // 商家id

    @Getter
    @Setter
    private String sellerName;      // 商家账户

    @Getter
    @Setter
    private Long shopId;            // 店铺id

    @Getter
    @Setter
    private String shopName;        // 店铺名称

    @Getter
    @Setter
    private String outerCode;       // 商户编码

    @Getter
    @Setter
    private Long business;          // 行业编码

    @Getter
    @Setter
    private Long cashFee;           // 提现金额

    @Getter
    @Setter
    private Integer cashType;       // 提现类型 1:基础金 2:技术服务费

    @Getter
    @Setter
    private Integer status;         // 状态 1:已提现

    @Getter
    @Setter
    private String voucher;         // 凭证

    @Getter
    @Setter
    private Integer vouched;        // 是否已打印凭证

    @Getter
    @Setter
    private Integer synced;         // 同步JDE完成 0:未完成, 1:已完成

    @Getter
    @Setter
    private Date syncedAt;          // 同步至JDE时间

    @Getter
    @Setter
    private Date vouchedAt;         // 凭证打印时间

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 修改时间


    public static enum Status {
        NOT(0, "未提现"),
        DONE(1,"已提现");


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

    public static enum CashType {
        DEPOSIT(1, "保证金"),
        TECH_FEE(2,"技术服务费");

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

}
