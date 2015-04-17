package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.settle.model.DailySettlement;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import static com.aixforce.rrs.TestConstants.ADMIN;
import static com.aixforce.rrs.TestConstants.SELLER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-10 6:40 PM  <br>
 * Author: xiao
 */
@SuppressWarnings("all")
public class DailySettlementServiceTest extends BaseServiceTest {

    @SpringBean("dailySettlementServiceImpl")
    private DailySettlementService dailySettlementService;


    @Test
    @DataSet("DailySettlementServiceTest.testQuery.xml")
    public void testFindBy() {

        // 没权限返回空列表
        Response<Paging<DailySettlement>> actual = dailySettlementService.findBy(null, null, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 指定日期范围查询
        actual = dailySettlementService.findBy("2000-10-10",  "2915-10-12", null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(3L));
        assertThat(actual.getResult().getData().size(), is(3));


        // 指定日期范围查询
        actual = dailySettlementService.findBy("2014-10-10", "2014-10-12", 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(3L));
        assertThat(actual.getResult().getData().size(), is(3));
    }
}
