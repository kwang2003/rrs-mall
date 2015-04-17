package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.trade.model.DeliveryMethod;

import java.util.List;

/**
 * Created by yangzefeng on 14-9-3
 */
public interface DeliveryMethodService {

    /**
     * 创建配送方式
     * @param deliveryMethod deliveryMethod对象
     * @return 创建对象id
     */
    public Response<Long> create(DeliveryMethod deliveryMethod);

    /**
     * 更新配送方式
     * @param deliveryMethod 待更新配送方式
     * @return 是否成功
     */
    public Response<Boolean> update(DeliveryMethod deliveryMethod);

    /**
     * 更新配送信息方式
     * @param id 配送方式id
     * @param status 配送方式待更新状态（1启用，-1停用，-2删除）
     * @return 是否成功
     */
    public Response<Boolean> updateStatus(Long id, Integer status);

    /**
     * 根据id找配送方式
     * @param id 配送方式id
     * @return 配送方式
     */
    public Response<DeliveryMethod> findById(Long id);

    /**
     * 根据状态筛选配送方式,type 默认为1, 已删除（状态为-2）的不返回
     * @param status 配送方式的状态
     * @param type   配送时间or配送承诺
     * @return 配送方式的列表
     */
    public Response<List<DeliveryMethod>> findBy(@ParamInfo("status") Integer status,
                                                 @ParamInfo("type") Integer type);
}
