package com.aixforce.user.mysql;

import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.UserProfile;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-12
 */
public class UserProfileDaoTest extends BaseMysqlDaoTest {
    @Autowired
    private UserProfileDao userProfileDao;

    private UserProfile userProfile;

    @Before
    public void setUp() throws Exception {
        userProfile = newUserProfile();
        userProfileDao.create(userProfile);
    }

    @Test
    public void testFindByUserId() throws Exception {
        UserProfile actual = userProfileDao.findByUserId(userProfile.getUserId());
        assertThat(actual, notNullValue());
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(userProfile.getId(), notNullValue());
    }

    @Test
//    @Ignore
    public void testUpdate() throws Exception {
        UserProfile updated = new UserProfile();
        updated.setId(userProfile.getId());
        updated.setGender(0);
        updated.setUserId(userProfile.getUserId());
        updated.setDescription("change sex");
        userProfileDao.updateByUserId(updated);
        UserProfile actual = userProfileDao.findByUserId(userProfile.getUserId());
        assertThat(actual, notNullValue());
        assertThat(actual.getGender(), is(0));
        assertThat(actual.getDescription(), is("change sex"));
    }

    @Test
    public void testDeleteByUserId() throws Exception {
        userProfileDao.deleteByUserId(userProfile.getUserId());
        UserProfile actual = userProfileDao.findByUserId(userProfile.getUserId());
        assertThat(actual, nullValue());
    }

    private UserProfile newUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(33L);
        //userProfile.setAvatar("x.png");
        userProfile.setGender(1);
        return userProfile;
    }
}
