package com.aixforce.agreements.dao;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.common.model.Paging;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 预授权押金Dao
 * Created by neusoft on 14-11-21.
 */
@Repository
public class PreAuthorizationDao extends SqlSessionDaoSupport {

    /**
     * 根据订单号、查询预授权押金信息
     *
     * @param orderId
     * @return PreAuthorizationDepositOrder
     */
    public PreAuthorizationDepositOrder findOneByOrderId(Long orderId) {
        PreAuthorizationDepositOrder preAuthorizationDepositOrder =
                getSqlSession().selectOne("PreAuthorizationDepositOrder.findPreAuthorizationByOrderId", orderId);
        return preAuthorizationDepositOrder;
    }

    /**
     * 根据订单号、创建预授权押金信息
     *
     * @param preAuthorizationDepositOrder
     * @return 结果
     */
    public Long create(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {

        getSqlSession().insert("PreAuthorizationDepositOrder.create", preAuthorizationDepositOrder);
        return preAuthorizationDepositOrder.getId();
    }

    /**
     * 根据订单号、更新预授权押金信息
     *
     * @param preAuthorizationDepositOrder
     * @return 结果
     */
    public void update(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {
        getSqlSession().update("PreAuthorizationDepositOrder.update", preAuthorizationDepositOrder);
    }


    /**
     * 根据订单号、查询预授权押金信息
     *
     * @param orderId
     * @return PreAuthorizationDepositOrder
     */
    public PreAuthorizationDepositOrder findAgreementByOrderId(Long orderId) {
        PreAuthorizationDepositOrder preAuthorizationDepositOrder =
                getSqlSession().selectOne("PreAuthorizationDepositOrder.findAgreementsByOrderId", orderId);
        return preAuthorizationDepositOrder;
    }

    /**
     * 根据订单号、更新预授权押金信息
     *
     * @param preAuthorizationDepositOrder
     * @return 结果
     */
    public void updateById(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {
        getSqlSession().update("PreAuthorizationDepositOrder.updateById", preAuthorizationDepositOrder);
    }

    /**
     * 取得正在试用中的最大订单号(已付押金且已发货)
     *
     * @return 结果
     */
    public Long maxOnTrialId() {
        return getSqlSession().selectOne("PreAuthorizationDepositOrder.maxOnTrialId");
    }

    /**
     * 取得正在试用中的订单号(已付押金且已发货)
     * @param lastId 最大订单号
     * @param limit  200
     * @return 结果
     */
    public List<Long> findOnTrialOrderId(Long lastId,Integer limit) {
        return getSqlSession().selectList("PreAuthorizationDepositOrder.findOnTrialOrderId",
                ImmutableMap.of("lastId", lastId, "limit", limit) );
    }

    /**
     * 根据用户号、查询预授权押金信息
     *
     * @param userId
     * @return PreAuthorizationDepositOrder
     */
    public Paging<Long> findPredepositOrderByUserId(Long userId) {

        Paging<Long> result = new Paging<Long>();

        List<Long> list =
                getSqlSession().selectList("PreAuthorizationDepositOrder.findPredepositOrderByUserId", userId);

        if (list != null && list.size() > 0) {
            return new Paging<Long>(Long.valueOf(list.size()), list);
        }

        return new Paging<Long>(0L, Collections.<Long>emptyList());
    }

}
