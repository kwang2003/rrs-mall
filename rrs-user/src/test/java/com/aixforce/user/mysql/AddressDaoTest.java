package com.aixforce.user.mysql;

import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.Address;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-14
 */
public class AddressDaoTest extends BaseMysqlDaoTest {

    @Autowired
    private AddressDao addressDao;

    Address address;

    @Before
    public void setUp() {
        address = new Address();
        address.setName("测试地址");
        address.setId(1);
        address.setLevel(1);
        address.setParentId(0);
        addressDao.create(address);
        assertThat(address.getId(), notNullValue());
    }


    @Test
    public void testFindById() throws Exception {
        Address address = addressDao.findById(1);
        assertThat(address, notNullValue());
    }

    @Test
    public void testFindByParentId() throws Exception {
        assertThat(addressDao.findByParentId(1), notNullValue());
    }

    @Test
    public void testFindByLevel() throws Exception {
        List<Address> byLevel = addressDao.findByLevel(1);
        assertThat(byLevel, notNullValue());
        assertThat(byLevel.size(), greaterThan(0));
    }
}
