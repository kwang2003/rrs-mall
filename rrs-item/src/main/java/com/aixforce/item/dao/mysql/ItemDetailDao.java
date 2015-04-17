package com.aixforce.item.dao.mysql;

import com.aixforce.item.model.ItemDetail;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-01-31
 */
@Repository
public class ItemDetailDao extends SqlSessionDaoSupport {
    public ItemDetail findById(Long id) {
        return getSqlSession().selectOne("ItemDetail.findById", id);
    }

    public void create(ItemDetail itemDetail) {
        getSqlSession().insert("ItemDetail.create", itemDetail);
    }

    public void delete(Long id) {
        getSqlSession().delete("ItemDetail.delete", id);
    }

    public void deleteByItemId(Long itemId) {
        getSqlSession().delete("ItemDetail.deleteByItemId", itemId);
    }

    public void update(ItemDetail itemDetail) {
        getSqlSession().update("ItemDetail.update", itemDetail);
    }

    public void updateByItemId(ItemDetail itemDetail) {
        getSqlSession().update("ItemDetail.updateByItemId", itemDetail);
    }

    public ItemDetail findByItemId(Long itemId) {
        return getSqlSession().selectOne("ItemDetail.findByItemId", itemId);
    }
}
