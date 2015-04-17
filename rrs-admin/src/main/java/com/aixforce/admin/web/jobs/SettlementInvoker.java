package com.aixforce.admin.web.jobs;

import com.aixforce.rrs.settle.service.SettlementJobService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 结算批处理任务
 * <p/>
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-21 10:04 AM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SettlementInvoker {
    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


    private final AdminLeader adminLeader;
    private final SettlementJobService settlementJobService;


    @Autowired
    public SettlementInvoker(AdminLeader adminLeader, SettlementJobService settlementJobService) {
        this.settlementJobService = settlementJobService;
        this.adminLeader = adminLeader;
    }


    /**
     * 更新88码
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void updateOuterCode() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        log.info("[OUTERCODE-UPDATE] doneAt {}",
                DFT.print(DateTime.now()));
        settlementJobService.updateOuterCode(DateTime.now().toDate());
        log.info("[OUTERCODE-UPDATE] DONE");
    }


    /**
     * 更新88码
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void updateOuterCodeFully() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        log.info("[OUTERCODE-UPDATE-FULL] doneAt {}",
                DFT.print(DateTime.now()));
        settlementJobService.updateOuterCodeFully(DateTime.now().toDate());
        log.info("[OUTERCODE-UPDATE-FULL] DONE");
    }


    /**
     * T日自动确认所有 T - 7 的订单
     */
    @Scheduled(cron = "0 30 0 * * *")
    public void autoConfirm() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[AUTO-CONFIRM] begin {}", DFT.print(DateTime.now()));
        settlementJobService.autoConfirmed(doneAt);
        log.info("[AUTO-CONFIRM] DONE");
    }


    /**
     * 更新费率
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void updateRate() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SETTLEMENT-RATE-UPDATE] doneAt {}",
                DFT.print(new DateTime(doneAt)));
        settlementJobService.updateRate(doneAt);
        log.info("[SETTLEMENT-RATE-UPDATE] DONE");

    }

    /**
     * 创建当天的结算任务
     */
    @Scheduled(cron = "1 0 0 * * *")
    public void create() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SETTLEMENT-JOB-CREATE] doneAt {}",
                DFT.print(new DateTime(doneAt)));
        settlementJobService.createJobs(doneAt);
        log.info("[SETTLEMENT-JOB-CREATE] DONE");

    }


    /**
     * 回写凭证单据
     */
    @Scheduled(cron = "0 0 4-12 * * *")
    public void vouch() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SETTLEMENT-VOUCHER] begin at {}", DFT.print(DateTime.now()));
        settlementJobService.updateVoucher(doneAt);
        log.info("[SETTLEMENT-VOUCHER] done");

    }


    /**
     * 日汇总
     */
    @Scheduled(cron = "0 30 1 * * *")
    public void summary() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SUMMARY-DAILY] begin {}", DFT.print(DateTime.now()));
        settlementJobService.summary(doneAt);
        log.info("[SUMMARY-DAILY] DONE");

    }


    /**
     * 同步JDE
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void sync() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SYNC-JDE] begin {}", DFT.print(DateTime.now()));
        settlementJobService.syncToJde(doneAt);
        log.info("[SYNC-JDE] DONE");

    }


    /**
     * 更新手续费
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void alipay() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[UPDATE-ALIPAY-FEE] doneAt {}",
                DFT.print(new DateTime(doneAt)));
        settlementJobService.updateAlipayFee(doneAt);
        log.info("[UPDATE-ALIPAY-FEE] DONE");

    }

    /**
     * 统计当天提现金额
     */
    @Scheduled(cron = "0 30 3 * * *")
    public void cash() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[ALIPAY-SUMMARY] doneAt {} }",
                DFT.print(new DateTime(doneAt)));
        settlementJobService.summaryAlipayCash(doneAt);
        log.info("[ALIPAY-SUMMARY] DONE");

    }

    /**
     * 处理上一天完成的订单
     */
    @Scheduled(cron = "0 30 4 * * *")
    public void finish() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[ORDER-DONE] begin {}", DFT.print(DateTime.now()));
        settlementJobService.markedOrderAsFinished(doneAt);
        log.info("[ORDER-DONE] DONE");

    }

    /**
     * 结算
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void settlement() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[SETTLEMENT] begin {}", DFT.print(DateTime.now()));
        settlementJobService.settle(doneAt);
        log.info("[SETTLEMENT] DONE");

    }


    /**
     * 补帐
     */
    @Scheduled(cron = "0 10 0 * * *")
    public void fix() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        log.info("[FIX] begin {}", DFT.print(DateTime.now()));
        settlementJobService.fix(doneAt);
        log.info("[FIX] DONE");

    }


}
