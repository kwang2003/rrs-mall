package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.AbnormalTrans;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 2:23 PM  <br>
 * Author: xiao
 */
public class AbnormalTransDaoTest extends BaseDaoTest {

    @Autowired
    private AbnormalTransDao abnormalTransDao;
    private AbnormalTrans a;


    private AbnormalTrans mock() {
        AbnormalTrans mock = new AbnormalTrans();
        mock.setSettlementId(1L);
        mock.setOrderId(1L);
        mock.setReason("12345");
        return mock;
    }


    @Before
    public void setUp() {
        a = mock();
        abnormalTransDao.create(a);
        assertThat(a.getId(), notNullValue());
        AbnormalTrans actual = abnormalTransDao.get(a.getId());
        a.setCreatedAt(actual.getCreatedAt());
        a.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(a));
    }


    @Test
    public void test() {}
}
