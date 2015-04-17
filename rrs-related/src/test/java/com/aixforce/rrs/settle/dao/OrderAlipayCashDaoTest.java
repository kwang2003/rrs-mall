package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-24 5:52 PM  <br>
 * Author: xiao
 */

public class OrderAlipayCashDaoTest extends BaseDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    private OrderAlipayCash positive;   // 正向提现单

    private OrderAlipayCash negative;   // 负向提现单

    private Date now = DateTime.now().toDate();

    private OrderAlipayCash mock() {
        OrderAlipayCash o = new OrderAlipayCash();
        o.setOrderId(1L);

        o.setBuyerId(1L);
        o.setBuyerName("买家");
        o.setSellerId(2L);
        o.setSellerName("卖家");
        o.setShopId(1L);
        o.setShopName("店铺");

        o.setTotalFee(1000L);
        o.setRefundFee(0L);
        o.setCashFee(800L);
        o.setAlipayFee(100L);
        o.setStatus(0);
        o.setTradedAt(now);

        o.setType(OrderAlipayCash.Type.PLAIN.value());

        o.setVoucher("1234567");
        o.setOperator("admin");

        return o;
    }

    @Before
    public void setUp() throws Exception {
        positive = mock();
        orderAlipayCashDao.create(positive);
        assertThat(positive.getId(), notNullValue());

        OrderAlipayCash actual = orderAlipayCashDao.get(positive.getId());
        positive.setCreatedAt(actual.getCreatedAt());
        positive.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(positive));


        negative = mock();
        negative.setTotalFee(0L);
        negative.setCashFee(-1000L);
        negative.setRefundFee(1000L);
        negative.setAlipayFee(0L);
        negative.setOrderId(2L);
        negative.setOrderItemId(2L);
        orderAlipayCashDao.create(negative);

        actual = orderAlipayCashDao.get(negative.getId());

        assertThat(negative.getId(), notNullValue());
        negative.setCreatedAt(actual.getCreatedAt());
        negative.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(negative));


    }

    @Test
    public void testCreateWithTradedAtNull() {
        OrderAlipayCash creating = mock();
        creating.setTradedAt(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("traded.at.can.not.be.empty");
        orderAlipayCashDao.create(creating);
    }

    @Test
    public void testCreateWithOrderIdNull() {
        OrderAlipayCash creating = mock();
        creating.setOrderId(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("order.id.can.not.be.empty");
        orderAlipayCashDao.create(creating);
    }

    @Test
    public void testCreateWithCashFeeNull() {
        OrderAlipayCash creating = mock();
        creating.setCashFee(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("cash.fee.can.not.be.empty");
        orderAlipayCashDao.create(creating);
    }

    @Test
    public void testCreateWithAlipayFeeNull() {
        OrderAlipayCash creating = mock();
        creating.setAlipayFee(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("alipay.fee.can.not.be.empty");
        orderAlipayCashDao.create(creating);
    }

    @Test
    public void testCreateWithTotalFeeNull() {
        OrderAlipayCash creating = mock();
        creating.setTotalFee(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("total.fee.can.not.be.empty");
        orderAlipayCashDao.create(creating);
    }


    /**
     * 测试插入重复的提现单（订单和交易日期相同)
     */
    @Test
    public void testCreatePositiveDuplicate() {
        OrderAlipayCash creating = mock();
        mock().setOrderId(1L);
        mock().setTradedAt(now);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("order.alipay.cash.record.duplicate");
        orderAlipayCashDao.create(creating);
    }

    /**
     * 测试插入重复的提现单（订单好和子订单号相同)
     */
    @Test
    public void testCreateNegativeDuplicate() {
        OrderAlipayCash creating = mock();
        mock().setOrderId(2L);
        mock().setOrderItemId(2L);
        mock().setCashFee(-1000L);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("order.alipay.cash.record.duplicate");
        orderAlipayCashDao.create(creating);
    }

    @Test
    public void testGetBy() {
        OrderAlipayCash actual = orderAlipayCashDao.getBy(positive.getOrderId(), positive.getTradedAt());
        assertThat(actual, notNullValue());
        assertThat(actual, is(positive));


        actual = orderAlipayCashDao.getBy(negative.getOrderId(), negative.getOrderItemId());
        assertThat(actual, notNullValue());
        assertThat(actual, is(negative));
    }

    @Test
    public void testFindBy() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("offset", 0);
        params.put("limit", 10);

        Paging<OrderAlipayCash> paging = orderAlipayCashDao.findBy(params);
        assertThat(paging.getTotal(), is(2L));

        OrderAlipayCash criteria = new OrderAlipayCash();
        criteria.setSellerId(2L);
        criteria.setOrderId(1L);
        criteria.setStatus(OrderAlipayCash.Status.NOT.value());
        params.put("criteria", criteria);
        params.put("tradedStartAt", startOfDay(now));
        params.put("tradedEndAt", endOfDay(now));
        paging = orderAlipayCashDao.findBy(params);
        assertThat(paging.getTotal(), is(1L));


        criteria.setTradedAt(now);
        paging = orderAlipayCashDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(1L));
        assertThat(paging.getData().size(), is(1));

        criteria.setType(OrderAlipayCash.Type.PRE_SELL.value());
        paging = orderAlipayCashDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(0L));



        orderAlipayCashDao.cashing(positive.getId(), "admin");
        orderAlipayCashDao.cashing(negative.getId(), "admin");
        criteria = new OrderAlipayCash();
        criteria.setCashedAt(DateTime.now().toDate());
        params = Maps.newHashMap();
        params.put("offset", 0);
        params.put("limit", 10);
        params.put("criteria", criteria);
        paging = orderAlipayCashDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(2L));
        assertThat(paging.getData().size(), is(2));
    }

    @Test
    public void testSummaryCashesDaily() {
        orderAlipayCashDao.cashing(positive.getId(), "admin");
        orderAlipayCashDao.cashing(negative.getId(), "admin");

        AlipayCash actual = orderAlipayCashDao.summaryCashesDaily(now);
        assertThat(actual.getTotalFee(), is(1000L));
        assertThat(actual.getCashFee(), is(-200L));
        assertThat(actual.getAlipayFee(), is(100L));
        assertThat(actual.getRefundFee(), is(1000L));
    }

    @Test
    public void testSumSellerAlipayCash() {
        orderAlipayCashDao.cashing(positive.getId(), "admin");
        orderAlipayCashDao.cashing(negative.getId(), "admin");

        SellerAlipayCash actual = orderAlipayCashDao.sumSellerAlipayCash(2L, now);
        assertThat(actual.getTotalFee(), is(1000L));
        assertThat(actual.getCashFee(), is(-200L));
        assertThat(actual.getAlipayFee(), is(100L));
        assertThat(actual.getRefundFee(), is(1000L));
    }

    @Test
    public void testCashing() {
        boolean success = orderAlipayCashDao.cashing(positive.getId(),  "admin");
        assertThat(success, is(Boolean.TRUE));

        OrderAlipayCash actual = orderAlipayCashDao.get(positive.getId());

        assertThat(actual.getStatus(), is(OrderAlipayCash.Status.DONE.value()));
        assertThat(actual.getCashedAt(), notNullValue());
        assertThat(actual.getOperator(), is("admin"));
    }

    @Test
    public void testCountOf() {
        OrderAlipayCash criteria = new OrderAlipayCash();
        criteria.setSellerId(2L);
        criteria.setOrderId(1L);
        criteria.setStatus(0);
        criteria.setTradedAt(now);

        Long actual = orderAlipayCashDao.countOf(criteria);
        assertThat(actual, is(1L));
    }

    @Test
    public void testFindByIds() {
        List<OrderAlipayCash> actual = orderAlipayCashDao
                .findByIds(Lists.newArrayList(positive.getId(), negative.getId()));
        assertThat(actual, contains(positive, negative));
    }

    @Test
    public void testSumCashedAmountOfSellerDaily() {
        OrderAlipayCash cashed = mock();
        cashed.setOrderId(3L);
        cashed.setSellerId(3L);
        cashed.setStatus(OrderAlipayCash.Status.DONE.value());
        cashed.setCashedAt(now);
        orderAlipayCashDao.create(cashed);
        assertThat(cashed.getId(), notNullValue());


        Long actual = orderAlipayCashDao.sumCashedAmountOfSellerDaily(3L, now);
        assertThat(actual, is(800L));
    }

    @Test
    public void testSumCashedAmountOfDaily() {
        OrderAlipayCash cashed = mock();
        cashed.setOrderId(3L);
        cashed.setSellerId(3L);
        cashed.setStatus(OrderAlipayCash.Status.DONE.value());
        cashed.setCashedAt(now);
        orderAlipayCashDao.create(cashed);
        assertThat(cashed.getId(), notNullValue());


        Long actual = orderAlipayCashDao.sumCashedAmountOfDaily(now);
        assertThat(actual, is(800L));
    }

    @Test
    public void testFindByOrderId() {
        List<OrderAlipayCash> actual = orderAlipayCashDao.findByOrderId(1L);
        assertThat(actual, contains(positive));
    }

    @Test
    public void testBatchCashingOfSeller() {
        orderAlipayCashDao.batchCashingOfSeller(2L, now);
        OrderAlipayCash actual = orderAlipayCashDao.get(positive.getId());
        assertThat(actual.getStatus(), is(OrderAlipayCash.Status.DONE.value()));
        assertThat(actual.getCashedAt(), notNullValue());
    }

    @Test
    public void testUpdate() {
        OrderAlipayCash updating = new OrderAlipayCash();
        updating.setId(positive.getId());
        Date cashedNow = DateTime.now().toDate();
        updating.setCashedAt(cashedNow);
        updating.setStatus(OrderAlipayCash.Status.DONE.value());
        updating.setOperator("finance");
        updating.setVoucher("8888888");

        boolean success = orderAlipayCashDao.update(updating);
        assertThat(success, is(Boolean.TRUE));

        OrderAlipayCash actual = orderAlipayCashDao.get(positive.getId());
        assertThat(actual.getCashedAt(), is(cashedNow));
        assertThat(actual.getStatus(), is(OrderAlipayCash.Status.DONE.value()));
        assertThat(actual.getOperator(), is("finance"));
        assertThat(actual.getVoucher(), is("8888888"));
    }

    @Test
    public void testBatchVouching() {
        orderAlipayCashDao.cashing(positive.getId(), "admin");
        orderAlipayCashDao.cashing(negative.getId(), "admin");

        orderAlipayCashDao.batchVouching(2L, DateTime.now().toDate(), "8888888888");
        OrderAlipayCash actual1 = orderAlipayCashDao.get(positive.getId());
        assertThat(actual1.getVoucher(), is("8888888888"));

        OrderAlipayCash actual2 = orderAlipayCashDao.get(negative.getId());
        assertThat(actual2.getVoucher(), is("8888888888"));
    }


}
