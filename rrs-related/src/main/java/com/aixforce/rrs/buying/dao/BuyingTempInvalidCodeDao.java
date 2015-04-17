package com.aixforce.rrs.buying.dao;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by wangyb on 15-1-30.
 */
@Repository
public class BuyingTempInvalidCodeDao extends SqlSessionDaoSupport{
    public Long findByOrderId(Long id) {
        return getSqlSession().selectOne("BuyingTempInvalidCode.findById", id);
    }
}
