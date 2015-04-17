package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrderLogisticsInfo;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * 订单物流信息Dao实现
 * Author: haolin
 * On: 9/22/14
 */
@Repository
public class OrderLogisticsInfoDao extends SqlSessionDaoSupport {

    /**
     * 创建订单物流信息
     * @param orderLogisticsInfo 订单物流信息
     * @return 创建记录数
     */
    public Integer create(OrderLogisticsInfo orderLogisticsInfo){
        return getSqlSession().insert("OrderLogisticsInfo.create", orderLogisticsInfo);
    }

    /**
     * 更新订单物流信息
     * @param orderLogisticsInfo 订单物流信息
     * @return 更新记录数
     */
    public Integer update(OrderLogisticsInfo orderLogisticsInfo){
        return getSqlSession().update("OrderLogisticsInfo.update", orderLogisticsInfo);
    }

    /**
     * 删除订单物流信息(逻辑删除)
     * @param id 订单物流信息id
     * @return 删除记录数
     */
    public Integer delete(Long id){
        return getSqlSession().update("OrderLogisticsInfo.delete", id);
    }

    /**
     * 获取订单物流信息
     * @param id 订单物流信息id
     * @return 订单物流信息
     */
    public OrderLogisticsInfo load(Long id){
        return getSqlSession().selectOne("OrderLogisticsInfo.load", id);
    }

    /**
     * 通过名称查找订单物流信息
     * @param orderId 订单id
     * @return OrderLogisticsInfo或者Null
     */
    public OrderLogisticsInfo findByOrderId(Long orderId){
        return getSqlSession().selectOne("OrderLogisticsInfo.findByOrderId", orderId);
    }
}
