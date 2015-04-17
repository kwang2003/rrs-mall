package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.exception.ServiceException;
import com.aixforce.trade.dao.ExpressInfoDao;
import com.aixforce.trade.dao.OrderLogisticsInfoDao;
import com.aixforce.trade.dto.OrderLogisticsInfoDto;
import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.trade.model.OrderLogisticsInfo;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单物流信息服务实现
 * Author: haolin
 * On: 9/23/14
 */
@Service @Slf4j
public class OrderLogisticsInfoServiceImpl implements OrderLogisticsInfoService {

    @Autowired
    private OrderLogisticsInfoDao orderLogisticsInfoDao;

    @Autowired
    private ExpressInfoDao expressInfoDao;

    private void checkBeforeCreate(OrderLogisticsInfoDto orderLogisticsInfoDto) {
        OrderLogisticsInfo orderLogisticsInfo = orderLogisticsInfoDto.getOrderLogisticsInfo();
        if (Objects.equal(OrderLogisticsInfo.Type.THIRD.value(),
                orderLogisticsInfo.getType())){
            ExpressInfo expressInfo = expressInfoDao.findByName(orderLogisticsInfo.getExpressName());
            if (expressInfo == null || Objects.equal(ExpressInfo.Status.DELETED.value(), expressInfo.getStatus())){
                log.error("express info({}) isn't exist or deleted.", expressInfo);
                throw new ServiceException("express.info.not.exist");
            } else if (Objects.equal(ExpressInfo.Status.DISABLED.value(), expressInfo.getStatus())){
                log.error("express info({}) is disabled", expressInfo);
                throw new ServiceException("express.info.disabled");
            }
        }
    }

    /**
     * 查询订单的物流信息
     *
     * @param orderId 订单id
     * @return 订单的物流信息
     */
    @Override
    public Response<OrderLogisticsInfo> findByOrderId(Long orderId) {
        Response<OrderLogisticsInfo> resp = new Response<OrderLogisticsInfo>();
        try {
            OrderLogisticsInfo orderLogisticsInfo = orderLogisticsInfoDao.findByOrderId(orderId);
            if (orderLogisticsInfo == null){
                log.error("order(id={})'s logistics info isn't exist.", orderId);
                resp.setError("order.logistics.info.not.exist");
                return resp;
            }
            resp.setResult(orderLogisticsInfo);
        } catch (Exception e){
            log.error("failed to find OrderLogisticsInfo(orderId={}), cause: {}", orderId, Throwables.getStackTraceAsString(e));
            resp.setError("order.logistics.info.find.fail");
        }
        return resp;
    }
}
