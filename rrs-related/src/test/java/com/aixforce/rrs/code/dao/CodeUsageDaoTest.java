package com.aixforce.rrs.code.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.code.model.CodeUsage;
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
public class CodeUsageDaoTest extends BaseDaoTest {

    @Autowired
    private CodeUsageDao codeUsageDao;

    private CodeUsage codeUsage;

    @Before
    public void setUp() throws Exception {
        codeUsage = new CodeUsage();
        codeUsage.setCode("测试优惠码");
        codeUsage.setActivityId(2l);
        codeUsage.setActivityName("测试活动");
        codeUsage.setBusinessId(1l);
        codeUsage.setBuyerId(1l);
        codeUsage.setBuyerName("q");
        codeUsage.setChannelType(1);
        codeUsage.setSellerId(1l);
        codeUsage.setActivityType(1);
        codeUsage.setSellerName("test");
        codeUsage.setDiscount(1000);
        codeUsage.setOrderId(1l);
        codeUsage.setOriginPrice(200);
        codeUsage.setPrice(2000);
        codeUsage.setUsedCount(1);
        codeUsageDao.create(codeUsage);
    }

    @Test
    public void testDelete(){
        codeUsageDao.delete(codeUsage.getId());
        assertThat(codeUsageDao.findById(codeUsage.getId()), nullValue());

    }

    @Test
    public void testUpdate() throws Exception {
        CodeUsage codeUsage=codeUsageDao.findByName("测试优惠码");
        codeUsage.setCode("测试优惠码2");
        codeUsageDao.update(codeUsage);
        Assert.assertEquals(codeUsageDao.findByName("测试优惠码2").getCode(), "测试优惠码2");

    }
    @Test
    public void testGetCodeUsageByActivityId(){
        Paging<CodeUsage> codeUsagePaging = codeUsageDao.getCodeUsageByActivityId(2l, 0, 5);
        List<CodeUsage> list=codeUsagePaging.getData();
        System.out.print("list size===="+list.size()+"codeUsagePaging.total=="+codeUsagePaging.getTotal());
        for(CodeUsage codeUsage1 :list){
            System.out.print("activityName===="+codeUsage1.getActivityName());
        }

        Assert.assertNotNull(codeUsagePaging.getData());
    }

    @Test
    public void testGetAllCodeUsageByActivityId(){
        List<CodeUsage>  codeUsageList= codeUsageDao.getAllCodeUsageByActivityId(1l);
        Assert.assertNotNull(codeUsageList);
    }

    @Test
    public void testGetCodeUsageByOrderId(){
        CodeUsage codeUsage = codeUsageDao.getCodeUsageByOrderId(1l);
        Assert.assertNotNull(codeUsage);
    }



}
