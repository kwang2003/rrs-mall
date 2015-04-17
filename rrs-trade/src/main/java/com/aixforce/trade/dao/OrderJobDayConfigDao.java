/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.OrderJobDayConfig;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
public class OrderJobDayConfigDao extends SqlSessionDaoSupport {

    public Long create(OrderJobDayConfig orderJobOverDay) {
        getSqlSession().insert("OrderJobOverDayConfig.create", orderJobOverDay);
        return orderJobOverDay.getId();
    }

    public void delete(Long id) {
        getSqlSession().delete("OrderJobOverDayConfig.delete", id);
    }

    public void update(OrderJobDayConfig orderJobOverDay) {
        getSqlSession().update("OrderJobOverDayConfig.update", orderJobOverDay);
    }
    public Long maxId() {
        return Objects.firstNonNull((Long)getSqlSession().selectOne("Order.maxId"),0L);
    }

    public Paging<OrderJobDayConfig> findBy(OrderJobDayConfig criteria) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("criteria", criteria);
//        params.put("offset", offset);
//        params.put("limit", limit);

        return findBy(params);
    }

    public Paging<OrderJobDayConfig> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("OrderJobOverDayConfig.countOf", params);
        if (total == 0L) {
            return new Paging<OrderJobDayConfig>(0L, Collections.<OrderJobDayConfig>emptyList());
        }
        List<OrderJobDayConfig> settlements = getSqlSession().selectList("OrderJobOverDayConfig.findBy", params);
        return new Paging<OrderJobDayConfig>(total, settlements);
    }

    public OrderJobDayConfig findBySkuId(Long skuId) {

        OrderJobDayConfig orderJobDayConfig = getSqlSession().selectOne("OrderJobOverDayConfig.findBySkuId", skuId);
        return orderJobDayConfig;
    }

}
