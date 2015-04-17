package com.aixforce.item.manager;

import com.aixforce.item.dao.redis.DefaultItemRedisDao;
import com.aixforce.item.model.BaseSku;
import com.aixforce.item.model.DefaultItem;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-10
 */
@Component
public class DefaultItemManager {

    @Autowired
    private DefaultItemRedisDao defaultItemRedisDao;

    @Transactional
    public void createOrUpdate(DefaultItem defaultItem , List<BaseSku> baseSkus) {
        List<String> outerIds = Lists.transform(baseSkus, new Function<BaseSku, String>() {
            @Override
            public String apply(BaseSku input) {
                return input.getOuterId();
            }
        });
        //create default item
        if(defaultItem.getId() == null) {
            defaultItemRedisDao.create(defaultItem, outerIds);
        }else { //update default item
            defaultItemRedisDao.update(defaultItem, outerIds);
        }
    }
}
