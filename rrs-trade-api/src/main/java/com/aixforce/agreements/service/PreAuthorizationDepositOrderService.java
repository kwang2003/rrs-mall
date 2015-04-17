package com.aixforce.agreements.service;

import com.aixforce.common.model.Response;
import com.aixforce.agreements.model.PreAuthorizationDepositOrder;

import java.util.List;

/**
 * 网签用户Service
 * Created by neusoft on 14-11-21.
 */
public interface PreAuthorizationDepositOrderService {

    /**
     * 保存预授权订单信息
     *
     */
    Response<Boolean> createPreDepositOrder(PreAuthorizationDepositOrder preAuthorizationDepositOrder);

    /**
     * 更新预授权订单信息
     *
     */
    Response<Boolean> updatePreDepositOrder(PreAuthorizationDepositOrder preAuthorizationDepositOrder);

    /**
     * 查找预授权订单信息
     *
     */
    Response<PreAuthorizationDepositOrder> findAgreementsByOrderId(Long orderId);

    /**
     * 更新预授权订单信息
     *
     */
    Response<Boolean> updatePreDepositById(PreAuthorizationDepositOrder preAuthorizationDepositOrder);

    /**
     *
     * */
    Response<PreAuthorizationDepositOrder> findPreDepositByOrderId(Long orderId);

    /**
     * 取得正在试用中的最大订单号(已付押金且已发货)
     *
     */
    Response<Long> maxOnTrialId();

    /**
     * 取得正在试用中的订单号(已付押金且已发货)
     * @param lastId 最大订单号
     * @param limit  200
     * @return 结果
     */
    Response<List<Long>> findOnTrialOrderId(Long lastId,Integer limit);

    /**
     * 根据订单号取得预授权押金订单信息
     * @param orderId 订单号
     * @return 结果
     */
    Response<PreAuthorizationDepositOrder> findOneByOrderId(Long orderId);
}
