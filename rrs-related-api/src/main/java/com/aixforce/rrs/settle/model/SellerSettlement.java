package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 按天商户汇总
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-21 3:48 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class SellerSettlement implements Serializable, Voucher, Receipt {
    private static final long serialVersionUID = 3792272980467340942L;

    @Getter
    @Setter
    private Long id;                        // 自增主键

    @Getter
    @Setter
    private Long sellerId;                  // 商户id

    @Getter
    @Setter
    private String sellerName;              // 商户名称

    @Getter
    @Setter
    private String outerCode;               // 商家88码

    @Getter
    @Setter
    private Long business;                  // 行业代码

    @Getter
    @Setter
    private Integer orderCount;             // 交易笔数

    @Getter
    @Setter
    private Long totalEarning;              // 交易总收入

    @Getter
    @Setter
    private Long totalExpenditure;          // 交易总支出

    @Getter
    @Setter
    private Long sellerEarning;             // 商家收入

    @Getter
    @Setter
    private Long rrsCommission;             // 平台佣金收入

    @Getter
    @Setter
    private Long scoreEarning;              // 积分收入

    @Getter
    @Setter
    private Long presellDeposit;            // 预售定金收入

    @Getter
    @Setter
    private Long presellCommission;         // 营业外收入

    @Getter
    @Setter
    private Long thirdPartyCommission;      // 第三方佣金收入

    @Getter
    @Setter
    private String voucher;                 // 凭证号

    @Getter
    @Setter
    private String thirdPartyReceipt;       // 凭证号(支付宝手续费的凭证)

    @Getter
    @Setter
    private Integer settleStatus;           // 结算状态 0:待结算,1:结算中(各收支，如佣金计算完毕),2:待确认,3:已确认,4:已结算,-1:结算失败

    @Getter
    @Setter
    private Integer confirmed;              // 是否已经确认 0:未确认 1:已确认

    @Getter
    @Setter
    private Integer synced;                 // 是否已经同步JDE 0:未同步 1:已同步

    @Getter
    @Setter
    private Integer vouched;                // 是否已经回写凭证号 0:未更新 1:已更新

    @Getter
    @Setter
    private Integer receipted;              // 是否已打印发票

    @Getter
    @Setter
    private Integer printed;                // 是否打印

    @Getter
    @Setter
    private Date confirmedAt;               // 结算时间(商户确认的时间)

    @Getter
    @Setter
    private Date syncedAt;                  // 同步JDE时间

    @Getter
    @Setter
    private Date vouchedAt;                 // JDE 打印凭证时间

    @Getter
    @Setter
    private Date thirdPartyReceiptAt;       // JDE 打印第三方（支付宝手续费）时间

    @Getter
    @Setter
    private Date printedAt;                 // 打印时间

    @Getter
    @Setter
    private Date createdAt;                 // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                 // 修改时间

    @Override
    public String getReceipt() {
        return thirdPartyReceipt;
    }

    @Override
    public Date getReceiptedAt() {
        return thirdPartyReceiptAt;
    }

    @Override
    public void setReceipt(String receipt) { this.thirdPartyReceipt = receipt; }

    @Override
    public void setReceiptedAt(Date receiptAt) { this.thirdPartyReceiptAt = receiptAt; }


    public static enum Confirmed {
        NOT(0, "未确认"),
        DONE(1,"已确认");


        private final int value;

        private final String description;

        private Confirmed(int value, String description) {
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

    public static enum Printed {
        NOT(0, "未打印"),
        DONE(1,"已打印");


        private final int value;

        private final String description;

        private Printed(int value, String description) {
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

    public static enum SettleStatus {
        NOT(0, "待结算"),
        ING(1,"结算中"),
        CONFIRMING(2, "待确认"),
        CONFIRM(3, "已确认"),
        DONE(4,"已结算"),
        FAIL(-1,"结算失败");


        private final int value;

        private final String description;

        private SettleStatus(int value, String description) {
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
