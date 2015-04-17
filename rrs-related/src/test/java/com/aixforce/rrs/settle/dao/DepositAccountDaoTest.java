package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.DepositAccount;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-21 10:52 AM  <br>
 * Author: xiao
 */
public class DepositAccountDaoTest extends BaseDaoTest {

    @Autowired
    private DepositAccountDao depositAccountDao;

    private static DepositAccount da;

    @Before
    public void setUp() {
        da = new DepositAccount();
        da.setSellerId(1L);
        da.setSellerName("商家账户");
        da.setShopId(1L);
        da.setShopName("商家店铺");

        da.setBusiness(1L);
        da.setOuterCode("8888888888");
        da.setBalance(200L);

        depositAccountDao.create(da);
        assertThat(da.getId(), notNullValue());
    }

    @Test
    public void testFindBy() {
        DepositAccount criteria = new DepositAccount();
        criteria.setSellerId(da.getSellerId());
        criteria.setBusiness(1L);
        Paging<DepositAccount> p = depositAccountDao.findBy(criteria, 0, 1000,  0, 10);
        assertThat(p.getTotal(),is(1L));
    }

    @Test
    public void testFindBySellerId() {
        DepositAccount da = depositAccountDao.findBySellerId(1L);
        assertThat(da, notNullValue());
        assertThat(da.getSellerName(), is("商家账户"));
    }

    @Test
    public void testUpdateBal() {
        depositAccountDao.updateBal(da.getId(), da.getBalance() - 100);
        da = depositAccountDao.findBySellerId(1L);
        assertThat(da.getBalance(), is(100L));

    }

    @Test
    public void testUpdateOuterCode() {
        int success = depositAccountDao.updateOuterCode("99999999", 1L);
        assertThat(success, greaterThan(0));

        DepositAccount depositAccount = depositAccountDao.get(da.getId());
        assertThat(depositAccount.getOuterCode(), is("99999999"));
    }

}
