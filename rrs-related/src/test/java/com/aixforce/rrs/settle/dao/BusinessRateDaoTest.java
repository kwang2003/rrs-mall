package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.BusinessRate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-04-03 1:45 PM  <br>
 * Author: xiao
 */
public class BusinessRateDaoTest extends BaseDaoTest {

    @Autowired
    private BusinessRateDao businessRateDao;

    private BusinessRate b;

    @Before
    public void setUp() {
        b = new BusinessRate();
        b.setBusiness(1L);
        b.setRate(0.01);
        Long id = businessRateDao.create(b);
        assertThat(b.getId(), notNullValue());
        assertThat(b.getId(), is(id));

        BusinessRate actual = businessRateDao.get(b.getId());
        b.setCreatedAt(actual.getCreatedAt());
        b.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(b));
    }

    @Test
    public void testFindByBusiness() {
        BusinessRate actual = businessRateDao.findByBusiness(1L);
        assertThat(actual, is(b));
    }

}
