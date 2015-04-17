package com.aixforce.admin.web.jobs;

import com.aixforce.common.model.Response;
import com.aixforce.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-28
 */
@Component
public class ShopSearchDumper {
    private final static Logger log = LoggerFactory.getLogger(ShopSearchDumper.class);


    private final ShopService shopService;

    private final AdminLeader adminLeader;

    @Autowired
    public ShopSearchDumper(ShopService shopService, AdminLeader adminLeader) {
        this.shopService = shopService;
        this.adminLeader = adminLeader;
    }

    /**
     * run every midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void fullDump() {

        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        try {

            log.info("[CRON-JOB]search shop refresh job begin");
            Response<Boolean> result = shopService.fullDump();
            checkState(result.isSuccess(), result.getError());
            log.info("[CRON-JOB]search shop refresh job ends");

        } catch (IllegalStateException e) {
            log.error("[CRON-JOB]search shop refresh failed, error:{}", e.getMessage());
        } catch (Exception e) {
            log.error("[CRON-JOB]search shop refresh failed", e);
        }
    }

    /**
     * run every 15 minutes;
     */
    @Scheduled(cron = "0 0/15 * * * ?")  //每隔15分钟触发一次
    public void deltaDump() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }
        log.info("[DELTA_DUMP_SHOP] shop delta dump start");
        shopService.deltaDump(15);
        log.info("[DELTA_DUMP_SHOP] shop delta finished");

    }
}
