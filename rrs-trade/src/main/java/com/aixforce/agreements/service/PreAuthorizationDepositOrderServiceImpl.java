package com.aixforce.agreements.service;

import com.aixforce.agreements.dao.PreAuthorizationDao;
import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.common.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 网签用户Service实现
 * Created by neusoft on 14-11-21.
 */
@Slf4j
@Service
public class PreAuthorizationDepositOrderServiceImpl implements PreAuthorizationDepositOrderService {

    @Autowired
    private PreAuthorizationDao preAuthorizationDao;

    /**
     * 保存预授权订单信息

     * @return 创建结果
     */
    @Override
    public Response<Boolean> createPreDepositOrder(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {
        Response<Boolean> result = new Response<Boolean>();

        if (preAuthorizationDepositOrder == null) {
            log.warn("preAuthorizationDepositOrder is null, return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }
        try {
            preAuthorizationDao.create(preAuthorizationDepositOrder);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to create preAuthorizationDepositOrder, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setResult(Boolean.FALSE);
        }
        return result;
    }

    /**
     * 更新预授权订单信息
     *
     */
    @Override
    public Response<Boolean> updatePreDepositOrder(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {

        Response<Boolean> result = new Response<Boolean>();

        if (preAuthorizationDepositOrder == null) {
            log.warn("preAuthorizationDepositOrder is null, return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }
        try {
            preAuthorizationDao.update(preAuthorizationDepositOrder);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to update preAuthorizationDepositOrder by orderid, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setResult(Boolean.FALSE);
        }
        return result;
    }

    @Override
    public Response<PreAuthorizationDepositOrder> findAgreementsByOrderId(Long orderId) {
        Response<PreAuthorizationDepositOrder> result = new Response<PreAuthorizationDepositOrder>();

        if (orderId == null) {
            log.warn("orderId is null, return directly");
            result.setError("orderId is null");
            return result;
        }
        try {
            result.setResult(preAuthorizationDao.findAgreementByOrderId(orderId));
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("failed to update preAuthorizationDepositOrder by orderid, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
        }
        return result;
    }

    @Override
    public Response<Boolean> updatePreDepositById(PreAuthorizationDepositOrder preAuthorizationDepositOrder) {
        Response<Boolean> result = new Response<Boolean>();

        if (preAuthorizationDepositOrder == null) {
            log.warn("preAuthorizationDepositOrder is null, return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }
        try {
            preAuthorizationDao.updateById(preAuthorizationDepositOrder);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to update preAuthorizationDepositOrder by orderid, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setResult(Boolean.FALSE);
        }
        return result;
    }


    @Override
    public Response<PreAuthorizationDepositOrder> findPreDepositByOrderId(Long orderId) {
        Response<PreAuthorizationDepositOrder> result = new Response<PreAuthorizationDepositOrder>();

        if (orderId == null) {
            log.warn("orderId is null, return directly");
            result.setError("orderId is null");
            return result;
        }
        try {
            result.setResult(preAuthorizationDao.findOneByOrderId(orderId));
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("failed to update preAuthorizationDepositOrder by orderid, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
        }
        return result;
    }

    /**
     * 取得正在试用中的最大订单号(已付押金且已发货)
     *
     */
    @Override
    public Response<Long> maxOnTrialId() {
        Response<Long> result = new Response<Long>();

        try {
            result.setResult(preAuthorizationDao.maxOnTrialId());
            result.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to get max on tria orderid, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setSuccess(Boolean.FALSE);
        }
        return result;
    }

    /**
     * 取得正在试用中的订单号(已付押金且已发货)
     * @param lastId 最大订单号
     * @param limit  200
     * @return 结果
     */
    @Override
    public Response<List<Long>> findOnTrialOrderId(Long lastId,Integer limit) {
        Response<List<Long>> result = new Response<List<Long>>();

        try {
            result.setResult(preAuthorizationDao.findOnTrialOrderId(lastId,limit));
            result.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to get on trial order, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setSuccess(Boolean.FALSE);
        }
        return result;
    }

    /**
     * 根据订单号取得预授权押金订单信息
     * @param orderId 订单号
     * @return 结果
     */
    @Override
    public Response<PreAuthorizationDepositOrder> findOneByOrderId(Long orderId) {
        Response<PreAuthorizationDepositOrder> result = new Response<PreAuthorizationDepositOrder>();

        try {
            result.setResult(preAuthorizationDao.findOneByOrderId(orderId));
            result.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to get order, cause:", e);
            result.setError("preAuthorizationDepositOrder.query.fail");
            result.setSuccess(Boolean.FALSE);
        }
        return result;
    }
}
