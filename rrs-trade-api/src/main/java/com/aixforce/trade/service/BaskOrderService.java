package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.model.BaskOrder;

import javax.annotation.Nullable;

/**
 * Created by songrenfei on 14-9-16.
 */
public interface BaskOrderService {

    /**
     * 创建晒单
     * @param baskOrder
     * @return
     */
    Response<Long> create(BaskOrder baskOrder);

    /**
     * 创建晒单 也评论关联
     * @param orderItemId 子订单
     * @param baskOrder 晒单
     * @param userId 评论者id
     * @return
     */
    Response<Long> create(Long orderItemId,BaskOrder baskOrder,Long userId);


    /**
     * 更新 晒单
     * @param baskOrder
     * @return
     */
    Response<Boolean> update(BaskOrder baskOrder);

    /**
     * 删除晒单
     * @param id 晒单id
     * @return
     */
    Response<Boolean> delete(Long id);

    /**
     * 根据晒单id查找该条记
     * @param id 优惠码使用情况id
     * @return
     */
    public Response<BaskOrder> findById(Long id);

    /**
     * 根据itemId 查询商品的所有晒单信息
     * @param itemId 商品id
     * @param pageNo 当前页码
     * @param size   每页显示条数
     * @return
     */
    Response<Paging<BaskOrder>> paging(@ParamInfo("itemId") @Nullable Long itemId,
                                   @ParamInfo("pageNo") @Nullable Integer pageNo,
                                   @ParamInfo("size") @Nullable Integer size);
    
    
}
