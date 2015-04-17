package com.aixforce.rrs.settle.manager;

import com.aixforce.rrs.settle.dao.DepositAccountDao;
import com.aixforce.rrs.settle.dao.DepositFeeCashDao;
import com.aixforce.rrs.settle.dao.DepositFeeDao;
import com.aixforce.rrs.settle.model.DepositAccount;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.rrs.settle.model.DepositFee.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-10 12:18 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class DepositManager {

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private DepositAccountDao depositAccountDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;


    public Boolean isAccountLocked(Long sellerId, Integer threshold) {
        DepositAccount account = depositAccountDao.findBySellerId(sellerId);
        checkState(notNull(account) && notNull(account.getBalance()), "deposit.account.not.found");
        return isAccountLocked(account, threshold);
    }

    public Boolean isAccountLocked(DepositAccount account, Integer threshold) {
        Long balance = account.getBalance();
        return balance < threshold;
    }


    /**
     * 商户缴纳保证金
     *
     * @param fee   保证金信息
     * @return 记录标识
     */
    @Transactional
    public Long createDepositFee(DepositFee fee) {
        DepositAccount account = depositAccountDao.findBySellerId(fee.getSellerId());
        checkState(notNull(account), "deposit.account.not.found");
        Long balance = account.getBalance();

        fee.setSellerName(account.getSellerName());
        fee.setBusiness(account.getBusiness());
        fee.setOuterCode(account.getOuterCode());
        fee.setShopId(account.getShopId());
        fee.setShopName(account.getShopName());

        Long id = depositFeeDao.create(fee);
        // 当支付方式为支付宝时需要创建提现单据
        if (equalWith(fee.getType(), Type.INCREMENT.value()) || equalWith(fee.getType(), Type.TECH_SERVICE.value())) {
            createDepositCash(fee, id);
        }

        if (Objects.equal(fee.getType(), DepositFee.Type.TECH_SERVICE.value())) {  // 技术服务费不需要更新账户
            return id;
        } else if (Objects.equal(fee.getType(), DepositFee.Type.INCREMENT.value())) {
            balance += fee.getDeposit();
        } else if (Objects.equal(fee.getType(), DepositFee.Type.DEDUCTION.value()) ||
                Objects.equal(fee.getType(), DepositFee.Type.REFUND.value())) {
            balance -= fee.getDeposit();
        }

        depositAccountDao.updateBal(account.getId(), balance);
        return id;
    }

    private void createDepositCash(DepositFee fee, Long id) {
        if (equalWith(fee.getPaymentType(), PaymentType.ALIPAY.value())) {
            DepositFeeCash depositFeeCash = new DepositFeeCash();
            depositFeeCash.setDepositId(id);
            depositFeeCash.setSellerId(fee.getSellerId());
            depositFeeCash.setSellerName(fee.getSellerName());
            depositFeeCash.setShopId(fee.getShopId());
            depositFeeCash.setShopName(fee.getShopName());
            depositFeeCash.setStatus(DepositFeeCash.Status.NOT.value());
            depositFeeCash.setSynced(DepositFeeCash.Synced.NOT.value());
            depositFeeCash.setVouched(DepositFeeCash.Vouched.NOT.value());
            depositFeeCash.setCashFee(fee.getDeposit());
            depositFeeCash.setBusiness(fee.getBusiness());
            depositFeeCash.setOuterCode(fee.getOuterCode());

            Integer cashType = Objects.equal(fee.getType(), Type.INCREMENT.value()) ?
                    DepositFeeCash.CashType.DEPOSIT.value() : DepositFeeCash.CashType.TECH_FEE.value();

            depositFeeCash.setCashType(cashType);
            depositFeeCashDao.create(depositFeeCash);
        }
    }

    /**
     * 标记同步完成
     *
     * @param fees  保证金或技术服务费列表
     */
    @Transactional
    public void batchSynced(List<DepositFee> fees) {
        for (DepositFee fee : fees) {
            depositFeeDao.synced(fee.getId());
        }
    }


    /**
     * 更新保证金或者技术服务费
     *
     * @param origin        原始费用
     * @param updating      更新后的费用
     * @return  更新后的费用id
     */
    @Transactional
    public Long updateDeposit(DepositFee origin, DepositFee updating) {

        checkState(depositFeeDao.update(updating), "deposit.fee.update.fail");


        if (equalWith(origin.getPaymentType(), PaymentType.ALIPAY.value())) { // 支付宝提现需要同步修改提现单据
            DepositFeeCash cash = depositFeeCashDao.getByDepositId(origin.getId());
            checkState(notNull(cash), "deposit.cash.not.found");

            DepositFeeCash cashUpdating = new DepositFeeCash();
            cashUpdating.setId(cash.getId());
            cashUpdating.setCashFee(updating.getDeposit());
            checkState(depositFeeCashDao.update(cashUpdating), "deposit.cash.update.fail");
        }

        if (isTechService(origin)) {   // 技术服务费不用更新账户
            return updating.getId();
        }

        Long originFee = origin.getDeposit();
        Long updatingFee = updating.getDeposit();
        Long delta = updatingFee - originFee;

        DepositAccount account = depositAccountDao.findBySellerId(origin.getSellerId());
        checkState(notNull(account), "deposit.account.not.found");

        Long balance = account.getBalance();

        if (isIncrement(origin)) { // 新增
            balance += delta;
        } else if (isDeduction(origin) || isRefund(origin)) {
            balance -= delta;
        } else {
            throw new IllegalStateException("deposit.fee.type.incorrect");
        }

        account.setBalance(balance);
        checkState(depositAccountDao.updateBal(account.getId(), balance), "deposit.account.update.fail");

        return updating.getId();
    }


    @Transactional
    public void batchSyncedCash(List<DepositFeeCash> cashes) {
        for (DepositFeeCash cash : cashes) {
            depositFeeCashDao.synced(cash.getId());
        }

    }

}
