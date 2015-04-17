package com.aixforce.rrs.settle.endpoint;

import com.aixforce.rrs.settle.enums.JobType;
import com.aixforce.rrs.settle.handle.*;
import com.aixforce.rrs.settle.manager.JobManager;
import com.aixforce.rrs.settle.model.SettleJob;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-25 9:30 AM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SettlementExecutor {
    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final int DEFAULT_QUEUE_SIZE = 1000;

    private final ExecutorService executorService = new ThreadPoolExecutor(4, 6, 60L, TimeUnit.MINUTES,
            new ArrayBlockingQueue<Runnable>(DEFAULT_QUEUE_SIZE),
            new ThreadFactoryBuilder().setNameFormat("settle-worker-%d").build(),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                    SettlementTask settlementTask = (SettlementTask) runnable;
                    log.error("settlement(id={}) call request is rejected", settlementTask.getJobType());
            }
            });

    @Autowired
    private JobManager jobManager;

    @Autowired
    private SettlementHandle settlementHandle;

    @Autowired
    private FinishHandle finishHandle;

    @Autowired
    private SettlementSummaryHandle settlementSummaryHandle;

    @Autowired
    private VouchHandle vouchHandle;

    @Autowired
    private CashSummaryHandle cashSummaryHandle;

    @Autowired
    private AlipayFeeHandle alipayFeeHandle;

    @Autowired
    private SyncHandle syncHandle;

    @Autowired
    private RateUpdateHandle rateUpdateHandle;

    @Autowired
    private AutoConfirmHandle confirmHandle;

    @Autowired
    private FixSettlementHandle fixSettlementHandle;

    @Autowired
    private OuterCodeScheduleHandle outerCodeScheduleHandle;

    public void submit(JobType jobType, Date doneAt) {
        if (log.isDebugEnabled()) {
            log.debug("asynchronous submit {} to engine ", jobType);
        }
        this.executorService.submit(new SettlementTask(jobType, doneAt));
    }


    private class SettlementTask implements Runnable {
        private JobType jobType;
        private Date doneAt;

        private SettlementTask(JobType jobType, Date doneAt) {
            this.jobType = jobType;
            this.doneAt = doneAt;
        }

        @Override
        public void run() {
            Date tradeAt = new DateTime(doneAt).minusDays(1).withTimeAtStartOfDay().toDate();
            if (Objects.equal(jobType, JobType.CREATE_JOBS)) {
                jobManager.createJobs(doneAt, tradeAt);
                return;
            }

            if (Objects.equal(jobType, JobType.UPDATE_OUTER_CODE)) {
                outerCodeScheduleHandle.syncOuterCode(doneAt);
                return;
            }

            if (Objects.equal(jobType, JobType.UPDATE_OUTER_CODE_FULL)) {
                outerCodeScheduleHandle.full();
                return;
            }

            SettleJob settleJob = jobManager.getByType(doneAt, jobType);
            if (settleJob == null) {
                log.info("no matched job with doneAt({}), type({})",
                        DFT.print(new DateTime(doneAt)), jobType.value());
                return;
            }

            if (Objects.equal(jobType, JobType.UPDATE_VOUCHER)) {
                vouchHandle.updateVouchersAndReceipts(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.SUMMARY_SETTLEMENTS)) {
                settlementSummaryHandle.summarySettlements(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.SYNC_TO_JDE)) {
                syncHandle.syncToJde(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.UPDATE_ALIPAY_FEES)) {
                alipayFeeHandle.updateAlipayFees(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.SUMMARY_ALIPAY_CASHES)) {
                cashSummaryHandle.summaryAlipayCashes(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.MARK_SETTLEMENT_FINISHED)) {
                finishHandle.markSettlementFinished(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.SETTLEMENT)) {
                settlementHandle.settlement(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.UPDATE_RATE)) {
                rateUpdateHandle.updateRrsRate(settleJob);
                return;
            }

            if (Objects.equal(jobType, JobType.AUTO_CONFIRM)) {
                confirmHandle.autoConfirm(settleJob);
            }

            if (Objects.equal(jobType, JobType.FIX_SETTLEMENT)) {
                fixSettlementHandle.fix(settleJob);
            }

        }

        public JobType getJobType() {
            return jobType;
        }
    }

    public void call(JobType jobType, Date doneAt) {
        SettlementTask settlementTask = new SettlementTask(jobType, doneAt);
        this.executorService.submit(settlementTask);
    }

    @PreDestroy
    public void destroy() {
        log.info("shutdown settle executor.....");
        this.executorService.shutdown();
    }

}
