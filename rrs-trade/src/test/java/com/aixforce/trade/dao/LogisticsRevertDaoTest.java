package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsRevert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by chaopeng on 14-4-21.
 */

public class LogisticsRevertDaoTest extends BaseDaoTest {

    @Autowired
    private LogisticsRevertDao logisticsRevertDao;

    private LogisticsRevert logisticsRevert;

    private void mock() {
        logisticsRevert = new LogisticsRevert();
        logisticsRevert.setOrderItemId(1L);
        logisticsRevert.setCompanyName("天天");
        logisticsRevert.setFreightNote("123457");
        logisticsRevert.setLogisticsStatus(LogisticsRevert.Status.REJECT.value());
        logisticsRevert.setBuyerId(1L);
        logisticsRevert.setBuyerName("jack");
        logisticsRevert.setSendFee(200);
    }

    @Before
    public void setUp() {
        mock();
        logisticsRevertDao.create(logisticsRevert);
    }

    @Test
    public void testCreate() {
        Assert.assertNotNull(logisticsRevert.getId());
    }

    @Test
    public void testFindById() {
        LogisticsRevert expected = logisticsRevertDao.findById(logisticsRevert.getId());
        Assert.assertEquals(expected.getId(), logisticsRevert.getId());
    }

    @Test
    public void testDelete() {
        logisticsRevertDao.delete(logisticsRevert.getId());
        LogisticsRevert expected = logisticsRevertDao.findById(logisticsRevert.getId());
        Assert.assertNull(expected);
    }

    @Test
    public void testUpdate() {

        LogisticsRevert updateModel = new LogisticsRevert();
        updateModel.setId(logisticsRevert.getId());
        updateModel.setCompanyName("upd");
        logisticsRevertDao.update(updateModel);

        LogisticsRevert findModel = logisticsRevertDao.findById(logisticsRevert.getId());

        Assert.assertEquals(findModel.getCompanyName(), updateModel.getCompanyName());
    }

    @Test
    public void testFindByOrderItemId() {
        LogisticsRevert model = logisticsRevertDao.findByOrderItemId(logisticsRevert.getOrderItemId());
        Assert.assertEquals(model.getId(), logisticsRevert.getId());
    }

}
