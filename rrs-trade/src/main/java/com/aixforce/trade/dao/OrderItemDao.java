/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.OrderItem;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class OrderItemDao extends SqlSessionDaoSupport {

    public OrderItem findById(Long id) {
        return getSqlSession().selectOne("OrderItem.findById", id);
    }

    public OrderItem findByOriginId(Long id) {
        return getSqlSession().selectOne("OrderItem.findByOriginId", id);
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        return getSqlSession().selectList("OrderItem.findByOrderId", orderId);
    }

    public OrderItem findByMap(Long orderId,int type){
        Map<String, Object> map = Maps.newHashMap();
        map.put("orderId",orderId);
        map.put("type",type);
        return getSqlSession().selectOne("OrderItem.findByMap",map);
    }

    public List<OrderItem> findByPaymentCode(String paymentCode) {
        return getSqlSession().selectList("OrderItem.findByPaymentCode", paymentCode);
    }

    public void create(OrderItem orderItem) {
        getSqlSession().insert("OrderItem.create", orderItem);
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("OrderItem.delete", id) == 1;
    }

    public void update(OrderItem orderItem) {
        getSqlSession().update("OrderItem.update", orderItem);
    }

    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if(ids.isEmpty()){
            return;
        }
        getSqlSession().update("OrderItem.batchUpdateStatus", ImmutableMap.of("ids", ids, "status", status));
    }

    public List<OrderItem> findInOrderIds(Long... ids) {
        if (ids.length == 0) {
            return Collections.emptyList();
        }
        return getSqlSession().selectList("OrderItem.findInOrderIds", ImmutableMap.of("ids", ids));
    }

    public List<String> findItemNameOfOrders(Long... ids) {
        if (ids.length == 0) {
            return Collections.emptyList();
        }
        return getSqlSession().selectList("OrderItem.findItemNameOfOrders", ImmutableMap.of("ids", ids));
    }

    public List<OrderItem> findOutOfCommentDeadLineOrderItem(Integer expireDays) {
        Objects.firstNonNull(expireDays, 15);

        return getSqlSession().selectList("OrderItem.findOutOfCommentDeadLine", ImmutableMap.of("expireDays", expireDays));
    }

    public void batchUpdatePaymentCode(List<Long> ids, String paymentCode) {
        getSqlSession().update("OrderItem.batchUpdatePaymentCode",
                ImmutableMap.of("ids", ids, "paymentCode", paymentCode));
    }

    public Paging<Long> findOrderIdsBy(Map<String, String> params, Integer offset, Integer limit) {
        Map<String, Object> p = Maps.newHashMap();
        if (params.containsKey("buyerId")) {
            p.put("buyerId", Long.parseLong(params.get("buyerId")));
        }
        if (params.containsKey("sellerId")) {
            p.put("sellerId", Long.parseLong(params.get("sellerId")));
        }
        if (params.containsKey("status")) {
            p.put("status", Integer.parseInt(params.get("status")));
        }
        if (params.containsKey("type")) {
            p.put("type", Integer.parseInt(params.get("type")));
        }
        if (params.containsKey("businessId")) {
            p.put("businessId", Integer.parseInt(params.get("businessId")));
        }
        if (params.containsKey("itemId")) {
            p.put("itemId", Long.parseLong(params.get("itemId")));
        }
        p.put("offset", offset);
        p.put("limit", limit);
        Long count = getSqlSession().selectOne("OrderItem.countOrderIds", p);
        if (count == 0) {
            return new Paging<Long>(0L, Collections.<Long>emptyList());
        } else {
            List<Long> orderIds = getSqlSession().selectList("OrderItem.findOrderIdsBy", p);
            return new Paging<Long>(count, orderIds);
        }
    }

    public List<OrderItem> findNotConfirmRefund(Long lastId, String startAt, String endAt, Integer limit) {
        return getSqlSession().selectList("OrderItem.findNotConfirmRefund",
                ImmutableMap.of("lastId", lastId, "limit", limit, "startAt", startAt, "endAt", endAt));
    }

    public Long maxId() {
        return getSqlSession().selectOne("OrderItem.maxId");
    }

    /**
     * 汇总子订单的退款金额
     *
     * @param sellerId  商户金额
     * @param refundAt  退款时间
     * @return 汇总金额
     */
    public Long sumRefundAmountOfShopInDate(Long sellerId, Date refundAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("sellerId", sellerId);
        params.put("refundStartAt", startOfDay(refundAt));
        params.put("refundEndAt", endOfDay(refundAt));
        Long value = getSqlSession().selectOne("OrderItem.sumRefundAmountOfSellerInDate", params);
        return  value == null ? 0L : value;

    }

    public Paging<OrderItem> findBy(OrderItem criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getRefundAt() != null) {
            Date refundAt = criteria.getRefundAt();
            params.put("refundStartAt", startOfDay(refundAt));
            params.put("refundEndAt", endOfDay(refundAt));
        }

        return findBy(params);
    }

    public Paging<OrderItem> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("OrderItem.countOf", params);
        if (total == 0L) {
            return new Paging<OrderItem>(0L, Collections.<OrderItem>emptyList());
        }
        List<OrderItem> orderItems = getSqlSession().selectList("OrderItem.findBy", params);
        return new Paging<OrderItem>(total, orderItems);
    }


    private Date startOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    private Date endOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().plusDays(1).toDate();
    }

    /**
     * 更新子订单中总订单的code
     * @param oldId
     * @param newId
     */
    public Boolean updateOrderId(Long oldId, Long newId) {
        if(oldId==null||newId==null){
            return false;
        }
       return (getSqlSession().update("OrderItem.updateOrderId", ImmutableMap.of("oldId", oldId, "newId", newId))>0);
    }

	public String smsUserStatus(Map<String, Object> map) {
		return getSqlSession().selectOne("OrderItem.smsUserStatus", map);
	}

	public List<Long> itmeIdList() {
		return getSqlSession().selectList("OrderItem.itmeIdList");
	}

	public List<Map<String, Object>> buyerIdList(Map<String, Object> map) {
		return getSqlSession().selectList("OrderItem.buyerIdList", map);
	}
	public Integer updateSmsFloag(Map<String, Object> map) {
		return getSqlSession().update("OrderItem.updateSmsFloag", map);
	}

    public void updateOrderIdType(OrderItem orderItem) {
        getSqlSession().update("OrderItem.updateOrderIdType", orderItem);
    }

    public Paging<Long> queryOrderCouponsByCouponsId(Map<String, String> params, Integer offset, Integer limit) {
        Map<String, Object> p = Maps.newHashMap();
        if (params.containsKey("sellerId")) {
            p.put("sellerId", Long.parseLong(params.get("sellerId")));
        }
        if (params.containsKey("couponsId")) {
            p.put("couponsId", Long.parseLong(params.get("couponsId")));
        }
        p.put("offset", offset);
        p.put("limit", limit);
        Long count = getSqlSession().selectOne("OrderItem.countQueryOrderCouponsByCouponsId", p);
        if (count == 0) {
            return new Paging<Long>(0L, Collections.<Long>emptyList());
        } else {
            List<Long> orderIds = getSqlSession().selectList("OrderItem.queryOrderCouponsByCouponsId", p);
            return new Paging<Long>(count, orderIds);
        }
    }

}
