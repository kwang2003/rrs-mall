package com.aixforce.user.mysql;

import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.UserExtra;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 14-3-4
 */
public class UserExtraDaoTest extends BaseMysqlDaoTest {

    @Autowired
    private UserExtraDao userExtraDao;

    private UserExtra userExtra;

    @Before
    public void init() {
        userExtra = factory();
        userExtraDao.create(userExtra);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(userExtra, notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        userExtra.setTradeQuantity(50);
        userExtraDao.update(userExtra);
        UserExtra actual = userExtraDao.findById(userExtra.getId());
        assertThat(actual.getTradeQuantity(), is(50));
    }

    @Test
    @Ignore
    public void testFindById() throws Exception {

    }

    @Test
    public void testFindByUserId() throws Exception {
        assertThat(userExtraDao.findByUserId(1l), notNullValue());
    }

    private UserExtra factory() {
        UserExtra userExtra = new UserExtra();
        userExtra.setUserId(1l);
        userExtra.setTradeQuantity(10);
        userExtra.setTradeSum(20l);
        return userExtra;
    }
}
