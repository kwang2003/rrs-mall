package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.settle.dao.AlipayCashDao;
import com.aixforce.rrs.settle.dao.SellerAlipayCashDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import org.joda.time.DateTime;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import java.util.Date;

import static com.aixforce.rrs.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-04-08 2:12 PM  <br>
 * Author: xiao
 */
@SuppressWarnings("all")
public class AlipayCashServiceTest extends BaseServiceTest {

    @SpringBean("alipayCashServiceImpl")
    private AlipayCashService alipayCashService;

    @SpringBean("alipayCashDao")
    private AlipayCashDao alipayCashDao;

    @SpringBean("settlementDao")
    private SettlementDao settlementDao;

    @SpringBean("sellerAlipayCashDao")
    private SellerAlipayCashDao sellerAlipayCashDao;

    private final Date summedAt = DateTime.parse("2010-10-10").toDate();

    @Test
    @DataSet("AlipayCashServiceTest.testQuery.xml")
    public void testFindSellerAlipayCashesBy() {
        // 测试用户没有权限的场景, 返回空列表
        Response<Paging<SellerAlipayCash>> actual = alipayCashService.findSellerAlipayCashesBy("seller1", null, null, null, Boolean.FALSE, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));

        // 错误的用户类型，返回空列表
        actual = alipayCashService.findSellerAlipayCashesBy(null, null, null, null, Boolean.FALSE, 0, 10, NONTYPE);
        assertThat(actual.getResult().getTotal(), is(0L));


        // 测试ADMIN查询全部列表的场景
        actual = alipayCashService.findSellerAlipayCashesBy(null, "2010-10-01", "2010-10-11", null, Boolean.FALSE, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(4L));
        assertThat(actual.getResult().getData().size(), is(4));

        // 指定具体的商户
        actual = alipayCashService.findSellerAlipayCashesBy("seller1", "2010-10-01", "2010-10-11", null, Boolean.FALSE, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 指定到某一天,那么范围查询将失效, 只能查询指定当天的数据
        actual = alipayCashService.findSellerAlipayCashesBy(null, "2010-10-01", "2010-10-11", "2010-10-11", Boolean.FALSE, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 商户只能查询自己的，且无法指定具体的商户
        SELLER.setId(3L);
        SELLER.setName("seller3");
        actual = alipayCashService.findSellerAlipayCashesBy(null, "2010-10-01", "2010-10-12", null, Boolean.FALSE, 0, 10,  SELLER);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));
    }


    @Test
    @DataSet("AlipayCashServiceTest.testQuery.xml")
    public void testFindBy() {
        // 测试用户没有权限的场景, 返回空列表
        Response<Paging<AlipayCash>> actual = alipayCashService.findBy(null, null, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));

        // 指定日期范围的查询
        actual = alipayCashService.findBy("2010-10-01", "2010-10-12", 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));

    }
}
