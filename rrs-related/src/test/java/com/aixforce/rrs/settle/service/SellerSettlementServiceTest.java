package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.settle.model.SellerSettlement;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import static com.aixforce.rrs.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;



/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-11 10:41 AM  <br>
 * Author: xiao
 */
@SuppressWarnings("all")
public class SellerSettlementServiceTest extends BaseServiceTest {

    @SpringBean("sellerSettlementServiceImpl")
    private SellerSettlementService sellerSettlementService;



    @Test
    @DataSet("SellerSettlementServiceTest.testQuery.xml")
    public void testFindBy() {
        SELLER.setId(1L);
        SELLER.setName("seller1");

        // 商户不能查询姓名
        Response<Paging<SellerSettlement>> actual = sellerSettlementService.findBy("seller2", null, null, null, Boolean.FALSE, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 错误的用户类型，返回空列表
        actual = sellerSettlementService.findBy(null, null, null, null,Boolean.FALSE, 0, 10, NONTYPE);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));


        // 正确的用户类型, 各种值都未填写
        actual = sellerSettlementService.findBy(null, null, null, null, Boolean.FALSE,  null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(4L));
        assertThat(actual.getResult().getData().size(), is(4));



        // 测试ADMIN查询全部列表的场景
        actual = sellerSettlementService.findBy(null, "2000-10-01", "2118-10-11", null, Boolean.FALSE,  0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(4L));
        assertThat(actual.getResult().getData().size(), is(4));

        // 指定具体的商户
        actual = sellerSettlementService.findBy("seller1", "2010-10-01", "2118-10-11", null, Boolean.FALSE, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 指定到某一天,那么范围查询将失效, 只能查询指定当天的数据
        actual = sellerSettlementService.findBy(null, "2010-10-01", "2010-10-11", "2014-03-11", Boolean.FALSE, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));


        // 商户只能查询自己的，且无法指定具体的商户
        SELLER.setId(3L);
        SELLER.setName("seller3");
        actual = sellerSettlementService.findBy(null, "2010-10-01", "2110-10-12", null, Boolean.FALSE, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));
    }


}
