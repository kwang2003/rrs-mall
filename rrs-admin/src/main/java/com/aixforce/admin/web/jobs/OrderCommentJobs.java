package com.aixforce.admin.web.jobs;

import com.aixforce.admin.service.OrderCommentJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Date: 14-2-26
 * Time: PM12:56
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Component
public class OrderCommentJobs {
    private final static Logger log = LoggerFactory.getLogger(OrderCommentJobs.class);

    private final AdminLeader adminLeader;

    private final OrderCommentJobService orderCommentJobService;


    @Autowired
    public OrderCommentJobs(AdminLeader adminLeader,
                            OrderCommentJobService orderCommentJobService) {
        this.adminLeader = adminLeader;
        this.orderCommentJobService = orderCommentJobService;
    }

    /**
     * run every midnight 1:00
     * calculate shop's average score
     */
   // @Scheduled(cron = "0/15 * * * * *")
    @Scheduled(cron = "0 0 1 * * *")
    public void calculateShopExtraScores() {

        log.info("exect calculateShopExtraScores begin");
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[CALC_SHOP_SCORE] calculate shop extra's score begin");
        //orderCommentJobService.calcShopExtraScore();
        orderCommentJobService.statisticsShopExtraScore();
        log.info("[CALC_SHOP_SCORE] calculate shop extra's score end");
    }

    /**
     * 每个星期日凌晨1点
     * 全量统计店铺评分job
     */
    @Scheduled(cron = "0 0 1 ? * SUN")
    public void fullDumpShopExtraScores() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[FULL_DUMP_SHOP_SCORE] full dump shop extra's score begin");
        orderCommentJobService.fullDumpShopExtraScore();
        log.info("[FULL_DUMP_SHOP_SCORE] full dump shop extra's score end");
    }

    /**
     * run every midnight
     * close expired order comment, and set score to 5
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void expireOrderComment() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[EXPIRE_ORDER_COMMENT] close expired order comments begin");
        orderCommentJobService.expireOrderComment();
        log.info("[EXPIRE_ORDER_COMMENT] close expired order comments end");

    }
}
