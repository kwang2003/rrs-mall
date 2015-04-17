package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.LogisticsOrder;
import com.aixforce.trade.model.LogisticsInfo;

/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-22-上午10:09
 */
public interface LogisticsInfoService {

    /**
     * 录入发货物流信息
     *
     * @param logisticsInfo 发货物流信息
     * @return 生成的id
     */
    public Response<Long> create(LogisticsInfo logisticsInfo);

    /**
     * 根据订单id获取查看物流所需显示的订单信息
     *
     * @param orderId       订单id
     * @param currentUserId 当前操作用户id
     * @return 订单信息
     */
    public Response<LogisticsOrder> findOrderByOrderId(Long orderId, Long currentUserId);

    /**
     * 根据订单id更改发货物流信息
     *
     * @param logisticsInfo 待更新的物流信息
     * @param currentUserId 当前操作用户id
     * @return 操作是否成功
     */
    public Response<Boolean> update(LogisticsInfo logisticsInfo, Long currentUserId);


    /**
     * 根据订单号获取发货物流信息
     *
     * @param currentUserId 当前操作用户id
     * @param orderId       订单号
     * @return 发货物流信息
     */
    public Response<LogisticsInfo> findByOrderId(Long orderId, Long currentUserId);    /**

     * 根据订单号获取发货物流信息 运营后台调用
     *
     * @param orderId       订单号
     * @return 发货物流信息
     */
    public Response<LogisticsInfo> findByOrderId(Long orderId);

}
