package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.*;
import com.aixforce.trade.model.Order;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-19
 */
public class SettlementDaoTest extends BaseDaoTest {

    @Autowired
    private SettlementDao settlementDao;

    private Settlement s;

    private DateTime now = DateTime.now();
    
    private Date startOfDay = DateTime.now().withTimeAtStartOfDay().toDate();
    
    private Date endOfDay = DateTime.now().withTimeAtStartOfDay().plusDays(1).toDate();
    

    private void tearDown(Long id) {
        settlementDao.delete(id);
    }


    private Settlement mock() {
        Settlement s = new Settlement();
        s.setSellerId(1L);
        s.setOrderId(11L);
        s.setBuyerId(22L);

        s.setTotalEarning(100L);
        s.setTotalExpenditure(100L);
        s.setSellerEarning(100L);
        s.setRrsCommission(100L);
        s.setScoreEarning(100L);
        s.setPresellDeposit(100L);
        s.setThirdPartyCommission(100L);
        s.setCommissionRate(0.0000);
        s.setFee(100L);
        s.setPaymentCode("2014051636669165");

        s.setTradeStatus(1);
        s.setType(1);
        s.setPayType(1);
        s.setSellerName("12345");
        s.setBuyerName("54321");

        s.setSettleStatus(Settlement.SettleStatus.NOT.value());
        s.setCashed(Settlement.Cashed.NOT.value());
        s.setFinished(Settlement.Finished.NOT.value());
        s.setSettled(Settlement.Settled.NOT.value());
        s.setConfirmed(Settlement.Confirmed.NOT.value());
        s.setSynced(Settlement.Synced.NOT.value());
        s.setVouched(Settlement.Vouched.NOT.value());
        s.setFixed(Boolean.TRUE);

        s.setOrderedAt(now.toDate());
        s.setPaidAt(now.toDate());
        s.setSettledAt(now.toDate());
        s.setConfirmedAt(now.toDate());
        s.setFinishedAt(now.toDate());

        s.setMultiPaid(0);
        return s;
    }

    @Before
    public void setUp() {
        s  = this.mock();
        settlementDao.create(s);
        assertThat(s.getId(), notNullValue());
        Settlement actual = settlementDao.get(s.getId());
        s.setCreatedAt(actual.getCreatedAt());
        s.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(s));
    }


    @Test
    public void testSettled() {
        assertThat(settlementDao.settled(s.getId()), is(true));
        Settlement actual = settlementDao.get(s.getId());
        assertThat(actual.getSettleStatus(), is(3));
    }

    @Test
    public void testCloseOrder() {
        assertThat(settlementDao.closeOrder(s.getSellerId(), s.getOrderId(), Order.Status.DONE), is(true));
        Settlement actual = settlementDao.get(s.getId());
        assertThat(actual.getSettledAt(), notNullValue());
        assertThat(actual.getTradeStatus(),is(Order.Status.DONE.value()));
    }

    @Test
    public void testFindBy() {
        Settlement criteria = new Settlement();
        criteria.setOrderedAt(now.toDate());
        criteria.setPaidAt(now.toDate());
        criteria.setFinishedAt(now.toDate());
        criteria.setSettledAt(now.toDate());
        criteria.setConfirmedAt(now.toDate());

        criteria = new Settlement();
        criteria.setSellerId(1L);
        criteria.setOrderId(11L);
        criteria.setBuyerId(22L);

        criteria.setTotalEarning(100L);
        criteria.setTotalExpenditure(100L);
        criteria.setSellerEarning(100L);
        criteria.setRrsCommission(100L);
        criteria.setScoreEarning(100L);
        criteria.setPresellDeposit(100L);
        criteria.setThirdPartyCommission(100L);
        criteria.setFee(100L);
        criteria.setMultiPaid(Settlement.MultiPaid.NOT.value());

        criteria.setTradeStatus(1);
        criteria.setType(1);
        criteria.setPayType(1);
        criteria.setSellerName("12345");
        criteria.setBuyerName("54321");


        Paging<Settlement> actual = settlementDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(),is(1L));
    }


    @Test
    public void testCountOf() {
        assertThat(settlementDao.closeOrder(s.getSellerId(), s.getOrderId(), Order.Status.DONE), is(true));
        Settlement criteria = new Settlement();
        criteria.setOrderedAt(now.toDate());
        criteria.setPaidAt(now.toDate());
        criteria.setFinishedAt(now.toDate());
        criteria.setSettledAt(now.toDate());
        criteria.setConfirmedAt(now.toDate());

        Long actual = settlementDao.countOf(criteria);
        assertThat(actual,is(1L));
    }

    @Test
    public void testFindByOrderId() {
        Settlement actual = settlementDao.getByOrderId(11L);
        assertThat(actual, notNullValue());
        assertThat(actual.getSellerName(), is("12345"));
        assertThat(actual.getBuyerName(), is("54321"));
        assertThat(actual.getCashed(), is(Settlement.Cashed.NOT.value()));

    }

    @Test
    public void testSumSellerEarning() {
        Settlement s = new Settlement();
        s.setSellerId(1L);
        s.setOrderId(1L);
        s.setBuyerId(22L);

        s.setTotalEarning(100L);
        s.setTotalExpenditure(100L);
        s.setSellerEarning(100L);
        s.setRrsCommission(100L);
        s.setScoreEarning(100L);
        s.setPresellDeposit(100L);
        s.setThirdPartyCommission(100L);
        s.setFee(100L);

        s.setTradeStatus(1);
        s.setType(1);
        s.setSettleStatus(1);
        s.setPayType(1);
        s.setOrderedAt(now.toDate());
        s.setPaidAt(now.toDate());
        s.setFinishedAt(now.toDate());
        s.setSettledAt(now.toDate());
        s.setConfirmedAt(now.toDate());
        s.setMultiPaid(0);
        settlementDao.create(s);
        Date startAt = startOfDay;
        Date endAt = now.withTimeAtStartOfDay().plusDays(1).toDate();
        SellerSettlement actual = settlementDao.sumSellerSettlement(1L, startAt, endAt);

        assertThat(actual.getTotalEarning(), is(200L));
        assertThat(actual.getTotalExpenditure(), is(200L));
        assertThat(actual.getSellerEarning(), is(200L));
        assertThat(actual.getRrsCommission(), is(200L));
        assertThat(actual.getScoreEarning(), is(200L));
        assertThat(actual.getPresellDeposit(), is(200L));
        assertThat(actual.getThirdPartyCommission(), is(200L));
    }


    @Test
    public void testSumDailySettle() {
        Settlement s = new Settlement();
        s.setSellerId(2L);
        s.setOrderId(1L);
        s.setBuyerId(22L);

        s.setTotalEarning(100L);
        s.setTotalExpenditure(100L);
        s.setSellerEarning(100L);
        s.setRrsCommission(100L);
        s.setScoreEarning(100L);
        s.setPresellDeposit(100L);
        s.setThirdPartyCommission(100L);
        s.setFee(100L);

        s.setTradeStatus(1);
        s.setType(1);
        s.setSettleStatus(1);
        s.setPayType(1);
        s.setOrderedAt(now.toDate());
        s.setPaidAt(now.toDate());
        s.setFinishedAt(now.toDate());
        s.setSettledAt(now.toDate());
        s.setConfirmedAt(now.toDate());
        s.setMultiPaid(0);
        settlementDao.create(s);
        Date startAt = startOfDay;
        Date endAt = now.withTimeAtStartOfDay().plusDays(1).toDate();
        DailySettlement actual = settlementDao.sumDailySettlement(startAt, endAt);

        assertThat(actual.getTotalEarning(), is(200L));
        assertThat(actual.getTotalExpenditure(), is(200L));
        assertThat(actual.getSellerEarning(), is(200L));
        assertThat(actual.getRrsCommission(), is(200L));
        assertThat(actual.getScoreEarning(), is(200L));
        assertThat(actual.getPresellDeposit(), is(200L));
        assertThat(actual.getThirdPartyCommission(), is(200L));
    }


    @Test
    public void testFindByFinishedAt() {
        DateTime startAt  = now.withTimeAtStartOfDay();
        DateTime endAt = now.plusDays(1).withTimeAtStartOfDay();
        Paging<Settlement>  actual = settlementDao.findByFinishedAt(startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData().size(), is(1));

    }

    public void mock2(){
        Settlement s = new Settlement();
        s.setSellerId(1L);
        s.setOrderId(1L);
        s.setBuyerId(22L);

        s.setTotalEarning(100L);
        s.setTotalExpenditure(100L);
        s.setSellerEarning(100L);
        s.setRrsCommission(100L);
        s.setScoreEarning(100L);
        s.setPresellDeposit(100L);
        s.setThirdPartyCommission(100L);
        s.setFee(100L);

        s.setTradeStatus(1);
        s.setType(1);
        s.setSettleStatus(1);
        s.setPayType(1);
        s.setOrderedAt(now.toDate());
        s.setPaidAt(now.minusDays(1).withTimeAtStartOfDay().toDate());
        s.setFinishedAt(now.toDate());
        s.setSettledAt(now.toDate());
        s.setConfirmedAt(now.toDate());
        s.setMultiPaid(1);
        settlementDao.create(s);
    }

    @Test
    public void testFindOfNoMultiPaid() {
        mock2();
        Settlement criteria = new Settlement();
        Date paidAt = now.toDate();
        criteria.setPaidAt(paidAt);
        Paging<Settlement> paging = settlementDao.findOfNoMultiPaid(criteria, 0, 10);
        assertThat(paging.getTotal(), is(1L));
    }

    @Test
    public void testFindOfMultiPaid() {
        mock2();
        Settlement criteria = new Settlement();
        Date paidAt = now.minusDays(1).withTimeAtStartOfDay().toDate();
        criteria.setPaidAt(paidAt);
        Paging<Settlement> paging = settlementDao.findOfMultiPaid(criteria, 0, 10);
        assertThat(paging.getTotal(), is(1L));
    }

    @Test
    public void testUpdate() {
        DateTime startAt  = now.minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = now.plusDays(1);
        Paging<Settlement> paging = settlementDao.findBy(null, null, startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(paging.getTotal(),is(1L));
        Settlement s = paging.getData().get(0);

        s.setFee(1000L);
        s.setTotalEarning(1000L);
        s.setTotalExpenditure(1000L);
        s.setSellerEarning(1000L);
        s.setRrsCommission(1000L);
        s.setScoreEarning(1000L);
        s.setPresellDeposit(1000L);
        s.setThirdPartyCommission(1000L);
        s.setOrderedAt(now.toDate());
        s.setSettleStatus(Settlement.SettleStatus.NOT.value());

        settlementDao.update(s);

        Settlement actual = settlementDao.get(s.getId());
        assertThat(actual.getFee(), is(1000L));
        assertThat(actual.getTotalEarning(), is(1000L));
        assertThat(actual.getTotalExpenditure(), is(1000L));
        assertThat(actual.getSellerEarning(), is(1000L));
        assertThat(actual.getRrsCommission(), is(1000L));
        assertThat(actual.getScoreEarning(), is(1000L));
        assertThat(actual.getPresellDeposit(), is(1000L));
        assertThat(actual.getThirdPartyCommission(), is(1000L));
        assertThat(actual.getSettleStatus(), is(Settlement.SettleStatus.NOT.value()));


    }

    @Test
    public void testConfirmed() {
        settlementDao.confirmed(s.getId());
        Settlement actual = settlementDao.get(s.getId());
        assertThat(actual.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
    }

    @Test
    public void testFinished() {
        s.setTradeStatus(Order.Status.DONE.value());
        settlementDao.finished(s);
        Settlement actual = settlementDao.getByOrderId(s.getOrderId());
        assertThat(actual.getFinished(), is(Settlement.Finished.DONE.value()));
        assertThat(actual.getFinishedAt(), DateMatchers.sameDay(now.toDate()));
    }


    @Test
    public void testBatchVouched() {
        tearDown(s.getId());

        Date vouchedAt = DateTime.parse("2014-04-01").toDate();
        Date thirdPartyReceiptAt = DateTime.parse("2014-04-01").toDate();

        for (int i = 0; i < 10; i++) {
            Settlement mock = mock();
            mock.setOrderId((long) i + 1);
            settlementDao.create(mock);
        }

        settlementDao.batchVouched(s.getSellerId(),
                "VOUCHER", vouchedAt,
                "RECEIPT", thirdPartyReceiptAt,
                startOfDay, now.withTimeAtStartOfDay().plusDays(1).toDate());


        Settlement criteria = new Settlement();
        Paging<Settlement> paging = settlementDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(10L));

        for (Settlement settlement : paging.getData()) {
            assertThat(settlement.getVoucher(), is("VOUCHER"));
            assertThat(settlement.getVouchedAt(), is(vouchedAt));
            assertThat(settlement.getThirdPartyReceipt(), is("RECEIPT"));
            assertThat(settlement.getThirdPartyReceiptAt(), is(thirdPartyReceiptAt));
            assertThat(settlement.getVouched(), is(Settlement.Vouched.DONE.value()));
        }
    }

    @Test
    public void testBatchSynced() {
        tearDown(s.getId());
        for (int i = 0; i < 10; i++) {
            Settlement mock = mock();
            mock.setOrderId((long) i + 1);
            settlementDao.create(mock);
        }

        settlementDao.batchSynced(startOfDay, endOfDay);
        Settlement criteria = new Settlement();
        Paging<Settlement> paging = settlementDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(10L));

        for (Settlement settlement : paging.getData()) {
            assertThat(settlement.getSynced(), is(Settlement.Synced.DONE.value()));
        }
    }

    @Test
    public void testFindByPaymentCode() {
        List<Settlement> settlements =  settlementDao.findByPaymentCode("2014051636669165");
        assertThat(settlements.size(), is(1));
    }

    @Test
    public void testBatchCashed() {
        tearDown(s.getId());
        for (int i = 0; i < 10; i++) {
            Settlement mock = mock();
            mock.setSellerId(999L);
            mock.setOrderId((long) i + 1);
            settlementDao.create(mock);
        }

        settlementDao.batchSetSettlementsAsCashed(999L, startOfDay, endOfDay);
        Settlement criteria = new Settlement();
        criteria.setSellerId(999L);
        Paging<Settlement> paging = settlementDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(10L));

        for (Settlement settlement : paging.getData()) {
            assertThat(settlement.getCashed(), is(Settlement.Cashed.DONE.value()));
        }
    }


    @Test
    public void testBatchMarkAsIng() {
        tearDown(s.getId());
        for (int i = 0; i < 10; i++) {
            Settlement mock = mock();
            mock.setSellerId(999L);
            mock.setOrderId((long) i + 1);
            settlementDao.create(mock);
        }

        settlementDao.batchSetSettlementsAsCashed(999L, startOfDay, endOfDay);
        settlementDao.batchSetSettlementAsIng(999L, startOfDay, endOfDay);
        Settlement criteria = new Settlement();
        criteria.setSellerId(999L);
        Paging<Settlement> paging = settlementDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(10L));


        // 此时应该尚未到结算中这个状态
        for (Settlement settlement : paging.getData()) {
            assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.NOT.value()));
            settlementDao.finished(settlement);
        }


        // 只有提现完成&订单关闭同时存在时才会真正到结算中
        settlementDao.batchSetSettlementAsIng(999L, startOfDay, endOfDay);
        paging = settlementDao.findBy(criteria, 0, 10);
        assertThat(paging.getTotal(), is(10L));
        for (Settlement settlement : paging.getData()) {
            assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.ING.value()));
        }
    }

}