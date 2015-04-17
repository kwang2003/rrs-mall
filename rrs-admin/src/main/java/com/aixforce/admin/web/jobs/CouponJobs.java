package com.aixforce.admin.web.jobs;

import com.aixforce.admin.service.CouponJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Effet on 4/26/14.
 */
@Slf4j
@Component
public class CouponJobs {

    private  CouponJobService couponJobService;

    private  AdminLeader adminLeader;

    @Autowired
    public CouponJobs(AdminLeader adminLeader, CouponJobService couponJobService) {
        this.adminLeader = adminLeader;
        this.couponJobService = couponJobService;
    }

    /**
     * run every midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void couponCorrecting() {
        boolean isLeader = this.adminLeader.isLeader();
        if (!isLeader) {
            log.info("current admin leader is:{}, return redirect", adminLeader.currentLeaderId());
            return;
        }

        log.info("[COUPON_CORRECTING] correcting coupon begin");
        couponJobService.correctAllInvalid();
        log.info("[COUPON_CORRECTING] correcting coupon end");

    }
}
