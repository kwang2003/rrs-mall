package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.dto.FatSettlement;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.trade.dao.OrderDao;
import com.aixforce.trade.model.Order;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import static com.aixforce.rrs.TestConstants.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-11 4:20 PM  <br>
 * Author: xiao
 */
@DataSet
@SuppressWarnings("all")
public class SettlementServiceTest extends BaseServiceTest {

    @SpringBean("settlementServiceImpl")
    private SettlementService settlementService;

    @SpringBean("settlementDao")
    private SettlementDao settlementDao;

    @SpringBean("orderDao")
    private OrderDao orderDao;


    @Test
    public void testConfirmed() {
        // 测试失败的情况
        Response<Boolean> actual = settlementService.confirmed(null, 1L);
        assertThat(actual.getError(), is("settlement.confirmed.id.null"));

        actual = settlementService.confirmed(1L, null);
        assertThat(actual.getError(), is("settlement.confirmed.user.id.null"));

        actual = settlementService.confirmed(1L, 2L);
        assertThat(actual.getError(), is("settlement.user.has.no.permission"));

        actual = settlementService.confirmed(1L, 1L);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));

        Settlement confirmed = settlementDao.get(1L);
        assertThat(confirmed.getSettleStatus(), is(Settlement.SettleStatus.CONFIRMED.value()));
        assertThat(confirmed.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
        assertThat(confirmed.getConfirmedAt(), notNullValue());
    }

    @Test
    public void testGenerateWithError() {
        // 测试各种失败的场景
        Response<Long> actual = settlementService.generate(null);
        assertThat(actual.getError(), is("settlement.generate.order.id.null"));

        // 普通在线支付非已经支付状态（状态码:3) 不结算
        actual = settlementService.generate(2L);
        assertThat(actual.getError(), is("settlement.generate.status.incorrect"));

        // 预售订单货到付款未完成或关闭的订单不结算
        actual = settlementService.generate(3L);
        assertThat(actual.getError(), is("settlement.generate.status.incorrect"));

        // 预售订单在线支付, 卖家正在确认退货状态, 不会进结算
        actual = settlementService.generate(4L);
        assertThat(actual.getError(), is("settlement.generate.status.incorrect"));

    }

    @Test
    public void testGenerateOk() {
        // 测试成功的场景
        Response<Long> actual = settlementService.generate(5L);
        assertThat(actual.isSuccess(), is(Boolean.TRUE));


        Long id = actual.getResult();
        // 测试数据是否实际插入到库中
        Settlement settlement = settlementDao.get(id);
        assertThat(settlement, notNullValue());

        Settlement expected = new Settlement();
        Order order = orderDao.findById(5L);
        BeanMapper.copy(order, expected);

        expected.setId(id);
        expected.setOrderId(order.getId());
        expected.setTradeStatus(order.getStatus());
        expected.setPayType(order.getPaymentType());
        expected.setSettleStatus(0);
        expected.setCashed(0);
        expected.setFinished(0);
        expected.setConfirmed(0);
        expected.setSettled(0);
        expected.setSynced(0);
        expected.setVouched(0);
        expected.setSellerName("seller");
        expected.setBuyerName("buyer");
        expected.setFinishedAt(null);
        expected.setOrderedAt(order.getCreatedAt());

        expected.setCreatedAt(settlement.getCreatedAt());
        expected.setUpdatedAt(settlement.getUpdatedAt());
        expected.setMultiPaid(settlement.getMultiPaid());
        expected.setCommissionRate(settlement.getCommissionRate());

        assertThat(settlement, is(expected));
    }

    @Test
    @DataSet("SettlementServiceTest.testQuery.xml")
    public void testFindSubsBy() {
        // 未输入订单id
        Response<Paging<ItemSettlement>> actual = settlementService.findSubsBy(null, null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 用户无权限
        actual = settlementService.findSubsBy(1L, 0, 10, NONTYPE);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 卖家查询不属于自己的订单
        actual = settlementService.findSubsBy(1L, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));


        // 查询正确的场景
        SELLER.setId(1L);
        SELLER.setName("seller");
        actual = settlementService.findSubsBy(1L, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));
    }


    @Test
    @DataSet("SettlementServiceTest.testQuery.xml")
    public void testFindBy() {

        SELLER.setId(1L);
        SELLER.setName("seller1");

        // 商户不能查询姓名
        Response<Paging<FatSettlement>> actual = settlementService.findBy("seller2", null, null, null, null, null, null, null, null, null, null, null, SELLER);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 错误的用户类型，返回空列表
        actual = settlementService.findBy(null, null, null, null, null, null, null, null, null, null, 0, 10, NONTYPE);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 错误的用户类型，返回空列表
        actual = settlementService.findBy(null, null, null, null, null, null, null, null, null, null, null, null, ADMIN);
        assertThat(actual.getResult().getTotal(), is(0L));
        assertThat(actual.getResult().getData().size(), is(0));

        // 测试ADMIN查询全部列表的场景
        actual = settlementService.findBy(null, null, null, "2000-10-01", "2118-10-11", null, null, null, null, null,  0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(3L));
        assertThat(actual.getResult().getData().size(), is(3));

        // 指定具体的商户
        actual = settlementService.findBy("seller1", null, null, "2010-10-01", "2118-10-11", null, null, null, null, null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(2L));
        assertThat(actual.getResult().getData().size(), is(2));

        // 指定到某一天,那么范围查询将失效, 只能查询指定当天的数据
        actual = settlementService.findBy(null, null, null, "2010-10-01", "2110-10-11", "2010-10-11", null, null, null, null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));


        // 指定到某一天的确认日期,那么按付款时间的查询将失效, 只能查询指定天确认的数据
        actual = settlementService.findBy(null, null, null, "2010-10-01", "2110-10-11", "2010-10-11", "2010-10-10", null, null, null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 商户只能查询自己的，且无法指定具体的商户
        SELLER.setId(2L);
        SELLER.setName("seller2");
        actual = settlementService.findBy(null, null, null, "2010-10-01", "2110-10-12", null, null, null, null, null, 0, 10, SELLER);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

        // 根据结算状态筛选
        actual = settlementService.findBy(null, null, 2, "2010-10-01", "2110-10-12", null, null, null, null, null, 0, 10, ADMIN);
        assertThat(actual.getResult().getTotal(), is(1L));
        assertThat(actual.getResult().getData().size(), is(1));

    }





}
