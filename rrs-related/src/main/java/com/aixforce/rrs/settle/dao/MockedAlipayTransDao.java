package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.MockedAlipayTrans;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 2:06 PM  <br>
 * Author: xiao
 */
@Repository
public class MockedAlipayTransDao extends SqlSessionDaoSupport {

    private static final String NAMESPACE = "MockedAlipayTrans.";


    public Long create(MockedAlipayTrans alipayTrans) {
        MockedAlipayTrans criteria = new MockedAlipayTrans();
        criteria.setIwAccountLogId(alipayTrans.getIwAccountLogId());

        MockedAlipayTrans existed = getBy(criteria);
        if (existed != null) {
            return existed.getId();
        }

        getSqlSession().insert(NAMESPACE + "create", alipayTrans);
        return alipayTrans.getId();
    }

    public MockedAlipayTrans get(Long id) {
        return getSqlSession().selectOne(NAMESPACE + "get", id);

    }

    public boolean delete(Long id) {
        return getSqlSession().delete(NAMESPACE + "delete", id) == 1;
    }


    public MockedAlipayTrans getBy(MockedAlipayTrans criteria) {
        return getSqlSession().selectOne(NAMESPACE + "getBy", criteria);
    }

    public List<MockedAlipayTrans> findByTradeNo(String paymentCode) {
        return getSqlSession().selectList(NAMESPACE + "findByTradeNo", paymentCode);
    }

    public List<MockedAlipayTrans> findByMerchantNo(String merchantOuterTradeNo) {
        return getSqlSession().selectList(NAMESPACE + "findByMerchantNo", merchantOuterTradeNo);
    }


    public List<MockedAlipayTrans> list(String tradeNo) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        MockedAlipayTrans criteria = new MockedAlipayTrans();
        criteria.setTradeNo(tradeNo);
        params.put("criteria", criteria);
        return getSqlSession().selectList(NAMESPACE + "findBy", params);
    }


    public Paging<MockedAlipayTrans> findBy(MockedAlipayTrans criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getCreatedAt() != null) {
            Date createdAt = criteria.getCreatedAt();
            params.put("createdStartAt", startOfDay(createdAt));
            params.put("createdEndAt", endOfDay(createdAt));
        }
        return findBy(params);
    }
    


    public Paging<MockedAlipayTrans> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne(NAMESPACE + "countOf", params);
        if (total == 0L) {
            return new Paging<MockedAlipayTrans>(0L, Collections.<MockedAlipayTrans>emptyList());
        }
        List<MockedAlipayTrans> transes = getSqlSession().selectList(NAMESPACE + "findBy", params);
        return new Paging<MockedAlipayTrans>(total, transes);
    }
}
