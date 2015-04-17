/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrderItem;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class OrderItemDaoTest extends BaseDaoTest {

    @Autowired
    private OrderItemDao orderItemDao;

    private OrderItem orderItem;

    private Date now = DateTime.now().toDate();

    private void tearDown(Long id) {
        orderItemDao.delete(id);
    }

    private OrderItem mock() {
        OrderItem mock = new OrderItem();
        mock.setOrderId(1L);
        mock.setItemId(33L);
        mock.setSellerId(3L);
        mock.setBuyerId(33L);
        mock.setSkuId(22L);
        mock.setFee(1);
        mock.setQuantity(1);
        mock.setDiscount(1);
        mock.setBrandId(1l);
        mock.setPaidAt(now);
        mock.setChannel("1111");
        return mock;
    }

    @Before
    public void setUp() throws Exception {
        orderItem = mock();
        orderItemDao.create(orderItem);
    }


    @Test
    public void testFindByOrderId() throws Exception {
        OrderItem item1 = new OrderItem();
        item1.setOrderId(1L);
        orderItemDao.create(item1);

        OrderItem item2 = new OrderItem();
        item2.setOrderId(2L);
        orderItemDao.create(item2);

        List<OrderItem> orderItems = orderItemDao.findByOrderId(1L);
        assertThat(orderItems, contains(orderItem, item1));
    }

    @Test
    public void testFindInOrders() {
        OrderItem item1 = new OrderItem();
        item1.setOrderId(98L);
        orderItemDao.create(item1);


        OrderItem item2 = new OrderItem();
        item2.setOrderId(99L);
        orderItemDao.create(item2);

        List<OrderItem> items = orderItemDao.
                findInOrderIds(item1.getOrderId(), item2.getOrderId());
        assertThat(items, contains(item1, item2));
    }

    @Test
    public void testFindItemNameOfOrders() {
        OrderItem item1 = new OrderItem();
        item1.setOrderId(98L);
        item1.setItemName("测试商品1");
        orderItemDao.create(item1);


        OrderItem item2 = new OrderItem();
        item2.setOrderId(99L);
        item2.setItemName("测试商品2");
        orderItemDao.create(item2);

        List<String> titles = orderItemDao.
                findItemNameOfOrders(item1.getOrderId(), item2.getOrderId());
        assertThat(titles, contains("测试商品1", "测试商品2"));
    }

    @Test
    public void testFindById() throws Exception {
        OrderItem actual = orderItemDao.findById(orderItem.getId());
        assertThat(actual, notNullValue());

        orderItem.setCreatedAt(actual.getCreatedAt());
        orderItem.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(orderItem));
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(orderItem.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setFee(22);
        updated.setDiscount(10);
        Date requestDate = new Date();
        updated.setRequestRefundAt(requestDate);
        orderItemDao.update(updated);
        OrderItem actual = orderItemDao.findById(orderItem.getId());
        assertThat(actual.getOrderId(), is(1L));
        assertThat(actual.getItemId(), is(33L));
        assertThat(actual.getSkuId(), is(22L));
        assertThat(actual.getFee(), is(22));
        assertThat(actual.getQuantity(), is(1));
        assertThat(actual.getDiscount(), is(10));
        assertThat(actual.getRequestRefundAt(), is(requestDate));
    }

    @Test
    public void testDelete() throws Exception {
        orderItemDao.delete(orderItem.getId());
        assertThat(orderItemDao.findById(orderItem.getId()), nullValue());
    }

    @Test
    public void testBatchUpdateStatus() throws Exception {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrderId(1L);
        orderItem1.setItemId(33L);
        orderItem1.setSkuId(22L);
        orderItem1.setFee(1);
        orderItem1.setQuantity(1);
        orderItem1.setDiscount(1);
        orderItemDao.create(orderItem1);
        orderItemDao.batchUpdateStatus(Lists.newArrayList(orderItem.getId(), orderItem1.getId()), OrderItem.Status.PAYED.value());
        OrderItem actual = orderItemDao.findById(orderItem.getId());
        OrderItem actual1 = orderItemDao.findById(orderItem1.getId());
        assertThat(actual.getStatus(), is(OrderItem.Status.PAYED.value()));
        assertThat(actual1.getStatus(), is(OrderItem.Status.PAYED.value()));
    }

    @Test
    public void testSumRefundAmountOfShopInDate() {
        tearDown(orderItem.getId());

        // 按退款时间汇总，应该为500
        for (int i = 0; i < 10; i++) {
            OrderItem orderItem = mock();
            orderItem.setRefundAmount(100);
            orderItem.setSellerId(1L);

            if (i % 2 == 0) {
                orderItem.setRefundAt(DateTime.now().toDate());
            } else {
                orderItem.setReturnGoodsAt(DateTime.now().toDate());
            }
            orderItemDao.create(orderItem);

        }

        Long sum = orderItemDao.sumRefundAmountOfShopInDate(1L, DateTime.now().toDate());
        assertThat(sum, is(500L));


        sum = orderItemDao.sumRefundAmountOfShopInDate(2L, DateTime.now().toDate());
        assertThat(sum, is(0L));
    }

    @Test
    public void testUpdateOrderId(){
        Long oldId = orderItem.getOrderId();
        Long newId = orderItem.getOrderId() + 1;

        Boolean isUpdate = orderItemDao.updateOrderId(oldId, newId);
        List<OrderItem> actual = orderItemDao.findByOrderId(newId);
        Assert.assertNotNull(actual);
        Assert.assertTrue(isUpdate);
        Assert.assertThat(actual.get(0).getOrderId(), is(newId));
    }


}
