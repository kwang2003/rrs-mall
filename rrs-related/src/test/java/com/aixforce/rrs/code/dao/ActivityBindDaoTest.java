package com.aixforce.rrs.code.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.code.model.ActivityBind;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by songrenfei on 14-7-4.
 */
public class ActivityBindDaoTest extends BaseDaoTest {

    @Autowired
    private ActivityBindDao activityBindDao;

    private ActivityBind activityBind;

    @Before
    public void setUp() throws Exception {
        activityBind=new ActivityBind();
        activityBind.setActivityId(1l);
        activityBind.setTargetType(1);
        activityBind.setTargetId(1l);
        activityBindDao.create(activityBind);
    }

    @Test
    public void testDelete(){
        activityBindDao.delete(activityBind.getId());
        assertThat(activityBindDao.findById(activityBind.getId()), nullValue());

    }

    @Test
    public void testUpdate() throws Exception {
        ActivityBind bind= activityBindDao.findByActivityId(1l);
        bind.setTargetId(4l);
        Assert.assertTrue(activityBindDao.update(bind));

    }
    @Test
    public void testFindByActivityId(){
        ActivityBind bind= activityBindDao.findByActivityId(1L);
        Assert.assertNotNull(bind);
    }

    @Test
    public void testFindBindIdsByCodeName(){
        List<Long> ids= activityBindDao.findBindIdsByActivityId(1l, 1);
        Assert.assertNotNull(ids);
    }

    @Test
    public void testDeleteActivityBindByActivityId(){
        activityBindDao.deleteActivityBindByActivityId(1l);
       // Assert.assertNotNull(ids);
    }





}
