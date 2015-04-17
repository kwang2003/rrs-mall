package com.aixforce.rrs.buying.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class BuyingOrderRecordDaoTest extends BaseDaoTest {

    @Autowired
    private BuyingOrderRecordDao buyingOrderRecordDao;

    private BuyingOrderRecord buyingOrderRecord;

    @Before
    public void setUp() throws Exception {
        buyingOrderRecord = new BuyingOrderRecord();
        buyingOrderRecord.setOrderId(1l);
        buyingOrderRecord.setItemId(2l);
        buyingOrderRecord.setBuyingActivityId(1l);
        buyingOrderRecord.setBuyerId(2l);
        buyingOrderRecord.setQuantity(23);
        buyingOrderRecord.setItemBuyingPrice(23);
        buyingOrderRecord.setItemOriginPrice(43);
        buyingOrderRecord.setCreatedAt(new Date());
        buyingOrderRecord.setUpdatedAt(new Date());

        this.testCreate();
    }


    public void testCreate() throws Exception {
       Long id = buyingOrderRecordDao.create(buyingOrderRecord);
       Assert.assertNotNull(id);

    }

    @Test
    public void testDelete() throws Exception {
       Boolean isDel = buyingOrderRecordDao.delete(buyingOrderRecord.getId());
       Assert.assertTrue(isDel);
    }

    @Test
    public void testUpdate() throws Exception {
        BuyingOrderRecord newBuyingOrderRecord = new BuyingOrderRecord();
        newBuyingOrderRecord.setBuyingActivityId(1l);
        newBuyingOrderRecord.setId(buyingOrderRecord.getId());
        Boolean isUpdate =  buyingOrderRecordDao.update(newBuyingOrderRecord);
        Assert.assertTrue(isUpdate);

    }

    @Test
    public void testFindById() throws Exception {
        BuyingOrderRecord exitBuyingOrderRecord = buyingOrderRecordDao.findById(buyingOrderRecord.getId());
        Assert.assertNotNull(exitBuyingOrderRecord);

    }
}