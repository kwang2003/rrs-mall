package com.aixforce.shop.dao.redis;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

/**
 * Created by yangzefeng on 13-12-16
 */
@Repository
public class ShopRedisDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ShopRedisDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public String findById(final long shopId) {
        String exist = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopItemCount(shopId));
            }
        });
        //缓存失效返回-1
        if (Strings.isNullOrEmpty(exist))
            exist = "-1";
        return exist;
    }

    public void incrShopSoldQuantityCount(final long shopId, final long soldQuantity) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.incrBy(KeyUtils.shopSoldQuantityCount(shopId), soldQuantity);
            }
        });
    }

    public void incrShopSalesCount(final long shopId, final long sales) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.incrBy(KeyUtils.shopSalesCount(shopId), sales);
            }
        });
    }

    public String findShopSoldQuantityById(final long shopId) {
        String soldQuantity = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopSoldQuantityCount(shopId));
            }
        });
        if(Strings.isNullOrEmpty(soldQuantity)) {
            return "0";
        }
        return soldQuantity;
    }

    public String findShopSalesById(final long shopId) {
        String sales = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.shopSalesCount(shopId));
            }
        });
        if(Strings.isNullOrEmpty(sales)) {
            return "0";
        }
        return sales;
    }
}
