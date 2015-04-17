package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrdersPopularize;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by neusoft on 14-9-15.
 */
@Repository
public class OrdersPopularizeDao extends SqlSessionDaoSupport{

    public int create(Map map) {
        return getSqlSession().insert("OrdersPopularize.insert", map);
    }

    public List<OrdersPopularize> findOrdersByCreatedAt(Map map) {
        return getSqlSession().selectList("OrdersPopularize.getByCreatedAt", map);
    }

    public List<OrdersPopularize> findOrdersByUpdatedAt(Map map) {
        return getSqlSession().selectList("OrdersPopularize.getByUpdateAt", map);
    }
}
