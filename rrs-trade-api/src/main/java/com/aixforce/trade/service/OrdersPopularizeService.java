package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.OrdersPopularize;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yjgsjone@163.com on 14-9-15.
 */
public interface OrdersPopularizeService {

    /**
     * 向推广订单表中写入数据
     * @param map  数据对象
     * @return 影响记录的条数
     */
    public Response<Integer> create(Map map);

    /**
     * 向亿起发推送订单信息
     * @param mapCookie Cookie中的信息
     * @param orderItem 子订单信息
     * @return 推送结果true或则false
     */
    public Response<Boolean> sendYqf(Map mapCookie, OrderItem orderItem, Order order);

    /**
     * 向亿起发推送订单状态信息
     * @param mapCookie Cookie中的信息
     * @param orderItem 子订单信息
     * @return 推送结果true或则false
     */
    public Response<Boolean> sendYqfOrderStatus(Map mapCookie, OrderItem orderItem, Order order);

    /**
     * 根据下单时间查询某时间段内创建的订单数据
     * @param cid 活动ID
     * @param orderStartTime 订单生成开始时间
     * @param orderEndTime 订单生成结束时间
     * @return 订单数据
     */
    public Response<List<OrdersPopularize>> findOrdersByCreatedAt(String cid, Date orderStartTime, Date orderEndTime);

    /**
     * 根据更新时间查询某时间段内更新过的订单状态、支付状态、支付方式等等信息
     * @param cid 活动ID
     * @param updateStartTime 订单更新开始时间
     * @param updateEndTime 订单更新结束时间
     * @return 订单数据
     */
    public Response<List<OrdersPopularize>> findOrdersByUpdatedAt(String cid, Date updateStartTime, Date updateEndTime);
}
