package com.aixforce.rrs.buying.dao;

import com.aixforce.rrs.buying.model.BuyingItem;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by songrenfei on 14-9-22.
 */

@Repository
public class BuyingItemDao extends SqlSessionDaoSupport {

    public Long create(BuyingItem buyingItem) {
        getSqlSession().insert("BuyingItem.create", buyingItem);
        return buyingItem.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("BuyingItem.delete", id) == 1;
    }

    public boolean update(BuyingItem buyingItem) {
        return getSqlSession().update("BuyingItem.update", buyingItem) == 1;
    }

    public BuyingItem findById(Long id) {
        return getSqlSession().selectOne("BuyingItem.findById", id);
    }

    public List<BuyingItem> findByActivityId(Long id) {
        return getSqlSession().selectList("BuyingItem.findByActivityId", id);
    }

    /**
     * 根据活动id删除商品关联
     * @param id 活动id
     * @return 是否删除成功
     */
    public Boolean deleteByActivityId(Long id){
        return  getSqlSession().delete("BuyingItem.deleteByActivityId",id)>0;
    }


    public BuyingItem findByActivityIdAnditemId(Long activityId, Long itemId){
        return getSqlSession().selectOne("BuyingItem.findByActivityIdAnditemId", ImmutableMap.of("activityId",activityId,
                "itemId",itemId));
    }

    public BuyingItem findLatestByItemId(Long itemId) {
        return getSqlSession().selectOne("BuyingItem.findLatestByItemId", itemId);
    }

}
