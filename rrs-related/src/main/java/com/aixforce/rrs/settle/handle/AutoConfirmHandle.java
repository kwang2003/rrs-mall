package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.rrs.settle.util.SettlementVerification;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.rrs.settle.util.SettlementVerification.*;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * 商户 7 天自动确认  <br/>
 *
 * 普通订单：<br/>
 *     在线支付(交易成功): 若保证金不足则不确认 <br/>
 *     货到付款: 无需确认保证金余额 <br/>
 *
 * 预售订单：<br/>
 *     定金 + 尾款已交(交易成功): 若保证金余额不足则不确认  <br/>
 *     定金已交， 尾款未交: 无需确认保证金余额 <br/>
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-31 11:46 AM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class AutoConfirmHandle extends JobHandle {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private DepositManager depositManager;

    @Value("#{app.threshold}")
    @Setter
    private Integer threshold;

    @Value("#{app.permitDay}")
    @Setter
    private Integer permitDay;

    private static final DateTimeFormatter DFT2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");   // 统一日期时间


    public void autoConfirm(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[AUTO-CONFIRM] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());

            Integer pageNo = 1;
            boolean next = batchAutoConfirm(job.getDoneAt(), pageNo, BATCH_SIZE);

            while (next) {
                pageNo++;
                next = batchAutoConfirm(job.getDoneAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[AUTO-CONFIRM] successfully done");


        } catch (IllegalStateException e) {
            log.error("[AUTO-CONFIRM] failed with job:{}, error:{}", job, e);
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[AUTO-CONFIRM] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[AUTO-CONFIRM] end at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
        
        
    }

    private boolean batchAutoConfirm(Date doneAt, Integer pageNo, Integer size) {
        // 获取 已结算 未同步的 7天前订单
        List<Settlement> settlements = getSettlements(doneAt, pageNo, size);
        int current = settlements.size();

        for (Settlement settlement : settlements) {
            try {

                log.info("process settlement(id:{})", settlement.getId());

                if (equalWith(settlement.getConfirmed(), Settlement.Confirmed.DONE.value())) {  // 已经确认的则无需确认
                    log.info("settlement(id:{}) has done", settlement.getId());
                    continue;
                }

                if (isPlainOnlineSuccess(settlement) && accountLocked(settlement.getSellerId())) { // 普通在线支付成功订单, 若余额不足则跳过
                    log.info("settlement(id:{}) is plain-online-success but locked", settlement.getId());
                    continue;
                }

                if (isPresellSuccess(settlement) && accountLocked(settlement.getSellerId())) { // 预售成功订单，若余额不足则跳过
                    log.info("settlement(id:{}) is presell-success but locked", settlement.getId());
                    continue;
                }

                // 自动确认订单
                boolean success = settlementDao.confirmed(settlement.getId());
                log.info("auto confirm settlement (id:{}, earning:{}, createdAt:{})",
                        settlement.getId(), settlement.getSellerEarning(), DFT2.print(new DateTime(settlement.getCreatedAt())));
                checkState(success, "settle.confirmed.persist.fail");

            } catch (IllegalStateException e) {
                log.error("confirm {} failed with error:{}", settlement, e.getMessage());
            } catch (Exception e) {
                log.error("confirm {} failed with cause:{}", settlement, Throwables.getStackTraceAsString(e));
            }
        }

        return current == size;  // 判断是否存在下一个要处理的批次
    }

    private boolean isPlainOnlineSuccess(Settlement settlement) {
        return isPlain(settlement) && isOnline(settlement) && SettlementVerification.done(settlement);
    }

    private boolean isPresellSuccess(Settlement settlement) {
        return isPreSale(settlement) && done(settlement);
    }

    private boolean accountLocked(Long sellerId) {
        return depositManager.isAccountLocked(sellerId, threshold);
    }

    private List<Settlement> getSettlements(Date doneAt, Integer pageNo, Integer size) {
        Date endAt = new DateTime(doneAt).minusDays(permitDay).withTimeAtStartOfDay().toDate();
        Settlement criteria = new Settlement();
        criteria.setSettled(Settlement.Settled.DONE.value());
        criteria.setSynced(Settlement.Synced.NOT.value());
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);

        params.put("criteria", criteria);
        params.put("offset", pageInfo.offset);
        params.put("limit", pageInfo.limit);
        params.put("createdEndAt", endAt);
        log.info("try to query settlements with params:{}", params);
        Paging<Settlement> paging = settlementDao.findBy(params);
        return paging.getData();
    }
}
