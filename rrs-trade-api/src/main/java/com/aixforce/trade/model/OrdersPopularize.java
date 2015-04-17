package com.aixforce.trade.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yjgsjone@163.com on 14-9-15.
 */
@Data
public class OrdersPopularize implements Serializable {

    private java.lang.Long id;   // 自增主键

    private java.lang.Long orderId;   // 订单id

    private java.lang.Long orderSubId;  // 子订单id

    private java.lang.String promoter;  // 推广者

    private java.lang.String source;  // 数据来源

    private java.lang.String channel;  // 推广渠道

    private java.lang.String cid;  // 活动ID

    private java.lang.String wi;  // 反馈标签

    private java.lang.Integer status;

    private java.util.Date createdAt;  // 创建时间

    private java.util.Date updatedAt;  // 修改时间
}
