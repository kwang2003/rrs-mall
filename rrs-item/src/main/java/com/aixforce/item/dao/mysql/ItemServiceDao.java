package com.aixforce.item.dao.mysql;

import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by Andy on 14-12-2.
 */
@Repository
public class ItemServiceDao extends SqlSessionDaoSupport {

    public Integer countByItemId(Long itemId) {
        return getSqlSession().selectOne("ItemService.itemServiceCountById", itemId);
    }

    public String findTemplateByItemId(Long itemId) {
        return getSqlSession().selectOne("ItemService.findTemplateByItemId", itemId);
    }

    public boolean isItemTempBinded(Long itemId, String tempId) {
        ImmutableMap map = ImmutableMap.of("itemId", itemId, "tempId", tempId);
        Long total = getSqlSession().selectOne("ItemService.countItemTempBind", map);
        return total > 0;
    }

    public boolean saveItemTempRelation(Long itemId, String tempId, String tempName) {
        ImmutableMap map = ImmutableMap.of("itemId", itemId, "tempId", tempId, "tempName", tempName);
        return getSqlSession().insert("ItemService.saveRelation", map) == 1;
    }

    public int deleteItemTempRelation(Long itemId) {
        return getSqlSession().update("ItemService.deleteRelation", itemId);
    }

}
