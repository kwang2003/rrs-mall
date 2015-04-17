package com.aixforce.item.dao.redis;

import com.aixforce.item.model.DefaultItem;
import com.aixforce.redis.dao.RedisBaseDao;
import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * Created by yangzefeng on 13-12-17
 */
@Repository
public class DefaultItemRedisDao extends RedisBaseDao<DefaultItem> {

    @Autowired
    public DefaultItemRedisDao(JedisTemplate template) {
        super(template);
    }

    public void create(final DefaultItem defaultItem, final List<String> outerIds) {
        final long id = newId();
        defaultItem.setId(id);
        final long spuId = defaultItem.getSpuId();
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                //add defaultItem
                t.hmset(KeyUtils.entityId(DefaultItem.class, id), stringHashMapper.toHash(defaultItem));
                //add spu-defaultItem index
                t.set(KeyUtils.spuDefaultItem(spuId), String.valueOf(id));
                //add outerId - defaultItem index
                for (String outerId : outerIds) {
                    t.set(KeyUtils.outerIdDefaultItem(outerId), String.valueOf(id));
                }
                t.exec();
            }
        });
    }

    public void update(final DefaultItem defaultItem, final List<String> outerIds) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.hmset(KeyUtils.entityId(DefaultItem.class, defaultItem.getId()), stringHashMapper.toHash(defaultItem));
                for(String outerId : outerIds) {
                    t.set(KeyUtils.outerIdDefaultItem(outerId), String.valueOf(defaultItem.getId()));
                }
                t.exec();
            }
        });
    }

    public DefaultItem findBySpuId(final long spuId) {
        String id = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.spuDefaultItem(spuId));
            }
        });
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        return findByKey(Long.parseLong(id));
    }

    public List<DefaultItem> findBySpuIds(final List<Long> spuIds) {
        List<Object> ids = template.execute(new JedisTemplate.JedisAction<List<Object>>() {
            @Override
            public List<Object> action(Jedis jedis) {
                Transaction t = jedis.multi();
                for(Long id : spuIds) {
                    t.get(KeyUtils.spuDefaultItem(id));
                }
                return t.exec();
            }
        });
        //remove null default item id
        Iterables.removeIf(ids, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return input == null;
            }
        });
        return findByIds(Lists.transform(ids, new Function<Object, String>() {
            @Override
            public String apply(Object input) {
                return String.valueOf(input);
            }
        }));
    }

    public DefaultItem findByOuterId(final String outerId) {
        String id = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(KeyUtils.outerIdDefaultItem(outerId));
            }
        });
        return findByKey(Long.valueOf(id));
    }
}
