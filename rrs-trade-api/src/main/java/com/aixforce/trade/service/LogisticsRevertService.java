package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.model.LogisticsRevert;

/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-22-上午10:10
 */
public interface LogisticsRevertService {
    /**
     * 记录退货物流信息
     *
     * @param logisticsRevert 退货物流信息
     * @return 生成的id
     */
    public Response<Long> create(LogisticsRevert logisticsRevert);


    /**
     * 更改退货物流的状态
     *
     * @param id            退货物流id
     * @param status        退货物流状态
     * @param currentUserId 当前操作用户id
     * @return 操作是否成功
     */
    public Response<Boolean> updateStatus(Long id, Integer status, Long currentUserId);


    /**
     * 根据子订单号获取退货物流信息
     *
     * @param orderItemId   子订单号
     * @param currentUserId 当前操作用户id
     * @return 退货物流信息
     */
    public Response<LogisticsRevert> findByOrderItemId(Long orderItemId, Long currentUserId);

}
