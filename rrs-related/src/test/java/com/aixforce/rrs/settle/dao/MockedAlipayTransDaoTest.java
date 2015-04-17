package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.MockedAlipayTrans;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-22 2:02 PM  <br>
 * Author: xiao
 */
public class MockedAlipayTransDaoTest extends BaseDaoTest {

    @Autowired
    private MockedAlipayTransDao dao;
    private MockedAlipayTrans a;


    private DateTime now = DateTime.now();
    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


    private MockedAlipayTrans mock() {
        MockedAlipayTrans mock = new MockedAlipayTrans();
        mock.setBalance("1000.00");
        mock.setBankAccountName("张三");
        mock.setBankAccountNo("6228098765432109877");
        mock.setBankName("中国工商银行");
        mock.setCurrency("156");
        mock.setDepositBankNo("2012145258965236");
        mock.setIncome("1000.00");
        mock.setIwAccountLogId("340005462320");
        mock.setMemo("备注");
        mock.setMerchantOutOrderNo("1925,1231");
        mock.setOtherAccountEmail("other@aixforce.com");
        mock.setOtherAccountFullname("xx公司");
        mock.setOtherUserId("2088001368431897");
        mock.setOutcome("1000.00");
        mock.setPartnerId("2088101118137074");
        mock.setSellerAccount("买家支付宝人民币支付帐号");
        mock.setSellerFullname("");
        mock.setServiceFee("1000.00");
        mock.setServiceFeeRatio("0.01");
        mock.setTotalFee("10000.00");
        mock.setTradeNo("2012050726014177");
        mock.setTradeRefundAmount("1.00");
        mock.setTransAccount("20881010118937220156");
        mock.setTransCodeMsg("转账");
        mock.setTransDate(DFT.print(now));
        mock.setTransOutOrderNo("2012050726014177");
        mock.setSubTransCodeMsg("账户冻结");
        mock.setSignProductName("无卡支付");
        mock.setRate("0.003");
        return mock;
    }


    @Before
    public void setUp() {
        a = mock();
        dao.create(a);
        assertThat(a.getId(), notNullValue());
        MockedAlipayTrans actual = dao.get(a.getId());
        a.setCreatedAt(actual.getCreatedAt());
        a.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(a));
    }

    @Test
    public void testGet() {
        a.getId();
    }

    @Test
    public void testGetBy() {
        MockedAlipayTrans criteria = new MockedAlipayTrans();
        criteria.setIwAccountLogId("340005462320");

        MockedAlipayTrans actual = dao.getBy(criteria);
        assertThat(actual, notNullValue());

    }

    @Test
    public void testFindByTradeNo() {
        List<MockedAlipayTrans> actual = dao.findByTradeNo("2012050726014177");
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testFindByMerchantNo() {
        List<MockedAlipayTrans> actual = dao.findByMerchantNo("1925,1231");
        assertThat(actual.size(), is(1));
    }

    @Test
    public void testFindBy() {
        MockedAlipayTrans criteria = new MockedAlipayTrans();
        criteria.setCreatedAt(now.toDate());
        Paging<MockedAlipayTrans> actual = dao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData().get(0), is(a));


        criteria = new MockedAlipayTrans();
        criteria.setCreatedAt(now.minusDays(1).toDate());
        actual = dao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(0L));
    }

}
