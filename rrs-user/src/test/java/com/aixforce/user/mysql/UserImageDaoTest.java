package com.aixforce.user.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.user.BaseMysqlDaoTest;
import com.aixforce.user.model.UserImage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-13
 */
public class UserImageDaoTest extends BaseMysqlDaoTest {

    @Autowired
    private UserImageDao userImageDao;

    private UserImage first;

    private UserImage second;

    private UserImage third;

    @Before
    public void setUp() throws Exception {
        first = newUserImage(11L, null, "a.jpg", 110);
        second = newUserImage(11L, null, "b.jpg", 220);
        third = newUserImage(11L, "item:1", "c.jpg", 330);
        userImageDao.create(first);
        userImageDao.create(second);
        userImageDao.create(third);
    }

    @Test
    public void testFindById() throws Exception {
        assertThat(userImageDao.findById(first.getId()), is(first));
    }

    @Test
    public void testTotalCountOf() throws Exception {
        assertThat(userImageDao.totalCountOf(first.getUserId()), is(3));
    }

    @Test
    public void testFindByUserIdAndCategory() throws Exception {
        Paging<UserImage> images = userImageDao.findByUserIdAndCategory(first.getUserId(), null, 0, 10);
        assertThat(images.getTotal(), is(2L));
        assertThat(images.getData(), contains(second, first));
        images = userImageDao.findByUserIdAndCategory(first.getUserId(), "item:1", 0, 2);
        assertThat(images.getTotal(), is(1L));
        assertThat(images.getData(), contains(third));
    }


    @Test
    public void testCreate() throws Exception {
        assertThat(first.getId(), notNullValue());
        assertThat(second.getId(), notNullValue());
        assertThat(third.getId(), notNullValue());
    }

    @Test
    public void testDelete() throws Exception {
        userImageDao.delete(first.getId());
        assertThat(userImageDao.findById(first.getId()), nullValue());
        assertThat(userImageDao.findByUserIdAndCategory(first.getUserId(), null, 0, 3).getTotal(), is(1L));
    }

    @Test
    public void testTotalSize() throws Exception {
        long total = userImageDao.totalSizeByUserId(first.getUserId());
        assertThat(total, is((long) first.getFileSize() + second.getFileSize() + third.getFileSize()));

    }

    private UserImage newUserImage(long userId, String category, String fileName, int fileSize) {
        UserImage userImage = new UserImage();
        userImage.setUserId(userId);
        userImage.setCategory(category);
        userImage.setFileName(fileName);
        userImage.setFileSize(fileSize);
        return userImage;
    }
}
