package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 3:55 PM  <br>
 * Author: xiao
 */
public class SellerSettlementDaoTest extends BaseDaoTest {

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    private SellerSettlement s;


    public SellerSettlement mock() {
        SellerSettlement s = new SellerSettlement();
        s.setSellerId(1L);
        s.setSellerName("test");
        s.setConfirmedAt(DateTime.now().toDate());
        s.setConfirmed(SellerSettlement.Confirmed.NOT.value());
        s.setSynced(SellerSettlement.Synced.NOT.value());
        s.setVouched(SellerSettlement.Vouched.NOT.value());
        s.setSettleStatus(SellerSettlement.SettleStatus.NOT.value());
        s.setConfirmed(SellerSettlement.Confirmed.NOT.value());
        s.setReceipted(SellerSettlement.Receipted.NOT.value());
        s.setOrderCount(5);

        return s;

    }

    @Before
    public void setUp() {
        s = mock();
        sellerSettlementDao.create(s);
        assertThat(s.getId(), notNullValue());
    }

    @Test
    public void testFindBy() {
        DateTime startAt  = DateTime.now().minusYears(1).withTimeAtStartOfDay();
        DateTime endAt = DateTime.now();
        Paging<SellerSettlement> actual = sellerSettlementDao.findBy(null, null, startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(actual.getTotal(),is(1L));
    }

    @Test
    public void testGet() {
        SellerSettlement actual = sellerSettlementDao.get(s.getId());
        assertThat(actual, notNullValue());
        assertThat(actual.getSellerName(), is("test"));
        assertThat(actual.getOrderCount(), is(5));
    }


    @Test
    public void testSynced() {
        sellerSettlementDao.synced(s.getId());
        SellerSettlement actual = sellerSettlementDao.get(s.getId());
        assertThat(actual, notNullValue());
        assertThat(actual.getSynced(), is(SellerSettlement.Synced.DONE.value()));
        assertThat(actual.getSyncedAt(), Matchers.notNullValue());
    }

    @Test
    public void testPrinting() {
        sellerSettlementDao.printing(s);
        SellerSettlement actual = sellerSettlementDao.get(s.getId());
        assertThat(actual, notNullValue());
        assertThat(actual.getPrinted(), is(SellerSettlement.Printed.DONE.value()));
        assertThat(actual.getPrintedAt(), notNullValue());
    }

    @Test
    public void testBatchUpdateOuterCode() {
        List<Long> ids1 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            SellerSettlement mock = mock();
            mock.setSellerId(8L);
            mock.setOuterCode("8888888888");
            mock.setConfirmedAt(DateTime.now().minusDays(i + 1).toDate());

            sellerSettlementDao.create(mock);
            ids1.add(mock.getId());
        }

        List<Long> ids2 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            SellerSettlement mock = mock();
            mock.setSellerId(9L);
            mock.setOuterCode("7777777777");
            mock.setConfirmedAt(DateTime.now().minusDays(i + 1).toDate());

            sellerSettlementDao.create(mock);
            ids2.add(mock.getId());
        }

        // 测试批次更新的数据隔离性
        sellerSettlementDao.batchUpdateOuterCode("9999999999", 8L);

        for (Long id : ids1) {
            SellerSettlement sellerSettlement =  sellerSettlementDao.get(id);
            assertThat(sellerSettlement.getOuterCode(), is("9999999999"));
        }

        for (Long id : ids2) {
            SellerSettlement sellerSettlement =  sellerSettlementDao.get(id);
            assertThat(sellerSettlement.getOuterCode(), is("7777777777"));
        }

    }

}



