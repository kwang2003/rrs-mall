package com.aixforce.rrs.settle.enums;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-25 11:38 AM  <br>
 * Author: xiao
 */
public enum JobType {
    CREATE_JOBS(0, "创建任务"),
    UPDATE_VOUCHER(1, "更新财务凭证发票"),
    SUMMARY_SETTLEMENTS(2, "结算汇总报表"),
    SYNC_TO_JDE(3, "同步JDE"),
    UPDATE_ALIPAY_FEES(4, "更新支付宝手续费"),
    SUMMARY_ALIPAY_CASHES(5, "汇总可提现金额"),
    MARK_SETTLEMENT_FINISHED(6, "更新已完成订单结算状态"),
    SETTLEMENT(7, "结算订单"),
    UPDATE_RATE(8, "更新商家费率"),
    UPDATE_OUTER_CODE(9, "更新商家8码"),
    AUTO_CONFIRM(10, "自动确认商户提现"),
    UPDATE_OUTER_CODE_FULL(11, "更新商家8码"),
    FIX_SETTLEMENT(12, "补帐");



    private final int value;

    private final String description;

    private JobType(int value, String description) {
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