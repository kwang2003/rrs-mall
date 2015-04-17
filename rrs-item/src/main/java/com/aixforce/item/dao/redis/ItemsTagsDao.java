package com.aixforce.item.dao.redis;

import com.aixforce.common.model.Paging;
import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-01
 */
@Repository
public class ItemsTagsDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ItemsTagsDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    public Paging<Long> itemsOfTag(@Nonnull final Long userId, @Nonnull final String tag,
                                   final int offset, final int size) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Paging<Long>>() {

            @Override
            public Paging<Long> action(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                Response<Long> r = pipeline.zcard(KeyUtils.itemsOfTag(userId, tag));
                Response<Set<String>> i = pipeline.zrevrange(KeyUtils.itemsOfTag(userId, tag), offset, offset + size - 1);
                pipeline.sync();
                Long total = r.get();
                if (total > 0) {
                    List<Long> ids = Lists.newArrayListWithCapacity(total.intValue());
                    for (String s : i.get()) {
                        ids.add(Long.parseLong(s));
                    }
                    return new Paging<Long>(total, ids);
                }
                return new Paging<Long>(0L, Collections.<Long>emptyList());
            }
        });
    }

    public ListMultimap<Long, String> tagsOfItems(final Long userId, final List<Long> itemIds) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<ListMultimap<Long, String>>() {
            @Override
            public ListMultimap<Long, String> action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                List<Response<Set<String>>> all = Lists.newArrayListWithCapacity(itemIds.size());
                for (Long itemId : itemIds) {
                    all.add(p.smembers(KeyUtils.tagsOfItem(userId, itemId)));
                }
                p.sync();
                ListMultimap<Long, String> result = ArrayListMultimap.create(itemIds.size(), 3);
                for (int i = 0; i < itemIds.size(); i++) {
                    result.putAll(itemIds.get(i), all.get(i).get());
                }
                return result;
            }
        });
    }

    public Set<String> tagsOfItem(final Long userId, final Long itemId) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.smembers(KeyUtils.tagsOfItem(userId, itemId));
            }
        });
    }


    public Paging<Long> findUnclassifiedItems(final Long userId, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Paging<Long>>() {
            @Override
            public Paging<Long> action(Jedis jedis) {
                Transaction t = jedis.multi();
                Response<Long> r = t.zcard(KeyUtils.unclassifiedItems(userId));
                Response<Set<String>> i = t.zrange(KeyUtils.unclassifiedItems(userId), offset, offset + limit - 1);
                t.exec();
                Long total = r.get();
                if (total > 0) {
                    List<Long> ids = Lists.newArrayListWithCapacity(total.intValue());
                    for (String s : i.get()) {
                        ids.add(Long.parseLong(s));
                    }
                    return new Paging<Long>(total, ids);
                }
                return new Paging<Long>(0L, Collections.<Long>emptyList());
            }
        });
    }


    public Set<String> findByTag(final Long userId, final String tag, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.zrange(KeyUtils.itemsOfTag(userId, tag), offset, offset + limit - 1);
            }
        });
    }
}
