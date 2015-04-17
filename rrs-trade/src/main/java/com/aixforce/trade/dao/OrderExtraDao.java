package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrderExtra;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
@Repository
public class OrderExtraDao extends SqlSessionDaoSupport {

    public Long create(OrderExtra orderExtra) {
        getSqlSession().insert("OrderExtra.create", orderExtra);
        return orderExtra.getId();
    }

    public boolean updateByOrderId(OrderExtra orderExtra) {
        int count = getSqlSession().insert("OrderExtra.updateByOrderId", orderExtra);
        return count > 0;
    }

    public OrderExtra findByOrderId(Long orderId) {
        return getSqlSession().selectOne("OrderExtra.findByOrderId", orderId);
    }

    public List<OrderExtra> findInOrderIds(Long... orderIds) {
        if (orderIds.length == 0)
            return Collections.emptyList();

        return getSqlSession().selectList("OrderExtra.findInOrderIds", ImmutableMap.of("ids", orderIds));
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
        return (getSqlSession().update("OrderExtra.updateOrderId", ImmutableMap.of("oldId", oldId, "newId", newId))>0);
    }

}
