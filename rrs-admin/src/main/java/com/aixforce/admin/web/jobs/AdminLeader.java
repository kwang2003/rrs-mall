package com.aixforce.admin.web.jobs;

import com.aixforce.zookeeper.leader.Leaders;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-06-05
 */
@Component
public class AdminLeader {

    private final static Logger log = LoggerFactory.getLogger(AdminLeader.class);

    @Autowired(required = false)
    private Leaders leaders;

    private final String hostName;

    private LeaderLatch leaderLatch;

    public AdminLeader() {
        String tempHostName = UUID.randomUUID().toString().substring(0, 6);
        try {
            tempHostName = InetAddress.getLocalHost().getHostName();
            log.info("get local host name:{}", tempHostName);
        } catch (Exception e) {
            log.error("failed to get local host name", e);
        }
        hostName = tempHostName;
    }

    @PostConstruct
    public void init() throws Exception {
        if (leaders != null) {
            leaderLatch = leaders.initLeaderLatch("/rrs_admin_leader", hostName);
        }
    }

    public String currentLeaderId() {
        try {
            if (leaders != null) {
                return leaderLatch.getLeader().getId();
            } else {
                return hostName;
            }
        } catch (Exception e) {
            log.error("failed to get current leader id",e);
            return "unknown";
        }
    }

    public boolean isLeader() {
        if (leaders == null) {
            return true;
        }
        try {
            return leaders.isLeader(leaderLatch, hostName);
        } catch (Exception e) {
            log.error("oops, zookeeper failed,", e);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() throws Exception {
        if (leaders != null) {
            leaderLatch.close();
            leaders.close();
        }
    }
}
