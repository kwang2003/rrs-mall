package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 2:59 PM  <br>
 * Author: xiao
 */
public class ItemSettlementDaoTest extends BaseDaoTest {

    @Autowired
    private ItemSettlementDao itemSettlementDao;


    private ItemSettlement s;

    @Before
    public void setUp() throws Exception {
        s = new ItemSettlement();
        s.setSellerId(1L);
        s.setOrderId(1L);
        s.setOrderItemId(1L);
        s.setBuyerId(22L);
        s.setTotalEarning(100L);
        s.setTradeStatus(1);
        s.setType(1);
        s.setSettleStatus(1);
        s.setPayType(1);
        s.setPaymentCode("20112312313123");
        s.setFee(5000L);
        s.setItemName("海尔");
        s.setItemQuantity(100);
        s.setBuyerName("12345");
        s.setPaidAt(DateTime.now().toDate());
        s.setThirdPartyCommission(200L);
        s.setCommissionRate(0.0000);
        s.setFixed(Boolean.TRUE);

        DateTime now = DateTime.now();
        s.setSettledAt(now.toDate());
        itemSettlementDao.create(s);
        assertThat(s.getId(), notNullValue());

        ItemSettlement actual = itemSettlementDao.get(s.getId());
        s.setCreatedAt(actual.getCreatedAt());
        s.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(s));

    }

    @Test
    public void testFindBy() throws Exception {
        DateTime startAt  = DateTime.now().minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = DateTime.now();

        Map<String, Object> params = Maps.newHashMap();
        ItemSettlement criteria = new ItemSettlement();
        criteria.setOrderId(1L);
        params.put("criteria", criteria);
        params.put("confirmedStartAt", startAt.toDate());
        params.put("confirmedEndAt", endAt.toDate());
        Paging<ItemSettlement> actual = itemSettlementDao.findBy(params);
        assertThat(actual.getTotal(),is(1L));
    }

    @Test
    public void testFindByOrderId() throws Exception {
        List<ItemSettlement> actual = itemSettlementDao.list(1L);
        assertThat(actual.size(), is(1));

        List<ItemSettlement> actual2 = itemSettlementDao.findByOrderId(1L);
        assertThat(actual2.size(), is(1));
    }


    @Test
    public void testUpdate() throws  Exception {
        DateTime startAt  = DateTime.now().minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = DateTime.now();

        Map<String, Object> params = Maps.newHashMap();
        ItemSettlement criteria = new ItemSettlement();
        criteria.setOrderId(1L);
        params.put("criteria", criteria);
        params.put("confirmedStartAt", startAt.toDate());
        params.put("confirmedEndAt", endAt.toDate());


        Paging<ItemSettlement> paging = itemSettlementDao.findBy(params);
        assertThat(paging.getTotal(),is(1L));
        ItemSettlement s = paging.getData().get(0);

        s.setFee(1000L);
        s.setTotalEarning(1000L);
        s.setTotalExpenditure(1000L);
        s.setSellerEarning(1000L);
        s.setRrsCommission(1000L);
        s.setScoreEarning(1000L);
        s.setPresellDeposit(1000L);
        s.setPaidAt(DateTime.now().toDate());
        s.setThirdPartyCommission(1000L);
        s.setSettleStatus(Settlement.SettleStatus.NOT.value());
        s.setRefundAmount(10000L);
        s.setReason("退货理由");
        itemSettlementDao.update(s);

        ItemSettlement actual = itemSettlementDao.get(s.getId());
        assertThat(actual.getFee(), is(1000L));
        assertThat(actual.getTotalEarning(), is(1000L));
        assertThat(actual.getTotalExpenditure(), is(1000L));
        assertThat(actual.getSellerEarning(), is(1000L));
        assertThat(actual.getRrsCommission(), is(1000L));
        assertThat(actual.getScoreEarning(), is(1000L));
        assertThat(actual.getPresellDeposit(), is(1000L));
        assertThat(actual.getThirdPartyCommission(), is(1000L));
        assertThat(actual.getItemName(), is("海尔"));
        assertThat(actual.getItemQuantity(), is(100));
        assertThat(actual.getSettleStatus(), is(Settlement.SettleStatus.NOT.value()));
        assertThat(actual.getRefundAmount(), is(10000L));
        assertThat(actual.getReason(), is("退货理由"));
    }

    @Test
    public void testFinished() {
        s.setTradeStatus(Settlement.TradeStatus.CANCELED_BY_REFUND.value());
        s.setRefundAmount(100000L);
        s.setReason("没有理由才是最大的理由");
        itemSettlementDao.finished(s);
        ItemSettlement actual = itemSettlementDao.get(s.getId());
        assertThat(actual.getReason(), is("没有理由才是最大的理由"));
        assertThat(actual.getRefundAmount(), is(100000L));
        assertThat(actual.getTradeStatus(), is(Settlement.TradeStatus.CANCELED_BY_REFUND.value()));
    }


}
