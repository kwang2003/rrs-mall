package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dto.BuyingActivityOrderDto;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;

import javax.annotation.Nullable;

/**
 * Created by songrenfei on 14-9-23
 */
public interface BuyingOrderRecordService {

    /**
     * 创建抢购活动交易记录
     * @param buyingOrderRecord 抢购活动交易记录对象
     * @return 活动交易记录
     */
    Response<BuyingOrderRecord> create(BuyingOrderRecord buyingOrderRecord);

    /**
     * 更新 抢购活动交易记录
     * @param buyingOrderRecord 更新的抢购活动交易记录对象
     * @return 是否更新成功 true or false
     */
    Response<Boolean> update(BuyingOrderRecord buyingOrderRecord);

    /**
     * 删除抢购活动交易记录
     * @param id 抢购活动交易记录id
     * @return 是否删除成功 true or false
     */
    Response<Boolean> delete(Long id);


    /**
     * 根据抢购活动交易记录id查找该条记录
     * @param id 抢购活动交易记录id
     * @return 抢购活动交易记录
     */
    public Response<BuyingOrderRecord> findById(Long id);


    /**
     * 根据活动id查询参与该活动的所有订单
     * @param activityId 活动id
     * @param pageNo 当前页码
     * @param size  每页显示条数
     * @return 分页dto
     */
    public Response<Paging<BuyingActivityOrderDto>> getBuyingActivityOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId,
                                                                      @ParamInfo("orderId") @Nullable Long orderId,
                                                                      @ParamInfo("itemId") @Nullable Long itemId,
                                                                      @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                      @ParamInfo("size") @Nullable Integer size);

    /**
     * 更新订单记录的code（当订单的支付渠道发生变化）
     * @param oldId
     * @param newId
     */
    public Response<Boolean> updateOrderId(Long oldId, Long newId);
}
