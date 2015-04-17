package com.aixforce.rrs.settle.manager;

import com.aixforce.rrs.settle.dao.SettleJobDao;
import com.aixforce.rrs.settle.enums.JobType;
import com.aixforce.rrs.settle.model.SettleJob;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 结算的批处理任务 <br/>
 *
 * 创建批处理任务记录 T表示实际处理的日期 <br/>
 *  ⤷ 1.拉取JDE中已经填写好凭证号的数据,更新各种凭证号 (T-1) <br/>
 *  ⤷ 2.生成汇总报表 (状态"确认"，商户汇总基于商户与日期 T-1; 日汇总基于日期; )  <br/>
 *      ⤷ 3.同步各类数据至JDE   <br/>
 *  ⤷ 4.更新支付宝手续费(T-1日)  <br/>
 *      ⤷ 5.统计T-1日的可提现金额(粒度：商户、汇总)，于T日供财务查询 (T-1日) <br/>
 *      ⤷ 6.标记已完成（订单状态关闭 & 已提现）的订单的结算记录为已关闭(T-1) <br/>
 *          ⤷ 7. 计算已关闭状态结算记录的平台佣金，商家收入，积分，预售金扣除等各种金额 ("已关闭" 且 "未结算") <br/>
 *
 *
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-13 9:27 AM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class JobManager {
    @Autowired
    private SettleJobDao settleJobDao;


    /**
     * 创建任务
     *
     * @param doneAt    处理时间
     * @param tradedAt   交易时间
     */
    @Transactional
    public void createJobs(Date doneAt, Date tradedAt) {

        Date startAt = new DateTime(doneAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(doneAt).withTimeAtStartOfDay().plusDays(1).toDate();
        boolean exists = settleJobDao.check(startAt, endAt);
        if (exists) {  // 存在相同时间任务则返回
            log.info("job exists, skipped");
            return;
        }

        log.info("begin to create jobs");
        Stopwatch stopwatch = Stopwatch.createStarted();


        SettleJob updateVoucherJob = SettleJob.updateVoucher(doneAt, tradedAt); // 任务1:创建回写订单凭据的任务
        settleJobDao.create(updateVoucherJob);


        SettleJob autoConfirmJob =  SettleJob.autoConfirm(doneAt, tradedAt);    // 任务10: 自动确认
        settleJobDao.create(autoConfirmJob);

        SettleJob reportJob = SettleJob.report(doneAt, tradedAt);               // 任务2:生成日汇总
        reportJob.setDependencyId(autoConfirmJob.getId());
        settleJobDao.create(reportJob);

        SettleJob syncJdeJob = SettleJob.syncJde(doneAt, tradedAt);             // 任务3:同步数据至JDE, 依赖任务2
        syncJdeJob.setDependencyId(reportJob.getId());
        settleJobDao.create(syncJdeJob);

        SettleJob alipayFeeJob = SettleJob.alipayFee(doneAt, tradedAt);         // 任务4:更新支付宝手续费
        settleJobDao.create(alipayFeeJob);

        SettleJob alipayCashJob = SettleJob.alipayCash(doneAt, tradedAt);       // 任务5:统计T-1日可提现金额，此任务依赖任务4
        alipayCashJob.setDependencyId(alipayFeeJob.getId());
        settleJobDao.create(alipayCashJob);

        SettleJob finishOrderJob = SettleJob.markSettlementFinished(doneAt, tradedAt);     // 任务6:标记已关闭订单状态，依赖任务4
        finishOrderJob.setDependencyId(alipayFeeJob.getId());
        settleJobDao.create(finishOrderJob);

        SettleJob settleJob = SettleJob.settle(doneAt, tradedAt);               // 任务7:结算各项金额，依赖任务6
        settleJob.setDependencyId(finishOrderJob.getId());
        settleJobDao.create(settleJob);

        SettleJob updateRateJob =  SettleJob.updateRate(doneAt, tradedAt);      // 任务8: 更新费率
        settleJobDao.create(updateRateJob);


        SettleJob fixJob =  SettleJob.fixSettlement(doneAt, tradedAt);      // 任务12: 补系统帐务
        settleJobDao.create(fixJob);


        stopwatch.stop();
        log.info("create jobs done, cost={}", stopwatch.elapsed(TimeUnit.SECONDS));
    }


    /**
     * 根据 处理日期与类型 获取任务
     * @param doneAt    处理日期
     * @param type      任务类型
     * @return  任务对象
     */
    public SettleJob getByType(Date doneAt, JobType type) {
        return settleJobDao.getByDoneAtAndJobType(doneAt, type.value());
    }


}
