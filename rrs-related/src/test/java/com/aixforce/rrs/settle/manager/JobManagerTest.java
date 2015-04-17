package com.aixforce.rrs.settle.manager;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseManagerTest;
import com.aixforce.rrs.settle.dao.*;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.enums.JobType;
import com.aixforce.rrs.settle.handle.CashSummaryHandle;
import com.aixforce.rrs.settle.handle.FinishHandle;
import com.aixforce.rrs.settle.handle.SettlementHandle;
import com.aixforce.rrs.settle.handle.SettlementSummaryHandle;
import com.aixforce.rrs.settle.model.*;
import com.google.common.base.Objects;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-13 12:08 PM  <br>
 * Author: xiao
 */
@Ignore
public class JobManagerTest extends BaseManagerTest {

    @Autowired
    private JobManager jobManager;

    @Autowired
    private AlipayCashManager alipayCashManager;

    @Autowired
    private SettleJobDao settleJobDao;

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private DailySettlementDao dailySettlementDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private AlipayCashDao alipayCashDao;

    private SettleJob reportJob;
    private SettleJob alipayCashJob;
    private SettleJob finishOrderJob;
    private SettleJob settlementJob;

    @Autowired
    private SettlementHandle settlementHandle;

    @Autowired
    private FinishHandle finishHandle;

    @Autowired
    private SettlementSummaryHandle settlementSummaryHandle;

    @Autowired
    private CashSummaryHandle cashSummaryHandle;


    @Before
    public void setUp(){
        Date doneAt = DateTime.parse("2010-10-11").withTimeAtStartOfDay().toDate();
        Date tradedAt = DateTime.parse("2010-10-10").withTimeAtStartOfDay().toDate();
        jobManager.createJobs(doneAt, tradedAt);
        reportJob =  settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_SETTLEMENTS.value());
        alipayCashJob = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_ALIPAY_CASHES.value());
        finishOrderJob = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.MARK_SETTLEMENT_FINISHED.value());
        settlementJob = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SETTLEMENT.value());
    }

    @Test
    public void testCreateJobsOk() {
        Date doneAt = DateTime.now().withTimeAtStartOfDay().toDate();
        Date tradedAt = DateTime.now().minusDays(1).withTimeAtStartOfDay().toDate();
        jobManager.createJobs(doneAt, tradedAt);
        // 回写凭证号
        SettleJob vouchUpdate = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.UPDATE_VOUCHER.value());
        // 统计 T-1 商户确认的报表信息
        SettleJob report = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_SETTLEMENTS.value());
        // 同步已经确认的商户日报表至JDE
        SettleJob syncJde = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SYNC_TO_JDE.value());
        // 更新支付宝手续费
        SettleJob alipayFee = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.UPDATE_ALIPAY_FEES.value());
        // 统计支付宝提现金额
        SettleJob alipayCash = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_ALIPAY_CASHES.value());
        // 标记 T-1 结束订单
        SettleJob markFinished = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.MARK_SETTLEMENT_FINISHED.value());
        // 计算 T-1 结束订单各项收支
        SettleJob settle = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SETTLEMENT.value());

        assertThat(vouchUpdate.getDependencyId(), nullValue());
        assertThat(vouchUpdate.getId(), notNullValue());
        assertThat(vouchUpdate.getType(), is(JobType.UPDATE_VOUCHER.value()));
        assertThat(vouchUpdate.getDoneAt(), is(doneAt));
        assertThat(vouchUpdate.getTradedAt(), is(tradedAt));
        assertThat(vouchUpdate.getStatus(), is(JobStatus.NOT.value()));

        assertThat(report.getDependencyId(), nullValue());
        assertThat(report.getId(), notNullValue());
        assertThat(report.getType(), is(JobType.SUMMARY_SETTLEMENTS.value()));
        assertThat(report.getDoneAt(), is(doneAt));
        assertThat(report.getTradedAt(), is(tradedAt));
        assertThat(vouchUpdate.getStatus(), is(JobStatus.NOT.value()));

        assertThat(syncJde.getDependencyId(), is(report.getId()));  // 依赖日汇总的结果
        assertThat(syncJde.getId(), notNullValue());
        assertThat(syncJde.getType(), is(JobType.SYNC_TO_JDE.value()));
        assertThat(syncJde.getDoneAt(), is(doneAt));
        assertThat(syncJde.getTradedAt(), is(tradedAt));
        assertThat(syncJde.getStatus(), is(JobStatus.NOT.value()));

        assertThat(alipayFee.getDependencyId(), nullValue());
        assertThat(alipayFee.getId(), notNullValue());
        assertThat(alipayFee.getType(), is(JobType.UPDATE_ALIPAY_FEES.value()));
        assertThat(alipayFee.getDoneAt(), is(doneAt));
        assertThat(alipayFee.getTradedAt(), is(tradedAt));
        assertThat(alipayFee.getStatus(), is(JobStatus.NOT.value()));

        assertThat(alipayCash.getDependencyId(), is(alipayFee.getId()));    // 依赖支付宝手续费更新的结果
        assertThat(alipayCash.getId(), notNullValue());
        assertThat(alipayCash.getType(), is(JobType.SUMMARY_ALIPAY_CASHES.value()));
        assertThat(alipayCash.getDoneAt(), is(doneAt));
        assertThat(alipayCash.getTradedAt(), is(tradedAt));
        assertThat(alipayCash.getStatus(), is(JobStatus.NOT.value()));

        assertThat(markFinished.getDependencyId(), is(alipayFee.getId()));    // 依赖支付宝手续费更新的结果
        assertThat(markFinished.getId(), notNullValue());
        assertThat(markFinished.getType(), is(JobType.MARK_SETTLEMENT_FINISHED.value()));
        assertThat(markFinished.getDoneAt(), is(doneAt));
        assertThat(markFinished.getTradedAt(), is(tradedAt));
        assertThat(markFinished.getStatus(), is(JobStatus.NOT.value()));

        assertThat(settle.getDependencyId(), is(markFinished.getId()));         // 依赖标记结束订单的结果
        assertThat(settle.getId(), notNullValue());
        assertThat(settle.getType(), is(JobType.SETTLEMENT.value()));
        assertThat(settle.getDoneAt(), is(doneAt));
        assertThat(settle.getTradedAt(), is(tradedAt));
        assertThat(settle.getStatus(), is(JobStatus.NOT.value()));
    }

    @Test
    public void testUpdateVouchersAndReceiptsOk() {}

    @Test
    public void testFinishOrdersOk() {
        finishHandle.markSettlementFinished(finishOrderJob);
        Date tradedAt = reportJob.getTradedAt();
        Date beginAt = new DateTime(tradedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(tradedAt).withTimeAtStartOfDay().plusDays(1).toDate();

        Paging<Settlement> paging = settlementDao.findBy(null, 0, null, null, 0, 10);
        assertThat(paging.getTotal(), is(3L));
        List<Settlement> settlements = paging.getData();
        for (Settlement settlement : settlements) {
            assertThat(settlement.getFinishedAt(), DateMatchers.before(endAt));
            assertThat(settlement.getFinishedAt(), DateMatchers.after(beginAt));
            assertThat(settlement.getFinished(), is(Settlement.Finished.DONE.value()));
            assertThat(settlement.getUpdatedAt(), DateMatchers.sameDay(DateTime.now().toDate()));

        }

    }

    @Test
    public void testSummaryAlipayCashesOk() {
        cashSummaryHandle.summaryAlipayCashes(alipayCashJob);

        Date tradedAt = reportJob.getTradedAt();

        Date beginAt = new DateTime(tradedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(tradedAt).withTimeAtStartOfDay().plusDays(1).toDate();

        Paging<SellerAlipayCash> sellerPaging = sellerAlipayCashDao.findBy(null, null, beginAt, endAt, 0, 10);
        assertThat(sellerPaging.getTotal(), is(3L));
        List<SellerAlipayCash> sellerAlipayCashes = sellerPaging.getData();

        for (SellerAlipayCash sellerAlipayCash : sellerAlipayCashes) {
            if (Objects.equal(sellerAlipayCash.getSellerId(), 1L)) {
                assertThat(sellerAlipayCash.getTotalFee(), is(10000L));
                assertThat(sellerAlipayCash.getAlipayFee(), is(100L));
                assertThat(sellerAlipayCash.getCashFee(), is(9900L));
                assertThat(sellerAlipayCash.getSummedAt(), is(tradedAt));
            }

            if (Objects.equal(sellerAlipayCash.getSellerId(), 2L)) {
                assertThat(sellerAlipayCash.getTotalFee(), is(20000L));
                assertThat(sellerAlipayCash.getAlipayFee(), is(200L));
                assertThat(sellerAlipayCash.getCashFee(), is(19800L));
                assertThat(sellerAlipayCash.getSummedAt(), is(tradedAt));
            }

            if (Objects.equal(sellerAlipayCash.getSellerId(), 3L)) {
                assertThat(sellerAlipayCash.getTotalFee(), is(0L));
                assertThat(sellerAlipayCash.getAlipayFee(), is(0L));
                assertThat(sellerAlipayCash.getCashFee(), is(0L));
                assertThat(sellerAlipayCash.getSummedAt(), is(tradedAt));
            }
        }


        Paging<AlipayCash> alipayCashPaging = alipayCashDao.findBy(beginAt, endAt, 0, 10);
        assertThat(alipayCashPaging.getTotal(), is(1L));
        AlipayCash actual = alipayCashPaging.getData().get(0);

        assertThat(actual, notNullValue());
        assertThat(actual.getTotalFee(), is(30000L));
        assertThat(actual.getAlipayFee(), is(300L));
        assertThat(actual.getCashFee(), is(29700L));
        assertThat(actual.getSummedAt(), is(tradedAt));
    }

    @Test
    public void testSummaryReportOk() {

        settlementSummaryHandle.summarySettlements(reportJob);

        Date tradedAt = reportJob.getTradedAt();
        Date beginAt = new DateTime(tradedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(tradedAt).withTimeAtStartOfDay().plusDays(1).toDate();
        Paging<SellerSettlement> sellerPaging = sellerSettlementDao.findBy(null, null, beginAt, endAt, 0, 10);

        assertThat(sellerPaging.getTotal(), is(3L));
        List<SellerSettlement> sellerSettlements = sellerPaging.getData();

        for (SellerSettlement settlement : sellerSettlements) {
            if (Objects.equal(settlement.getSellerId(), 1L)) {
                assertThat(settlement.getScoreEarning(), is(10000L));
                assertThat(settlement.getThirdPartyCommission(), is(100L));
                assertThat(settlement.getConfirmedAt(), is(tradedAt));
            }

            if (Objects.equal(settlement.getSellerId(), 2L)) {
                assertThat(settlement.getScoreEarning(), is(20000L));
                assertThat(settlement.getThirdPartyCommission(), is(200L));
                assertThat(settlement.getConfirmedAt(), is(tradedAt));
            }

            if (Objects.equal(settlement.getSellerId(), 3L)) {
                assertThat(settlement.getScoreEarning(), is(0L));
                assertThat(settlement.getThirdPartyCommission(), is(0L));
                assertThat(settlement.getConfirmedAt(), is(tradedAt));
            }
        }

        Paging<DailySettlement> dailyPaging = dailySettlementDao.findBy(beginAt, endAt, 0, 10);
        assertThat(dailyPaging.getTotal(), is(1L));
        DailySettlement actual = dailyPaging.getData().get(0);

        assertThat(actual, notNullValue());
        assertThat(actual.getScoreEarning(), is(30000L));
        assertThat(actual.getConfirmedAt(), is(tradedAt));
    }


}
