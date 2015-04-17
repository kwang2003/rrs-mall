package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.OrderInstallInfoDao;
import com.aixforce.trade.model.OrderInstallInfo;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 订单物流信息服务实现
 * Author: haolin
 * On: 9/23/14
 */
@Service @Slf4j
public class OrderInstallInfoServiceImpl implements OrderInstallInfoService {

    @Autowired
    private OrderInstallInfoDao orderInstallInfoDao;

    /**
     * 创建订单安装信息
     *
     * @param orderInstallInfo 订单安装信息
     * @return
     */
    @Override
    public Response<Long> create(OrderInstallInfo orderInstallInfo) {
        Response<Long> resp = new Response<Long>();
        try {
            orderInstallInfoDao.create(orderInstallInfo);
            resp.setResult(orderInstallInfo.getId());
        } catch (Exception e){
            log.error("failed to create order install info({}), cause: {}",
                    orderInstallInfo, Throwables.getStackTraceAsString(e));
            resp.setError("order.install.info.create.fail");
        }
        return resp;
    }

    /**
     * 查询订单的物流信息
     *
     * @param orderId 订单id
     * @return 订单的物流信息
     */
    @Override
    public Response<List<OrderInstallInfo>> findByOrderId(Long orderId) {
        Response<List<OrderInstallInfo>> resp = new Response<List<OrderInstallInfo>>();
        try {
            List<OrderInstallInfo> orderInstallInfo = orderInstallInfoDao.findByOrderId(orderId);
            resp.setResult(orderInstallInfo);
        } catch (Exception e){
            log.error("failed to find OrderInstallInfo(orderId={}), cause: {}", orderId, Throwables.getStackTraceAsString(e));
            resp.setError("order.install.info.find.fail");
        }
        return resp;
    }
}
