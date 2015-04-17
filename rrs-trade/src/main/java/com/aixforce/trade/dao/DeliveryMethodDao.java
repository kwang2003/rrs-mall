package com.aixforce.trade.dao;

import com.aixforce.trade.model.DeliveryMethod;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 14-9-3
 */
@Repository
public class DeliveryMethodDao extends SqlSessionDaoSupport {

    public Long create(DeliveryMethod deliveryMethod) {
        getSqlSession().insert("DeliveryMethod.create", deliveryMethod);
        return deliveryMethod.getId();
    }

    public void update(DeliveryMethod deliveryMethod) {
        getSqlSession().update("DeliveryMethod.update", deliveryMethod);
    }

    public void updateStatus(Long id, Integer status) {
        getSqlSession().update("DeliveryMethod.updateStatus", ImmutableMap.of(
                "id", id, "status", status
        ));
    }

    public DeliveryMethod findById(Long id) {
        return getSqlSession().selectOne("DeliveryMethod.findById", id);
    }

    public List<DeliveryMethod> findBy(Map<String, Object> params) {
        return getSqlSession().selectList("DeliveryMethod.findBy", params);
    }
}
