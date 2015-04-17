package com.aixforce.rrs.predeposit.dao;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Created by yangzefeng on 14-8-8
 */
@Repository
public class PreDepositBuyLimitDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public PreDepositBuyLimitDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    /**
     * 获取某个预售某个用户已经购买的数量
     * @param buyerId 用户id
     * @param preSaleId 预售id
     * @return 购买数量，如果还未购买则返回null
     */
    public Integer getPreDepositBuyCount(final Long buyerId, final Long preSaleId) {
        String count = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.preSaleBuyCount(buyerId, preSaleId));
            }
        });

        if(!Strings.isNullOrEmpty(count)) {
            return Integer.valueOf(count);
        }

        return null;
    }

    /**
     * 设置一个买家对某个预售的购买数量，同时设置预售结束时间为这个key的过期时间
     * @param buyerId 买家id
     * @param preSaleId 预售id
     * @param buyCount 购买数量
     * @param unixTime 预售结束时间unixTime格式
     */
    public void setPreDepositBuyCount(final Long buyerId, final Long preSaleId,
                                   final Integer buyCount, final long unixTime) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.set(KeyUtils.preSaleBuyCount(buyerId,preSaleId), String.valueOf(buyCount));
                t.expireAt(KeyUtils.preSaleBuyCount(buyerId,preSaleId), unixTime);
                t.exec();
            }
        });
    }

    public void setPreDepositBuyCountWithoutExpireTime(final Long buyerId, final Long preSaleId,
                                                    final Integer buyCount) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(KeyUtils.preSaleBuyCount(buyerId,preSaleId),String.valueOf(buyCount));
            }
        });
    }
}
