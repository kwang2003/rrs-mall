package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.shop.model.ShopExtra;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-04 5:33 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class RateUpdateHandle extends JobHandle {


    /**
     * 更新费率
     */
    public void updateRrsRate(SettleJob job) {
        log.info("[UPDATE-RATE] job begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            Preconditions.checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());  // mark job is processing

            log.info("[UPDATE-RATE] start update rate");
            updateRateOfShop();

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
            log.info("[UPDATE-RATE] done successfully");

        } catch (IllegalStateException e) {
            log.error("[UPDATE-RATE] fail to update rate with job:{}, error:{}", job, e.getMessage());
        } catch (Exception e) {
            log.error("[UPDATE-RATE] fail to update rate with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
        }

        stopwatch.stop();
        settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
        log.info("[UPDATE-RATE] done at {} cast {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));

    }

    private void updateRateOfShop() {
        int pageNo = 1;
        boolean next = batchUpdateRateOfShop(pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchUpdateRateOfShop(pageNo, BATCH_SIZE);
        }
    }

    private boolean batchUpdateRateOfShop(int pageNo, Integer size) {

        Response<Paging<ShopExtra>> extraQueryResult = shopService.findExtraBy(pageNo, size);
        checkState(extraQueryResult.isSuccess(), extraQueryResult.getError());

        Paging<ShopExtra> paging = extraQueryResult.getResult();
        List<ShopExtra> extras = paging.getData();
        if (CollectionUtils.isEmpty(extras))  return false;

        for (ShopExtra extra : extras) {
            try {

                Double rateUpdating = extra.getRateUpdating();
                Double rate = extra.getRate();
                Boolean needUpdate = Boolean.FALSE;

                if (notNull(rateUpdating)) {  // 更新T+1费率
                    extra.setRate(rateUpdating);
                    extra.setRateUpdating(null);
                    log.info("update rate from:{} ->to:{} of shop:(id:{}) ", rate, rateUpdating, extra.getShopId());
                    needUpdate = Boolean.TRUE;


                } else if (isNull(rate)) {   // 更新费率为0
                    extra.setRate(0.0000);
                    extra.setRateUpdating(null);
                    log.info("update rate from:null ->to:0000 of shop:(id:{}) ", extra.getShopId());
                    needUpdate = Boolean.TRUE;
                }

                if (needUpdate) {
                    Response<Boolean> updateResult = shopService.updateExtra(extra);
                    checkState(updateResult.isSuccess(), updateResult.getError());
                }

            } catch (IllegalStateException e) {
                log.error("fail to update rate with extra:{}, error:{}", extra, e.getMessage());
            } catch (Exception e) {
                log.error("fail to update rate with extra:{}, cause:{}", extra, Throwables.getStackTraceAsString(e));
            }
        }

        int current = extras.size();
        return current == size;
    }

}
