package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.settle.model.BusinessRate;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-14 10:21 AM  <br>
 * Author: xiao
 */
@Repository
public class BusinessRateDao extends SqlSessionDaoSupport {

    public Long create(BusinessRate rate) {
        getSqlSession().insert("BusinessRate.create", rate);
        return rate.getId();
    }

    public BusinessRate get(Long id) {
        return getSqlSession().selectOne("BusinessRate.get", id);
    }

    public BusinessRate findByBusiness(Long business) {
        return getSqlSession().selectOne("BusinessRate.findByBusiness",
                ImmutableMap.of("business", business));
    }
}
