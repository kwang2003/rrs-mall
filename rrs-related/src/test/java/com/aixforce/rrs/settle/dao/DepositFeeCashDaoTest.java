package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.DepositFeeCash;
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
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-20 12:07 PM  <br>
 * Author: xiao
 */
public class DepositFeeCashDaoTest extends BaseDaoTest {

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    private static DepositFeeCash da;

    private DepositFeeCash mock() {
        DepositFeeCash da = new DepositFeeCash();
        da.setDepositId(1L);
        da.setSellerId(1L);
        da.setSellerName("卖家");
        da.setShopId(1L);
        da.setShopName("店铺");

        da.setCashFee(10000L);
        da.setStatus(DepositFeeCash.Status.NOT.value());
        da.setSynced(DepositFeeCash.Synced.NOT.value());
        da.setVouched(DepositFeeCash.Vouched.NOT.value());
        da.setSyncedAt(DateTime.now().toDate());
        return da;
    }

    @Before
    public void setUp() throws Exception {
        da = mock();
        depositFeeCashDao.create(da);
        assertThat(da.getId(), notNullValue());


        DepositFeeCash actual = depositFeeCashDao.get(da.getId());
        da.setCreatedAt(actual.getCreatedAt());
        da.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(da));
    }

    @Test
    public void testGetByDepositId() {
        DepositFeeCash actual = depositFeeCashDao.getByDepositId(1L);
        assertThat(actual, Matchers.notNullValue());
        assertThat(actual, is(da));
    }

    @Test
    public void testBatchUpdateOuterCode() {

        List<Long> ids1 = Lists.newArrayList();
        for (Integer i = 0; i < 10; i++) {
            DepositFeeCash mock = mock();
            mock.setSellerId(8L);
            mock.setOuterCode("8888888888");

            Long depositId = i.longValue() + 10L;
            mock.setDepositId(depositId);

            depositFeeCashDao.create(mock);
            ids1.add(mock.getId());
        }

        List<Long> ids2 = Lists.newArrayList();
        for (Integer i = 0; i < 10; i++) {
            DepositFeeCash mock = mock();
            mock.setSellerId(9L);
            mock.setOuterCode("7777777777");

            Long depositId = i.longValue() + 30L;
            mock.setDepositId(depositId);

            depositFeeCashDao.create(mock);
            ids2.add(mock.getId());
        }

        // 测试批次更新的数据隔离性
        depositFeeCashDao.batchUpdateOuterCode("9999999999", 8L);

        for (Long id : ids1) {
            DepositFeeCash cash =  depositFeeCashDao.get(id);
            assertThat(cash.getOuterCode(), is("9999999999"));
        }

        for (Long id : ids2) {
            DepositFeeCash cash =  depositFeeCashDao.get(id);
            assertThat(cash.getOuterCode(), is("7777777777"));
        }
    }


    @Test
    public void testCashing() {
        boolean success = depositFeeCashDao.cashing(da);
        assertThat(success, is(Boolean.TRUE));

        DepositFeeCash actual = depositFeeCashDao.get(da.getId());
        assertThat(actual.getStatus(), is(DepositFeeCash.Status.DONE.value()));
    }

}
