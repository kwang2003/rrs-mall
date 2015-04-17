package com.aixforce.rrs.code.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ActivityDefinitionDaoTest extends BaseDaoTest {

    @Autowired
    private ActivityDefinitionDao activityDefinitionDao;

    @Autowired
    private ActivityCodeDao activityCodeDao;

    private ActivityDefinition activityDefinition;
    private Long createdId = null;

    @Before
    public void setUp() throws Exception {
        activityDefinition = new ActivityDefinition();
        activityDefinition.setActivityType(1);
        activityDefinition.setActivityDesc("活动");
        activityDefinition.setActivityName("活动");
        activityDefinition.setBusinessId(1l);
        activityDefinition.setChannelType(1);
        activityDefinition.setDiscount(100000);
        activityDefinition.setEndAt(new Date());
        activityDefinition.setOrderCount(100);
        activityDefinition.setStartAt(new Date());
        activityDefinition.setStatus(1);
        activityDefinition.setStock(10000000);
        activityDefinition.setUseLimit(10000);
        createdId = activityDefinitionDao.create(activityDefinition);
        Assert.assertTrue(createdId>=1);
        System.out.println(createdId+" was created!");
    }

    @Test
    public void testCreate() throws Exception {

    }

    @Test
    public void testFindById() throws Exception {
        System.out.println("结果"+activityDefinitionDao.findByPaging(Maps.<String, Object>newHashMap()).getData());
        ActivityDefinition activityDefinition = activityDefinitionDao.findById(createdId+0l);
        Assert.assertTrue("Result name must be `活动`", activityDefinition.getActivityName().equals("活动"));
    }

    @Test
    public void testFindByIds() throws Exception {
        List<ActivityDefinition> activityDefinitionDaoByIds = activityDefinitionDao.findByIds(Lists.newArrayList(1l, 2l, 3l));
        Assert.assertTrue("Result must >= 1", activityDefinitionDaoByIds.size()>=1);
    }

    @Test
    public void testFindByPaging() throws Exception {
        Map<String, Object> param = Maps.newHashMap();
        param.put("activityName", "活动");
        param.put("offset", 0);
        param.put("limit", 20);
        Paging<ActivityDefinition> findByCond = activityDefinitionDao.findByPaging(param);
        Assert.assertTrue("Result can not be empty", findByCond.getData()!=null && findByCond.getTotal()>0);
    }

    @Test
    public void testUpdate() throws Exception {
        System.out.println("created Id is: "+createdId);
        ActivityDefinition activityDefinition = new ActivityDefinition();
        activityDefinition.setId(createdId.longValue());
        activityDefinition.setActivityType(1);
        activityDefinition.setActivityDesc("活动[updated]");
        activityDefinition.setActivityName("活动[updated]");
        int updatedNum = activityDefinitionDao.update(activityDefinition);
        Assert.assertTrue("1 record must to be updated", updatedNum==1);
        Assert.assertTrue("1 record must to be updated", activityDefinitionDao.findById(createdId+0l).getActivityName().equals("活动[updated]"));
    }

    @Test
    public void testDeleteByIds() throws Exception {
        int deleteByIds = activityDefinitionDao.deleteByIds(ImmutableList.of(createdId));
        Assert.assertTrue("1 record must been removed", deleteByIds==1);
    }

    @Test
    public void testFindValidActivityDefinitionsByIds() throws Exception {
        List<Long> list=Lists.newArrayList();

        ActivityCode activityCode = new ActivityCode();
        activityCode.setActivityId(activityDefinition.getId());
        activityCode.setActivityName("优惠活动N");
        activityCode.setActivityType(ActivityDefinition.ActivityType.PUBLIC_CODE.value());
        activityCode.setCode("HAHA");
        activityCode.setUsage(0);
        activityCodeDao.create(activityCode);
        createdId = activityCodeDao.create(activityCode);
        Assert.assertTrue("New created id must greater than 1", createdId >= 1);
       list.add(activityCode.getId());
       // activityDefinitionDao.findValidByIds(list);
        Assert.assertNotNull( activityDefinitionDao.findValidByIds(list));
    }


    @Test
    public void testUpdateToExpiryByHand() throws Exception {
        Boolean isupdate= activityDefinitionDao.updateToExpiryByHand(activityDefinition.getId());
        Assert.assertTrue(isupdate);
    }
}