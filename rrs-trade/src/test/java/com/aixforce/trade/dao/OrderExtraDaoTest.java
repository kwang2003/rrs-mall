package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrderExtra;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
public class OrderExtraDaoTest extends BaseDaoTest {
    @Autowired
    private OrderExtraDao orderExtraDao;

    private OrderExtra orderExtra;

    @Before
    public void setUp() throws Exception {
        orderExtra = new OrderExtra();
        orderExtra.setBuyerNotes("buyer notes");
        orderExtra.setOrderId(1L);
        orderExtra.setInvoice("invoice detail");
        orderExtraDao.create(orderExtra);
    }

    @Test
    public void testCreate() throws Exception {

        assertThat(orderExtra.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        OrderExtra u = new OrderExtra();
        u.setOrderId(orderExtra.getOrderId());
        u.setInvoice("123");
        orderExtraDao.updateByOrderId(u);
        OrderExtra actual = orderExtraDao.findByOrderId(orderExtra.getOrderId());
        assertThat(actual, notNullValue());
        assertThat(actual.getInvoice(), is(u.getInvoice()));
    }

    @Test
    public void testFindByOrderId() throws Exception {
        OrderExtra actual = orderExtraDao.findByOrderId(orderExtra.getOrderId());
        assertThat(actual, notNullValue());
    }
}
