package com.aixforce.rrs.settle.manager;

import com.aixforce.rrs.BaseManagerTest;
import com.aixforce.rrs.settle.dao.SettleJobDao;
import com.aixforce.rrs.settle.enums.JobType;
import com.aixforce.rrs.settle.handle.CashSummaryHandle;
import com.aixforce.rrs.settle.model.SettleJob;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-26 5:04 PM  <br>
 * Author: xiao
 */
@Ignore
public class AlipayCashManagerTest extends BaseManagerTest {

    @Autowired
    private SettleJobDao settleJobDao;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private CashSummaryHandle cashSummaryHandle;

    private SettleJob alipayCashJob;

    @Before
    public void setUp() {
        Date doneAt = DateTime.parse("2010-10-11").withTimeAtStartOfDay().toDate();
        Date tradedAt = DateTime.parse("2010-10-10").withTimeAtStartOfDay().toDate();
        jobManager.createJobs(doneAt, tradedAt);
        alipayCashJob = settleJobDao.getByDoneAtAndJobType(doneAt, JobType.SUMMARY_ALIPAY_CASHES.value());
        cashSummaryHandle.summaryAlipayCashes(alipayCashJob);
    }

}
