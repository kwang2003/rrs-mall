package com.aixforce.trade.dao;

import com.aixforce.trade.model.OrderInstallInfo;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单安装信息Dao
 * Author: haolin
 * On: 9/23/14
 */
@Repository
public class OrderInstallInfoDao extends SqlSessionDaoSupport {

    /**
     * 创建订单安装信息
     * @param orderInstallInfo 订单安装信息
     * @return 创建记录数
     */
    public Integer create(OrderInstallInfo orderInstallInfo){
        return getSqlSession().insert("OrderInstallInfo.create", orderInstallInfo);
    }

    /**
     * 更新订单安装信息
     * @param orderInstallInfo 订单安装信息
     * @return 更新记录数
     */
    public Integer update(OrderInstallInfo orderInstallInfo){
        return getSqlSession().update("OrderInstallInfo.update", orderInstallInfo);
    }

    /**
     * 查询订单安装信息
     * @param orderId 订单id
     * @return 订单安装信息
     */
    public List<OrderInstallInfo> findByOrderId(Long orderId){
        return getSqlSession().selectList("OrderInstallInfo.findByOrderId", orderId);
    }
}
