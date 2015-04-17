package com.aixforce.rrs.jde;

import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.rrs.settle.model.SellerSettlement;

import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-04 10:00 AM  <br>
 * Author: xiao
 */
public interface JdeClient {
    /**
     * 同步商家收入至海尔的财务系统
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    JdeWriteResponse syncSellerEarning(SellerSettlement sellerSettlement);

    /**
     * 同步平台佣金及第三方(支付宝)手续费
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    JdeWriteResponse syncCommissionAndThird(SellerSettlement sellerSettlement);

    /**
     * 同步积分收入
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    JdeWriteResponse syncScoreEarning(SellerSettlement sellerSettlement);

    /**
     * 同步预售金扣除
     *
     * @param sellerSettlement  商家已经确认的日汇总
     * @return  是否同步成功
     */
    JdeWriteResponse syncPresellDeposit(SellerSettlement sellerSettlement);

    /**
     * 同步保证金退款
     *
     * @param depositFee  保证金信息
     * @return  是否同步成功
     */
    JdeWriteResponse syncDepositRefund(DepositFee depositFee);

    /**
     * 同步退货款，传负数金额
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return  是否同步成功
     */
    JdeWriteResponse syncPaymentRefund(SellerSettlement sellerSettlement);

    /**
     * 同步缴纳保证金
     *
     * @param depositFee  保证金信息
     * @return  是否同步成功
     */
    JdeWriteResponse syncDepositPay(DepositFee depositFee);

    /**
     * 同步技术服务费（订单）
     *
     * @param techFee  技术服务费
     * @return  是否同步成功
     */
    JdeWriteResponse syncTechFeeOrder(DepositFee techFee);

    /**
     * 同步技术服务费（订单）
     *
     * @param techFee  技术服务费
     * @return  是否同步成功
     */
    JdeWriteResponse syncTechFeeSettlement(DepositFee techFee);

    /**
     * 同步每日可提现金额
     *
     * @param sellerAlipayCash  商户每日提现
     * @return 是否同步成功
     */
    JdeWriteResponse syncSellerAlipayCash(SellerAlipayCash sellerAlipayCash);

    /**
     * 同步每日商户订单汇总
     *
     * @param sellerSettlement 商户每日汇总
     * @return 是否同步成功
     */
    JdeWriteResponse syncSellerOrderTotal(SellerSettlement sellerSettlement);


    /**
     * 同步商户的汇总至JDE(包含订单总金额、商户返款、积分、预售金扣除、佣金及手续费、退货款）
     *
     * @param sellerSettlement  商户汇总信息
     * @return  是否同步成功
     */
    JdeWriteResponse syncSellerSettlement(SellerSettlement sellerSettlement);

    /**
     *
     * @param serial  批次号
     * @return  凭证信息
     */
    JdeVoteResponse pullVoucher(String serial);


    /**
     * 批量同步 保证金（缴纳保证金、退保证金）
     *
     * @param depositFees 保证金变更列表
     * @return 是否同步成功
     */
    JdeWriteResponse batchSyncedDepositFees(List<DepositFee> depositFees);

    /**
     * 批量同步 技术服务费（订单、对账）
     *
     * @param techFees 技术服务费变更列表
     * @return 是否同步成功
     */
    JdeWriteResponse batchSyncedTechFees(List<DepositFee> techFees);


    /**
     * 批量同步 基础金提现单（技术服务费， 押金）
     *
     * @param depositFeeCashes 提现单表
     * @return 是否同步成功
     */
    JdeWriteResponse batchSyncedDepositCash(List<DepositFeeCash> depositFeeCashes);


    /**
     * 同步预售金
     *
     * @param deduction  预售金单据
     * @return  是否同步成功
     */
    JdeWriteResponse syncDepositDeduction(DepositFee deduction);
}
