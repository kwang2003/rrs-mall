package com.aixforce.admin.web.jobs;

import com.aixforce.rrs.code.service.ActivityDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * Created by songrenfei on 14-7-8
 */
@Component
public class ActivityDefinitionStatusJobs {
    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    private final static Logger log = LoggerFactory.getLogger(ActivityDefinitionStatusJobs.class);

    /**
     * run every 1:00
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void update() {

        log.info("update activityDefintion status to Effect job begin");
        activityDefinitionService.updateToEffect();
        log.info("update activityDefintion status to Effect job end");

        log.info("update activityDefintion status to Expiry job begin");
        activityDefinitionService.updateToExpiry();
        log.info("update activityDefintion status to Expiry job end");
    }
}
