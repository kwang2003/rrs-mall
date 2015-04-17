package com.aixforce.trade.dao;

import com.aixforce.trade.model.UserVatInvoice;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-05 3:55 PM  <br>
 * Author: xiao
 */
public class UserVatInvoiceDaoTest extends BaseDaoTest {

    private UserVatInvoice u;

    @Autowired
    private UserVatInvoiceDao userVatInvoiceDao;

    @Before
    public void setUp() {
        u = new UserVatInvoice();
        u.setUserId(1L);
        u.setCompanyName("公司名称");
        u.setTaxRegisterNo("888888888888888");
        u.setRegisterAddress("杭州端点");
        u.setRegisterPhone("13511111111");
        u.setRegisterBank("端点银行");
        u.setBankAccount("88888888");
        u.setTaxCertificate("http://aixforce.com/1.jpg");
        u.setTaxpayerCertificate("http://aixforce.com/1.jpg");
        userVatInvoiceDao.create(u);
        assertThat(u.getId(), notNullValue());
        UserVatInvoice actual = userVatInvoiceDao.get(u.getId());

        u.setCreatedAt(actual.getCreatedAt());
        u.setUpdatedAt(actual.getUpdatedAt());
        assertThat(u, is(actual));
    }


    @Test
    public void testGetByUserId() {
        UserVatInvoice res = userVatInvoiceDao.getByUserId(1L);
        assertThat(res, is(u));
    }



    @Test
    public void testUpdate() {
        u.setCompanyName("公司名称2");
        u.setTaxRegisterNo("777777777777");
        u.setRegisterAddress("杭州端点2");
        u.setRegisterPhone("1358888888");
        u.setRegisterBank("端点银行2");
        u.setBankAccount("77777777");
        u.setTaxCertificate("http://aixforce.com/4.jpg");
        u.setTaxpayerCertificate("http://aixforce.com/6.jpg");

        userVatInvoiceDao.update(u);

        UserVatInvoice actual = userVatInvoiceDao.get(u.getId());
        u.setUpdatedAt(actual.getUpdatedAt());
        assertThat(u, is(actual));

    }




}
