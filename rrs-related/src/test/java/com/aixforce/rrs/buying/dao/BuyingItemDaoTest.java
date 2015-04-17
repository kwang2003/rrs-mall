package com.aixforce.rrs.buying.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.buying.model.BuyingItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class BuyingItemDaoTest extends BaseDaoTest {

    @Autowired
    private BuyingItemDao buyingItemDao;

    private BuyingItem buyingItem;

    @Before
    public void setUp() throws Exception {
        buyingItem = new BuyingItem();
        buyingItem.setBuyLimit(2);
        buyingItem.setBuyingActivityId(1l);
        buyingItem.setDiscount(10);
        buyingItem.setFakeSoldQuantity(20);
        buyingItem.setIsStorage(false);
        buyingItem.setItemBuyingPrice(100);
        buyingItem.setItemOriginPrice(200);
        buyingItem.setItemId(2l);
        buyingItem.setCreatedAt(new Date());
        buyingItem.setUpdatedAt(new Date());

        this.testCreate();
    }


    public void testCreate() throws Exception {
       Long id = buyingItemDao.create(buyingItem);
       Assert.assertNotNull(id);

    }

    @Test
    public void testDelete() throws Exception {
       Boolean isDel = buyingItemDao.delete(buyingItem.getId());
       Assert.assertTrue(isDel);
    }

    @Test
    public void testUpdate() throws Exception {
        BuyingItem newBuyingItem = new BuyingItem();
        newBuyingItem.setBuyLimit(1);
        newBuyingItem.setId(buyingItem.getId());
        Boolean isUpdate =  buyingItemDao.update(newBuyingItem);
        Assert.assertTrue(isUpdate);

    }

    @Test
    public void testFindById() throws Exception {
        BuyingItem exitBuyingItem = buyingItemDao.findById(buyingItem.getId());
        Assert.assertNotNull(exitBuyingItem);

    }

    @Test
    public void testFindByActivityId() throws Exception {

        List<BuyingItem> buyingItemList = buyingItemDao.findByActivityId(buyingItem.getBuyingActivityId());
        Assert.assertNotNull(buyingItemList);
    }
}