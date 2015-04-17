package com.aixforce.item.dao.redis;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * Created by yangzefeng on 14-5-22
 */
@Repository
public class ItemCountDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ItemCountDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public void setShopItemCount(final long shopId, final long count) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(KeyUtils.shopItemCount(shopId), String.valueOf(count));
            }
        });
    }

    public void deleteItemCount(final List<Long> shopIds) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                for(Long shopId : shopIds) {
                    t.del(KeyUtils.shopItemCount(shopId));
                }
                t.exec();
            }
        });
    }
}
