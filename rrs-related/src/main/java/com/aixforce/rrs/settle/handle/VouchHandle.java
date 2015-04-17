package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.rrs.jde.Jde;
import com.aixforce.rrs.jde.JdeClient;
import com.aixforce.rrs.jde.JdeResult;
import com.aixforce.rrs.jde.JdeVoteResponse;
import com.aixforce.rrs.settle.dao.*;
import com.aixforce.rrs.settle.manager.AlipayCashManager;
import com.aixforce.rrs.settle.model.*;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 10:00 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class VouchHandle extends JobHandle {

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private AlipayCashManager alipayCashManager;

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private SettleJobDao settleJobDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    @Autowired
    private JdeClient client;

    /**
     * 此方法支持循环调用
     *
     * 回写凭证号或发票号
     *
     * @param job  任务信息
     */
    public void updateVouchersAndReceipts(SettleJob job) {
        log.info("[UPDATE-VOUCHERS-AND-RECEIPTS] job begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        if (!dependencyOk(job)) {
            log.info("dependency job is not over, skipped");
            return;
        }

        settleJobDao.ing(job.getId());  // mark job is processing

        try {
            log.info("start seller vouchers");
            try {
                updateSettlementVouchersAnd3rdReceipt();
            } catch (Exception e) {
                log.error("fail to pull seller vouchers with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            }
            log.info("done");

            log.info("start seller cash vouchers");
            try {
                updateCashVouchers();
            } catch (Exception e) {
                log.error("fail to pull seller cash vouchers with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            }
            log.info("done");


            log.info("start to pull deposit vouchers");
            try {
                updateDepositVoucherAndReceipt();
            } catch (Exception e) {
                log.error("fail to pull deposit vouchers with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            }
            log.info("done");


            log.info("start to pull deposit cash vouchers");
            try {
                updateDepositCashVoucher();
            } catch (Exception e) {
                log.error("fail to pull deposit vouchers with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            }
            log.info("done");


        } catch (Exception e) {
            log.error("[UPDATE-VOUCHERS-AND-RECEIPTS] failed, job({}) cause:{} ", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
        log.info("[UPDATE-VOUCHERS-AND-RECEIPTS] done at {} cast {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }


    /**
     * 更新商户汇总中的凭证号和发票号
     */
    private void updateSettlementVouchersAnd3rdReceipt() {
        int pageNo = 1;
        boolean next = batchVouchSellerSettlements(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchVouchSellerSettlements(pageNo, BATCH_SIZE);
        }
    }


    /**
     * 更新商户提现汇总中的凭证号和发票号
     */
    private void updateCashVouchers() {
        int pageNo = 1;
        boolean next = batchVouchSellerCash(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchVouchSellerCash(pageNo, BATCH_SIZE);
        }
    }


    /**
     * 更新保证金凭证号和发票号
     *
     */
    private void updateDepositVoucherAndReceipt() {
        int pageNo = 1;
        boolean next = batchVouchDepositFees(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchVouchDepositFees(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 更新保证金提现凭证号
     *
     */
    private void updateDepositCashVoucher() {
        int pageNo = 1;
        boolean next = batchVouchDepositFeeCash(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchVouchDepositFeeCash(pageNo, BATCH_SIZE);
        }
    }


    /**
     *
     * @param pageNo    批次号
     * @param size      批次数量
     * @return   是否存在下一批次
     */
    private boolean batchVouchDepositFees(int pageNo, int size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<DepositFee> paging = depositFeeDao.findSynced(pageInfo.offset, pageInfo.limit);
        List<DepositFee> depositFees = paging.getData();

        if (CollectionUtils.isEmpty(depositFees)) {
            return false;
        }

        for (DepositFee depositFee : depositFees) {   // 去JDE拉取凭证
            try {
                if (!equalWith(depositFee.getVouched(), DepositFee.Vouched.DONE.value())) {
                    vouchByJde(depositFee);
                    if (notEmpty(depositFee.getVoucher())) {
                        depositFeeDao.vouching(depositFee);
                    }
                }

                if (!equalWith(depositFee.getReceipted(), DepositFee.Receipted.DONE.value())) {
                    receiptByJde(depositFee);
                    if (notEmpty(depositFee.getReceipt())) {
                        depositFeeDao.receipting(depositFee);
                    }
                }

            } catch (Exception e) {
                log.error("fail to pull vouchers for depositFee({}), cause:{}", depositFee, Throwables.getStackTraceAsString(e));
            }
        }

        int current = depositFees.size();
        return current == size;
    }


    private boolean batchVouchSellerCash(int pageNo, Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<SellerAlipayCash> paging = sellerAlipayCashDao.findSynced(pageInfo.offset, pageInfo.limit);
        List<SellerAlipayCash> cashes = paging.getData();

        if (CollectionUtils.isEmpty(cashes)) {
            return false;
        }

        for (SellerAlipayCash cash : cashes) {   // 去JDE拉取凭证
            try {
                if (!equalWith(cash.getVouched(), DepositFee.Vouched.DONE.value())) {
                    vouchByJde(cash);
                    if (notEmpty(cash.getVoucher())) {
                        alipayCashManager.vouching(cash);
                    }
                }

            } catch (Exception e) {
                log.error("fail to pull vouchers for depositFee({}), cause:{}", cash, Throwables.getStackTraceAsString(e));
            }
        }

        int current = cashes.size();
        return current == size;
    }

    /**
     *
     * 批量更新凭证号或第三方手续费
     * @param pageNo    批次号
     * @param size      批次数量
     * @return  是否存在下一批次
     */
    private boolean batchVouchSellerSettlements(int pageNo, int size) {

        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<SellerSettlement> paging = sellerSettlementDao.findSynced(pageInfo.offset, pageInfo.limit);
        List<SellerSettlement> sellerSettlements = paging.getData();

        if (CollectionUtils.isEmpty(sellerSettlements)) {
            return false;
        }

        for (SellerSettlement sellerSettlement : sellerSettlements) {   // 去JDE拉取凭证

            try {
                if (!equalWith(sellerSettlement.getVouched(), SellerSettlement.Vouched.DONE.value())) {
                    vouchByJde(sellerSettlement);
                    if (notEmpty(sellerSettlement.getVoucher())) {
                        sellerSettlementDao.vouching(sellerSettlement);
                    }

                }

                if (!equalWith(sellerSettlement.getReceipted(), SellerSettlement.Receipted.DONE.value())) {
                    receiptByJde(sellerSettlement);
                    if (notEmpty(sellerSettlement.getReceipt())) {
                        sellerSettlementDao.receipting(sellerSettlement);
                    }
                }

            } catch (IllegalStateException e) {
                log.error("fail to pull vouchers for sellerSettlement({}), error:{}", sellerSettlement, e.getMessage());
            }
        }

        int current = sellerSettlements.size();
        return current == size;
    }



    /**
     * 批量更新基础金提现单的凭证号
     * @param pageNo    批次号
     * @param size      批次数量
     * @return   是否存在下一批次
     */
    private boolean batchVouchDepositFeeCash(int pageNo, Integer size) {
        // 查询1个月内的结算记录
        PageInfo pageInfo = new PageInfo(pageNo, size);

        Paging<DepositFeeCash> paging = depositFeeCashDao.findSynced(pageInfo.offset, pageInfo.limit);
        List<DepositFeeCash> depositFeeCashes = paging.getData();
        if (CollectionUtils.isEmpty(depositFeeCashes)) {
            return false;
        }

        for (DepositFeeCash depositFeeCash : depositFeeCashes) {   // 去JDE拉取凭证
            try {

                if (!equalWith(depositFeeCash.getVouched(), SellerSettlement.Vouched.DONE.value())) {
                    vouchByJde(depositFeeCash);
                    if (notEmpty(depositFeeCash.getVoucher())) {
                        depositFeeCashDao.vouching(depositFeeCash);
                    }

                }

            } catch (IllegalStateException e) {
                log.error("fail to vouch to deposit fee cash. depositFeeCash:{}, error:{}", depositFeeCash, e.getMessage());
            }
        }

        int current = depositFeeCashes.size();
        return current == size;
    }


    private void vouchByJde(Voucher voucher) {
        try {
            String vouchCode = getVouchCode(voucher);
            String serial = Jde.generate(voucher.getId(), vouchCode);
            JdeVoteResponse response = client.pullVoucher(serial);

            checkState(notNull(response), "jde.voucher.convert.error");
            checkState(response.isSuccess(), response.getError());
            JdeResult result = response.getResult();

            checkState(result.isSuccess(), result.getError());
            checkState(notEmpty(result.getVoucher()), "jde.voucher.empty");

            voucher.setVoucher(result.getVoucher());
            DateTime vouchedAt = DFT.parseDateTime(result.getVouchedDate());
            voucher.setVouchedAt(vouchedAt.toDate());

        } catch (IllegalStateException e) {
            log.error("jde pull voucher raise bill:{} error:{}", voucher, e.getMessage());
        }
    }

    private void receiptByJde(Receipt receipt) {
        try {
            String receiptedCode = getReceiptCode(receipt);
            if (isEmpty(receiptedCode)) {
                return;
            }

            String serial = Jde.generate(receipt.getId(), receiptedCode);
            JdeVoteResponse response = client.pullVoucher(serial);

            checkState(notNull(response), "jde.receipt.convert.error");
            checkState(response.isSuccess(), response.getError());
            JdeResult result = response.getResult();

            checkState(result.isSuccess(), result.getError());
            checkState(notEmpty(result.getReceipt()), "jde.receipt.empty");

            receipt.setReceipt(result.getReceipt());
            DateTime receiptedAt = DFT.parseDateTime(result.getReceiptedDate());
            receipt.setReceiptedAt(receiptedAt.toDate());

        } catch (IllegalStateException e) {
            log.error("jde pull receipt raise bill:{} error:{}", receipt, e.getMessage());
        }
    }


    private String getReceiptCode(Bill bill) {
        String receiptCode;

        if (bill instanceof SellerSettlement) {
            receiptCode = Jde.Type.COMMISSION_AND_THIRD.code();
        } else if (bill instanceof  DepositFee) {
            DepositFee depositFee = (DepositFee)bill;
            if (equalWith(depositFee.getType(), DepositFee.Type.TECH_SERVICE.value())) {
                receiptCode = Jde.Type.TECH_FEE_ORDER.code();
            } else {
                return "";
            }

        } else {
            throw new IllegalStateException("bill.instance.incorrect");
        }
        return receiptCode;
    }

    private String getVouchCode(Bill bill) {
        String vouchCode;

        if (bill instanceof SellerSettlement) {
            vouchCode = Jde.Type.SELLER_EARNING.code();
        } else if (bill instanceof SellerAlipayCash) {
            vouchCode = Jde.Type.ALIPAY_CASH.code();
        } else if (bill instanceof DepositFee) {
            DepositFee depositFee = (DepositFee)bill;
            if (equalWith(depositFee.getType(), DepositFee.Type.TECH_SERVICE.value())) {
                vouchCode = Jde.Type.TECH_FEE_SETTLEMENT.code();
            } else if (equalWith(depositFee.getType(), DepositFee.Type.INCREMENT.value())) {
                vouchCode = Jde.Type.DEPOSIT_PAY.code();
            } else if (equalWith(depositFee.getType(), DepositFee.Type.REFUND.value())) {
                vouchCode = Jde.Type.DEPOSIT_REFUND.code();
            } else if (equalWith(depositFee.getType(), DepositFee.Type.DEDUCTION.value())) {
                vouchCode = Jde.Type.DEPOSIT_DEDUCTION.code();
            } else {
                throw new IllegalStateException("deposit.type.incorrect");
            }

        } else if (bill instanceof DepositFeeCash) {
            vouchCode = Jde.Type.DEPOSIT_CASH.code();
        } else {
            throw new IllegalStateException("bill.instance.incorrect");
        }

        return vouchCode;
    }

}
