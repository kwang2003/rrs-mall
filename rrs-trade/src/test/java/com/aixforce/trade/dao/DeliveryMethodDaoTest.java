package com.aixforce.trade.dao;

import com.aixforce.trade.model.DeliveryMethod;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 14-9-3
 */

public class DeliveryMethodDaoTest extends BaseDaoTest {

    private DeliveryMethod deliveryMethod;

    @Autowired
    private DeliveryMethodDao deliveryMethodDao;

    @Before
    public void init() {
        deliveryMethod = new DeliveryMethod();
        deliveryMethod.setName("name");
        deliveryMethod.setStatus(1);
        deliveryMethod.setType(2);
        deliveryMethodDao.create(deliveryMethod);
    }

    @Test
    public void testUpdate() {
        deliveryMethod.setName("test");
        deliveryMethod.setStatus(2);
        deliveryMethodDao.update(deliveryMethod);
        DeliveryMethod actual = deliveryMethodDao.findById(deliveryMethod.getId());
        assertThat(actual.getName(), is(deliveryMethod.getName()));
        assertThat(actual.getStatus(), is(deliveryMethod.getStatus()));
    }

    @Test
    public void testFindById() {
        assertThat(deliveryMethodDao.findById(deliveryMethod.getId()).getId(), is(deliveryMethod.getId()));
    }

    @Test
    public void testFindBy() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("status",1);
        assertThat(deliveryMethodDao.findBy(params).size(), is(1));
    }
}
