package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.*;
import com.aixforce.rrs.settle.model.*;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * 结算计算核心测试流程，本测试主要针对订单提现逻辑
 *
 *
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-14 4:13 PM  <br>
 * Author: xiao
 */
public class CashSummaryHandleTest extends HandleTest {


    @InjectMocks
    @Spy
    private CashSummaryHandle cashSummaryHandle;
    @Mock
    private ItemSettlementDao itemSettlementDao;
    @Mock
    private SettlementDao settlementDao;
    @Mock
    private SettleJobDao settleJobDao;
    @Mock
    private OrderAlipayCashDao orderAlipayCashDao;
    @Mock
    private AlipayCashDao alipayCashDao;
    @Mock
    private ShopService shopService;
    @Mock
    private AccountService<User> accountService;
    @Mock
    private OrderQueryService orderQueryService;
    @Mock
    private CashSummaryHandle.CashFactory cashFactory;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * 测试普通订单生成提现记录的场景
     */
    @Test
    public void testPlainOrderCashSuccess() {
        SettleJob job = getSettleJob();
        User seller = getUser(User.TYPE.SELLER);
        Shop shop = getShop();


        // 跳过预售订单的测试逻辑
        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_DEPOSIT, job.getTradedAt(), 1, 100)).thenReturn(Paging.empty(ItemSettlement.class));
        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_REST, job.getTradedAt(), 1, 100)).thenReturn(Paging.empty(ItemSettlement.class));

        // 跳过退款的测试逻辑
        Response<Paging<OrderItem>> orderItemRes = new Response<Paging<OrderItem>>();
        orderItemRes.setResult(Paging.empty(OrderItem.class));
        when(orderQueryService.findRefundedOrderItemInDate(any(Date.class), anyInt(), anyInt())).thenReturn(orderItemRes);

        // 跳过商户日汇总的逻辑
        Response<Paging<Shop>> shopPagingRes = new Response<Paging<Shop>>();
        shopPagingRes.setResult(Paging.empty(Shop.class));
        when(shopService.findBy(anyListOf(Integer.class), anyInt(), anyInt())).thenReturn(shopPagingRes);

        // 跳过日汇总逻辑
        AlipayCash alipayCash = new AlipayCash();
        alipayCash.setCashTotalCount(0);
        when(orderAlipayCashDao.summaryCashesDaily(any(Date.class))).thenReturn(alipayCash);


        // =============== 测试 普通-在线支付-完成支付 订单的场景(2个子订单成功) =====================

        // (订单)普通-在线-成功, 货款 2000, 手续费20
        Settlement settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.PAID);
        settlement.setThirdPartyCommission(20L);
        settlement.setCashed(Settlement.Cashed.NOT.value());
        settlement.setSettleStatus(Settlement.SettleStatus.NOT.value());
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        Settlement criteria = new Settlement();
        criteria.setPaidAt(job.getTradedAt());
        criteria.setType(Settlement.Type.PLAIN.value());

        when(settlementDao.findBy(criteria, 0, 100)).thenReturn(paging);


        Response<User> userRes = new Response<User>();
        userRes.setResult(seller);
        when(accountService.findUserById(seller.getId())).thenReturn(userRes);
        Response<Shop> shopRes = new Response<Shop>();
        shopRes.setResult(shop);
        when(shopService.findByUserId(seller.getId())).thenReturn(shopRes);

        OrderAlipayCash actual = spy(new OrderAlipayCash());
        actual.setType(Order.Type.PLAIN.value());
        actual.setCashType(OrderAlipayCash.CashType.PLAIN.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PLAIN)).thenReturn(actual);


        // 测试
        cashSummaryHandle.summaryAlipayCashes(job);

        // 总金额   提现金额  手续费   (普通订单提现)
        // 2000    1980     20
        assertThat(actual.getAlipayFee(), is(20L));
        assertThat(actual.getTotalFee(), is(2000L));
        assertThat(actual.getCashFee(), is(1980L));
        assertThat(actual.getRefundFee(), is(0L));

        assertThat(actual.getBuyerId(), is(2L));
        assertThat(actual.getBuyerName(), is("买家"));
        assertThat(actual.getSellerId(), is(1L));
        assertThat(actual.getSellerName(), is("卖家"));
        assertThat(actual.getStatus(), is(OrderAlipayCash.Status.NOT.value()));
        assertThat(actual.getType(), is(Settlement.Type.PLAIN.value()));
        assertThat(actual.getCashType(), is(OrderAlipayCash.CashType.PLAIN.value()));



        // =============== 测试 普通-货到付款-交易成功 订单的场景(2个子订单成功) =====================
        // (订单)普通-在线-成功, 货款 2000, 手续费20
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(0L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));
        when(settlementDao.findBy(criteria, 0, 100)).thenReturn(paging);

        actual = spy(new OrderAlipayCash());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PLAIN)).thenReturn(actual);


        Settlement updating = spy(new Settlement());
        when(cashSummaryHandle.newSettlement()).thenReturn(updating);

        cashSummaryHandle.summaryAlipayCashes(job);

        // 不会生成结算单，但会更新结算单状态为 "已提现"
        verify(orderAlipayCashDao, times(1)).create(any(OrderAlipayCash.class));
        assertThat(updating.getId(), is(settlement.getId()));
        assertThat(updating.getCashed(), is(Settlement.Cashed.DONE.value()));
        verify(settlementDao, times(1)).update(updating);
    }

    /**
     * 测试预售订单生成提现记录的场景
     */
    @Test
    public void testPresellOrderCashSuccess() {
        SettleJob job = getSettleJob();
        User seller = getUser(User.TYPE.SELLER);
        Shop shop = getShop();

        Response<User> userRes = new Response<User>();
        userRes.setResult(seller);
        when(accountService.findUserById(seller.getId())).thenReturn(userRes);


        Response<Shop> shopRes = new Response<Shop>();
        shopRes.setResult(shop);
        when(shopService.findByUserId(seller.getId())).thenReturn(shopRes);


        // 跳过普通订单的测试逻辑
        when(settlementDao.findBy(any(Settlement.class), anyInt(), anyInt())).thenReturn(Paging.empty(Settlement.class));

        // 跳过退款的测试逻辑
        Response<Paging<OrderItem>> orderItemRes = new Response<Paging<OrderItem>>();
        orderItemRes.setResult(Paging.empty(OrderItem.class));
        when(orderQueryService.findRefundedOrderItemInDate(any(Date.class), anyInt(), anyInt())).thenReturn(orderItemRes);

        // 跳过店铺日汇总的额逻辑
        Response<Paging<Shop>> shopPagingRes = new Response<Paging<Shop>>();
        shopPagingRes.setResult(Paging.empty(Shop.class));
        when(shopService.findBy(anyListOf(Integer.class), anyInt(), anyInt())).thenReturn(shopPagingRes);

        // 跳过日汇总的逻辑
        AlipayCash alipayCash = new AlipayCash();
        alipayCash.setCashTotalCount(0);
        when(orderAlipayCashDao.summaryCashesDaily(any(Date.class))).thenReturn(alipayCash);


        // =============== 测试 预售-在线支付-完成支付 订单的场景(2个子订单成功) =====================
        // (订单)预售-在线支付-成功, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(20L);

        // (子订单A) 预售-在线支付-成功, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        itemSettlementA.setFee(500L);
        itemSettlementA.setThirdPartyCommission(5L);

        // (子订单B) 预售-在线支付-成功, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(15L);


        List<ItemSettlement> deposits = Lists.newArrayList(itemSettlementA);
        List<ItemSettlement> rests = Lists.newArrayList(itemSettlementB);

        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_DEPOSIT, job.getTradedAt(), 1, 100)).thenReturn(new Paging<ItemSettlement>(1L, deposits));
        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_REST, job.getTradedAt(), 1, 100)).thenReturn(new Paging<ItemSettlement>(1L, rests));

        OrderAlipayCash depositCash = spy(new OrderAlipayCash());
        depositCash.setType(Settlement.Type.PRE_SELL.value());
        depositCash.setCashType(OrderAlipayCash.CashType.PRESELL_DEPOSIT.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_DEPOSIT)).thenReturn(depositCash);

        OrderAlipayCash restCash = spy(new OrderAlipayCash());
        restCash.setType(Settlement.Type.PRE_SELL.value());
        restCash.setCashType(OrderAlipayCash.CashType.PRESELL_REST.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_REST)).thenReturn(restCash);


        // 测试
        cashSummaryHandle.summaryAlipayCashes(job);


        // 总金额   提现金额  手续费   (预售定金提现)
        // 500     495      5
        assertThat(depositCash.getTotalFee(), is(500L));
        assertThat(depositCash.getCashFee(), is(495L));
        assertThat(depositCash.getAlipayFee(), is(5L));
        assertThat(depositCash.getRefundFee(), is(0L));

        assertThat(depositCash.getBuyerId(), is(2L));
        assertThat(depositCash.getBuyerName(), is("买家"));
        assertThat(depositCash.getSellerId(), is(1L));
        assertThat(depositCash.getSellerName(), is("卖家"));
        assertThat(depositCash.getStatus(), is(OrderAlipayCash.Status.NOT.value()));

        assertThat(depositCash.getType(), is(Settlement.Type.PRE_SELL.value()));
        assertThat(depositCash.getCashType(), is(OrderAlipayCash.CashType.PRESELL_DEPOSIT.value()));

        // 总金额   提现金额  手续费   (预售尾款提现)
        // 1500    1485     15
        assertThat(restCash.getTotalFee(), is(1500L));
        assertThat(restCash.getCashFee(), is(1485L));
        assertThat(restCash.getAlipayFee(), is(15L));
        assertThat(restCash.getRefundFee(), is(0L));

        assertThat(depositCash.getBuyerId(), is(2L));
        assertThat(depositCash.getBuyerName(), is("买家"));
        assertThat(depositCash.getSellerId(), is(1L));
        assertThat(depositCash.getSellerName(), is("卖家"));
        assertThat(depositCash.getStatus(), is(OrderAlipayCash.Status.NOT.value()));

        assertThat(restCash.getType(), is(Settlement.Type.PRE_SELL.value()));
        assertThat(restCash.getCashType(), is(OrderAlipayCash.CashType.PRESELL_REST.value()));
    }

    @Test
    public void testPresellOrderCashRefundSuccess() {
        SettleJob job = getSettleJob();
        User buyer = getUser(User.TYPE.BUYER);
        User seller = getUser(User.TYPE.SELLER);
        Shop shop = getShop();

        Response<User> buyerRes = new Response<User>();
        buyerRes.setResult(buyer);
        when(accountService.findUserById(buyer.getId())).thenReturn(buyerRes);

        Response<User> sellerRes = new Response<User>();
        sellerRes.setResult(seller);
        when(accountService.findUserById(seller.getId())).thenReturn(sellerRes);


        Response<Shop> shopRes = new Response<Shop>();
        shopRes.setResult(shop);
        when(shopService.findByUserId(seller.getId())).thenReturn(shopRes);

        // 跳过普通订单的测试逻辑
        when(settlementDao.findBy(any(Settlement.class), anyInt(), anyInt())).thenReturn(Paging.empty(Settlement.class));

        // 跳过预售订单的测试逻辑
        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_DEPOSIT, job.getTradedAt(), 1, 100)).thenReturn(Paging.empty(ItemSettlement.class));
        when(itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_REST, job.getTradedAt(), 1, 100)).thenReturn(Paging.empty(ItemSettlement.class));

        // 跳过店铺日汇总的额逻辑
        Response<Paging<Shop>> shopPagingRes = new Response<Paging<Shop>>();
        shopPagingRes.setResult(Paging.empty(Shop.class));
        when(shopService.findBy(anyListOf(Integer.class), anyInt(), anyInt())).thenReturn(shopPagingRes);

        // 跳过日汇总的逻辑
        AlipayCash alipayCash = new AlipayCash();
        alipayCash.setCashTotalCount(0);
        when(orderAlipayCashDao.summaryCashesDaily(any(Date.class))).thenReturn(alipayCash);



        // (订单)普通-在线支付-子订单, 货款 1000, 退款 1000
        OrderItem plainRefund = getOrderItem(1L, 1L, OrderItem.Type.PLAIN.value());
        plainRefund.setFee(1000);
        plainRefund.setRefundAmount(1000);
        plainRefund.setRefundAt(job.getTradedAt());

        // (订单)预售-在线支付-定金, 货款 500, 退款 500
        OrderItem depositRefund = getOrderItem(2L, 2L, OrderItem.Type.PRESELL_DEPOSIT.value());
        depositRefund.setFee(500);
        depositRefund.setRefundAmount(500);
        depositRefund.setRefundAt(job.getTradedAt());

        // (订单)预售-在线支付-定金, 货款 1500, 退款 1500
        OrderItem restRefund = getOrderItem(2L, 2L, OrderItem.Type.PRESELL_REST.value());
        restRefund.setFee(1500);
        restRefund.setRefundAmount(1500);
        restRefund.setRefundAt(job.getTradedAt());

        Paging<OrderItem> refundPaging = new Paging<OrderItem>(3L, Lists.newArrayList(plainRefund, depositRefund, restRefund));
        Response<Paging<OrderItem>> refundRes = new Response<Paging<OrderItem>>();
        refundRes.setResult(refundPaging);
        when(orderQueryService.findRefundedOrderItemInDate(job.getTradedAt(), 1, 100)).thenReturn(refundRes);


        // 注入测试数据
        OrderAlipayCash plainRefundCash = spy(new OrderAlipayCash());
        plainRefundCash.setType(Order.Type.PLAIN.value());
        plainRefundCash.setCashType(OrderAlipayCash.CashType.PLAIN_REFUND.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PLAIN_REFUND)).thenReturn(plainRefundCash);

        OrderAlipayCash depositRefundCash = spy(new OrderAlipayCash());
        depositRefundCash.setType(Order.Type.PRE_SELL.value());
        depositRefundCash.setCashType(OrderAlipayCash.CashType.PRESELL_DEPOSIT_REFUND.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_DEPOSIT_REFUND)).thenReturn(depositRefundCash);


        OrderAlipayCash restRefundCash = spy(new OrderAlipayCash());
        restRefundCash.setType(Order.Type.PRE_SELL.value());
        restRefundCash.setCashType(OrderAlipayCash.CashType.PRESELL_REST_REFUND.value());
        when(cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_REST_REFUND)).thenReturn(restRefundCash);

        // 测试
        cashSummaryHandle.summaryAlipayCashes(job);


        // 总金额   提现金额  手续费  退款  (普通子订单退款提现)
        // 0       -1000    0      1000
        assertThat(plainRefundCash.getTotalFee(), is(0L));
        assertThat(plainRefundCash.getCashFee(), is(-1000L));
        assertThat(plainRefundCash.getAlipayFee(), is(0L));
        assertThat(plainRefundCash.getRefundFee(), is(1000L));

        assertThat(plainRefundCash.getBuyerId(), is(2L));
        assertThat(plainRefundCash.getBuyerName(), is("买家"));
        assertThat(plainRefundCash.getSellerId(), is(1L));
        assertThat(plainRefundCash.getSellerName(), is("卖家"));
        assertThat(plainRefundCash.getStatus(), is(OrderAlipayCash.Status.NOT.value()));

        assertThat(plainRefundCash.getType(), is(Settlement.Type.PLAIN.value()));
        assertThat(plainRefundCash.getCashType(), is(OrderAlipayCash.CashType.PLAIN_REFUND.value()));


        // 总金额   提现金额  手续费  退款  (预售定金退款提现)
        // 0       -500     0      -500
        assertThat(depositRefundCash.getTotalFee(), is(0L));
        assertThat(depositRefundCash.getCashFee(), is(-500L));
        assertThat(depositRefundCash.getAlipayFee(), is(0L));
        assertThat(depositRefundCash.getRefundFee(), is(500L));

        assertThat(depositRefundCash.getBuyerId(), is(2L));
        assertThat(depositRefundCash.getBuyerName(), is("买家"));
        assertThat(depositRefundCash.getSellerId(), is(1L));
        assertThat(depositRefundCash.getSellerName(), is("卖家"));
        assertThat(depositRefundCash.getStatus(), is(OrderAlipayCash.Status.NOT.value()));

        assertThat(depositRefundCash.getType(), is(Settlement.Type.PRE_SELL.value()));
        assertThat(depositRefundCash.getCashType(), is(OrderAlipayCash.CashType.PRESELL_DEPOSIT_REFUND.value()));



        // 总金额   提现金额  手续费  退款  (预售定金退款提现)
        // 0       -1500     0      -1500
        assertThat(restRefundCash.getTotalFee(), is(0L));
        assertThat(restRefundCash.getCashFee(), is(-1500L));
        assertThat(restRefundCash.getAlipayFee(), is(0L));
        assertThat(restRefundCash.getRefundFee(), is(1500L));

        assertThat(restRefundCash.getBuyerId(), is(2L));
        assertThat(restRefundCash.getBuyerName(), is("买家"));
        assertThat(restRefundCash.getSellerId(), is(1L));
        assertThat(restRefundCash.getSellerName(), is("卖家"));
        assertThat(restRefundCash.getStatus(), is(OrderAlipayCash.Status.NOT.value()));

        assertThat(restRefundCash.getType(), is(Settlement.Type.PRE_SELL.value()));
        assertThat(restRefundCash.getCashType(), is(OrderAlipayCash.CashType.PRESELL_REST_REFUND.value()));

    }



    private OrderItem getOrderItem(Long id, Long orderId, Integer type) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBuyerId(2L);
        orderItem.setSellerId(1L);
        orderItem.setId(id);
        orderItem.setOrderId(orderId);
        orderItem.setType(type);
        orderItem.setPayType(OrderItem.PayType.ONLINE.value());
        return orderItem;
    }


}
