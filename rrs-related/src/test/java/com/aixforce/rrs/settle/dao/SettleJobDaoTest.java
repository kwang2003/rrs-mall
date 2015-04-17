package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.enums.JobType;
import com.aixforce.rrs.settle.model.SettleJob;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-25 11:33 AM  <br>
 * Author: xiao
 */
public class SettleJobDaoTest extends BaseDaoTest {

    @Autowired
    private SettleJobDao settleJobDao;

    private SettleJob sj;

    @Before
    public void setUp() throws Exception {
        sj = new SettleJob(DateTime.now().toDate(), JobType.MARK_SETTLEMENT_FINISHED.value());
        settleJobDao.create(sj);

        SettleJob actual = settleJobDao.get(sj.getId());
        sj.setCreatedAt(actual.getCreatedAt());
        sj.setUpdatedAt(actual.getUpdatedAt());
        sj.setStatus(actual.getStatus());
        sj.setCost(actual.getCost());
        assertThat(actual, is(sj));
    }

    @Test
    public void testUnfinished() {
        List<SettleJob> lst  = settleJobDao.unfinished();
        assertThat(lst.size(), is(1));
        SettleJob actual = lst.get(0);
        assertThat(actual.getStatus(), is(JobStatus.NOT.value()));
        assertThat(actual.getCost(), is(-1L));
        assertThat(actual.getType(), is(JobType.MARK_SETTLEMENT_FINISHED.value()));
    }

    @Test
    public void testGet() {
        SettleJob actual = settleJobDao.get(sj.getId());
        assertThat(actual, notNullValue());
    }

    @Test
    public void testDone() {
        settleJobDao.done(sj.getId(), 1000L);
        SettleJob actual = settleJobDao.get(sj.getId());
        assertThat(actual.getCost(), is(1000L));
        assertThat(actual.getStatus(), is(JobStatus.DONE.value()));
    }

    @Test
    public void testIng() {
        settleJobDao.ing(sj.getId());
        SettleJob actual = settleJobDao.get(sj.getId());
        assertThat(actual.getStatus(), is(JobStatus.ING.value()));
    }

    @Test
    public void testFail() {
        settleJobDao.fail(sj.getId());
        SettleJob actual = settleJobDao.get(sj.getId());
        assertThat(actual.getStatus(), is(JobStatus.FAIL.value()));
    }

    @Test
    public void testGetByDoneAtAndJobType() {
        SettleJob actual = settleJobDao.getByDoneAtAndJobType(sj.getDoneAt(), sj.getType());
        assertThat(actual, is(sj));
    }




}
