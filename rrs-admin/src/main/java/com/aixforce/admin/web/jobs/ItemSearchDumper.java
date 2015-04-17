/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.jobs;

import com.aixforce.item.service.ItemIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-21
 */
@Component
public class ItemSearchDumper {
    private final static Logger log = LoggerFactory.getLogger(ItemSearchDumper.class);

    private final ItemIndexService indexService;

    private final AdminLeader adminLeader;

    @Autowired
    public ItemSearchDumper(ItemIndexService indexService, AdminLeader adminLeader) {
        this.indexService = indexService;
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
        log.info("[CRON-JOB]search item refresh job begin");
        indexService.fullDump();
        log.info("[CRON-JOB]search item refresh job ends");


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

        log.info("[DELTA_DUMP_ITEM] item delta dump start");
        indexService.deltaDump(15);
        log.info("[DELTA_DUMP_ITEM] item delta finished");
    }
}
