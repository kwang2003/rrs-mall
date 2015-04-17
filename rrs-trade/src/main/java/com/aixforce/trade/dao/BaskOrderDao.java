package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.BaskOrder;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by songrenfei on 14-9-16.
 */

@Repository
public class BaskOrderDao extends SqlSessionDaoSupport{

    public Long create(BaskOrder baskOrder) {
        getSqlSession().insert("BaskOrder.create", baskOrder);
        return baskOrder.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("BaskOrder.delete", id) == 1;
    }

    public boolean update(BaskOrder baskOrder) {
        return getSqlSession().update("BaskOrder.update", baskOrder) == 1;
    }

    public BaskOrder findById(Long id) {
        return getSqlSession().selectOne("BaskOrder.findById", id);
    }

    public Paging<BaskOrder> pagingByItemId(Long itemId, Integer offset, Integer limit) {
        Map<String, Object> n = Maps.newHashMap();
        n.put("itemId", itemId);
        Long total = getSqlSession().selectOne("BaskOrder.countByItemId", n);
        if (total==0) {
            return Paging.empty(BaskOrder.class);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("itemId", itemId);
        params.put("offset", offset);
        params.put("limit", limit);
        List<BaskOrder> data = getSqlSession().selectList("BaskOrder.pagingByItemId", params);
        return new Paging<BaskOrder>(total, data);
    }

    /**
     * 根据评论id查找晒单
     * @param id
     * @return
     */
    public BaskOrder findByOrderCommentId(Long id){
        return getSqlSession().selectOne("BaskOrder.findByOrderCommentId",id);
    }

    /**
     * 根据子订单id查找晒单
     * @param id
     * @return
     */
    public BaskOrder findByOrderItemId(Long id){
        return getSqlSession().selectOne("BaskOrder.findByOrderItemId",id);
    }
}
