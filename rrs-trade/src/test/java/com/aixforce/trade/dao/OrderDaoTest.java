package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.Order;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
public class OrderDaoTest extends BaseDaoTest {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrderDao orderDao;

    private Order order;

    @Before
    public void setUp() throws Exception {
        order = createOrder(11L, 22L, Order.Status.WAIT_FOR_PAY.value());
        orderDao.create(order);
    }

    private Order createOrder(Long buyerId, Long sellerId, Integer status) {
        Order o = new Order();
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setStatus(status);
        o.setBusiness(1L);
        o.setIsBuying(Boolean.FALSE);
        return o;
    }

    @Test
    public void testFindById() throws Exception {
        Order actual = orderDao.findById(order.getId());
        assertThat(actual, is(order));
    }

    @Test
    public void testFindBy() throws Exception {
        Order another = createOrder(11L, 33L, Order.Status.PAID.value());
        orderDao.create(another);

        Order o3 = createOrder(44L, 33L, Order.Status.PAID.value());
        orderDao.create(o3);

        Order criteria = createOrder(11L, null, null);
        Paging<Order> orders = orderDao.findBy(criteria, 0, 5);
        assertThat(orders.getTotal(), is(2L));
        assertThat(orders.getData(), contains(order, another));


        criteria = new Order();
        criteria.setCreatedAt(DateTime.now().toDate());
        orders = orderDao.findBy(criteria, 0, 5);
        assertThat(orders.getTotal(), is(3L));


    }

    @Test
    public void testDelete() throws Exception {
        orderDao.delete(order.getId());
        assertThat(orderDao.findById(order.getId()), nullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        Order u = new Order();
        u.setId(order.getId());
        u.setStatus(Order.Status.CANCELED_BY_BUYER.value());
        u.setCanceledAt(new Date());
        orderDao.update(u);

        Order actual = orderDao.findById(order.getId());
        assertThat(actual.getStatus(), is(u.getStatus()));
        assertThat(actual.getUpdatedAt(), notNullValue());
        assertThat(actual.getBuyerId(), is(order.getBuyerId()));
        assertThat(actual.getSellerId(), is(order.getSellerId()));
    }

    @Test
    public void testCountOfFinished() throws Exception {
        order = createOrder(11L, 22L, Order.Status.WAIT_FOR_PAY.value());
        order.setFinishedAt(DateTime.now().toDate());
        orderDao.create(order);
        Date startAt = DateTime.now().minusDays(1).toDate();
        Date endAt = DateTime.now().plusDays(1).toDate();

        Long actual = orderDao.countOfFinished(startAt, endAt);
        assertThat(actual, is(1L));
    }

    @Test
    public void testFindNotFinished() throws Exception {
        List<Order> orders = orderDao.findNotFinished(1000L,DATE_TIME_FORMAT.print(DateTime.now().minusDays(1)),10);
        assertThat(orders.size(), is(1));
    }

    @Test
    public void testFindFinished() throws Exception {
        Order order = createOrder(11L, 22L, Order.Status.WAIT_FOR_PAY.value());
        order.setFinishedAt(DateTime.now().minusDays(1).toDate());
        orderDao.create(order);
        DateTime date = DateTime.now().minusDays(1);
        Paging<Order> paging = orderDao.findFinished(date.withTimeAtStartOfDay().toDate(),
                date.toDate(), 0, 100);
        assertThat(paging.getTotal(), is(1L));
    }

    @Test
    public void testFindUpdatedAt() throws Exception {
        Date beginAt = DateTime.now().withTimeAtStartOfDay().toDate();
        Date endAt = DateTime.now().toDate();
        Paging<Order> paging = orderDao.findUpdated(Lists.newArrayList(0L), beginAt, endAt, 0, 10);
        assertThat(paging.getTotal(), is(0L));

        paging = orderDao.findUpdated(Lists.newArrayList(1L), beginAt, endAt, 0, 10);
        assertThat(paging.getTotal(), is(1L));
    }
}
