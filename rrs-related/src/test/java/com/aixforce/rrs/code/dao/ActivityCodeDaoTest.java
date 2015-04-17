package com.aixforce.rrs.code.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityCodeDaoTest extends BaseDaoTest{

    @Autowired
    private ActivityCodeDao activityCodeDao;

    private Long createdId = null;


    @Test
    public void testFindAllBy() throws Exception {
        java.util.List<ActivityCode> activityCodeDaoAllBy =
                activityCodeDao.findAllBy(ImmutableMap.<String, Object>of("activityName", "优惠活动N"));
        Assert.assertTrue("Result number must be 1", activityCodeDaoAllBy.size()>=1);
    }

    @Test
    public void testFindByCode() throws Exception {
        java.util.List<ActivityCode> activityCodes = activityCodeDao.findByCode("HAHA");
        Assert.assertTrue("Find by code must be null", activityCodes.size()>=1);
    }

    @Test
    public void testCountUsageByActivityId() throws Exception {
        Integer count = activityCodeDao.countUsageByActivityId(1l);
        System.err.println("usage: "+count);
        Assert.assertTrue("Must return not null", count!=null);
    }


    @Test
    public void testActivityIdsByCode(){
        List<Long> ids = activityCodeDao.findActivityIdsByCode("HAHA");
        Assert.assertTrue("Result len must greater than 0", ids.size()>0);
    }

    @Test
    public void testUpdateUsageById() throws Exception {
        int updated = activityCodeDao.updateUsageById(
                ImmutableMap.<String, Object>of("id", createdId, "usage", 666));
        Assert.assertTrue("At least one be updated", updated>0);
    }


    @Test
    public void findOneByActivityIdAndCode(){
        ActivityCode code = activityCodeDao.findOneByActivityIdAndCode(1l, "HAHA");
        Assert.assertTrue("Must equals HAHA", code.getCode().equals("HAHA"));
    }

    @Test
    @Transactional()
    public void testDeleteByActivityIds() throws Exception {
        int deled = activityCodeDao.deleteByActivityIds(ImmutableList.<Long>of(1l));
        Assert.assertTrue("At least one should be removed", deled>0);
    }

    @Before
    public void setUp() throws Exception {
        ActivityCode activityCode = new ActivityCode();
        activityCode.setActivityId(1l);
        activityCode.setActivityName("优惠活动N");
        activityCode.setActivityType(ActivityDefinition.ActivityType.PUBLIC_CODE.value());
        activityCode.setCode("HAHA");
        activityCode.setUsage(0);
        createdId = activityCodeDao.create(activityCode);
        Assert.assertTrue("New created id must greater than 1", createdId >= 1);
        System.out.println("Created Id is: " + createdId);
    }

    @Test
    public void testCreate() throws Exception {

    }

    @Test
    public void testFindById() throws Exception {
        ActivityCode activityCodeDaoById = activityCodeDao.findById(createdId);
        Assert.assertTrue("ActName must equals ", activityCodeDaoById.getActivityName().equals("优惠活动N"));
    }

    @Test
    public void testFindBy() throws Exception {
        Map param = new HashMap();
        //param.put("activityId", 1l);
        //Assert.assertTrue(activityCodeDao.findByPaging(param).getData().size() >= 1);
        System.err.println(">>> " + activityCodeDao.findByPaging(param).getData());
    }

    @Test
    public void testUpdate() throws Exception {
        ActivityCode activityCode = new ActivityCode();
        activityCode.setId(createdId);
        activityCode.setActivityId(1l);
        activityCode.setActivityName("优惠活动N+1");
        int updated = activityCodeDao.update(activityCode);
        Assert.assertTrue("更新失败", updated==1);
        Assert.assertTrue("更新失败",activityCodeDao.findById(createdId.longValue()).getActivityName().equals("优惠活动N+1"));
    }

    @Test
    public void testDeleteByIds() throws Exception {
        int deleteByIds = activityCodeDao.deleteByIds(ImmutableList.of(createdId));
        Assert.assertTrue("删除失败", activityCodeDao.findById(createdId)==null);
    }


    @Test
    public void testFindActivityIdsByCode() throws Exception {
        Assert.assertNotNull(activityCodeDao.findActivityIdsByCode("HAHA"));
    }
}