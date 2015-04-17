package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.dao.BusinessRateDao;
import com.aixforce.rrs.settle.dao.ItemSettlementDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.model.BusinessRate;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notNull;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 *
 * 处理结算的Handle
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 9:05 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SettlementHandle extends JobHandle {


    private final LoadingCache<Long, BusinessRate> self = CacheBuilder.newBuilder().build(new CacheLoader<Long, BusinessRate>() {
        @Override
        public BusinessRate load(Long business) throws Exception {
            return businessRateDao.findByBusiness(business);
        }
    });

    @Autowired
    private BusinessRateDao businessRateDao;

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;


    /**
     * 按分页指定结算日的结算可结算(已完成&已提现)的数据(若未指定日期则默认T-1) <br/>
     * 结算完成的事项如下：<br/>
     * 0、计算当天营收金额 营收金额 = 当天收入 - 当天支出 <br/>
     * 1、计算当天的交易佣金
     *  a、非货到付款：交易佣金 = 营收金额 x 佣金润点(例如: 1%) ，营收为0则不计算佣金，(单位：分) 小数点四舍五入处理 <br/>
     *  b、货到付款：交易佣金 = 订单金额 x 佣金润点(例如: 1%) (单位：分) 小数点后四舍五入 <br/>
     * 2、计算积分（暂未实现）<br/>
     * 3、计算预售定金扣除(定金不计入商家收入）<br/>
     * 4、计算商家收入 商家收入 = 营收金额 - 交易佣金 - 积分转换(未实现) - 第三方（如支付宝）手续费  <br/>
     *
     * 注: 类型为预售定金的子订单如果因各种原因（超时、关闭等）被标记为“扣除定金”， 则不计算佣金，仅扣除第三方手续费用 <br/>
     *
     *
     * @param job        任务
     */
    public void settlement(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;     // 完成的任务无需再次处理

        log.info("[SETTLEMENT] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            checkState(dependencyOk(job), "job.dependency.not.over");

            settleJobDao.ing(job.getId());
            self.invalidateAll();   // 重载所有汇率

            Integer pageNo = 1;
            boolean next = batchSettlement(job.getDoneAt(), pageNo, BATCH_SIZE);
            while (next) {
                pageNo ++;
                next = batchSettlement(job.getDoneAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[SETTLEMENT] successfully done");

        } catch (IllegalStateException e) {
            log.error("[SETTLEMENT] failed with job:{}, error:{}", job, e.getMessage());
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[SETTLEMENT] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[SETTLEMENT] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }


    /**
     * 结算订单
     *
     * @param size     批次数量
     * @return  是否存在下一个批次
     */
    private boolean batchSettlement(Date doneAt, Integer pateNo, Integer size) {
        List<Settlement> settlements = getSettlements(doneAt, pateNo, size);
        for (Settlement settlement : settlements ) {
            try {
                if (equalWith(settlement.getSettled(), Settlement.Settled.DONE.value())) continue;      // 已结算的订单不处理
                if (equalWith(settlement.getCashed(), Settlement.Cashed.NOT.value())) continue;         // 未提现的订单不处理
                if (equalWith(settlement.getFinished(), Settlement.Finished.NOT.value())) continue;     // 未结束的订单不处理

                doSettlement(settlement);
            } catch (IllegalStateException e) {
                log.error("fail to settle with settlement:{}, error:{}", settlement, e.getMessage());
            } catch (Exception e) {
                log.error("fail to settle with settlement:{}", settlement, e);
            }
        }

        int current = settlements.size();
        return current == size;
    }

    private List<Settlement> getSettlements(Date doneAt, Integer pateNo, Integer size) {
        Date paidStartAt = new DateTime(doneAt).minusDays(60).toDate();
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(5);
        PageInfo pageInfo = new PageInfo(pateNo, size);
        params.put("paidStartAt", paidStartAt);
        params.put("paidEndAt", doneAt);
        params.put("offset", pageInfo.offset);
        params.put("limit", pageInfo.limit);
        Paging<Settlement> settlementPaging = settlementDao.findBy(params);
        return settlementPaging.getData();
    }

    /**
     * 结算订单
     * @param settlement    结算信息
     */
    private void doSettlement(Settlement settlement) {
        List<ItemSettlement> subs = itemSettlementDao.list(settlement.getOrderId());
        Long totalEarning = 0L;
        Long totalExpenditure = 0L;
        Long sellerEarning = 0L;
        Long rrsCommission = 0L;
        Long scoreEarning = 0L;
        Long presellDeposit = 0L;

        Long thirdPartyCommission;


        Integer type = settlement.getType();
        if (type == null) { return; }
        if (!Objects.equal(type, Order.Type.PLAIN.value()) && !Objects.equal(type, Order.Type.PRE_SELL.value())) {
            return;
        }

        Date settledAt = DateTime.now().toDate();

        for (ItemSettlement sub : subs) {  //计算子订单结算金额
            doSettlementItem(sub);
            sub.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value()); //标记子订单状态为"待商户确认"
            sub.setSettledAt(settledAt);

            totalEarning += sub.getTotalEarning();                  // 累计收入
            totalExpenditure += sub.getTotalExpenditure();          // 累计支出
            sellerEarning += sub.getSellerEarning();                // 累计商户收入
            rrsCommission += sub.getRrsCommission();                // 累计平台佣金
            scoreEarning += sub.getScoreEarning();                  // 累计积分收入
            presellDeposit += sub.getPresellDeposit();              // 累计营业外收入
        }

        checkState(notNull(settlement.getThirdPartyCommission()), "third.party.commission.empty");
        thirdPartyCommission = settlement.getThirdPartyCommission();

        if (Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value())
                ||Objects.equal(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value())) {
            // 卖家关闭交易，及付定金超时需要扣定金
            presellDeposit = totalEarning - totalExpenditure - thirdPartyCommission;
            sellerEarning = sellerEarning - presellDeposit;

        }

        // 商家收入 = 累计子订单金额 + 预售金收入（扣除） - 手续费
        sellerEarning = sellerEarning + presellDeposit - thirdPartyCommission;
        settlement.setTotalEarning(totalEarning);
        settlement.setTotalExpenditure(totalExpenditure);
        settlement.setSellerEarning(sellerEarning);
        settlement.setRrsCommission(rrsCommission);
        settlement.setScoreEarning(scoreEarning);
        settlement.setPresellDeposit(presellDeposit);

        if (presellDeposit > 0) {  // 若预售金手续费大于0,则取第三方手续费作为营业外收入
            settlement.setPresellCommission(thirdPartyCommission);
        } else {
            settlement.setPresellCommission(0L);
        }

        settlement.setThirdPartyCommission(thirdPartyCommission);
        settlement.setSettledAt(settledAt);



        // 补充逻辑，产生预售定金扣除的订单，无需商户确认直接更新为已确认
        if (equalWith(settlement.getType(), Settlement.Type.PRE_SELL.value()) && settlement.getPresellDeposit() > 0) {
            settlement.setSettleStatus(Settlement.SettleStatus.CONFIRMED.value());    // 标记为待商户确认
            settlement.setSettled(Settlement.Settled.DONE.value());
            settlement.setConfirmed(Settlement.Confirmed.DONE.value());
            settlement.setConfirmedAt(DateTime.now().toDate());
        } else {
            settlement.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value());    // 标记为待商户确认
            settlement.setSettled(Settlement.Settled.DONE.value());
        }


        settlementManager.update(settlement, subs);
    }



    /**
     * 计算子订单需要结算的各项金额
     *
     * @param sub 子订单结算信息
     */
    private void doSettlementItem(ItemSettlement sub) {
        Integer type = sub.getType();
        sub.setThirdPartyCommission(sub.getThirdPartyCommission() == null ? 0 : sub.getThirdPartyCommission());

        if (type == OrderItem.Type.PLAIN.value()) {  // 普通订单
            calculatePlainOrder(sub);

        } else if (type == OrderItem.Type.PRESELL_DEPOSIT.value()) {  // 预售定金
            calculatePresellDeposit(sub);

        } else if (type == OrderItem.Type.PRESELL_REST.value()) { // 预售尾款
            calculatePresellRest(sub);

        } else {  //无法处理的结算类型
            log.error("unhandled type = {}", type);
            throw new IllegalStateException("settle.sub.type.incorrect");
        }
    }


    /**
     * 结算普通订单
     *
     * 1. 在线支付 <br/>
     *  a.成功  <br/>
     *  收入 = 货款  <br/>
     *  支出 = 0  <br/>
     *  佣金 = 货款 * 佣金扣点  <br/> <br/>
     *
     *  b.退货退款  <br/>
     *  收入 = 货款  <br/>
     *  支出 = 退款金额   <br/>
     *  佣金  = 0   <br/><br/>
     *
     * 2. 货到付款 <br/>
     *  a.成功  <br/>
     *  收入 = 0  <br/>
     *  支出 = 0  <br/>
     *  佣金 = 货款 * 佣金扣点  <br/><br/>
     *
     *  b.退款退货  <br/>
     *  收入 = 0  <br/>
     *  支出 = 0  <br/>
     *  佣金 = 0  <br/><br/>
     *
     * 佣金计算公式:  <br/>
     *  a、非货到付款：交易佣金 = 营收金额 x 佣金润点(例如: 1%) ，营收为0则不计算佣金，(单位：分) 小数点四舍五入处理 <br/>
     *  b、货到付款：交易佣金 = 订单金额 x 佣金润点(例如: 1%) (单位：分) 小数点后四舍五入 <br/>
     *
     * 计算商家收入 商家收入 = 收入 - 支出 - 交易佣金 - 积分转换(未实现) - 预售金扣除 <br/>
     *
     */
    private void calculatePlainOrder(ItemSettlement sub) {
        Long scoreEarning = 0L;               // 积分收入
        Long totalExpenditure = 0L;           // 当天支出
        Long totalEarning = 0L;               // 当天收入
        Long rrsCommission = 0L;              // 平台佣金

        Double rate = sub.getCommissionRate();
        checkState(notNull(rate), "settlement.commission.rate.empty");

        int payType = sub.getPayType();
        if (Objects.equal(payType, Settlement.PayType.ONLINE.value())) {  // 在线支付
            totalEarning = sub.getFee();    // 收入 = 货款
            if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REFUND.value())
                    || Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS.value())) { // 退货或者退款
                totalExpenditure = sub.getRefundAmount();
            } else if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.DONE.value())) {   // 成功
                rrsCommission =   Math.round(totalEarning * rate);  // 营收金额 x 佣金润点
            }

        } else if (Objects.equal(payType, Settlement.PayType.COD.value())) { // 货到付款
            if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.DONE.value())) {  // 完成需要计算佣金
                rrsCommission =  Math.round(sub.getFee() * rate);  // 货款 x 佣金润点
            }

        }

        Long sellerEarning = totalEarning - totalExpenditure - scoreEarning - rrsCommission;    // 商户收入
        sub.setTotalEarning(totalEarning);
        sub.setTotalExpenditure(totalExpenditure);
        sub.setSellerEarning(sellerEarning);
        sub.setRrsCommission(rrsCommission);
        sub.setScoreEarning(scoreEarning);
        sub.setPresellDeposit(0L);
        sub.setPresellCommission(0L);
        sub.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value());    // 标记为待确认
    }

    /**
     * 结算预售定金(无货到付款) <br/>
     *
     * 成功 <br/>
     *  收入 = 定金  <br/>
     *  支出 = 0  <br/>
     *  佣金 = 定金 * 佣金扣点  <br/><br/>
     *
     * 全额退款  <br/>
     *  收入 = 定金 <br/>
     *  支出 = 定金 <br/>
     *  佣金 = 0 <br/><br/>
     *
     * 扣除定金（尾款支付超时）<br/>
     *  收入 = 定金 <br/>
     *  支出 = 0 <br/>
     *  佣金 = 0 <br/>
     *  预售定金扣除 = 定金 - 手续费 <br/><br/>
     *
     *
     * 计算商家收入 商家收入 = 营收金额 - 交易佣金 - 积分转换(未实现,填0) - 预售金扣除 <br/>
     *
     */
    private void calculatePresellDeposit(ItemSettlement sub) {

        Long totalExpenditure = 0L;              // 当天支出
        Long totalEarning = sub.getFee();   // 收入 = 定金
        Long scoreEarning = 0L;             // 积分收入
        Long rrsCommission = 0L;            // 平台佣金
        Long thirdPartyCommission  = sub.getThirdPartyCommission();
        Long presellDeposit = 0L;
        Long presellCommission = 0L;

        Double rate = sub.getCommissionRate();
        checkState(notNull(rate), "settlement.commission.rate.empty");


        if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.DONE.value())) {  // 成功单
            rrsCommission = Math.round(sub.getFee() * rate);   //计算佣金 = 货款 *  扣点

        } else if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REFUND.value())  // 退货、退款
                || Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS.value()))  {
            totalExpenditure = sub.getRefundAmount();   // 支出 = 退款
        } else if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value())
                ||(Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value()))) {
            // 买家关闭交易,付尾款超时，扣定金, 定金应该等于货款
            presellDeposit = sub.getFee();
            presellCommission = sub.getThirdPartyCommission();

        } else {   // 其他状态
            totalEarning = 0L;
        }

        Long sellerEarning = totalEarning - totalExpenditure - scoreEarning  - rrsCommission;   // 计算商户收入
        sub.setTotalEarning(totalEarning);
        sub.setTotalExpenditure(totalExpenditure);
        sub.setSellerEarning(sellerEarning);
        sub.setRrsCommission(rrsCommission);
        sub.setScoreEarning(scoreEarning);
        sub.setThirdPartyCommission(thirdPartyCommission);

        // 营业外收入和营收成本
        sub.setPresellDeposit(presellDeposit);
        sub.setPresellCommission(presellCommission);

        sub.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value());    // 标记为待确认
    }

    /**
     * 结算预售尾款
     *
     * 成功 <br/>
     *  收入 = 尾款 <br/>
     *  支出 = 0  <br/>
     *  佣金 = 尾款 * 佣金扣点 <br/><br/>
     *
     * 全额退款 <br/>
     *  收入 = 尾款  <br/>
     *  支出 = 尾款  <br/>
     *  佣金 = 0  <br/> <br/>
     *
     * 计算商家收入 商家收入 = 营收金额 - 交易佣金 - 积分转换(未实现) <br/>
     *
     */
    private void calculatePresellRest(ItemSettlement sub) {
        Long totalExpenditure;           // 当天支出
        Long totalEarning;               // 当天收入
        Long scoreEarning = 0L;           // 积分收入
        Long rrsCommission;              // 平台佣金
        Long thirdPartyCommission  = sub.getThirdPartyCommission();
        Long presellDeposit = 0L;
        Long presellCommission = 0L;


        Long sellerEarning;              // 商户收入
        Double rate = sub.getCommissionRate();
        checkState(notNull(rate), "settlement.commission.rate.empty");


        totalEarning = sub.getFee();

        if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.DONE.value())) {  // 成功单
            totalExpenditure = 0L; // 无支出
            rrsCommission = Math.round(sub.getFee() * rate);   //计算佣金 = 尾款 *  扣点
        } else if (Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REFUND.value())
                || Objects.equal(sub.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_RETURN_GOODS.value())) {
            totalExpenditure = sub.getFee();
            rrsCommission = 0L;
        } else {
            totalEarning = 0L;
            totalExpenditure = 0L;
            rrsCommission = 0L;
        }

        if (Objects.equal(sub.getPayType(), Settlement.PayType.COD.value())) { // 货到付款的订单无支出
            totalEarning = 0L;
            totalExpenditure = 0L;
        }

        sellerEarning = totalEarning - totalExpenditure - scoreEarning - rrsCommission;
        sub.setTotalEarning(totalEarning);
        sub.setTotalExpenditure(totalExpenditure);
        sub.setSellerEarning(sellerEarning);
        sub.setRrsCommission(rrsCommission);
        sub.setScoreEarning(scoreEarning);
        sub.setThirdPartyCommission(thirdPartyCommission);
        sub.setPresellDeposit(presellDeposit);
        sub.setPresellCommission(presellCommission);
        sub.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value());    // 标记为待确认
    }




}
