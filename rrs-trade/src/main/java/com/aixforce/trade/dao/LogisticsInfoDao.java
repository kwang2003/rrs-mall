package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsInfo;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Description：物流信息
 * Author：Guo Chaopeng
 * Created on 14-4-21-下午3:35
 */
@Repository
public class LogisticsInfoDao extends SqlSessionDaoSupport {

    public Long create(LogisticsInfo logisticsInfo) {
        getSqlSession().insert("LogisticsInfo.create", logisticsInfo);
        return logisticsInfo.getId();
    }

    public void delete(Long id) {
        getSqlSession().delete("LogisticsInfo.delete", id);
    }

    public LogisticsInfo findById(Long id) {
        return getSqlSession().selectOne("LogisticsInfo.findById", id);
    }

    public void updateByOrderId(LogisticsInfo logisticsInfo) {
        getSqlSession().update("LogisticsInfo.updateByOrderId", logisticsInfo);
    }

    public LogisticsInfo findByOrderId(Long orderId) {
        return getSqlSession().selectOne("LogisticsInfo.findByOrderId", orderId);
    }

}
