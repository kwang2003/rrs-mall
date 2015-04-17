package com.aixforce.shop.dao.redis;

import com.aixforce.redis.utils.JedisTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 13-12-18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/redis-dao-context-test.xml")
public class ShopRedisDaoTest {

    @Autowired
    private JedisTemplate jedisTemplate;

    @Autowired
    private ShopRedisDao shopRedisDao;

    @Before
    public void setUp() {

    }

    @After
    public void cleanDB() {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.flushDB();
            }
        });
    }

    @Test
    public void testFindById() throws Exception {
        String count = shopRedisDao.findById(1l);
        assertThat(count, is("0"));
    }
}
