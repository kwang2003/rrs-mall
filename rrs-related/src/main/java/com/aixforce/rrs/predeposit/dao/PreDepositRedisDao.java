package com.aixforce.rrs.predeposit.dao;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangzefeng on 14-2-12
 */
@Repository
public class PreDepositRedisDao {

    private final JedisTemplate jedisTemplate;

    private String keyAllReleasePreDepoists = "keyAllReleasePreDepoists";

    @Autowired
    public PreDepositRedisDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }


    public void addPreSaleToAllReleasePreSaleList(final long preSaleId) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.rpush(keyAllReleasePreDepoists, String.valueOf(preSaleId));
            }
        });
    }

    public List<Long> findAllPreSale() {
        return Lists.transform(jedisTemplate.execute(new JedisTemplate.JedisAction<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.lrange(keyAllReleasePreDepoists, 0, -1);
            }
        }), new Function<String, Long>() {
            @Override
            public Long apply(String s) {
                return Long.valueOf(s);
            }
        });
    }

    public void removePreSaleById(final long preSaleId) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.lrem(keyAllReleasePreDepoists, 1, String.valueOf(preSaleId));
            }
        });
    }

    public void addOrderIdToAllPreSaleItemOrdersList(final long orderId) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.sadd(KeyUtils.allPreSaleItemOrders(), String.valueOf(orderId));
            }
        });
    }

    public Iterable<Long> findAllPreSaleItemOrders() {
        return Iterables.transform(jedisTemplate.execute(new JedisTemplate.JedisAction<Iterable<String>>() {
            @Override
            public Iterable<String> action(Jedis jedis) {
                return jedis.smembers(KeyUtils.allPreSaleItemOrders());
            }
        }), new Function<String, Long>() {
            @Override
            public Long apply(String o) {
                return Long.valueOf(o);
            }
        });
    }

    public void removeOrder(final long orderId) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.srem(KeyUtils.allPreSaleItemOrders(), String.valueOf(orderId));
            }
        });
    }

    /**
     * 判断订单是否存在于预售订单列表中
     * @param orderId 订单id
     */
    public boolean orderIdExist(final long orderId) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.sismember(KeyUtils.allPreSaleItemOrders(), String.valueOf(orderId));
            }
        });
    }

    public void addStorageId2PreSaleOrder(final long orderId, final long storageId, final long itemId, final int quantity) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.setex(KeyUtils.preSaleOrderStorage(orderId), (int) TimeUnit.DAYS.toSeconds(31),
                        storageId + "_" + itemId+"_"+quantity);
            }
        });
    }


    public String findStorageInfoByOrderId(final Long orderId) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.preSaleOrderStorage(orderId));
            }
        });
    }
}
