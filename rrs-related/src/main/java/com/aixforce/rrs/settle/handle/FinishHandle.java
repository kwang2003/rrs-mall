package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.OrderAlipayCashDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderQueryService;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
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
import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.aixforce.rrs.settle.util.SettlementVerification.done;
import static com.aixforce.rrs.settle.util.SettlementVerification.isCod;
import static com.aixforce.rrs.settle.util.SettlementVerification.isPlain;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 9:28 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class FinishHandle extends JobHandle {


    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private OrderQueryService orderQueryService;



    /**
     * 标记 T-1 已完成（已完成 & 已提现）订单的结算状态为 “已关闭” <br/>
     * 关闭订单的同时需要更新对应的交易状态
     *
     * @param job  任务信息
     */
    public void markSettlementFinished(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[MARK-SETTLEMENT-FINISHED] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            Integer pageNo = 1;
            boolean next = batchMarkSettlementFinished(job.getDoneAt(), pageNo, BATCH_SIZE);

            while (next) {
                pageNo++;
                next = batchMarkSettlementFinished(job.getDoneAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[MARK-SETTLEMENT-FINISHED] successfully done");


        } catch (IllegalStateException e) {
            log.error("[MARK-SETTLEMENT-FINISHED] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[MARK-SETTLEMENT-FINISHED] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[MARK-SETTLEMENT-FINISHED] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    /**
     * 批量标记 已完成
     *
     * @param size     批次数量
     * @return  是否存在下一个批次
     */
    private boolean batchMarkSettlementFinished(Date doneAt, int pageNo, int size) {
        // 获取所有 “已提现”、但 “未结算” 的订单
        List<Settlement> settlements = getSettlements(doneAt, pageNo, size);
        int current = settlements.size();

        for (Settlement settlement : settlements) {
            try {
                if (equalWith(settlement.getSettleStatus(), Settlement.SettleStatus.ING.value())) {
                    log.info("settlement(id:{}) has been settling, skipped", settlement);
                    continue;
                }

                // 标记订单结束
                markAsFinished(settlement);
            } catch (IllegalArgumentException e) {
                log.error("mark settlement({}) as finished raise error", settlement, e);
            }
        }
        return current == size;  // 判断是否存在下一个要处理的批次
    }

    /**
     * 如果此订单所有提现明细都已经提现，则标记"已提现"，否则标记为"未提现"
     * @param settlement 订单结算信息
     */
    private void markAsCashedAfFinished(Settlement settlement) {
        List<OrderAlipayCash> cashes = orderAlipayCashDao.findByOrderId(settlement.getOrderId());
        Boolean cashed = Boolean.TRUE;

        if (isEmpty(cashes)) {
            cashed = Boolean.FALSE;
        }

        for (OrderAlipayCash cash : cashes) {
            if (!equalWith(cash.getStatus(), OrderAlipayCash.Status.DONE.value())) {  // 只要有一笔提现未提现，标记订单为“未提现”
                cashed = Boolean.FALSE;
                break;
            }
        }

        if (cashed) {
            log.info("settlement(id:{}, orderId:{} sellerId:{}) cashed",
                    settlement.getId(), settlement.getOrderId(), settlement.getSellerId());
            settlement.setCashed(Settlement.Cashed.DONE.value());

        } else {
            log.info("settlement(id:{}, orderId:{} sellerId:{}) not cashed yet",
                    settlement.getId(), settlement.getOrderId(), settlement.getSellerId());
            settlement.setCashed(Settlement.Cashed.NOT.value());
        }
    }


    private List<Settlement> getSettlements(Date doneAt, int pageNo, int size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Date startAt = new DateTime(doneAt).minusDays(60).toDate();
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("paidStartAt", startAt);
        params.put("paidEndAt", doneAt);
        params.put("offset", pageInfo.offset);
        params.put("limit", pageInfo.limit);

        Paging<Settlement> settlementPaging = settlementDao.findBy(params);
        return settlementPaging.getData();
    }


    /**
     * 标记指定的订单信息为已结束
     *
     * @param settlement 订单结算信息
     */
    private void markAsFinished(Settlement settlement) {
        try {

            Long orderId = settlement.getOrderId();
            Response<Order> orderQueryResult = orderQueryService.findById(orderId);
            checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());

            Order order = orderQueryResult.getResult();
            if (order.getFinishedAt() == null) {    // 订单尚未关闭
                return;
            }
            //判断是否 标记为已提现
            markAsCashedAfFinished(settlement);

            settlement.setFinished(Settlement.Finished.DONE.value());
            settlement.setFinishedAt(order.getFinishedAt());     // 记录订单完成时间
            settlement.setTradeStatus(order.getStatus());   // 记录订单关闭时的交易状态


            if (equalWith(settlement.getCashed(), Settlement.Cashed.DONE.value())) {  // 当提现完成 标记结算中
                settlement.setSettleStatus(Settlement.SettleStatus.ING.value());
            }

            if (isPlain(order) && isCod(order) && done(order)) {    // 普通货到付款订单直接已提现，且标记结算中
                settlement.setCashed(Settlement.Cashed.DONE.value());
                settlement.setSettleStatus(Settlement.SettleStatus.ING.value());
            }

            // 创建子订单的结算信息
            settlementManager.finished(settlement);

        } catch (Exception e) {
            log.error("fail to finish settlement({})", settlement, e);
            markFinishedFail(settlement);
        }
    }


    /**
     * 判断所有提现明细是否都已经提现
     *
     * @param settlement 结算明细
     * @return  订单是否已经提现（所有明细都已提现）
     */
    private Boolean isOrderCashed(Settlement settlement) {
        OrderAlipayCash criteria = new OrderAlipayCash();
        criteria.setOrderId(settlement.getOrderId());
        criteria.setStatus(OrderAlipayCash.Status.NOT.value());
        Long notCased  = orderAlipayCashDao.countOf(criteria);
        return equalWith(notCased, 0L);
    }

    private void markFinishedFail(Settlement settlement) {
        Settlement updating = new Settlement();
        updating.setId(settlement.getId());
        updating.setSettleStatus(Settlement.SettleStatus.FAIL.value());
        updating.setFinished(Settlement.Finished.FAIL.value());
        settlementDao.update(updating);
    }
}
