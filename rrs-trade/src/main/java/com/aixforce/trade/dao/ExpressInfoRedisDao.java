package com.aixforce.trade.dao;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.trade.model.ExpressInfo;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.Collections;
import java.util.List;

/**
 * Author: haolin
 * On: 9/24/14
 */
@Repository
public class ExpressInfoRedisDao {

    @Autowired
    private JedisTemplate template;

    public void add2Usual(final Long userId, final Long expressInfoId){
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.lpush(ExpressInfo.keyOfUsual(userId), String.valueOf(expressInfoId));
            }
        });
    }

    public void rmFromUsual(final Long userId,final  Long expressInfoId){
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.lrem(ExpressInfo.keyOfUsual(userId), 0, String.valueOf(expressInfoId));
            }
        });
    }

    public List<Long> usualExpressInfoIds(final Long userId){
        return template.execute(new JedisTemplate.JedisAction<List<Long>>() {
            @Override
            public List<Long> action(Jedis jedis) {
                List<String> expressInfoIdsStr = jedis.lrange(ExpressInfo.keyOfUsual(userId), 0, -1);
                if (expressInfoIdsStr.isEmpty()){
                    return Collections.emptyList();
                }
                return Lists.transform(expressInfoIdsStr, new Function<String, Long>() {
                    @Override
                    public Long apply(String idStr) {
                        return Long.valueOf(idStr);
                    }
                });
            }
        });
    }

    /**
     * 移除多个常用的快递
     * @param userId 用户id
     * @param expressInfoIds 快递id
     */
    public void rmFromUsual(final Long userId, final List<Long> expressInfoIds) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                for (Long expressInfoId : expressInfoIds){
                    p.lrem(ExpressInfo.keyOfUsual(userId), 0, String.valueOf(expressInfoId));
                }
                p.sync();
            }
        });
    }
}
