package com.aixforce.user.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.User;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-12
 */
public class UserDaoTest extends BaseMysqlDaoTest {
    private int sequence = 0;
    @Autowired
    private UserDao userDao;

    User user;

    @Before
    public void setUp() throws Exception {
        user = newUser();
        userDao.create(user);
    }

    @Test
    public void testFindById() throws Exception {
        User actual = userDao.findById(user.getId());
        assertThat(actual, notNullValue());
        assertThat(actual, is(user));
    }

    @Test
    public void testFindByEmail() throws Exception {
        User actual = userDao.findByEmail(user.getEmail());
        assertThat(actual, notNullValue());
        assertThat(actual, is(user));
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(user.getId(), notNullValue());
    }

    @Test
    public void testDelete() throws Exception {
        assertThat(userDao.delete(user.getId()), is(1));
    }

    @Test
    public void testUpdate() throws Exception {
        user.setStatus(1);
        user.setName("name0");
        assertThat(userDao.update(user), is(true));
        User actual = userDao.findById(user.getId());
        assertThat(actual.getStatus(), is(1));
        assertThat(actual.getName(), is("name0"));
    }

    @Test
    public void testPagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            User user = newUser();
            userDao.create(user);
        }
        Paging<User> pageOne = userDao.findUsers(0, 0, 4);
        assertThat(pageOne.getTotal(), is(6L));
        assertThat(pageOne.getData().size(), is(4));

        Paging<User> pageTwo = userDao.findUsers(0, 4, 4);
        assertThat(pageTwo.getTotal(), is(6L));
        assertThat(pageTwo.getData().size(), is(2));

    }

    @Test
    public void testBatchUpdate() {
        User user1 = new User();
        user1.setStatus(0);
        user1.setName("name1");
        user1.setEncryptedPassword("test1");
        user1.setType(1);
        userDao.create(user1);
        userDao.batchUpdateStatus(Lists.newArrayList(user.getId(), user1.getId()), 1);
        User actual = userDao.findById(user.getId());
        User actual1 = userDao.findById(user1.getId());
        assertThat(actual.getStatus(), is(1));
        assertThat(actual1.getStatus(), is(1));
    }

    @Test
    public void testCountByType() {
        assertThat(userDao.findByTypes(0,10,Lists.newArrayList(1)).getTotal(), is(1l));
    }

    private User newUser() {
        User user = new User();
        int s = sequence++;
        user.setEmail("a" + s + "@example.com");
        user.setName("name" + s);
        user.setStatus(0);
        user.setEncryptedPassword("heihei");
        user.setType(1);
        return user;
    }
}
