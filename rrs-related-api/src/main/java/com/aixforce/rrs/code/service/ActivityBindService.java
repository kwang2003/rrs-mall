package com.aixforce.rrs.code.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.trade.dto.BuyingFatOrder;
import com.aixforce.trade.dto.FatOrder;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;

import java.util.List;

/**
 * 优惠活动绑定service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-07-03 PM  <br>
 * Author: songrenfei
 */
public interface ActivityBindService {

    /**
     * 创建优惠活动绑定
     * @param activityBind 优惠活动绑定对象
     * @return
     */
    Response<ActivityBind> create(ActivityBind activityBind);

    /**
     * 创建优惠活动
     * @param ActivityId 优惠活动id
     * @param targetId 绑定ID
     * @param targetType 1 item 2spu 3品类
     * @return
     */
    Response<ActivityBind> create(Long ActivityId, Long targetId,
                              Integer targetType);

    /**
     * 更新 优惠活动绑定
     * @param activityBind 更新的优惠活动绑定对象
     * @return
     */
    Response<Boolean> update(ActivityBind activityBind);

    /**
     * 删除优惠活动绑定
     * @param id 优惠活动id
     * @return
     */
    Response<Boolean> delete(Long id);


    /**
     * 根据优惠活动绑定id查找该条记录
     * @param id 优惠活动绑定id
     * @return 优惠活动绑定
     */
    public Response<ActivityBind> findById(Long id);

    /**
     * 根据优惠活动id查找该条记录
     * @param activityId 优惠活动id
     * @return 优惠活动绑定
     */
    public Response<ActivityBind> findByActivityId(Long activityId);

    /**
     * 根据优惠活动名称查找绑定的所有绑定（sku,spu,品类）id
     * @param activityId 优惠活动
     * @param targetType 绑定类型 1item 2spu 3品类
     * @return 绑定id集合
     */
    public Response<List<Long>> findBindIdsByActivityId(Long activityId, Integer targetType);

    /**
     * 创建订单前预处理，优惠活动的计算
     * @return 每个sku可优惠的价格，每个卖家的优惠活动使用情况
     */
    public Response<DiscountAndUsage> processOrderCodeDiscount(List<? extends FatOrder> fatOrders, User buyer);

    /**
     * 创建抢购订单前预处理，优惠码活动的计算
     */
    public Response<CodeUsage> makeBuyingOrderCodeUsage(BuyingFatOrder buyingFatOrder, BaseUser buyer, Integer itemBuyingPrice);

    /**
     * 根据活动id 删除绑定
     * @param activityId 活动id
     */
    public Response<Boolean> deleteActivityBindByActivityId(Long activityId);
}
