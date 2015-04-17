package com.aixforce.item.dao.mysql;

import com.aixforce.item.model.Sku;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-02-01
 */
@Repository
public class SkuDao extends SqlSessionDaoSupport {

    public void create(List<Sku> skuList) {
        for (Sku sku : skuList) {
            getSqlSession().insert("Sku.create", sku);
        }
    }

    public void create(Sku... skus) {
        for (Sku sku : skus) {
            getSqlSession().insert("Sku.create", sku);
        }
    }

    public List<Sku> findByItemId(Long itemId) {
        return getSqlSession().selectList("Sku.findByItemId", itemId);
    }

    public List<Sku> findByItemIds(List<Long> itemIds) {
        return getSqlSession().selectList("Sku.findByItemIds", itemIds);
    }

    public List<Sku> findByIds(List<Long> ids) {
        return getSqlSession().selectList("Sku.findByIds", ids);
    }

    public Sku findById(long id) {
        return getSqlSession().selectOne("Sku.findById", id);
    }

    public void update(List<Sku> skuList) {
        for (Sku sku : skuList) {
            getSqlSession().update("Sku.update", sku);
        }
    }

    public void update(Sku... skus) {
        for (Sku sku : skus) {
            getSqlSession().update("Sku.update", sku);
        }
    }

    public void delete(long id) {
        getSqlSession().delete("Sku.delete", id);
    }

    public void deleteByItemId(long itemId) {
        getSqlSession().delete("Sku.deleteByItemId", itemId);
    }

    public void changeStock(long id, int count) {
        getSqlSession().update("Sku.changeStock", ImmutableMap.of("id", id, "count", count));
    }

    public Sku findSkuByAttributeValuesAndItemId(long itemId, String attributeValue1, String attributeValue2) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("itemId", itemId);
        if(attributeValue1 != null) {
            params.put("attributeValue1",attributeValue1);
        }
        if(attributeValue2 != null) {
            params.put("attributeValue2",attributeValue2);
        }
        return getSqlSession().selectOne("Sku.findSkuByAttributeValuesAndItemId",
                params);
    }
}
