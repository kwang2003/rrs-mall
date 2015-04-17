package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.settle.model.AlipayTrans;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 2:06 PM  <br>
 * Author: xiao
 */
@Repository
public class AlipayTransDao extends SqlSessionDaoSupport {

    public Long create(AlipayTrans alipayTrans) {
        AlipayTrans criteria = new AlipayTrans();
        criteria.setIwAccountLogId(alipayTrans.getIwAccountLogId());

        AlipayTrans existed = getBy(criteria);
        if (existed != null) {
            return existed.getId();
        }

        getSqlSession().insert("AlipayTrans.create", alipayTrans);
        return alipayTrans.getId();
    }

    public AlipayTrans get(Long id) {
        return getSqlSession().selectOne("AlipayTrans.get", id);

    }

    public boolean delete(Long id) {
        return getSqlSession().delete("AlipayTrans.delete", id) == 1;
    }


    public AlipayTrans getBy(AlipayTrans criteria) {
        return getSqlSession().selectOne("AlipayTrans.getBy", criteria);
    }

    public List<AlipayTrans> findByTradeNo(String paymentCode) {
        return getSqlSession().selectList("AlipayTrans.findByTradeNo", paymentCode);
    }

    public List<AlipayTrans> findByMerchantNo(String merchantOuterTradeNo) {
        return getSqlSession().selectList("AlipayTrans.findByMerchantNo", merchantOuterTradeNo);
    }
}
