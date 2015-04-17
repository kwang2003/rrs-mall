package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付宝提现
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 1:40 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class AlipayCash implements Serializable {
    private static final long serialVersionUID = 3792272980467340942L;

    @Getter
    @Setter
    private Long id;                // 主键

    @Getter
    @Setter
    private Integer cashTotalCount; // 交易笔数

    @Getter
    @Setter
    private Long totalFee = 0L;     // 总收入金额日汇总

    @Getter
    @Setter
    private Long alipayFee = 0L;    // 支付宝手续费日汇总

    @Getter
    @Setter
    private Long cashFee = 0L;      // 可提现金额：可提现金额=总金额-支付宝手续费'

    @Getter
    @Setter
    private Long refundFee = 0L;    // 退款金额

    @Getter
    @Setter
    private Integer status;         // 状态 0:未提现 1:已提现

    @Getter
    @Setter
    private Date summedAt;          // 统计时间 (主要筛选条件)

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 修改时间


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

}
