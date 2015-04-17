package com.aixforce.rrs.settle.model;

import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.enums.JobType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-25 10:33 AM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class SettleJob implements Serializable {
    private static final long serialVersionUID = 3792272980467340942L;

    @Getter
    @Setter
    private Long id;            // 主键

    @Getter
    @Setter
    private Long dependencyId;  // 依赖的上游任务

    @Getter
    @Setter
    private Integer type;       // 处理类型: 见 JobType

    @Getter
    @Setter
    private Integer status;     // 状态：见 JobStatus

    @Getter
    @Setter
    private Long cost;          // 处理耗时

    @Getter
    @Setter
    private Date doneAt;        // 处理时间

    @Getter
    @Setter
    private Date tradedAt;       // 交易时间

    @Getter
    @Setter
    private Date createdAt;     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;     // 修改时间


    public SettleJob() {}

    public SettleJob (Integer type, Date doneAt, Date tradedAt) {
        this.type = type;
        this.doneAt = doneAt;
        this.tradedAt = tradedAt;
        this.status = JobStatus.NOT.value();
    }

    public SettleJob (Date doneAt, Integer type) { // 时间和类型可以确认任务的性质
        this.doneAt = doneAt;
        this.tradedAt = new DateTime(doneAt).minusDays(1).toDate();
        this.type = type;
    }

    /**
     * 更新财务凭证
     */
    public static SettleJob updateVoucher(Date doneAt, Date tradedAt) {
          return new SettleJob(JobType.UPDATE_VOUCHER.value(), doneAt, tradedAt);
    }

    /**
     * 支付宝手续费
     */
    public static SettleJob alipayFee(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.UPDATE_ALIPAY_FEES.value(), doneAt, tradedAt);
    }

    /**
     * 汇总可提现金额
     */
    public static SettleJob alipayCash(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.SUMMARY_ALIPAY_CASHES.value(), doneAt, tradedAt);
    }

    /**
     * 更新订单结算状态
     */
    public static SettleJob markSettlementFinished(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.MARK_SETTLEMENT_FINISHED.value(), doneAt, tradedAt);
    }

    /**
     * 结算订单
     */
    public static SettleJob settle(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.SETTLEMENT.value(), doneAt, tradedAt);
    }

    /**
     * 生成商家日汇总报表
     */
    public static SettleJob report(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.SUMMARY_SETTLEMENTS.value(), doneAt, tradedAt);
    }

    /**
     * 同步JDE
     */
    public static SettleJob syncJde(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.SYNC_TO_JDE.value(), doneAt, tradedAt);
    }

    /**
     * 更新商家费率
     */
    public static SettleJob updateRate(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.UPDATE_RATE.value(), doneAt, tradedAt);
    }

    /**
     * 自动确认
     */
    public static SettleJob autoConfirm(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.AUTO_CONFIRM.value(), doneAt, tradedAt);
    }


    /**
     * 补系统帐务
     */
    public static SettleJob fixSettlement(Date doneAt, Date tradedAt) {
        return new SettleJob(JobType.FIX_SETTLEMENT.value(), doneAt, tradedAt);
    }
}
