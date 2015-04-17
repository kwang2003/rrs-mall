package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.settle.dao.DepositAccountDao;
import com.aixforce.rrs.settle.dao.DepositFeeDao;
import com.aixforce.rrs.settle.model.DepositAccount;
import com.aixforce.rrs.settle.model.DepositFee;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import static com.aixforce.rrs.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-10 2:00 PM  <br>
 * Author: xiao
 */
@DataSet
@SuppressWarnings("all")
public class DepositFeeServiceTest extends BaseServiceTest{


    @SpringBean("depositFeeServiceImpl")
    private DepositFeeService depositFeeService;

    @SpringBean("depositAccountDao")
    private DepositAccountDao depositAccountDao;

    @SpringBean("depositFeeDao")
    private DepositFeeDao depositFeeDao;


    @Test
    public void testCreateWithError() {
        // 用户没有权限
        Response<Long> actual = depositFeeService.create(new DepositFee(), SELLER);
        assertThat(actual.getError(), is("user.has.no.permission"));

        // 没传金额
        DepositFee creating = new DepositFee();
        creating.setPaymentType(DepositFee.PaymentType.ALIPAY.value());
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("deposit.create.fee.null"));

        // 没传商户名
        creating.setDeposit(10000L);
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("deposit.create.seller.name.null"));

        // 没传type
        creating.setSellerName("seller");
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("deposit.create.type.null"));

        // 传了错误的type
        creating.setType(99);
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("deposit.fee.type.incorrect"));

        // 找不到商户
        creating.setType(DepositFee.Type.INCREMENT.value());
        creating.setSellerName("Anonymous");
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("user.not.found"));

        // 金额为负数
        creating.setType(DepositFee.Type.INCREMENT.value());
        creating.setSellerName("manWithNoAccount");
        creating.setDeposit(-10000L);
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.getError(), is("deposit.create.lt0"));
    }

    @Test
    public void testCreateWithNoAccount() {
        // 没有保证金账户会创建保证金账户
        DepositFee creating = new DepositFee();
        creating.setDeposit(10000L);
        creating.setSellerName("manWithNoAccount");
        creating.setType(DepositFee.Type.INCREMENT.value());
        creating.setPaymentType(DepositFee.PaymentType.ALIPAY.value());

        Response<Long> actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        // 账户被成功插入, 并且余额已累计
        DepositAccount accountOfTheMan = depositAccountDao.findBySellerId(2L);
        assertThat(accountOfTheMan, notNullValue());
        assertThat(accountOfTheMan.getBalance(), is(10000L));

        // 保证金记录被正确创建
        Long id = actual.getResult();
        DepositFee feeOfMan = depositFeeDao.get(id);
        creating.setCreatedAt(feeOfMan.getCreatedAt());
        creating.setUpdatedAt(feeOfMan.getUpdatedAt());

        creating.setOrdered(0);
        creating.setSynced(DepositFee.Synced.NOT.value());
        creating.setVouched(DepositFee.Vouched.NOT.value());
        creating.setReceipted(DepositFee.Vouched.NOT.value());
        assertThat(feeOfMan, is(creating));
    }


    @Test
    public void testCreateOk() {
        DepositFee creating = new DepositFee();
        creating.setDeposit(10000L);
        creating.setSellerName("manWithAll");
        creating.setType(DepositFee.Type.INCREMENT.value());
        creating.setPaymentType(DepositFee.PaymentType.ALIPAY.value());

        // 账户余额增加 10000
        Response<Long> actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        Long id = actual.getResult();
        DepositFee feeOfMan = depositFeeDao.get(id);
        assertThat(feeOfMan, notNullValue());
        DepositAccount account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(10000L));

        // 扣 500 保证金
        creating.setDeposit(500L);
        creating.setType(DepositFee.Type.DEDUCTION.value());
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        id = actual.getResult();
        feeOfMan = depositFeeDao.get(id);
        assertThat(feeOfMan, notNullValue());

        account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(9500L));

        // 返还 500
        creating.setDeposit(500L);
        creating.setType(DepositFee.Type.REFUND.value());
        actual = depositFeeService.create(creating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        id = actual.getResult();
        feeOfMan = depositFeeDao.get(id);
        assertThat(feeOfMan, notNullValue());

        account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(9000L));
    }


    @Test
    @DataSet("DepositFeeServiceTest.testUpdate.xml")
    public void testUpdateWithError() {
        // 用户没有权限
        DepositFee depositFee = new DepositFee();
        depositFee.setSynced(DepositFee.Synced.NOT.value());

        Response<Long> actual = depositFeeService.update(depositFee, SELLER);
        assertThat(actual.getError(), is("user.has.no.permission"));

        // 没传id
        DepositFee updating = new DepositFee();
        actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.getError(), is("deposit.update.id.null"));

        // 没传金额
        updating.setId(1L);
        actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.getError(), is("deposit.update.fee.null"));

        // 没传type
        updating.setDeposit(10000L);
        actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.getError(), is("deposit.can.not.modify"));


        // 填了凭证号的不允许修改
        updating.setType(DepositFee.Type.INCREMENT.value());
        actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.getError(), is("deposit.can.not.modify"));


        // 没有找到记录
        updating.setId(999L);
        updating.setType(DepositFee.Type.INCREMENT.value());
        actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.getError(), is("deposit.not.found"));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testUpdate.xml")
    public void testUpdateIncrement() {
        DepositFee updating = new DepositFee();
        updating.setId(2L);
        updating.setDeposit(500L);
        updating.setType(DepositFee.Type.INCREMENT.value());

        Response<Long> actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));


        DepositFee fee = depositFeeDao.get(2L);
        assertThat(fee.getDeposit(), is(500L));
        DepositAccount account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(9500L));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testUpdate.xml")
    public void testUpdateDeduction() {
        DepositFee updating = new DepositFee();
        updating.setId(3L);
        updating.setDeposit(400L);
        updating.setType(DepositFee.Type.DEDUCTION.value());

        Response<Long> actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        DepositFee fee = depositFeeDao.get(3L);
        assertThat(fee.getDeposit(), is(400L));
        DepositAccount account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(10100L));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testUpdate.xml")
    public void testUpdateRefund() {
        DepositFee updating = new DepositFee();
        updating.setId(4L);
        updating.setDeposit(400L);
        updating.setType(DepositFee.Type.REFUND.value());

        Response<Long> actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        DepositFee fee = depositFeeDao.get(4L);
        assertThat(fee.getDeposit(), is(400L));
        DepositAccount account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(10100L));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testUpdate.xml")
    public void testUpdateTechFee() {
        DepositFee updating = new DepositFee();
        updating.setId(5L);
        updating.setDeposit(500L);
        updating.setType(DepositFee.Type.TECH_SERVICE.value());

        Response<Long> actual = depositFeeService.update(updating, ADMIN);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        DepositFee fee = depositFeeDao.get(5L);
        assertThat(fee.getDeposit(), is(500L));
        DepositAccount account = depositAccountDao.findBySellerId(1L);
        assertThat(account.getBalance(), is(10000L));
    }



    @Test
    @DataSet("DepositFeeServiceTest.testQuery.xml")
    public void testFindDepositDetailByName() {
        // 用户无权限
        Response<Paging<DepositFee>> actual = depositFeeService.findDepositDetailByName("seller2", null, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 商户可以查询自己
        SELLER.setId(1L);
        SELLER.setName("seller1");

        actual = depositFeeService.findDepositDetailByName(null, null, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));


        // admin 可以查指定商户
        actual = depositFeeService.findDepositDetailByName("seller3", null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));


        // 可以查指定行业
        actual = depositFeeService.findDepositDetailByName(null, 2L, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(3L));
        assertThat(actual.getResult().getData().size(), is(3));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testQuery.xml")
    public void testFindTechFeeDetailByName() {
        // 用户无权限
        Response<Paging<DepositFee>> actual = depositFeeService.findTechFeeDetailByName("seller2", null, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 商户可以查询自己
        SELLER.setId(1L);
        SELLER.setName("seller1");

        actual = depositFeeService.findTechFeeDetailByName(null, null, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));


        // admin 可以查指定商户
        actual = depositFeeService.findTechFeeDetailByName("seller3", null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));


        // 可以查指定行业
        actual = depositFeeService.findTechFeeDetailByName(null, 2L, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(3L));
        assertThat(actual.getResult().getData().size(), is(3));
    }


    @Test
    @DataSet("DepositFeeServiceTest.testQuery.xml")
    public void testFindBaseDetailByName() {
        // 用户无权限
        Response<Paging<DepositFee>> actual = depositFeeService.findBaseDetailByName(null, null, NONTYPE);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 用户
        SELLER.setId(1L);
        SELLER.setName("seller1");
        actual = depositFeeService.findBaseDetailByName(null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));
    }

    @Test
    @DataSet("DepositFeeServiceTest.testQuery.xml")
    public void testFindDepositDetailByID() {
        // 用户无权限
        Response<DepositFee> actual = depositFeeService.findDepositDetailByID(null);
        assertThat(actual.getError(), is("deposit.fee.id.null"));


        actual = depositFeeService.findDepositDetailByID(9999L);
        assertThat(actual.getError(), is("deposit.fee.not.found"));

        actual = depositFeeService.findDepositDetailByID(1L);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));
        assertThat(actual.getResult(), notNullValue());
    }

}
