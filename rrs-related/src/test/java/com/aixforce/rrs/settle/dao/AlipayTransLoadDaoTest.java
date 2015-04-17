package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.AlipayTransLoad;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 3:00 PM  <br>
 * Author: xiao
 */
public class AlipayTransLoadDaoTest extends BaseDaoTest {

    @Autowired
    private AlipayTransLoadDao alipayTransLoadDao;
    private AlipayTransLoad a;
    private Date now = DateTime.now().toDate();



    private AlipayTransLoad mock() {
        AlipayTransLoad mock = new AlipayTransLoad();
        mock.setQueryStart(now);
        mock.setQueryEnd(now);
        mock.setPageNo(1);
        mock.setPageSize(5000);
        mock.setNext(Boolean.TRUE);
        mock.setStatus(-1);
        return mock;
    }


    @Before
    public void setUp() {
        a = mock();
        alipayTransLoadDao.create(a);
        assertThat(a.getId(), notNullValue());
        AlipayTransLoad actual = alipayTransLoadDao.get(a.getId());
        a.setCreatedAt(actual.getCreatedAt());
        a.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(a));
    }

    @Test
    public void testGet() {
        a.getId();
    }


    @Test
    public void testGetBy() {
        AlipayTransLoad criteria = new AlipayTransLoad();
        criteria.setPageNo(1);
        criteria.setQueryStart(now);
        criteria.setQueryEnd(now);
        AlipayTransLoad actual = alipayTransLoadDao.getBy(criteria);
        assertThat(actual, notNullValue());

    }




}
