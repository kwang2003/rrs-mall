package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 5:00 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class SellerAlipayCash implements Serializable, Voucher {

    private static final long serialVersionUID = 8567471586539956654L;

    @Getter
    @Setter
    private Long id;            // 主键

    @Getter
    @Setter
    @NotNull
    private Long sellerId;      // 商家id

    @Getter
    @Setter
    private String sellerName;  // 商家账户

    @Getter
    @Setter
    private String outerCode;   // 商户8码

    @Getter
    @Setter
    private Long business;      // 行业编码

    @Getter
    @Setter
    private Integer cashTotalCount; // 交易笔数

    @Getter
    @Setter
    @NotNull
    private Long totalFee;      // 总收入金额日汇总

    @Getter
    @Setter
    @NotNull
    private Long alipayFee;     // 支付宝手续费日汇总

    @Getter
    @Setter
    @NotNull
    private Long cashFee;       // 可提现金额：可提现金额=总金额-支付宝手续费-退款金额


    @Getter
    @Setter
    private Long refundFee;     // 退款金额

    @Getter
    @Setter
    private String voucher;     // 凭证

    @Getter
    @Setter
    @NotNull
    private Integer status;     // 状态 0:未提现 1:已提现

    @Getter
    @Setter
    private Integer synced;     // 同步JDE完成 0:未完成, 1:已完成

    @Getter
    @Setter
    private Integer vouched;    // 是否已打印凭证

    @Getter
    @Setter
    @NotNull
    private Date summedAt;      // 统计时间

    @Getter
    @Setter
    private Date syncedAt;      // 同步至JDE时间

    @Getter
    @Setter
    private Date vouchedAt;     // 打印凭证时间

    @Getter
    @Setter
    private Date createdAt;     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;     // 更新时间


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

    public static enum Synced {
        NOT(0, "未同步"),
        DONE(1, "已同步");

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

}
