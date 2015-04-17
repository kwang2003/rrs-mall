package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.settle.model.AbnormalTrans;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 2:04 PM  <br>
 * Author: xiao
 */
@Repository
public class AbnormalTransDao extends SqlSessionDaoSupport{

    public Long create(AbnormalTrans abnormalTrans) {
        checkNotNull(abnormalTrans.getSettlementId());
        checkNotNull(abnormalTrans.getOrderId());

        getSqlSession().insert("AbnormalTrans.create", abnormalTrans);
        return abnormalTrans.getId();
    }


    public AbnormalTrans get(Long id) {
        return getSqlSession().selectOne("AbnormalTrans.get", id);
    }
}
