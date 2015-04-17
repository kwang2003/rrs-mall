package com.aixforce.item.dao.redis;

import com.aixforce.redis.utils.JedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

/**
 * @Description: 增量同步中，获取需更新的商品<br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/12/20 <br/>
 */
@Repository
public class FeedItemsDao {
    private final JedisTemplate jedisTemplate;

    @Autowired
    public FeedItemsDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }

    private static String feedChangeItemsName =  "feed-items:change";

    /**
     * 记录变化的itemId
     * @param itemId
     */
    public void create(final Long itemId){

        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                //获取现有的列表，如果有重复，不添加
                boolean exist = jedis.sismember(feedChangeItemsName,String.valueOf(itemId));
                if (!exist){
                    jedis.sadd(feedChangeItemsName, String.valueOf(itemId));
                }
            }
        });
    }

}
