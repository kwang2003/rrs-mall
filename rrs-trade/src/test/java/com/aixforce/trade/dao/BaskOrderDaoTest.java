package com.aixforce.trade.dao;

import com.aixforce.trade.model.BaskOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class BaskOrderDaoTest extends BaseDaoTest {

    @Autowired
    private BaskOrderDao baskOrderDao;

    private BaskOrder baskOrder;

    @Before
    public void setUp() throws Exception {
        baskOrder = new BaskOrder();
        baskOrder.setOrderCommentId(1l);
        baskOrder.setItemId(1l);
        baskOrder.setOrderItemId(1l);
        baskOrder.setContent("晒单内容");
        baskOrder.setPic("晒单图片");
        baskOrder.setUserName("songrenfei");
        baskOrderDao.create(baskOrder);

    }

    @Test
    public void testDelete() throws Exception {
       Boolean isDeleted = baskOrderDao.delete(baskOrder.getId());
        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testUpdate() throws Exception {
        BaskOrder newBaskOrder = new BaskOrder();
        newBaskOrder.setId(baskOrder.getId());
        newBaskOrder.setPic("更新");
        Boolean isUpdate =  baskOrderDao.update(newBaskOrder);
        Assert.assertTrue(isUpdate);
    }

    @Test
    public void testFindById() throws Exception {
        BaskOrder exitBaskOrder = baskOrderDao.findById(baskOrder.getId());
        Assert.assertNotNull(exitBaskOrder);

    }

    @Test
    public void testFindByOrderCommentId() throws Exception {
        BaskOrder exitBaskOrder = baskOrderDao.findById(baskOrder.getOrderCommentId());
        Assert.assertNotNull(exitBaskOrder);

    }

    @Test
    public void testFindByOrderItemId() throws Exception {
        BaskOrder exitBaskOrder = baskOrderDao.findById(baskOrder.getOrderItemId());
        Assert.assertNotNull(exitBaskOrder);

    }
}