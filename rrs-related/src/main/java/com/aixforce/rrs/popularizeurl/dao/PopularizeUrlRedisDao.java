package com.aixforce.rrs.popularizeurl.dao;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangmeng on 14-2-12
 */
@Repository
public class PopularizeUrlRedisDao {

    private final JedisTemplate jedisTemplate;

    private String keyAllReleasePopularizeUrl = "keyAllReleasePopularizeUrl";

    @Autowired
    public PopularizeUrlRedisDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public String findUrlByPopUrlContext(final String context) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(keyAllReleasePopularizeUrl + context);
            }
        });
    }

    public String createByPopUrlContext(final String context, final String url) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.set(keyAllReleasePopularizeUrl + context, url);
            }
        });
    }
}
