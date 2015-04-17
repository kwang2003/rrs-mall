package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.model.OrderInstallInfo;
import java.util.List;

/**
 * 订单安装信息服务
 * Author: haolin
 * On: 9/23/14
 */
public interface OrderInstallInfoService {

    /**
     * 创建订单安装信息
     * @param orderInstallInfo 订单安装信息
     * @return
     */
    Response<Long> create(OrderInstallInfo orderInstallInfo);

    /**
     * 查询订单的物流信息
     * @param orderId 订单id
     * @return 订单的物流信息
     */
    Response<List<OrderInstallInfo>> findByOrderId(Long orderId);
}
