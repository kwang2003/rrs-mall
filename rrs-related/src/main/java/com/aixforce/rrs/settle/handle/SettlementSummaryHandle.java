package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.DailySettlementDao;
import com.aixforce.rrs.settle.dao.SellerSettlementDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.DailySettlement;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.shop.model.Shop;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.isEmpty;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 9:52 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SettlementSummaryHandle extends JobHandle {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private DailySettlementDao dailySettlementDao;

    @Autowired
    private DepositManager depositManager;




    /**
     * 商家日汇总
     *
     * @param job  任务信息
     */
    public void summarySettlements(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;     // 完成的任务无需再次处理

        log.info("[SUMMARY-SETTLEMENTS] job begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            Date confirmedAt = job.getTradedAt();     // 商户确认时间

            int pageNo = 1;
            boolean next = batchSummarySellerSettlements(confirmedAt, pageNo, BATCH_SIZE);   // 拉取JDE数据
            while (next) {
                pageNo ++;
                next = batchSummarySellerSettlements(confirmedAt, pageNo, BATCH_SIZE);
            }
            summaryDailySettlements(confirmedAt);
            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));

        } catch (IllegalStateException e) {
            log.error("[SUMMARY-SETTLEMENTS] failed with job:{}, error:{} ", job, e.getMessage());
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[SUMMARY-SETTLEMENTS] failed with job:{}, cause:{} ", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[SUMMARY-SETTLEMENTS] done at {} cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    /**
     * 汇总每日订单
     *
     * @param confirmedAt  商户确认时间
     */
    private void summaryDailySettlements(Date confirmedAt) {
        Date startAt = new DateTime(confirmedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(startAt).plusDays(1).toDate();
        DailySettlement dailySettlement = settlementDao.sumDailySettlement(startAt, endAt);       // 汇总每日数据
        fillDailySettlementWithZero(dailySettlement);

        if (dailySettlement == null) {  // 若当天没有汇总数据，则生成一条空数据
            dailySettlement = DailySettlement.empty();
        }

        dailySettlement.setConfirmedAt(startAt);
        dailySettlementDao.create(dailySettlement);
    }


    /**
     * 批量汇总各商户的订单
     *
     * @param confirmedAt   确认时间
     * @param pageNo        批次号
     * @param size          每批数据处理的数据量
     * @return  是否还包含下一批数据
     */
    @SuppressWarnings("unchecked")
    private boolean batchSummarySellerSettlements(Date confirmedAt, int pageNo, int size) {
        List<Integer> statuses = Lists.newArrayList(Shop.Status.FAIL.value(), Shop.Status.FROZEN.value(), Shop.Status.OK.value());
        Response<Paging<Shop>> shopQueryResult = shopService.findBy(statuses, pageNo, size);
        checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());

        Paging<Shop> paging = shopQueryResult.getResult();
        List<Shop> shops = paging.getData();

        if (equalWith(paging.getTotal(), 0L) || CollectionUtils.isEmpty(shops)) {
            return Boolean.FALSE;
        }

        for (Shop shop : shops) {   // 汇总每日商户确认的数据

            try {
                if (!equalWith(shop.getStatus(), Shop.Status.OK.value())) {
                    log.info("shop (id:{}, name:{}, status:{}) not ok, skipped", shop.getId(), shop.getName(), shop.getStatus());
                    continue;
                }

                Date startAt = new DateTime(confirmedAt).withTimeAtStartOfDay().toDate();
                Date endAt = new DateTime(startAt).plusDays(1).toDate();
                SellerSettlement sellerSettlement = settlementDao.sumSellerSettlement(shop.getUserId(), startAt, endAt);


                // 无订单的商户直接默认已结算
                if (sellerSettlement.getOrderCount() == 0L) {
                    continue;
                } else {
                    sellerSettlement.setSettleStatus(SellerSettlement.SettleStatus.NOT.value());
                    sellerSettlement.setConfirmed(SellerSettlement.Confirmed.NOT.value());
                    sellerSettlement.setSynced(SellerSettlement.Synced.NOT.value());
                    sellerSettlement.setVouched(SellerSettlement.Vouched.NOT.value());
                    sellerSettlement.setReceipted(SellerSettlement.Receipted.NOT.value());
                }

                // 获取商家8码
                String outerCode = getOuterCodeOfShop(shop);

                if (isEmpty(outerCode) || outerCode.length() != 10) {  // 88码为空或不正确，则不创建
                    log.info("shop (id:{}, name:{}, status:{}) outerCode {} is null or incorrect, skipped",
                            shop.getId(), shop.getName(), shop.getStatus(), outerCode);
                    continue;
                }


                sellerSettlement.setOuterCode(outerCode);

                Long business = getBusinessOfShop(shop);
                sellerSettlement.setBusiness(business);

                sellerSettlement.setConfirmedAt(startAt);
                sellerSettlement.setSellerId(shop.getUserId());
                sellerSettlement.setSellerName(shop.getUserName());
                fillSellerSettlementWithZero(sellerSettlement);


                // 如果存在预售定金沉淀，那么手续费中需要扣除相应的部分
                if (sellerSettlement.getPresellDeposit() > 0) {   // 实际手续费 = 支付宝手续费 - 营收成本
                    Long thirdPartyCommission = sellerSettlement.getThirdPartyCommission();
                    Long presellCommission = sellerSettlement.getPresellCommission();
                    sellerSettlement.setThirdPartyCommission(thirdPartyCommission - presellCommission);
                }


                // 创建的时候需要判断该商户ID有没有已经汇总，防止重复汇总
                if (everyAmountZero(sellerSettlement)) {  // 若各项收支都为0,  则不创建汇总
                    log.info("sellerSettlements (sellerId:{}, sellerName:{}) all amount is 0, skipped",
                            sellerSettlement.getSellerId(), sellerSettlement.getSellerName());
                    continue;
                }

                sellerSettlementDao.create(sellerSettlement);
                createDeductionDepositIfNeed(sellerSettlement, outerCode, business);

            } catch (IllegalStateException e) {
                log.error("fail to summary seller settlements with seller:{}, confirmedAt:{}, error:{}",
                        shop, confirmedAt, e.getMessage());
            } catch (Exception e) {
                log.error("fail to summary seller settlements with seller:{}, confirmedAt:{}, cause:{}",
                        shop, confirmedAt, Throwables.getStackTraceAsString(e));
            }
        }

        int current = shops.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    private boolean everyAmountZero(SellerSettlement sellerSettlement) {
        return equalWith(sellerSettlement.getTotalEarning(), 0L)
                && equalWith(sellerSettlement.getTotalExpenditure(), 0L)
                && equalWith(sellerSettlement.getSellerEarning(), 0L)
                && equalWith(sellerSettlement.getRrsCommission(), 0L)
                && equalWith(sellerSettlement.getScoreEarning(), 0L)
                && equalWith(sellerSettlement.getPresellDeposit(), 0L)
                && equalWith(sellerSettlement.getThirdPartyCommission(), 0L);
    }


    private void createDeductionDepositIfNeed(SellerSettlement sellerSettlement, String outerCode, Long business) {
        if (sellerSettlement.getSellerEarning()  < 0) {  // 生成扣款的单据
            DepositFee deduction = new DepositFee();

            deduction.setSellerId(sellerSettlement.getSellerId());
            deduction.setSellerName(sellerSettlement.getSellerName());
            deduction.setOuterCode(outerCode);
            deduction.setBusiness(business);

            deduction.setDeposit(Math.abs(sellerSettlement.getSellerEarning()));
            deduction.setType(DepositFee.Type.DEDUCTION.value());
            deduction.setPaymentType(DepositFee.PaymentType.ALIPAY.value());
            deduction.setDescription(DFT.print(new DateTime(sellerSettlement.getConfirmedAt())) + ":商户汇总负收入，自动扣除保证金");
            deduction.setAuto(Boolean.TRUE);

            depositManager.createDepositFee(deduction);
        }
    }


    /**
     * 将值为null的金额填充0
     *
     * @param settlement    卖家汇总信息
     */
    private void fillSellerSettlementWithZero(SellerSettlement settlement) {
        settlement.setTotalEarning(settlement.getTotalEarning() == null ? 0L : settlement.getTotalEarning());
        settlement.setTotalExpenditure(settlement.getTotalExpenditure() == null ? 0L : settlement.getTotalExpenditure());
        settlement.setSellerEarning(settlement.getSellerEarning() == null ? 0L : settlement.getSellerEarning());
        settlement.setRrsCommission(settlement.getRrsCommission() == null ? 0L : settlement.getRrsCommission());
        settlement.setScoreEarning(settlement.getScoreEarning() == null ? 0L : settlement.getScoreEarning());
        settlement.setPresellDeposit(settlement.getPresellDeposit() == null ? 0L : settlement.getPresellDeposit());
        settlement.setPresellCommission(settlement.getPresellCommission() == null ? 0L : settlement.getPresellCommission());
        settlement.setThirdPartyCommission(settlement.getThirdPartyCommission() == null ? 0L : settlement.getThirdPartyCommission());
    }

    /**
     * 将值为null的金额填充0
     *
     * @param settlement    卖家汇总信息
     */
    private void fillDailySettlementWithZero(DailySettlement settlement) {
        settlement.setTotalEarning(settlement.getTotalEarning() == null ? 0L : settlement.getTotalEarning());
        settlement.setTotalExpenditure(settlement.getTotalExpenditure() == null ? 0L : settlement.getTotalExpenditure());
        settlement.setSellerEarning(settlement.getSellerEarning() == null ? 0L : settlement.getSellerEarning());
        settlement.setRrsCommission(settlement.getRrsCommission() == null ? 0L : settlement.getRrsCommission());
        settlement.setScoreEarning(settlement.getScoreEarning() == null ? 0L : settlement.getScoreEarning());
        settlement.setPresellDeposit(settlement.getPresellDeposit() == null ? 0L : settlement.getPresellDeposit());
        settlement.setPresellCommission(settlement.getPresellCommission() == null ? 0L : settlement.getPresellCommission());
        settlement.setThirdPartyCommission(settlement.getThirdPartyCommission() == null ? 0L : settlement.getThirdPartyCommission());
    }


}
