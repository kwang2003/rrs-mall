package com.aixforce.item;

import com.aixforce.redis.utils.JedisTemplate;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;


/**
 * Created by yangzefeng on 13-12-16
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/redis-dao-context-test.xml")
public class BaseRedisDaoTest {
    @Autowired
    private JedisTemplate jedisTemplate;

    @After
    public void cleanDB() {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.flushDB();
            }
        });
    }
}
