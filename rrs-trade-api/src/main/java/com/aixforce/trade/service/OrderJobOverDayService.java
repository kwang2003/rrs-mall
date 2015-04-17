package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.model.OrderJobOverDay;

import java.util.Date;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-10
 */
public interface OrderJobOverDayService {

    /**
     * 根据id列表查找订单列表
     * @param orderIds 订单列表
     * @return  订单列表
     */
    public Response<Paging<OrderJobOverDay>> findByOrderIds(List<Long> orderIds);

    /**
     * 根据id列表查找订单列表
     * @param orderJobOverDay 检索对象
     * @return  订单列表
     */
    public Response<Paging<OrderJobOverDay>> findBy(OrderJobOverDay orderJobOverDay);

    /**
     * 更新Order状态
     */
    Response<Boolean> updateStatusByOrderIds(List<Long> orderIds);

    /**
     * 创建订单Job状态表
     */
    Response<Boolean> create(OrderJobOverDay orderJobOverDay);

    /**
     * 更新订单Job状态表
     */
    Response<Boolean> update(OrderJobOverDay orderJobOverDay);

}
