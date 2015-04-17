package com.aixforce.trade.dao;

import com.aixforce.common.utils.JsonMapper;
import com.aixforce.redis.utils.JedisTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

import static com.aixforce.redis.utils.KeyUtils.shopCart;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
@Repository
public class CartDao {
    private final static Logger log = LoggerFactory.getLogger(CartDao.class);

    private final ObjectMapper jackson;

    public static final TypeReference<Multiset<Long>> type = new TypeReference<Multiset<Long>>() {
    };

    private static final int TWO_WEEKSINT_IN_SECONDS = (int) TimeUnit.DAYS.toSeconds(14);


    private final JedisTemplate template;

    @Autowired
    public CartDao(JedisTemplate template) {
        this.template = template;
        jackson = JsonMapper.nonEmptyMapper().getMapper();
    }

    public Multiset<Long> getTemporary(final String key) throws Exception {
        String existed = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(shopCart(key));
            }
        });
        return unmarshal(existed);
    }

    public Multiset<Long> getPermanent(final Long userId) throws Exception {
        String existed = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(shopCart(userId.toString()));
            }
        });
        return unmarshal(existed);
    }


    public void setPermanent(final Long userId, final Multiset<Long> current) throws Exception {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                String serialized = marshal(current);
                jedis.set(shopCart(userId.toString()), serialized);
            }
        });
    }

    public void delete(final String key) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.del(shopCart(key));
            }
        });
    }

    Multiset<Long> unmarshal(String existed) {
        if (Strings.isNullOrEmpty(existed)) {
            return HashMultiset.create(1);
        }
        try {
            return jackson.readValue(existed, type);
        } catch (Exception e) {
            log.error("failed to unmarshal {} to Multiset<Long>,cause:{}", existed, Throwables.getStackTraceAsString(e));
            throw new RuntimeException("failed to deserialize json string", e);
        }
    }

    String marshal(Multiset<Long> current) {
        try {
            return jackson.writeValueAsString(current);
        } catch (JsonProcessingException e) {
            log.error("failed to serialize {} to json,cause:{}", current, Throwables.getStackTraceAsString(e));
            throw new RuntimeException("failed to serialize object to json", e);
        }
    }

    public void changeTemporaryCart(final String key, final Long skuId, final Integer quantity) throws Exception {
        final Multiset<Long> skuIds = getTemporary(key);
        if (quantity > 0) {
            skuIds.add(skuId, quantity);
        } else {
            skuIds.remove(skuId, -quantity);
        }
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.setex(shopCart(key), TWO_WEEKSINT_IN_SECONDS, marshal(skuIds));
            }
        });
    }

    public void changePermanentCart(final Long userId, Long skuId, Integer quantity) throws Exception {
        final Multiset<Long> skuIds = getPermanent(userId);
        if (quantity > 0) {
            skuIds.add(skuId, quantity);
        } else {
            skuIds.remove(skuId, -quantity);
        }
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(shopCart(userId.toString()), marshal(skuIds));
            }
        });
    }
}
