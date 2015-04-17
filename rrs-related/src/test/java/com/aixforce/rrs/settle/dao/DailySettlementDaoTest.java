package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.DailySettlement;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 3:36 PM  <br>
 * Author: xiao
 */
public class DailySettlementDaoTest extends BaseDaoTest {

    @Autowired
    private DailySettlementDao dailySettlementDao;

    @Before
    public void setUp() throws Exception {
        DailySettlement s = new DailySettlement();
        s.setConfirmedAt(DateTime.now().toDate());
        dailySettlementDao.create(s);
        assertThat(s.getId(), notNullValue());
    }

    @Test
    public void testFindBy() throws Exception {
        DateTime startAt  = DateTime.now().minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = DateTime.now();
        Paging<DailySettlement> actual = dailySettlementDao.findBy(startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(actual.getTotal(),is(1L));
    }

    @Test
    public void testGet() throws Exception {
        DailySettlement actual = dailySettlementDao.get(1L);
        assertThat(actual, notNullValue());
    }
}
