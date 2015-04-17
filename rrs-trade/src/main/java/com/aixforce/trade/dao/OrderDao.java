/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.Order;
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
public class OrderDao extends SqlSessionDaoSupport {

    public Order findById(Long id) {
        return getSqlSession().selectOne("Order.findById", id);
    }

    public Order findByOriginId(Long id) {
        return getSqlSession().selectOne("Order.findByOriginId", id);
    }

    public List<Order> findByIds(List<Long> ids){
        if(ids.isEmpty()){
            return Collections.emptyList();
        }
        return getSqlSession().selectList("Order.findByIds", ids);
    }



    public Long create(Order order) {
        getSqlSession().insert("Order.create", order);
        return order.getId();
    }

    public void delete(Long id) {
        getSqlSession().delete("Order.delete", id);
    }

    public void update(Order order) {
        getSqlSession().update("Order.update", order);
    }

    public Long countOfFinished(Date startAt, Date endAt) {
        return getSqlSession().selectOne("Order.countOfFinished",
                ImmutableMap.of("startAt", startAt, "endAt", endAt));
    }

    public List<Order> findNotFinished(Long lastId, String compared, Integer limit) {
        return getSqlSession().selectList("Order.findNotFinished",
                ImmutableMap.of("lastId", lastId, "limit", limit, "compared", compared) );
    }

    public List<Order> findNotPaid(Long lastId, String startAt, String endAt, Integer limit) {
        return getSqlSession().selectList("Order.findNotPaid",
                ImmutableMap.of("lastId", lastId, "limit", limit, "startAt", startAt, "endAt", endAt));
    }

    public List<Order> findNotConfirmDeliver(Long lastId, String startAt, String endAt, Integer limit) {
        return getSqlSession().selectList("Order.findNotConfirmDeliver",
                ImmutableMap.of("lastId", lastId, "limit", limit, "startAt", startAt, "endAt", endAt));
    }

    public Paging<Order> findFinished(Date startAt, Date endAt, int offset, Integer size) {
        Long count = getSqlSession().selectOne("Order.countOfFinished",
                ImmutableMap.of("startAt", startAt, "endAt", endAt));
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<Order> orders = getSqlSession().selectList("Order.findFinished",
                    ImmutableMap.of("startAt", startAt, "endAt", endAt, "offset", offset, "size", size));
            return new Paging<Order>(count, orders);
        }
        return new Paging<Order>(0L, Collections.<Order>emptyList());
    }

    public Paging<Order> findUpdated(List<Long> businesses, Date startAt, Date endAt, int offset, Integer limit) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("startAt", startAt);
        params.put("endAt", endAt);
        params.put("businesses", businesses);
        params.put("offset", offset);
        params.put("limit", limit);


        Long count = getSqlSession().selectOne("Order.countOfUpdated",params);
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<Order> orders = getSqlSession().selectList("Order.findUpdated", params);
            return new Paging<Order>(count, orders);
        }
        return new Paging<Order>(0L, Collections.<Order>emptyList());
    }

    public Paging<Order> findUpdatedAndSellerIds(List<Long> businesses, Date startAt, Date endAt, int offset, Integer limit, List<Long> sellerIds) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("startAt", startAt);
        params.put("endAt", endAt);
        params.put("businesses", businesses);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("sellerIds", sellerIds);


        Long count = getSqlSession().selectOne("Order.countOfUpdated",params);
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<Order> orders = getSqlSession().selectList("Order.findUpdated", params);
            return new Paging<Order>(count, orders);
        }
        return new Paging<Order>(0L, Collections.<Order>emptyList());
    }

    public boolean expired(Order order) {
        return getSqlSession().update("Order.expired", order) == 1;
    }

    public Long maxId() {
        return Objects.firstNonNull((Long)getSqlSession().selectOne("Order.maxId"),0L);
    }


    public boolean emptyCanceledAndFinishedAt(Long id) {
        return getSqlSession().update("Order.emptyCanceledAndFinishedAt", id) == 1L;
    }


    public Paging<Order> findBy(Order criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getCreatedAt() != null) {
            Date createdAt = criteria.getCreatedAt();
            params.put("createdStartAt", startOfDay(createdAt));
            params.put("createdEndAt", endOfDay(createdAt));
        }



        return findBy(params);
    }

    public Paging<Order> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("Order.countOf", params);
        if (total == 0L) {
            return new Paging<Order>(0L, Collections.<Order>emptyList());
        }
        List<Order> settlements = getSqlSession().selectList("Order.findBy", params);
        return new Paging<Order>(total, settlements);
    }

    /**
     * 取得押金试用失联（>=45天）的订单
     * @param ids id列表
     * @return  订单列表
     */
    public List<Long> findOnTrialTimeOutOrder(List<Long> ids) {
        if(ids.isEmpty()){
            return Collections.emptyList();
        }
        return getSqlSession().selectList("Order.findOnTrialTimeOutOrder", ids);
    }
}
