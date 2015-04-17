/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.OrderJobOverDay;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;

@Repository
public class OrderJobDayDao extends SqlSessionDaoSupport {

   public Paging<OrderJobOverDay> findByOrderIds(List<Long> ids){
       if (ids.isEmpty()) {
           return new Paging<OrderJobOverDay>(0L, Collections.<OrderJobOverDay>emptyList());
       }

       List<OrderJobOverDay> orderJobOverDayList = getSqlSession().selectList("OrderJobOverDay.findByOrderIds", ids);
       return new Paging<OrderJobOverDay>(Long.valueOf(orderJobOverDayList.size()), orderJobOverDayList);
    }

    public Long create(OrderJobOverDay orderJobOverDay) {
        getSqlSession().insert("OrderJobOverDay.create", orderJobOverDay);
        return orderJobOverDay.getId();
    }

    public void delete(Long id) {
        getSqlSession().delete("OrderJobOverDay.delete", id);
    }

    public void update(OrderJobOverDay orderJobOverDay) {
        getSqlSession().update("OrderJobOverDay.update", orderJobOverDay);
    }

    public void updateByOrderIds(List<Long> orderIds) {
        getSqlSession().update("OrderJobOverDay.updateByOrderIds", orderIds);
    }

    public Long maxId() {
        return Objects.firstNonNull((Long)getSqlSession().selectOne("Order.maxId"),0L);
    }

    public Paging<OrderJobOverDay> findBy(OrderJobOverDay criteria) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("criteria", criteria);
//        params.put("offset", offset);
//        params.put("limit", limit);

        return findBy(params);
    }

    public Paging<OrderJobOverDay> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("OrderJobOverDay.countOf", params);
        if (total == 0L) {
            return new Paging<OrderJobOverDay>(0L, Collections.<OrderJobOverDay>emptyList());
        }
        List<OrderJobOverDay> settlements = getSqlSession().selectList("OrderJobOverDay.findBy", params);
        return new Paging<OrderJobOverDay>(total, settlements);
    }

}
