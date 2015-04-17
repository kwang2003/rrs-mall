package com.aixforce.rrs.code.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.code.dto.RichActivityDefinition;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBean;

import java.util.List;
import java.util.Map;

public class ActivityDefinitionServiceImplTest extends BaseServiceTest{

    @SpringBean("activityDefinitionServiceImpl")
    ActivityDefinitionService actDefService;

    Long createdId = null;

    @Before
    public void setUp() throws Exception {
        ActivityDefinition actDef = new ActivityDefinition();
        actDef.setActivityType(1);
        actDef.setActivityDesc("活动");
        actDef.setActivityName("活动");
        actDef.setBusinessId(1l);
        actDef.setChannelType(1);
        actDef.setDiscount(100000);
        actDef.setOrderCount(100);
        actDef.setStatus(1);
        actDef.setStock(10000000);
        actDef.setUseLimit(10000);

        actDef.setStartAt(new DateTime(DateTime.now().toDate()).withTimeAtStartOfDay().toDate());
        actDef.setEndAt(DateTime.now().withDayOfMonth(28).toDate());

        List<String> codes = Lists.newArrayList();
        codes.add(null);
        codes.add("");
        codes.add("  ");
        for(int i=1;i<=500;i++)
            codes.add("CODE_"+i);


        createdId = actDefService.create(actDef, codes, Lists.newArrayList(1l), null).getResult();

        Assert.assertTrue(createdId >= 1);
        System.out.println(createdId+" was created!");

    }

    @Test
    public void testCreate() throws Exception {

    }

    @Test
    public void testFindActivityDefinitionById() throws Exception {

    }

    @Test
    public void testFindActivityDefinitionByPaging() throws Exception {
        Map<String, Object> param = Maps.newHashMap();
        param.put("activityName", "活动");
        param.put("activityType", "1");
        Response<Paging<RichActivityDefinition>> foundList = actDefService.findActivityDefinitionByPaging(param, 1, 20);
        Assert.assertTrue("One record must found", foundList.getResult().getTotal()>=1);
    }

    @Test
    public void testUpdate() throws Exception {
        ActivityDefinition actDef = new ActivityDefinition();
        actDef.setId(createdId);
        actDef.setActivityName("NEW NAME");
        Response<Integer> updated = actDefService.update(actDef, ImmutableList.of(1l,2l), null);

        Assert.assertTrue("One reocrd must be updated", updated.getResult()==1);

    }

    @Test
    public void testDeleteActivityDefinitionByIds() throws Exception {
           Response<Integer> deled = actDefService.deleteActivityDefinitionByIds(ImmutableList.of(createdId, 1l));
            Assert.assertTrue("One record must be deleted", deled.getResult()==1);
    }
}