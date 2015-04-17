/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.redis.utils.JedisTemplate;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/spring/redis-dao-context-test.xml", "classpath:/spring/mysql-dao-context-test.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public abstract class BaseDaoTest {
    @Autowired
    protected JedisTemplate template;

//    @After
    public void tearDown() {
//        template.execute(new JedisTemplate.JedisActionNoResult() {
//            @Override
//            public void action(Jedis jedis) {
//                jedis.flushDB();
//            }
//        });
    }
}
