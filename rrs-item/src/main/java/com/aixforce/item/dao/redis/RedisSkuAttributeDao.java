package com.aixforce.item.dao.redis;

import com.aixforce.item.model.SkuAttribute;
import com.aixforce.redis.dao.RedisBaseDao;
import com.aixforce.redis.utils.JedisTemplate;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

import static com.aixforce.redis.utils.KeyUtils.entityId;
import static com.aixforce.redis.utils.KeyUtils.skuAttributes;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-02-04
 */
@Repository
public class RedisSkuAttributeDao extends RedisBaseDao<SkuAttribute> {

    @Autowired
    public RedisSkuAttributeDao(JedisTemplate template) {
        super(template);
    }

    public void create(final SkuAttribute skuAttribute) {
        final Long id = newId();
        skuAttribute.setId(id);
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.hmset(entityId(SkuAttribute.class, id), stringHashMapper.toHash(skuAttribute));
                //add to sku index
                t.rpush(skuAttributes(skuAttribute.getSkuId()), id.toString());
                t.exec();
            }
        });
    }

    public SkuAttribute findById(final long id) {
        SkuAttribute skuAttribute = findByKey(id);
        return skuAttribute.getId() != null ? skuAttribute : null;
    }

    public void delete(final Long id) {
        final SkuAttribute skuAttribute = findById(id);
        if (skuAttribute == null) {
            return;
        }
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.del(entityId(SkuAttribute.class, id));
                //remove from spu index
                t.lrem(skuAttributes(skuAttribute.getSkuId()), 1, id.toString());
                t.exec();
            }
        });
    }

    public List<SkuAttribute> findBySkuId(final Long skuId) {
        final List<String> ids = template.execute(new JedisTemplate.JedisAction<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.lrange(skuAttributes(skuId), 0, -1);
            }
        });
        return super.findByIds(ids);
    }


/*    public Integer maxRank(Long spuId){
        return Objects.firstNonNull((Integer) getSqlSession().selectOne("SpuAttribute.maxRank", spuId), -1);
    }*/

    public void deleteBySkuId(final Long skuId) {
        final List<String> spuAttributes = template.execute(new JedisTemplate.JedisAction<List<String>>() {
            @Override
            public List<String> action(Jedis jedis) {
                return jedis.lrange(skuAttributes(skuId), 0, -1);
            }
        });
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.del(Iterables.toArray(Iterables.transform(spuAttributes, new Function<String, String>() {
                    @Override
                    public String apply(String attributeId) {
                        return entityId(SkuAttribute.class, attributeId);
                    }
                }), String.class));
                t.del(skuAttributes(skuId));
                t.exec();
            }
        });
    }
}
