package com.aixforce.rrs.settle.service;

import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-03 10:20 AM  <br>
 * Author: xiao
 */
public interface SettlementJobService {

    /**
     * 同步商家8码
     */
    void updateOuterCode(Date doneAt);

    /**
     * 全量同步商家8码
     */
    void updateOuterCodeFully(Date doneAt);


    /**
     * 创建任务
     * @param doneAt 处理时间
     */
    void createJobs(Date doneAt);

    /**
     * 更新已打印的凭证及发票
     * @param doneAt 处理时间
     */
    void updateVoucher(Date doneAt);

    /**
     * 统计确认结算日汇总
     * @param doneAt  处理时间
     */
    void summary(Date doneAt);


    /**
     * 同步至JDE
     * @param doneAt 处理时间
     */
    void syncToJde(Date doneAt);


    /**
     * 标记完成的订单计算状态
     * @param doneAt 处理时间
     */
    void markedOrderAsFinished(Date doneAt);

    /**
     * 结算各项金额
     * @param doneAt 处理时间
     */
    void settle(Date doneAt);

    /**
     * 统计支付宝日提现
     * @param doneAt  处理时间
     */
    void summaryAlipayCash(Date doneAt);

    /**
     * 更新支付宝手续费
     * @param doneAt  处理时间
     */
    void updateAlipayFee(Date doneAt);


    /**
     * 更新商户费率
     * @param doneAt  处理时间
     */
    void updateRate(Date doneAt);

    /**
     * 自动确认所有 T-7 的帐务记录
     * @param doneAt  处理时间
     */
    void autoConfirmed(Date doneAt);

    /**
     * 自动补T日的帐务记录
     * @param doneAt  处理时间
     */
    void fix(Date doneAt);
}
