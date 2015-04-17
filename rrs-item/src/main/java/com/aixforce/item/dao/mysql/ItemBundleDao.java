package com.aixforce.item.dao.mysql;

import com.aixforce.item.model.ItemBundle;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yangzefeng on 14-4-21
 */
@Repository
public class ItemBundleDao extends SqlSessionDaoSupport {

    public void create(ItemBundle itemBundle) {
        getSqlSession().insert("ItemBundle.create", itemBundle);
    }

    public void update(ItemBundle itemBundle) {
        getSqlSession().update("ItemBundle.update", itemBundle);
    }

    public ItemBundle findById(Long id) {
        return getSqlSession().selectOne("ItemBundle.findById", id);
    }

    public void delete(Long id) {
        getSqlSession().delete("ItemBundle.delete", id);
    }

    public List<ItemBundle> findBySellerId(Long sellerId) {
        return getSqlSession().selectList("ItemBundle.findBySellerId", sellerId);
    }
}
