package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.DepositFee;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * 保证金增减与技术服务费
 *
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
public class DepositFeeDaoTest extends BaseDaoTest {

    @Autowired
    private DepositFeeDao depositFeeDao;

    private DepositFee sd;

    public DepositFee getDumpSD(Long id) {
        DepositFee sd = new DepositFee();
        sd.setDeposit(200L);
        sd.setSellerId(id);
        sd.setSellerName("商家");
        return sd;
    }


    private DepositFee mock() {
        DepositFee sd = new DepositFee();
        sd.setDeposit(200L);
        sd.setSellerId(1L);
        sd.setSellerName("商家");
        sd.setShopId(1L);
        sd.setShopName("店铺");
        sd.setBusiness(1L);
        sd.setType(1);
        sd.setAuto(Boolean.TRUE);
        sd.setPaymentType(DepositFee.PaymentType.ALIPAY.value());
        sd.setSynced(DepositFee.Synced.NOT.value());
        sd.setVouched(DepositFee.Vouched.NOT.value());
        sd.setOuterCode("88888888");
        return sd;
    }

    @Before
    public void setUp() throws Exception {
        sd = mock();
        depositFeeDao.create(sd);
        assertThat(sd.getId(), notNullValue());


        DepositFee actual = depositFeeDao.get(sd.getId());
        sd.setCreatedAt(actual.getCreatedAt());
        sd.setUpdatedAt(actual.getUpdatedAt());
        sd.setOrdered(0);
        sd.setReceipted(0);
        sd.setVouched(0);
        sd.setSynced(0);
        assertThat(sd, is(actual));
    }

    @Test
    public void testFindBy() throws Exception {
        sd = new DepositFee();
        sd.setDeposit(200L);
        sd.setSellerId(1L);
        sd.setSellerName("kao");
        sd.setType(1);
        depositFeeDao.create(sd);

        DepositFee criteria = new DepositFee();
        criteria.setSellerId(sd.getSellerId());
        Paging<DepositFee> p = depositFeeDao.findBy(criteria, DepositFee.Type.values, 0, 10);
        assertThat(p.getTotal(),is(2L));
    }

    @Test
    public void testUpdate() {
        String name = "cao!";
        String desc = "this is new desc";

        sd = getDumpSD(22L);
        sd.setDeposit(100L);
        sd.setBusiness(3L);
        sd.setType(2);
        sd.setSellerName(name);
        depositFeeDao.create(sd);

        sd.setDeposit(9393L);
        sd.setType(1);
        sd.setDescription(desc);
        depositFeeDao.update(sd);

        // find from db
        sd = depositFeeDao.get(sd.getId());

        assertThat(sd.getDeposit(), is(9393L));
        assertThat(sd.getDescription(), is(desc));
        assertThat(sd.getType(), is(1));
    }

    @Test
    public void testSelectOne() {
        sd = depositFeeDao.get(1338L);

        assertEquals(sd, null);
    }

    @Test
    public void testVoucher() {
        Date expected = DateTime.now().toDate();
        sd.setVoucher("AABBCCDD");
        sd.setVouchedAt(DateTime.now().toDate());
        depositFeeDao.vouching(sd);
        DepositFee actual = depositFeeDao.get(sd.getId());
        assertThat(actual.getVoucher(), is("AABBCCDD"));
        assertThat(actual.getVouchedAt(), DateMatchers.sameDay(expected));
    }

    @Test
    public void testReceipt() {
        sd.setReceipt("AABBCCDD");
        DateTime now = DateTime.now();
        sd.setReceiptedAt(now.toDate());


        depositFeeDao.receipting(sd);
        DepositFee actual = depositFeeDao.get(sd.getId());
        assertThat(actual.getReceipt(), is("AABBCCDD"));
        assertThat(actual.getReceiptedAt(), DateMatchers.sameDay(now.toDate()));
    }


    @Test
    public void testSummaryTechFeeOf() {
        sd = new DepositFee();
        sd.setDeposit(200L);
        sd.setSellerId(1L);
        sd.setSellerName("kao");
        sd.setBusiness(1L);
        sd.setType(DepositFee.Type.TECH_SERVICE.value());
        sd.setAuto(Boolean.TRUE);
        sd.setPaymentType(DepositFee.PaymentType.ALIPAY.value());
        sd.setSynced(DepositFee.Synced.NOT.value());
        sd.setVouched(DepositFee.Vouched.NOT.value());
        sd.setOuterCode("88888888");
        depositFeeDao.create(sd);


        Long actual = depositFeeDao.summaryTechFeeOfSeller(2L);
        assertThat(actual, is(0L));
        actual = depositFeeDao.summaryTechFeeOfSeller(1L);
        assertThat(actual, is(200L));
    }


    @Test
    public void testBatchUpdateOuterCode() {
        List<Long> ids1 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            DepositFee mock = mock();
            mock.setSellerId(8L);
            mock.setOuterCode("8888888888");

            depositFeeDao.create(mock);
            ids1.add(mock.getId());
        }

        List<Long> ids2 = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            DepositFee mock = mock();
            mock.setSellerId(9L);
            mock.setOuterCode("7777777777");

            depositFeeDao.create(mock);
            ids2.add(mock.getId());
        }

        // 测试批次更新的数据隔离性
        depositFeeDao.batchUpdateOuterCode("9999999999", 8L);

        for (Long id : ids1) {
            DepositFee depositFee =  depositFeeDao.get(id);
            assertThat(depositFee.getOuterCode(), is("9999999999"));
        }

        for (Long id : ids2) {
            DepositFee depositFee =  depositFeeDao.get(id);
            assertThat(depositFee.getOuterCode(), is("7777777777"));
        }

    }

}
