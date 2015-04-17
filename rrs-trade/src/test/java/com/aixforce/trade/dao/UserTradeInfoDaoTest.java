package com.aixforce.trade.dao;

import com.aixforce.trade.model.UserTradeInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-12
 */
public class UserTradeInfoDaoTest extends BaseDaoTest {
    @Autowired
    private UserTradeInfoDao userTradeInfoDao;

    private UserTradeInfo userTradeInfo;

    @Before
    public void setUp() throws Exception {
        userTradeInfo = newUserTradeInfo();
        userTradeInfoDao.create(userTradeInfo);
    }

    @Test
    public void testFindByUserId() throws Exception {
        List<UserTradeInfo> actual = userTradeInfoDao.findValidByUserId(userTradeInfo.getUserId());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(userTradeInfo));
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(userTradeInfo.getId(), notNullValue());
    }

    @Test
    public void testDelete() throws Exception {
        userTradeInfoDao.delete(userTradeInfo.getId());
        assertThat(userTradeInfoDao.findValidByUserId(userTradeInfo.getUserId()).size(), is(0));
    }

    @Test
    public void testUpdate() throws Exception {
        UserTradeInfo updated = new UserTradeInfo();
        updated.setId(userTradeInfo.getId());
        updated.setName("asdf");
        userTradeInfoDao.update(updated);
        assertThat(userTradeInfoDao.findById(updated.getId()).getName(), is("asdf"));
    }

    @Test
    public void testCountOf() throws Exception {
        UserTradeInfo second = newUserTradeInfo();
        userTradeInfoDao.create(second);

        assertThat(userTradeInfoDao.countOf(userTradeInfo.getUserId()), is(2));
    }

    private UserTradeInfo newUserTradeInfo() {
        UserTradeInfo userTradeInfo = new UserTradeInfo();
        userTradeInfo.setName("jlchen");
        userTradeInfo.setUserId(22L);
        userTradeInfo.setProvince("浙江省");
        userTradeInfo.setCity("杭州市");
        userTradeInfo.setDistrict("西湖区");
        userTradeInfo.setZip("310000");
        userTradeInfo.setPhone("110-12345");
        userTradeInfo.setStatus(1);
        userTradeInfo.setIsDefault(1);
        return userTradeInfo;
    }

    @Test
    public void testFindValidByUserId() {
        assertThat(userTradeInfoDao.findValidByUserId(22l).size(), is(1));
    }

    @Test
    public void testFindDefault(){

        UserTradeInfo oldUserTradeInfo = userTradeInfoDao.findDefault(userTradeInfo.getUserId());
        Assert.assertNotNull(oldUserTradeInfo);
    }
}
