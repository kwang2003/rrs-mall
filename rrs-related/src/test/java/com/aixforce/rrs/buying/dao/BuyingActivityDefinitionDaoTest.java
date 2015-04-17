package com.aixforce.rrs.buying.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class BuyingActivityDefinitionDaoTest extends BaseDaoTest {

    @Autowired
    private BuyingActivityDefinitionDao buyingActivityDefinitionDao;

    private BuyingActivityDefinition buyingActivityDefinition;

    @Before
    public void setUp() throws Exception {
        buyingActivityDefinition = new BuyingActivityDefinition();
        buyingActivityDefinition.setStatus(1);
        buyingActivityDefinition.setActivityStartAt(new Date());
        buyingActivityDefinition.setActivityEndAt(new Date());
        buyingActivityDefinition.setActivityName("测试活动");
        buyingActivityDefinition.setOrderEndAt(new Date());
        buyingActivityDefinition.setOrderEndAt(new Date());
        buyingActivityDefinition.setCreatedAt(new Date());
        buyingActivityDefinition.setPayLimit(2);
        buyingActivityDefinition.setSellerId(1l);
        buyingActivityDefinition.setSellerName("name");
        buyingActivityDefinition.setShopId(2l);
        buyingActivityDefinition.setShopName("name");
        buyingActivityDefinition.setBusinessId(1l);
        buyingActivityDefinition.setUpdatedAt(new Date());

        this.testCreate();
    }


    public void testCreate() throws Exception {
       Long id = buyingActivityDefinitionDao.create(buyingActivityDefinition);
       Assert.assertNotNull(id);

    }

    @Test
    public void testDelete() throws Exception {
       Boolean isDel = buyingActivityDefinitionDao.delete(buyingActivityDefinition.getId());
       Assert.assertTrue(isDel);
    }

    @Test
    public void testUpdate() throws Exception {
        BuyingActivityDefinition newBuyingActivityDefinition = new BuyingActivityDefinition();
        newBuyingActivityDefinition.setActivityName("更新名称");
        newBuyingActivityDefinition.setId(buyingActivityDefinition.getId());
        Boolean isUpdate =  buyingActivityDefinitionDao.update(newBuyingActivityDefinition);
        Assert.assertTrue(isUpdate);

    }

    @Test
    public void testFindById() throws Exception {
        BuyingActivityDefinition exitBuyingActivityDefinition = buyingActivityDefinitionDao.findById(buyingActivityDefinition.getId());
        Assert.assertNotNull(exitBuyingActivityDefinition);

    }
}