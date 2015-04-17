package com.aixforce.collect.dao;

import com.aixforce.collect.model.CollectedItem;
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
public class CollectedItemDao extends SqlSessionDaoSupport {

    private static final String NAMESPACE = "CollectedItem.";

    public Long create(CollectedItem item) {
        //避免重复创建
        getSqlSession().insert(NAMESPACE + "create", item);
        return item.getId();
    }

    public CollectedItem get(Long id) {
        return getSqlSession().selectOne(NAMESPACE + "get", id);
    }

    public CollectedItem getByUserIdAndItemId(Long buyerId, Long itemId) {
        return getSqlSession().selectOne(NAMESPACE + "getByUserIdAndItemId",
                ImmutableMap.of("buyerId", buyerId, "itemId", itemId));
    }

    public Boolean delete(Long id) {
        return getSqlSession().delete(NAMESPACE + "delete", id) == 1;
    }

    public Long countOf(Long buyerId) {
        Map<String, Object> params = Maps.newHashMap();
        CollectedItem criteria = new CollectedItem();
        criteria.setBuyerId(buyerId);
        params.put("criteria", criteria);
        return countOf(params);
    }

    public Long countOf(Map<String, Object> params) {
        return getSqlSession().selectOne(NAMESPACE + "countOf", params);
    }


    public Paging<CollectedItem> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne(NAMESPACE + "countOf", params);
        if (total == 0L) {
            return new Paging<CollectedItem>(0L, Collections.<CollectedItem>emptyList());
        }
        List<CollectedItem> items = getSqlSession().selectList(NAMESPACE + "findBy", params);
        return new Paging<CollectedItem>(total, items);
    }


    public Paging<CollectedItem> findBy(CollectedItem criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    public CollectedItem getByUserIdAndItemIdAndActivityId(Long buyerId, Long itemId, Long buyingActivityId) {
        Map<String,Long> param = Maps.newHashMap();
        param.put("buyerId",buyerId);
        param.put("itemId",itemId);
        param.put("buyingActivityId",buyingActivityId);
        return getSqlSession().selectOne(NAMESPACE + "getByUserIdAndItemIdAndActivityId",param);
    }
}
