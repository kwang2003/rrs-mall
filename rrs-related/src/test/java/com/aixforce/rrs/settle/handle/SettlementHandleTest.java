package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.dao.BusinessRateDao;
import com.aixforce.rrs.settle.dao.ItemSettlementDao;
import com.aixforce.rrs.settle.dao.SettleJobDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.manager.SettlementManager;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;


/**
 *
 * 结算计算核心测试流程，本测试主要针对各项金额的计算
 *
 * 测试约定：订单总金额为 2000
 *
 * 普通订单有且仅有2个子订单，每个子订单金额1000
 * 预售订单的定金500， 尾款1500
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-16 1:55 PM  <br>
 * Author: xiao
 */
@SuppressWarnings("all")
public class SettlementHandleTest extends HandleTest {

    @InjectMocks
    private SettlementHandle settlementHandle;

    @Mock
    private BusinessRateDao businessRateDao;

    @Mock
    private SettlementDao settlementDao;

    @Mock
    private ItemSettlementDao itemSettlementDao;

    @Mock
    private SettleJobDao settleJobDao;

    @Mock
    private SettlementManager settlementManager;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * 测试普通在线支付订单为"交易完成"的场景
     */
    @Test
    public void testPlainOnlineSuccess() {
        SettleJob job = getSettleJob();

        // =============== 测试 普通-在线支付-成功 订单的场景(2个子订单成功) =====================

        // (订单)普通-在线-成功, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(20L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-成功, 货款 1000
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);

        // (子订单B) 普通-在线-成功, 货款 1000
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 1960.00	2000.00	    0.00	   20.00	0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(1960L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(20L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));


        // =============== 测试 普通-在线支付-成功 订单的场景(1个子订单成功, 1个子订单退款成功) =====================

        // (订单)普通-在线-成功, 货款 2000
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(20L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-成功, 货款 1000
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);

        // (子订单B) 普通-在线-退款成功, 货款 1000, 退款 1000
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementB.setRefundAmount(1000L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);

        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 970.00	    2000.00	    1000.00	   10.00	0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(970L));
        assertThat(settlement.getTotalExpenditure(), is(1000L));
        assertThat(settlement.getRrsCommission(), is(10L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));

        // =============== 测试 普通-在线支付-成功 订单的场景(1个子订单成功, 1个子订单退货成功) =====================

        // (订单)普通-在线-成功, 货款 2000
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(20L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-成功, 货款 1000
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);

        // (子订单B) 普通-在线-退款成功, 货款 1000, 退货 1000
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setRefundAmount(1000L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);

        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 970.00	    2000.00	    1000.00	   10.00	0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(970L));
        assertThat(settlement.getTotalExpenditure(), is(1000L));
        assertThat(settlement.getRrsCommission(), is(10L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));

    }

    /**
     * 测试普通在线支付订单为"退款成功" 或 “退货成功”的场景
     */
    @Test
    public void testPlainOnlineRefund() {
        SettleJob job = getSettleJob();

        // =============== 测试 普通-在线支付-退款成功 订单的场景(2个子订单退款成功) =====================

        // (订单)普通-在线-退款成功, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        settlement.setThirdPartyCommission(20L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-退款成功, 货款 1000
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementA.setRefundAmount(1000L);

        // (子订单B) 普通-在线-退款成功, 货款 1000
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementB.setRefundAmount(1000L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    2000.00	    2000.00	   0.00	    0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(2000L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));


        // =============== 测试 普通-在线支付-退款成功 订单的场景(2个子订单退货成功) =====================

        // (订单)普通-在线-退货成功, 货款 2000
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        settlement.setThirdPartyCommission(20L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-退货成功, 货款 1000
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementA.setRefundAmount(1000L);

        // (子订单B) 普通-在线-退货成功, 货款 1000
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setRefundAmount(1000L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    2000.00	    2000.00	   0.00	    0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(2000L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));



        // =============== 测试 普通-在线支付-退款成功 订单的场景(1个子订单退款成功， 1个退货成功) =====================

        // (订单)普通-在线-退款成功, 货款 2000
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        settlement.setThirdPartyCommission(20L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-在线-退款成功, 货款 1000
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementA.setRefundAmount(1000L);

        // (子订单B) 普通-在线-退货成功, 货款 1000
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setRefundAmount(1000L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    2000.00	    2000.00	   0.00	    0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(2000L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));
    }


    /**
     * 测试普通货到付款订单为"交易完成"的场景
     */
    @Test
    public void testPlainCodSuccess() {
        SettleJob job = getSettleJob();

        // =============== 测试 普通-货到付款-成功 订单的场景(2个子订单成功) =====================

        // (订单)普通-到货-成功, 货款 2000, 无手续费
        Settlement settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(0L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-到货-成功, 货款 1000
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);

        // (子订单B) 普通-到货-成功, 货款 1000
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    0.00	    0.00	   20.00	0.00	 0.00	    0.00        0.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(0L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(20L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(0L));

        // =============== 测试 普通-货到付款-成功 订单的场景(2个子订单成功) =====================

        // (订单)普通-到货-成功, 货款 2000, 无手续费
        settlement = getSettlement(Settlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(0L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 普通-到货-成功, 货款 1000
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.DONE);

        // (子订单B) 普通-到货-退货成功, 货款 1000
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PLAIN, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setRefundAmount(0L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -10.00	    0.00	    0.00	   10.00	0.00	 0.00	    0.00        0.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-10L));
        assertThat(settlement.getTotalEarning(), is(0L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(10L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(0L));
    }

    /**
     * 测试预售在线支付订单为"交易完成"的场景
     */
    @Test
    public void testPresellOnlineSuccess() {
        SettleJob job = getSettleJob();

        // =============== 测试 普通-货到付款-成功 订单的场景(2个子订单成功) =====================

        // (订单)预售-在线支付-成功, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(20L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-在线支付-成功, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        itemSettlementA.setFee(500L);

        // (子订单B) 预售-在线支付-成功, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.DONE);
        itemSettlementB.setFee(1500L);

        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 1960.00	2000.00	    0.00	   20.00	0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(1960L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(20L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));
    }

    /**
     * 测试预售在线支付订单为"退款成功" 或 “退货成功“的场景
     */
    @Test
    public void testPresellOnlineRefund() {
        SettleJob job = getSettleJob();

        // =============== 测试 预售-在线支付-退款成功 订单的场景(2个子订单退款成功) =====================

        // (订单)预售-在线支付-退款成功, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        settlement.setThirdPartyCommission(20L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-在线支付-退款成功, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-在线支付-退款成功, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REFUND);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setRefundAmount(1500L);
        itemSettlementB.setThirdPartyCommission(15L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    2000.00	    2000.00	   0.00	    0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(2000L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));


        // =============== 测试 预售-在线支付-退货成功 订单的场景(2个子订单退款成功) =====================

        // (订单)预售-在线支付-退货成功, 货款 2000
        settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        settlement.setThirdPartyCommission(20L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-在线支付-退货成功, 定金 500
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-在线支付-退货成功, 尾款 1500
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setRefundAmount(1500L);
        itemSettlementB.setThirdPartyCommission(15L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 -20.00	    2000.00	    2000.00	   0.00	    0.00	 0.00	    0.00        20.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-20L));
        assertThat(settlement.getTotalEarning(), is(2000L));
        assertThat(settlement.getTotalExpenditure(), is(2000L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(20L));
    }

    /**
     * 测试预售在线支付订单各种扣除定金的场景
     */
    @Test
    public void testPresellOnlineDeduction() {
        SettleJob job = getSettleJob();

        // =============== 测试 预售-在线支付-退款成功 尾款超时 =====================

        // (订单)预售-在线支付-尾款超时, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        settlement.setThirdPartyCommission(5L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-在线支付-尾款超时, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-在线支付-尾款超时, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 495.00	    500.00	    0.00	   0.00	    0.00	 495.00	    5.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(495L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(495L));
        assertThat(settlement.getPresellCommission(), is(5L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
        assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.CONFIRMED.value()));
        assertThat(settlement.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
        assertThat(settlement.getConfirmedAt(), notNullValue());




        // =============== 测试 预售-在线支付-卖家付定金关闭交易 =====================

        // (订单)预售-在线支付-卖家付定金关闭交易, 货款 2000
        settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        settlement.setThirdPartyCommission(5L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-在线支付-卖家付定金关闭交易, 定金 500
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-在线支付-卖家付定金关闭交易, 尾款 1500
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 495.00	    500.00	    0.00	   0.00	    0.00	 495.00	    5.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(495L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(495L));
        assertThat(settlement.getPresellCommission(), is(5L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
        assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.CONFIRMED.value()));
        assertThat(settlement.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
        assertThat(settlement.getConfirmedAt(), notNullValue());
    }



    /**
     * 测试预售货到付款订单成功的场景
     */
    @Test
    public void testPresellCodSuccess() {
        SettleJob job = getSettleJob();

        // =============== 测试 预售-在线支付-交易完成 =====================

        // (订单)预售-货到付款-交易完成, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        settlement.setThirdPartyCommission(5L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-货到付款-交易完成, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        itemSettlementA.setFee(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-货到付款-交易完成, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.COD, Settlement.TradeStatus.DONE);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 475.00	    500.00	    0.00	   20.00	0.00	 0.00	    0.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(475L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(20L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
    }


    /**
     * 测试预售货到付款订单成功的场景
     */
    @Test
    public void testPresellCodRefund() {
        SettleJob job = getSettleJob();

        // =============== 测试 预售-在线支付-交易完成 =====================

        // (订单)预售-货到付款-交易完成, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        settlement.setThirdPartyCommission(5L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-货到付款-交易完成, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-货到付款-交易完成, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 475.00	    500.00	    0.00	   20.00	0.00	 0.00	    0.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(-5L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(500L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(0L));
        assertThat(settlement.getPresellCommission(), is(0L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
    }




    /**
     * 测试预售货到付款订单各种扣除定金的场景
     */
    @Test
    public void testPresellCodDeduction() {
        SettleJob job = getSettleJob();

        // =============== 测试 预售-货到付款-尾款超时 =====================

        // (订单)预售-货到付款-尾款超时, 货款 2000
        Settlement settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        settlement.setThirdPartyCommission(5L);
        Paging<Settlement> paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-货到付款-尾款超时, 定金 500
        ItemSettlement itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-货到付款-尾款超时, 尾款 1500
        ItemSettlement itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.COD, Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 495.00	    500.00	    0.00	   0.00	    0.00	 495.00	    5.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(495L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(495L));
        assertThat(settlement.getPresellCommission(), is(5L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
        assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.CONFIRMED.value()));
        assertThat(settlement.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
        assertThat(settlement.getConfirmedAt(), notNullValue());




        // =============== 测试 预售-货到付款-卖家付定金关闭交易 =====================

        // (订单)预售-货到付款-卖家付定金关闭交易, 货款 2000
        settlement = getSettlement(Settlement.Type.PRE_SELL, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        settlement.setThirdPartyCommission(5L);
        paging = new Paging<Settlement>(1L, Lists.newArrayList(settlement));

        // (子订单A) 预售-货到付款-卖家付定金关闭交易, 定金 500
        itemSettlementA = getItemSettlement(ItemSettlement.Type.PRESELL_DEPOSIT, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        itemSettlementA.setFee(500L);
        itemSettlementA.setRefundAmount(500L);
        itemSettlementA.setThirdPartyCommission(5L);


        // (子订单B) 预售-货到付款-卖家付定金关闭交易, 尾款 1500
        itemSettlementB = getItemSettlement(ItemSettlement.Type.PRESELL_REST, Settlement.PayType.ONLINE, Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER);
        itemSettlementB.setFee(1500L);
        itemSettlementB.setThirdPartyCommission(0L);


        when(settlementDao.findBy(anyMap())).thenReturn(paging);
        when(itemSettlementDao.list(settlement.getOrderId())).thenReturn(Lists.newArrayList(itemSettlementA, itemSettlementB));
        settlementHandle.settlement(job);


        // 货款	     商户收入    	收入(元)	    支出(元)	   平台佣金	积分收入	 预售金扣除  营业成本	    手续费
        // 2000.00	 495.00	    500.00	    0.00	   0.00	    0.00	 495.00	    5.00        5.00
        assertThat(settlement.getFee(), is(2000L));
        assertThat(settlement.getSellerEarning(), is(495L));
        assertThat(settlement.getTotalEarning(), is(500L));
        assertThat(settlement.getTotalExpenditure(), is(0L));
        assertThat(settlement.getRrsCommission(), is(0L));
        assertThat(settlement.getScoreEarning(), is(0L));
        assertThat(settlement.getPresellDeposit(), is(495L));
        assertThat(settlement.getPresellCommission(), is(5L));
        assertThat(settlement.getThirdPartyCommission(), is(5L));
        assertThat(settlement.getSettleStatus(), is(Settlement.SettleStatus.CONFIRMED.value()));
        assertThat(settlement.getConfirmed(), is(Settlement.Confirmed.DONE.value()));
        assertThat(settlement.getConfirmedAt(), notNullValue());
        assertThat(settlement.getSettled(), is(Settlement.Settled.DONE.value()));
        assertThat(settlement.getSettledAt(), notNullValue());
    }










}
