package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.DeliveryMethodDao;
import com.aixforce.trade.model.DeliveryMethod;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 14-9-3
 */
@Service @Slf4j
public class DeliveryMethodServiceImpl implements DeliveryMethodService {

    @Autowired
    private DeliveryMethodDao deliveryMethodDao;

    @Override
    public Response<Long> create(DeliveryMethod deliveryMethod) {
        Response<Long> result = new Response<Long>();

        if(deliveryMethod == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            deliveryMethod.setStatus(DeliveryMethod.Status.STOP.value());
            Long id = deliveryMethodDao.create(deliveryMethod);
            result.setResult(id);
            return result;
        }catch (Exception e) {
            log.error("fail to create delivery method by {}, cause:{}",
                    deliveryMethod, Throwables.getStackTraceAsString(e));
            result.setError("delivery.method.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(DeliveryMethod deliveryMethod) {
        Response<Boolean> result = new Response<Boolean>();

        if(deliveryMethod == null || deliveryMethod.getId() == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            //type创建后就不能修改
            deliveryMethod.setType(null);
            deliveryMethodDao.update(deliveryMethod);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update delivery method {}, cause:{}",deliveryMethod,Throwables.getStackTraceAsString(e));
            result.setError("delivery.method.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateStatus(Long id, Integer status) {
        Response<Boolean> result = new Response<Boolean>();

        if(id == null || status == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            deliveryMethodDao.updateStatus(id, status);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update delivery method status by id={}, status={},cause:{}",
                    id, status, Throwables.getStackTraceAsString(e));
            result.setError("delivery.method.update.fail");
            return result;
        }
    }

    @Override
    public Response<DeliveryMethod> findById(Long id) {
        Response<DeliveryMethod> result = new Response<DeliveryMethod>();

        if(id == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            DeliveryMethod deliveryMethod = deliveryMethodDao.findById(id);
            result.setResult(deliveryMethod);
            return result;
        }catch (Exception e) {
            log.error("fail to find delivery method by id={}, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            result.setError("delivery.method.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<DeliveryMethod>> findBy(Integer status, Integer type) {
        Response<List<DeliveryMethod>> result = new Response<List<DeliveryMethod>>();

        Map<String, Object> params = Maps.newHashMap();
        if(status != null) {
            params.put("status", status);
        }
        type = Objects.firstNonNull(type, 1);
        params.put("type", type);

        try {
            List<DeliveryMethod> deliveryMethods = deliveryMethodDao.findBy(params);
            result.setResult(deliveryMethods);
            return result;
        }catch (Exception e) {
            log.error("fail to find by status={}, cause:{}",status, Throwables.getStackTraceAsString(e));
            result.setError("delivery.method.query.fail");
            return result;
        }
    }
}
