package com.aixforce.shop.dao;

import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-25
 */
@Repository
public class ItemTagDao {

    private final JedisTemplate jedisTemplate;

    @Autowired
    public ItemTagDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }


    public void removeTagOfItem(@Nonnull final Long userId, @Nonnull final Long itemId, @Nonnull final String tag) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                //将商品id从tag下面解除关联
                t.zrem(KeyUtils.itemsOfTag(userId, tag), itemId.toString());
                //将tag从item的tag集合中删除
                t.srem(KeyUtils.tagsOfItem(userId, itemId), tag);
                t.exec();
            }
        });
    }

    public void addTagsForItems(final long userId, final List<Long> itemIds, final List<String> tags) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                String[] itemIdStrings = new String[itemIds.size()];
                for (int i = 0; i < itemIds.size(); i++) {
                    Long itemId = itemIds.get(i);
                    itemIdStrings[i] = String.valueOf(itemId);
                    //将商品id挂到tag下面
                    for (String tag : tags) {
                        t.zadd(KeyUtils.itemsOfTag(userId, tag), itemId, String.valueOf(itemId));//按照itemId从小到大排序
                        //将tag加入item的tag集合
                        t.sadd(KeyUtils.tagsOfItem(userId, itemId), tag);
                    }
                }
                //将商品从未分类中移除
                if (!itemIds.isEmpty()) {
                    t.zrem(KeyUtils.unclassifiedItems(userId), itemIdStrings);
                }
                t.exec();
            }
        });
    }


    public void removeTagsOfItems(final long userId, final List<String> tags, final List<Long> itemIds) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                for (Long itemId : itemIds) {

                    for (String tag : tags) {
                        //将商品id从tag下面解除关联
                        t.zrem(KeyUtils.itemsOfTag(userId, tag), String.valueOf(itemId));
                        //将tag从item的tag集合中删除
                        t.srem(KeyUtils.tagsOfItem(userId, itemId), tag);
                    }
                }
                t.exec();
            }
        });
    }


    public Boolean addUnclassifiedItem(final Long userId, final Long itemId) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.zadd(KeyUtils.unclassifiedItems(userId), itemId, String.valueOf(itemId)) > 0;
            }
        });
    }

    public Boolean hasTags(final Long userId, final Long itemId) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                return jedis.scard(KeyUtils.tagsOfItem(userId, itemId)) > 0;
            }
        });
    }

    public void removeTagsOfItem(final Long userId, final Long itemId) {
        final Set<String> tags = jedisTemplate.execute(new JedisTemplate.JedisAction<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.smembers(KeyUtils.tagsOfItem(userId, itemId));
            }
        });
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                for (String tag : tags) {
                    t.zrem(KeyUtils.itemsOfTag(userId, tag), String.valueOf(itemId));
                }
                t.del(KeyUtils.tagsOfItem(userId, itemId));
                t.exec();
            }
        });
    }

    public void removeUnclassified(final Long userId, final Long itemId) {
        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.zrem(KeyUtils.unclassifiedItems(userId), String.valueOf(itemId));
            }
        });
    }

    public void removeTag(final Long userId, final String tag) {
        //找出tag下所有的商品id
        final Set<String> itemIdsOfTag = jedisTemplate.execute(new JedisTemplate.JedisAction<Set<String>>() {
            @Override
            public Set<String> action(Jedis jedis) {
                return jedis.zrange(KeyUtils.itemsOfTag(userId, tag), 0, -1);
            }
        });

        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                //删除tag到item的关联
                t.del(KeyUtils.itemsOfTag(userId, tag));

                //删除item到tag的关联
                for (String itemId : itemIdsOfTag) {
                    t.srem(KeyUtils.tagsOfItem(userId, Long.parseLong(itemId)), tag);
                }
                t.exec();
            }
        });
    }
}
