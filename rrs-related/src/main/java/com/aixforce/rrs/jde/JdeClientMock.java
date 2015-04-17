package com.aixforce.rrs.jde;

import com.aixforce.rrs.settle.dao.DepositFeeDao;
import com.aixforce.rrs.settle.dao.SellerSettlementDao;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.rrs.settle.model.SellerSettlement;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-03 5:11 PM  <br>
 * Author: xiao
 */
@Component
public class JdeClientMock implements JdeClient {
    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private DepositFeeDao depositFeeDao;
    private SellerSettlementDao sellerSettlementDao;
    private static final JdeWriteResponse SUCCESS = new JdeWriteResponse(true);

    @Autowired
    public JdeClientMock (SellerSettlementDao sellerSettlementDao, DepositFeeDao depositFeeDao) {
        this.sellerSettlementDao = sellerSettlementDao;
        this.depositFeeDao = depositFeeDao;
    }

    /**
     * 同步商家收入至海尔的财务系统
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncSellerEarning(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步平台佣金及第三方(支付宝)手续费
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncCommissionAndThird(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步积分收入
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncScoreEarning(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步预售金扣除
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncPresellDeposit(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步保证金退款
     *
     * @param depositFee 保证金信息
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncDepositRefund(DepositFee depositFee) {
        return SUCCESS;
    }

    /**
     * 同步退货款，传负数金额
     *
     * @param sellerSettlement 商家已经确认的日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncPaymentRefund(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步缴纳保证金
     *
     * @param depositFee 保证金信息
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncDepositPay(DepositFee depositFee) {
        return SUCCESS;
    }

    /**
     * 同步技术服务费（订单）
     *
     * @param techFee 技术服务费
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncTechFeeOrder(DepositFee techFee) {
        return SUCCESS;
    }

    /**
     * 同步技术服务费（订单）
     *
     * @param techFee 技术服务费
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncTechFeeSettlement(DepositFee techFee) {
        return SUCCESS;
    }

    /**
     * 同步每日可提现金额
     *
     * @param sellerAlipayCash 商户每日提现
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncSellerAlipayCash(SellerAlipayCash sellerAlipayCash) {
        return SUCCESS;
    }

    /**
     * 同步每日商户订单汇总
     *
     * @param sellerSettlement 商户每日汇总
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncSellerOrderTotal(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }

    /**
     * 同步商户的汇总至JDE
     *
     * @param sellerSettlement 商户汇总信息
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncSellerSettlement(SellerSettlement sellerSettlement) {
        return SUCCESS;
    }


    /**
     * 批量同步 技术服务费（订单、对账）
     *
     * @param techFees 技术服务费变更列表
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse batchSyncedTechFees(List<DepositFee> techFees) {
        return SUCCESS;
    }

    /**
     * 批量同步 基础金提现单（技术服务费， 押金）
     *
     * @param depositFeeCashes 提现单表
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse batchSyncedDepositCash(List<DepositFeeCash> depositFeeCashes) {
        return null;
    }

    @Override
    public JdeWriteResponse syncDepositDeduction(DepositFee deduction) {
        return null;
    }

    /**
     * 批量同步 保证金（缴纳保证金、退保证金）
     *
     * @param depositFees 保证金变更列表
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse batchSyncedDepositFees(List<DepositFee> depositFees) {
        return SUCCESS;
    }

    /**
     * 从JDE拉取已填写好订单号与发票号的JDE
     * @return  分页信息
     * @param serial
     */
    @Override
    public JdeVoteResponse pullVoucher(String serial) {
//        JdeVoteResponse<JdeResult> result = new JdeVoteResponse<JdeResult>();
//        result.setResult(mock(size));
//        return result;
        return null;
    }




    private JdeResult mock(int size) {
//        JdeResult<Jde> jdeResult = new JdeResult<Jde>();
//        Paging<SellerSettlement> settlementPaging = sellerSettlementDao.findCashed(null,
//                Settlement.SettleStatus.DONE.value(), null, null, 0, size); // 获取已结算订单
//        List<Jde> mocks = Lists.newArrayListWithCapacity(size);
//
//        List<SellerSettlement> sellerSettlements = settlementPaging.getData();
//        for (SellerSettlement sellerSettlement : sellerSettlements) {
//            String serial = Jde.generate(sellerSettlement.getId(), Jde.Type.SELLER_EARNING.code());
//            Jde mock = new Jde();
//            mock.setSerial(serial);
//            mock.setVoucher("V000000000000" + sellerSettlement.getId());
//            mock.setVoucherDate(DFT.print(DateTime.now()));
//            mock.setThirdPartyReceipt("R000000000000" + sellerSettlement.getId());
//            mock.setThirdPartyReceiptDate(DFT.print(DateTime.now()));
//            mocks.add(mock);
//        }
//
//
//        DepositFee criteria = new DepositFee();
//        Paging<DepositFee> depositFeePaging = depositFeeDao.findCashed(criteria, 0, 10);
//        List<DepositFee> depositFees = depositFeePaging.getData();
//        for (DepositFee depositFee : depositFees) {
//            String serial;
//            String serial2 = null;
//
//            if (Objects.equal(depositFee.getType(), DepositFee.Type.INCREMENT.value())) {
//                serial = Jde.generate(depositFee.getId(), Jde.Type.DEPOSIT_PAY.code());
//
//            } else if (Objects.equal(depositFee.getType(), DepositFee.Type.REFUND.value())) {
//                serial = Jde.generate(depositFee.getId(), Jde.Type.DEPOSIT_REFUND.code());
//
//            } else if (Objects.equal(depositFee.getType(), DepositFee.Type.TECH_SERVICE.value())) {
//                serial = Jde.generate(depositFee.getId(), Jde.Type.TECH_FEE_SETTLEMENT.code()); // 对账
//                serial2 = Jde.generate(depositFee.getId(), Jde.Type.TECH_FEE_ORDER.code());   // 订单
//
//            } else {
//                continue;
//            }
//
//            if (!Strings.isNullOrEmpty(serial)) {
//                Jde mock = new Jde();
//                mock.setSerial(serial);
//                mock.setVoucher("V000000000000" + depositFee.getId());
//                mock.setVoucherDate(DFT.print(DateTime.now()));
//                mocks.add(mock);
//            }
//
//            if (!Strings.isNullOrEmpty(serial2)) {
//                Jde mock = new Jde();
//                mock.setSerial(serial2);
//                mock.setReceipt("R000000000000" + depositFee.getId());
//                mock.setReceiptDate(DFT.print(DateTime.now()));
//                mocks.add(mock);
//            }
//
//
//        }
//
//        jdeResult.setTotal((long)mocks.size());
//        jdeResult.setData(mocks);
//        return jdeResult;
        return null;
    }

}
