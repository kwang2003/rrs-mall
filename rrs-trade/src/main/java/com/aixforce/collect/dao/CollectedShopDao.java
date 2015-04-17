package com.aixforce.collect.dao;

import com.aixforce.collect.model.CollectedShop;
import com.aixforce.common.model.Paging;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 3:21 PM  <br>
 * Author: xiao
 */
@Repository
public class CollectedShopDao extends SqlSessionDaoSupport {

    private static final String NAMESPACE = "CollectedShop.";


    public Long create(CollectedShop item) {
        //避免重复创建
        getSqlSession().insert(NAMESPACE + "create", item);
        return item.getId();
    }

    public CollectedShop get(Long id) {
        return getSqlSession().selectOne(NAMESPACE + "get", id);
    }

    public Boolean delete(Long id) {
        return getSqlSession().delete(NAMESPACE + "delete", id) == 1;
    }

    public Long countOf(Long buyerId) {
        Map<String, Object> params = Maps.newHashMap();
        CollectedShop criteria = new CollectedShop();
        criteria.setBuyerId(buyerId);
        params.put("criteria", criteria);
        return countOf(params);
    }

    public Long countOf(Map<String, Object> params) {
        return getSqlSession().selectOne(NAMESPACE + "countOf", params);
    }

    public Paging<CollectedShop> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne(NAMESPACE + "countOf", params);
        if (total == 0L) {
            return new Paging<CollectedShop>(0L, Collections.<CollectedShop>emptyList());
        }
        List<CollectedShop> items = getSqlSession().selectList(NAMESPACE + "findBy", params);
        return new Paging<CollectedShop>(total, items);
    }

    public Paging<CollectedShop> findBy(CollectedShop criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    public CollectedShop getByUserIdAndShopId(Long buyerId, Long shopId) {
        return getSqlSession().selectOne(NAMESPACE + "getByUserIdAndShopId",
                ImmutableMap.of("buyerId", buyerId, "shopId", shopId));
    }
}
