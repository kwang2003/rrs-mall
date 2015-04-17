/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item;

import com.aixforce.redis.utils.JedisTemplate;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/redis-dao-context-test.xml",
        "classpath:spring/mysql-dao-context-test.xml",
        "classpath:spring/item-service-context.xml"
})
public abstract class BaseServiceTest {
    @Autowired
    protected JedisTemplate template;

    @After
    public void tearDown() {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.flushDB();
            }
        });
    }
}
