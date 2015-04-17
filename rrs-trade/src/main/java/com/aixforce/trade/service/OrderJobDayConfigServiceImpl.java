package com.aixforce.trade.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.OrderJobDayConfigDao;
import com.aixforce.trade.dao.OrderJobDayDao;
import com.aixforce.trade.model.OrderJobDayConfig;
import com.aixforce.trade.model.OrderJobOverDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Author:  wangmeng
 * Date: 2014-11-19
 */
@Service
public class OrderJobDayConfigServiceImpl implements OrderJobOverDayConfigService {

    private final static Logger log = LoggerFactory.getLogger(OrderJobDayConfigServiceImpl.class);

    @Autowired
    private OrderJobDayConfigDao orderJobDayConfigDao;

    @Override
    public Response<Boolean> create(OrderJobDayConfig orderJobDayConfig) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            orderJobDayConfigDao.create(orderJobDayConfig);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobDayConfig by orderJobDayConfig:{} , cause:", orderJobDayConfig, e);
            result.setResult(false);
            result.setError("OrderJobDayConfig.query.fail");
        }

        return result;
    }

    @Override
    public Response<OrderJobDayConfig> findBySku(Long skuId) {
        Response<OrderJobDayConfig> result = new Response<OrderJobDayConfig>();

        try {
            OrderJobDayConfig orderJobDayConfig = orderJobDayConfigDao.findBySkuId(skuId);
            result.setResult(orderJobDayConfig);
        } catch (Exception e) {
            log.error("failed to find OrderJobDayConfig by orderIds, cause:", e);
            result.setError("OrderJobDayConfig.query.fail");
        }

        return result;
    }

    @Override
    public Response<Paging<OrderJobDayConfig>> findBy(Map<String, Object> params) {
        Response<Paging<OrderJobDayConfig>> result = new Response<Paging<OrderJobDayConfig>>();

        try {
            Paging<OrderJobDayConfig> orderJobDayConfigP = orderJobDayConfigDao.findBy(params);
            result.setResult(orderJobDayConfigP);
        } catch (Exception e) {
            log.error("failed to find OrderJobDayConfig by map, cause:", e);
            result.setError("OrderJobDayConfig.query.fail");
        }

        return result;
    }
}
