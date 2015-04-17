package com.aixforce.user.mysql;

import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.UserQuota;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-13
 */
public class UserQuotaDaoTest extends BaseMysqlDaoTest {

    @Autowired
    private UserQuotaDao userQuotaDao;

    private UserQuota userQuota;

    @Before
    public void setUp() throws Exception {
        userQuota = new UserQuota();
        userQuota.setUserId(1L);
        userQuota.setMaxImageCount(100);
        userQuota.setUsedImageCount(50);
        userQuota.setMaxImageSize(1000L);
        userQuota.setUsedImageSize(500L);
        userQuota.setMaxWidgetCount(600);
        userQuota.setUsedWidgetCount(300);
        userQuotaDao.create(userQuota);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(userQuota.getId(), notNullValue());
    }

    @Test
    public void testFindByUserId() throws Exception {
        UserQuota actual = userQuotaDao.findByUserId(1L);
        assertThat(actual, is(userQuota));
    }

    @Test
    public void testUpdateUsedImageInfo() throws Exception {
        userQuotaDao.updateUsedImageInfo(1L, 20, 200);
        UserQuota actual = userQuotaDao.findByUserId(1L);
        assertThat(actual.getUsedImageCount(), is(70));
        assertThat(actual.getUsedImageSize(), is(700L));
    }

    @Test
    public void testUpdateUsedWidgetCount() throws Exception {
        userQuotaDao.updateUsedWidgetCount(1L, 100);
        UserQuota actual = userQuotaDao.findByUserId(1L);
        assertThat(actual.getUsedWidgetCount(), is(400));
    }

    @Test
    public void testCalculateWidgetCount() throws Exception {
        UserQuota actual = userQuotaDao.findByUserId(1L);
        assertThat(actual.getUsedWidgetCount(), is(300));
    }

    @Test
    public void testCalculateUsedImageInfo() throws Exception {
        userQuotaDao.calculateUsedImageInfo(1L);
        UserQuota actual = userQuotaDao.findByUserId(1L);
        assertThat(actual.getUsedImageSize(), is(0L));
        assertThat(actual.getUsedImageCount(), is(0));
    }

    @Test
    public void testDeleteByUserId() throws Exception {
        userQuotaDao.deleteByUserId(1L);
        assertThat(userQuotaDao.findByUserId(1L), nullValue());
    }
}
