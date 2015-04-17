package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 5:29 PM  <br>
 * Author: xiao
 */
public class SellerAlipayCashDaoTest extends BaseDaoTest {

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    private SellerAlipayCash a;

    private Date now = DateTime.now().toDate();

    private SellerAlipayCash mock() {
        SellerAlipayCash a = new SellerAlipayCash();
        a.setSellerId(1L);
        a.setTotalFee(1000000L);
        a.setAlipayFee(5000L);
        a.setSynced(0);
        a.setStatus(0);
        a.setVouched(0);
        a.setSummedAt(now);
        a.setCashFee(a.getTotalFee() - a.getAlipayFee());
        a.setStatus(AlipayCash.Status.NOT.value());
        a.setOuterCode("872222212");

        a.setCashTotalCount(5);
        return a;
    }

    @Before
    public void setUp() throws Exception {
        a = mock();

        sellerAlipayCashDao.create(a);
        assertThat(a.getId(), notNullValue());

        SellerAlipayCash actual = sellerAlipayCashDao.get(a.getId());
        a.setCreatedAt(actual.getCreatedAt());
        a.setUpdatedAt(actual.getUpdatedAt());
        a.setSynced(0);
        assertThat(actual, is(a));

    }

    @Test
    public void testSynced() throws Exception {
        sellerAlipayCashDao.synced(a.getId());
        a = sellerAlipayCashDao.get(a.getId());
        assertThat(a, notNullValue());
        assertThat(a.getSynced(), is(SellerAlipayCash.Synced.DONE.value()));
        assertThat(a.getSyncedAt(), Matchers.notNullValue());
    }

    @Test
    public void testCountOf() throws Exception {
        a = new SellerAlipayCash();
        a.setSellerId(2L);
        a.setTotalFee(1000000L);
        a.setAlipayFee(5000L);
        a.setCashTotalCount(0);
        a.setSynced(0);
        a.setStatus(0);
        a.setVouched(0);
        a.setSummedAt(DateTime.now().toDate());
        a.setCashFee(a.getTotalFee() - a.getAlipayFee());
        a.setStatus(AlipayCash.Status.NOT.value());
        sellerAlipayCashDao.create(a);


        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSummedAt(a.getSummedAt());
        Long count = sellerAlipayCashDao.countOf(criteria);
        assertThat(count, is(2L));
    }

    @Test
    public void testDone() throws Exception {
        boolean success = sellerAlipayCashDao.cashing(a.getId());
        assertThat(success, is(Boolean.TRUE));
        a = sellerAlipayCashDao.get(a.getId());
        assertThat(a.getStatus(), is(SellerAlipayCash.Status.DONE.value()));
    }

    @Test
    public void testCashedAll() throws Exception {
        SellerAlipayCash b = new SellerAlipayCash();
        b.setSellerId(2L);
        b.setTotalFee(1000000L);
        b.setAlipayFee(5000L);
        b.setCashTotalCount(0);
        b.setSynced(0);
        b.setStatus(0);
        b.setVouched(0);
        b.setSummedAt(DateTime.now().toDate());
        b.setCashFee(b.getTotalFee() - b.getAlipayFee());
        sellerAlipayCashDao.create(b);

        boolean cashedAll = sellerAlipayCashDao.casedAll(a.getSummedAt());
        assertThat(cashedAll, is(Boolean.FALSE));
        boolean done = sellerAlipayCashDao.cashing(a.getId());
        assertThat(done, is(Boolean.TRUE));
        done = sellerAlipayCashDao.cashing(b.getId());
        assertThat(done, is(Boolean.TRUE));
        cashedAll = sellerAlipayCashDao.casedAll(a.getSummedAt());
        assertThat(cashedAll, is(Boolean.TRUE));
    }


    @Test
    public void testSumDailyAlipayCash() {
        AlipayCash alipay = sellerAlipayCashDao.sumAlipayCash(DateTime.now().toDate());
        assertThat(alipay.getCashTotalCount(), is(5));
        assertThat(alipay.getTotalFee(), is(1000000L));
        assertThat(alipay.getAlipayFee(), is(5000L));
        assertThat(alipay.getCashFee(), is(995000L));
    }

    @Test
    public void testBatchUpdateOuterCode() {
        List<Long> ids1 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            SellerAlipayCash mock = mock();
            mock.setSellerId(8L);
            mock.setOuterCode("8888888888");
            mock.setSummedAt(DateTime.now().minusDays(i + 1).toDate());

            sellerAlipayCashDao.create(mock);
            ids1.add(mock.getId());
        }

        List<Long> ids2 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            SellerAlipayCash mock = mock();
            mock.setSellerId(9L);
            mock.setOuterCode("7777777777");
            mock.setSummedAt(DateTime.now().minusDays(i + 1).toDate());

            sellerAlipayCashDao.create(mock);
            ids2.add(mock.getId());
        }

        // 测试批次更新的数据隔离性
        sellerAlipayCashDao.batchUpdateOuterCode("9999999999", 8L);

        for (Long id : ids1) {
            SellerAlipayCash sellerAlipayCash =  sellerAlipayCashDao.get(id);
            assertThat(sellerAlipayCash.getOuterCode(), is("9999999999"));
        }

        for (Long id : ids2) {
            SellerAlipayCash sellerAlipayCash =  sellerAlipayCashDao.get(id);
            assertThat(sellerAlipayCash.getOuterCode(), is("7777777777"));
        }
    }

    @Test
    public void testUpdate() {
        SellerAlipayCash updating = new SellerAlipayCash();

        Date expected = DateTime.now().toDate();

        updating.setVoucher("123456");
        updating.setStatus(SellerAlipayCash.Status.DONE.value());
        updating.setSynced(SellerAlipayCash.Synced.DONE.value());
        updating.setVouched(SellerAlipayCash.Vouched.DONE.value());

        updating.setSyncedAt(expected);
        updating.setVouchedAt(expected);

        updating.setCashTotalCount(10000);
        updating.setId(a.getId());

        sellerAlipayCashDao.update(updating);

        SellerAlipayCash actual = sellerAlipayCashDao.get(a.getId());

        assertThat(actual.getVoucher(), is("123456"));
        assertThat(actual.getStatus(), is(SellerAlipayCash.Status.DONE.value()));
        assertThat(actual.getSynced(), is(SellerAlipayCash.Synced.DONE.value()));
        assertThat(actual.getVouched(), is(SellerAlipayCash.Vouched.DONE.value()));

        assertThat(actual.getSyncedAt(), is(expected));
        assertThat(actual.getVouchedAt(), is(expected));

        assertThat(actual.getCashTotalCount(), is(10000));
    }

    @Test
    public void testFindNotCashedOfDaily() {
        SellerAlipayCash zeroCountCash = mock();
        zeroCountCash.setRefundFee(0L);
        zeroCountCash.setTotalFee(0L);
        zeroCountCash.setCashFee(0L);
        zeroCountCash.setAlipayFee(0L);
        zeroCountCash.setSellerId(2L);
        sellerAlipayCashDao.create(zeroCountCash);


        List<SellerAlipayCash> cashes = sellerAlipayCashDao.findNotCashedOfDaily(now);

        assertThat(cashes.size(), is(1));
        assertThat(cashes.get(0), is(a));

        sellerAlipayCashDao.cashing(a.getId());
        cashes = sellerAlipayCashDao.findNotCashedOfDaily(now);
        assertThat(cashes.size(), is(0));
    }


    @Test
    public void testFindBy() {
        Map<String, Object> params = Maps.newHashMap();
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSellerId(1L);
        criteria.setStatus(SellerAlipayCash.Status.NOT.value());
        criteria.setSynced(SellerAlipayCash.Synced.NOT.value());
        criteria.setVouched(SellerAlipayCash.Vouched.NOT.value());
        params.put("summedStartAt", startOfDay(now));
        params.put("summedEndAt", endOfDay(now));
        params.put("filter", Boolean.TRUE);
        Paging<SellerAlipayCash> actual = sellerAlipayCashDao.findBy(params);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData().size(), is(1));
    }
}
