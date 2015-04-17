package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.model.OrderLogisticsInfo;

/**
 * 订单物流信息服务
 * Author: haolin
 * On: 9/23/14
 */
public interface OrderLogisticsInfoService {

    /**
     * 查询订单的物流信息
     * @param orderId 订单id
     * @return 订单的物流信息
     */
    Response<OrderLogisticsInfo> findByOrderId(Long orderId);
}
