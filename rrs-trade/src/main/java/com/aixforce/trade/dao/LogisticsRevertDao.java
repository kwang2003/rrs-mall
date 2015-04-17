package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsRevert;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Description：退货物流信息
 * Author：Guo Chaopeng
 * Created on 14-4-21-下午5:47
 */
@Repository
public class LogisticsRevertDao extends SqlSessionDaoSupport {

    public Long create(LogisticsRevert logisticsRevert) {
        getSqlSession().insert("LogisticsRevert.create", logisticsRevert);
        return logisticsRevert.getId();
    }

    public void delete(Long id) {
        getSqlSession().delete("LogisticsRevert.delete", id);
    }

    public LogisticsRevert findById(Long id) {
        return getSqlSession().selectOne("LogisticsRevert.findById", id);
    }

    public void update(LogisticsRevert logisticsRevert) {
        getSqlSession().update("LogisticsRevert.update", logisticsRevert);
    }

    public LogisticsRevert findByOrderItemId(Long orderItemId) {
        return getSqlSession().selectOne("LogisticsRevert.findByOrderItemId", orderItemId);
    }


}
