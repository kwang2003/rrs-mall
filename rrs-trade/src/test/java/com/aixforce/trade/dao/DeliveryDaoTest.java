/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.trade.model.Delivery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class DeliveryDaoTest extends BaseDaoTest {

    @Autowired
    private DeliveryDao deliveryDao;

    private Delivery delivery;

    @Before
    public void setUp() throws Exception {
        delivery = new Delivery();
        delivery.setOrderId(1L);
        delivery.setCompany(1);
        delivery.setTrackCode("world");
        delivery.setType(1);
        deliveryDao.create(delivery);
    }

    @Test
    public void testFindById() throws Exception {
        Delivery actual = deliveryDao.findById(delivery.getId());
        assertThat(actual, notNullValue());
        assertThat(actual, is(delivery));
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(delivery.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {

        deliveryDao.update(delivery.getId(), "alibaba");
        Delivery actual = deliveryDao.findById(delivery.getId());
        assertThat(actual.getOrderId(), is(1L));
        assertThat(actual.getCompany(), is(1));
        assertThat(actual.getTrackCode(), is("alibaba"));
        assertThat(actual.getType(), is(1));
    }

    @Test
    public void testDelete() throws Exception {
        deliveryDao.delete(delivery.getId());
        assertThat(deliveryDao.findById(delivery.getId()), nullValue());
    }
}
