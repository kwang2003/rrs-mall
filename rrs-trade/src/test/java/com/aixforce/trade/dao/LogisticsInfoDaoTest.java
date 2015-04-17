package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by chaopeng on 14-4-21.
 */

public class LogisticsInfoDaoTest extends BaseDaoTest {

    @Autowired
    private LogisticsInfoDao logisticsInfoDao;

    private LogisticsInfo logisticsInfo;

    private void mock() {
        logisticsInfo = new LogisticsInfo();
        logisticsInfo.setOrderId(1L);
        logisticsInfo.setCompanyName("天天");
        logisticsInfo.setFreightNote("123457");
        logisticsInfo.setLogisticsStatus(LogisticsInfo.Status.SEND.value());
        logisticsInfo.setSenderId(32L);
        logisticsInfo.setSenderName("jack");
        logisticsInfo.setSendFee(200);
    }

    @Before
    public void setUp() {
        mock();
        logisticsInfoDao.create(logisticsInfo);
    }

    @Test
    public void testCreate() {
        Assert.assertNotNull(logisticsInfo.getId());
    }

    @Test
    public void testFindById() {
        LogisticsInfo expected = logisticsInfoDao.findById(logisticsInfo.getId());
        Assert.assertEquals(expected.getId(), logisticsInfo.getId());
    }

    @Test
    public void testDelete() {
        logisticsInfoDao.delete(logisticsInfo.getId());
        LogisticsInfo expected = logisticsInfoDao.findById(logisticsInfo.getId());
        Assert.assertNull(expected);
    }

    @Test
    public void testUpdateByOrderId() {
        LogisticsInfo updateModel = new LogisticsInfo();
        updateModel.setOrderId(logisticsInfo.getOrderId());
        updateModel.setCompanyName("upd");
        logisticsInfoDao.updateByOrderId(updateModel);

        LogisticsInfo findModel = logisticsInfoDao.findById(logisticsInfo.getId());

        Assert.assertEquals(findModel.getCompanyName(), updateModel.getCompanyName());
    }

    @Test
    public void testFindByOrderId() {
        LogisticsInfo model = logisticsInfoDao.findByOrderId(logisticsInfo.getOrderId());
        Assert.assertEquals(model.getId(), logisticsInfo.getId());
    }

}
