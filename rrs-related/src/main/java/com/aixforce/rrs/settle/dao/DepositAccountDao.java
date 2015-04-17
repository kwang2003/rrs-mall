package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.DepositAccount;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-20 11:32 AM  <br>
 * Author: xiao
 */
@Repository
public class DepositAccountDao extends SqlSessionDaoSupport{

    public Long create(DepositAccount account) {
        getSqlSession().insert("DepositAccount.create", account);
        return account.getId();
    }

    public DepositAccount get(Long id) {
        return getSqlSession().selectOne("DepositAccount.get", id);
    }

    public Paging<DepositAccount> findBy(DepositAccount criteria,
                                         Integer lower, Integer upper,
                                         Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("criteria", criteria);
        params.put("lower", lower);
        params.put("upper", upper);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    public Paging<DepositAccount> findBy(Map<String, Object> params) {
        Long count = getSqlSession().selectOne("DepositAccount.countOf", params);
        if(count == 0L){
            return new Paging<DepositAccount>(0L, Collections.<DepositAccount>emptyList());
        }

        List<DepositAccount> r = getSqlSession().selectList("DepositAccount.findBy",params);
        return new Paging<DepositAccount>(count, r);
    }


    /**
     * 根据商户号查询该商家的保证金账户
     *
     * @param sellerId  商家id
     * @return 商家保证金账户
     */
    public DepositAccount findBySellerId(Long sellerId) {
        return getSqlSession().selectOne("DepositAccount.findBySellerId", ImmutableMap.of("sellerId", sellerId));
    }

    /**
     * 更新账户余额
     *
     * @param id        账户id
     * @param balance   余额
     * @return  是否成功
     */
    public boolean updateBal(Long id, Long balance) {
        return getSqlSession().update("DepositAccount.updateBal", ImmutableMap.of("id", id, "balance", balance)) == 1;
    }

    /**
     * 更新商家88码
     */
    public Integer updateOuterCode(String outerCode, Long sellerId) {
        return getSqlSession().update("DepositAccount.updateOuterCode",
                ImmutableMap.of("outerCode", outerCode, "sellerId", sellerId));
    }

}
