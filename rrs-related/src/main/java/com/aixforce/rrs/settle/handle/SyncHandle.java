package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.rrs.jde.JdeClient;
import com.aixforce.rrs.jde.JdeWriteResponse;
import com.aixforce.rrs.settle.dao.DepositFeeCashDao;
import com.aixforce.rrs.settle.dao.DepositFeeDao;
import com.aixforce.rrs.settle.dao.SellerAlipayCashDao;
import com.aixforce.rrs.settle.dao.SellerSettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.*;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 10:12 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SyncHandle extends JobHandle {

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    @Autowired
    private DepositManager depositManager;

    @Autowired
    private JdeClient client;



    /**
     * 同步JDE
     *
     * @param job  任务信息
     */
    public void syncToJde(SettleJob job) {
        log.info("[SYNC-TO-JDE] begin at {}", DFT.print(DateTime.now()));

        if (!dependencyOk(job)) {
            log.info("dependency job is not over, skipped");
            return;
        }

        if (Objects.equal(job.getStatus(), JobStatus.DONE.value())) {
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        settleJobDao.ing(job.getId());
        boolean existError = false;

        try {
            // 同步商户各项收支
            syncSellerSettlements();
        } catch (Exception e) {
            log.error("sync seller settlement cause:{}", Throwables.getStackTraceAsString(e));
            existError = true;
        }

        try {
            // 同步保证金
            syncDepositsFee();
        } catch (Exception e) {
            log.error("sync depositFee fail cause:{}", Throwables.getStackTraceAsString(e));
            existError = true;
        }

        try {
            // 同步技术服务费
            syncTechFee();
        } catch (Exception e) {
            log.error("sync techFee fail, cause:{}", Throwables.getStackTraceAsString(e));
            existError = true;
        }

        try {
            // 同步技术服务费提现
            syncDepositsFeeCash();
        } catch (Exception e) {
            log.error("sync seller depositFeeCash fail, cause:{}", Throwables.getStackTraceAsString(e));
            existError = true;
        }

        try {
            // 同步支付宝日提现
            syncSellerAlipayCash();
        } catch (Exception e) {
            log.error("sync seller alipayCash fail, cause:{}", Throwables.getStackTraceAsString(e));
            existError = true;
        }


        if (existError) {
            log.error("[SYNC-TO-JDE] failed, job({}) fail", job);
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
        log.info("[SYNC-TO-JDE] begin at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }


    /**
     * 同步T-1日商户确认的汇总数据,错误的话暂时先跳过
     */
    private void syncSellerSettlements() {

        Integer pageNo = 1;
        boolean next = batchSyncSellerSettlements(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchSyncSellerSettlements(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 同步技术服务费 (包括 缴纳保证金、退保证金、扣保证金）
     */
    private void syncDepositsFee() {

        Integer pageNo = 1;
        boolean next = batchSyncDepositFee(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchSyncDepositFee(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 同步技术服务费(需要包含 订单及对账 信息）
     */
    private void syncTechFee() {
        Integer pageNo = 1;
        boolean next = batchSyncedTechFee(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchSyncedTechFee(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 同步基础金提现单
     */
    private void syncDepositsFeeCash() {
        Integer pageNo = 1;
        boolean next = batchSyncDepositFeeCash(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchSyncDepositFeeCash(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 同步商户提现记
     */
    private void syncSellerAlipayCash() {
        Integer pageNo = 1;
        boolean next = batchSyncSellerAlipayCash(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchSyncSellerAlipayCash(pageNo, BATCH_SIZE);
        }
    }

    /**
     * 校验outerCode是否合法，合法的判断依据是 商家编码不为空 以及 编码字符串长度为10
     *
     * @param outerCode     商家编码
     * @return  True:合法 False:非法
     */
    private boolean validOuterCode(String outerCode) {

        if (notEmpty(outerCode) && outerCode.length() == 10) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    /**
     * 同步商户汇总信息（包含订单全款、商家收入（返款）、手续费及佣金、退款）
     *
     * @param pageNo                第几批
     * @param size                  批次数量
     * @return  是否存在下一批待处理数据
     */
    private boolean batchSyncSellerSettlements(Integer pageNo, int size) {

        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<SellerSettlement> paging = sellerSettlementDao.findUnVouched(pageInfo.offset, pageInfo.limit);

        List<SellerSettlement> settlements = paging.getData();
        for (SellerSettlement sellerSettlement: settlements) {
            JdeWriteResponse result;
            try {
                if (equalWith(sellerSettlement.getSynced(), SellerSettlement.Synced.DONE.value())) continue;
                if (sellerSettlement.getOrderCount() == 0) continue;
                // 非法的商家编码则先不处理
                if (!validOuterCode(sellerSettlement.getOuterCode())) continue;

                result = client.syncSellerSettlement(sellerSettlement);
                checkState(result.isSuccess(), result.getError());
                settlementManager.synced(sellerSettlement.getId());

            } catch (IllegalStateException e) {
                log.error("fail to deal with sellerSettlement:{}, error:{}", sellerSettlement, e.getMessage());
            }
        }

        int current = settlements.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }


    /**
     * 批量同步保证金（包含新增保证金、退还保证金、暂时不支持扣除保证金）
     *
     * @param size      批次数量
     * @return  是否存在下一批待处理数据
     */
    private boolean batchSyncDepositFee(Integer pageNo, Integer size) {


        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<DepositFee> paging = depositFeeDao.findUnVouchedDeposits(pageInfo.offset, pageInfo.limit);

        List<DepositFee> depositFees = paging.getData();
        for (DepositFee fee : depositFees) {  // 这里先改成一次同步一条
            try {
                if (equalWith(fee.getSynced(), DepositFee.Synced.DONE.value())) continue;
                if (!validOuterCode(fee.getOuterCode())) continue;

                List<DepositFee> syncing = Lists.newArrayList(fee);
                JdeWriteResponse result = client.batchSyncedDepositFees(syncing);
                checkState(result.isSuccess(), result.getError());
                depositManager.batchSynced(syncing);   // 标记同步完成
            } catch (IllegalStateException e) {
                log.error("fail to sync depositFee:{}, error:{}", fee, e.getMessage());
            }
        }

        int current = depositFees.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 同步技术服务费（需要拆分 订单和对账 两种业务场景）
     *
     * @param size      批次数量
     * @return  是否存在下一批待处理数据
     */
    private boolean batchSyncedTechFee(Integer pageNo, Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<DepositFee> paging = depositFeeDao.findUnVouchedTechs(pageInfo.offset, pageInfo.limit);
        List<DepositFee> techFees = paging.getData();

        for (DepositFee fee : techFees) {  // 这里先改成一次同步一条
            try {
                if (equalWith(fee.getSynced(), DepositFee.Synced.DONE.value())) continue;
                if (!validOuterCode(fee.getOuterCode())) continue;

                List<DepositFee> syncing = Lists.newArrayList(fee);
                JdeWriteResponse result = client.batchSyncedTechFees(syncing);
                checkState(result.isSuccess(), result.getError());
                depositManager.batchSynced(syncing);   // 标记同步完成
            } catch (IllegalStateException e) {
                log.error("fail to sync techFee:{}, error:{}", fee, e.getMessage());
            }
        }

        int current = techFees.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 同步基础费用(包括技术服务费、押金单）
     *
     * @param size   批次数量
     * @return 是否存在下一批待处理数据
     */
    private boolean batchSyncDepositFeeCash(Integer pageNo, Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<DepositFeeCash> paging = depositFeeCashDao.findUnVouched(pageInfo.offset, pageInfo.limit);
        List<DepositFeeCash> cashes = paging.getData();

        for (DepositFeeCash cash : cashes) {  // 这里先改成一次同步一条
            if (!validOuterCode(cash.getOuterCode())) {     // 88码不正确不同步
                continue;
            }
            if (!equalWith(cash.getStatus(), DepositFeeCash.Status.DONE.value())) {   // 未提现的不同步
                continue;
            }

            try {

                List<DepositFeeCash> syncing = Lists.newArrayList(cash);
                JdeWriteResponse result = client.batchSyncedDepositCash(syncing);
                checkState(result.isSuccess(), result.getError());
                depositManager.batchSyncedCash(syncing);   // 标记同步完成
            } catch (IllegalStateException e) {
                log.error("fail to sync depositFeeCash:{}, error:{}", cash, e.getMessage());
            }
        }

        int current = cashes.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 批量同步商户提现记录
     *
     * @param size  批次数量
     * @return  是否存在下一个批次
     */
    private boolean batchSyncSellerAlipayCash(Integer pageNo, Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<SellerAlipayCash> paging = sellerAlipayCashDao.findCashedNotVouched(pageInfo.offset, pageInfo.limit);
        List<SellerAlipayCash> sellerAlipayCashes = paging.getData();
        for (SellerAlipayCash sellerAlipayCash: sellerAlipayCashes) {
            // 已完成的不处理
            if (equalWith(sellerAlipayCash.getSynced(), SellerAlipayCash.Synced.DONE.value())) {
                log.info("sellerAlipayCash(id:{}) already done, skipped", sellerAlipayCash.getId());
                continue;
            }

            // 各项金额为0的不处理
            if (equalWith(sellerAlipayCash.getTotalFee(), 0L)
                    && equalWith(sellerAlipayCash.getCashFee(), 0L)
                    && equalWith(sellerAlipayCash.getAlipayFee(), 0L)
                    && equalWith(sellerAlipayCash.getRefundFee(), 0L)) {
                log.info("sellerAlipayCash(id:{}) every amount is empty, skipped", sellerAlipayCash.getId());
                continue;
            }

            // 错误的商家88码不处理
            if (!validOuterCode(sellerAlipayCash.getOuterCode())) {
                log.info("sellerAlipayCash(id:{}) every has incorrect outerCode, skipped", sellerAlipayCash.getId());
                continue;
            }


            JdeWriteResponse result;
            try {
                result = client.syncSellerAlipayCash(sellerAlipayCash);
                checkState(result.isSuccess(), result.getError());
                sellerAlipayCashDao.synced(sellerAlipayCash.getId());
                log.info("handle sellerAlipayCash(id:{}) successfully", sellerAlipayCash.getId());

            } catch (IllegalStateException e) {
                log.error("fail to deal with sellerAlipayCash:{}, error:{}", sellerAlipayCash, e.getMessage());
            }
        }

        int current = sellerAlipayCashes.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }



}
