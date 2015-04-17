package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dto.BuyingTempOrderDto;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by songrenfei on 14-9-23
 */
public interface BuyingTempOrderService {

    /**
     * 创建抢购活动模拟订单记录
     * @param buyingTempOrder 抢购活动模拟订单记录对象
     * @return 模拟订单对象
     */
    Response<BuyingTempOrder> create(BuyingTempOrder buyingTempOrder);


    /**
     * 创建抢购活动模拟订单记录
     * @param buyingTempOrder 抢购活动模拟订单记录对象
     * @return 模拟订单对象id
     */
    Response<Long> createTempOrder(BuyingTempOrder buyingTempOrder);

    /**
     * 更新 抢购活动模拟订单记录
     * @param buyingTempOrder 更新的抢购活动模拟订单记录对象
     * @return true更新成功 false更新失败
     */
    Response<Boolean> update(BuyingTempOrder buyingTempOrder);

    /**
     * 取消虚拟订单
     * @param buyingTempOrder 虚拟订单
     * @return 是否成功
     */
    Response<Boolean> cancelOrder(BuyingTempOrder buyingTempOrder);

    /**
     * 删除抢购活动模拟订单记录
     * @param id 抢购活动模拟订单记录id
     * @return 是否删除成功 true or false
     */
    Response<Boolean> delete(Long id);


    /**
     * 根据抢购活动模拟订单记录id查找该条记录
     * @param id 抢购活动模拟订单记录id
     * @return 抢购活动模拟订单记录
     */
    public Response<BuyingTempOrder> findById(Long id);


    /**
     * 分页查询虚拟订单  供外部和运营调用
     * @param itemName 商品名称
     * @param id  自增id
     * @param buyerId  用户id
     * @param shopId 店铺id
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageNo 当前页码
     * @param size  每页显示多少条
     * @return  虚拟订单
     */
    Response<Paging<BuyingTempOrder>> paging(@ParamInfo("itemName") @Nullable String itemName,
                                              @ParamInfo("id") @Nullable Long id,
                                              @ParamInfo("buyerId") @Nullable Long buyerId,
                                              @ParamInfo("shopId") @Nullable Long shopId,
                                              @ParamInfo("startDate") @Nullable String startDate,
                                              @ParamInfo("endDate") @Nullable String endDate,
                                              @ParamInfo("pageNo") @Nullable Integer pageNo,
                                              @ParamInfo("size") @Nullable Integer size);


    /**
     * 分页查询虚拟订单  供用户中心调用
     * @param itemName 商品名称
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageNo 当前页码
     * @param size  每页显示多少条
     * @return  虚拟订单dto
     */
    Response<BuyingTempOrderDto> pagings(@ParamInfo("itemName") @Nullable String itemName,
                                             @ParamInfo("status") @Nullable Integer status,
                                             @ParamInfo("startDate") @Nullable String startDate,
                                             @ParamInfo("endDate") @Nullable String endDate,
                                             @ParamInfo("pageNo") @Nullable Integer pageNo,
                                             @ParamInfo("size") @Nullable Integer size,
                                             @ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 根据订单id 查找对应的虚拟订单信息
     * @param orderId 订单id
     * @return 虚拟订单信息
     */
    Response<BuyingTempOrder> getByOrderId(Long orderId);


    /**
     * 根据订单列表获取虚拟订单信息
     *
     * @param orderIds 订单列表
     * @return  虚拟订单列表
     */
    Response<List<BuyingTempOrder>> findByOrderIds(List<Long> orderIds);

    /**
     * job 更新状态 当当前时间>下单结束时间 将该订单设置为已过期
     */
    void batchUpdateStatus();

    /**
     * 更新虚拟订单订单的code（当订单的支付渠道发生变化）
     * @param oldId
     * @param newId
     */
    public Response<Boolean> updateOrderId(Long oldId, Long newId);


    /**
     * 得到具体用户已抢购的数量
     * @param activityId 活动id
     * @param itemId 商品id
     * @param userId 用户id
     * @return 数量
     */
    public Response<Integer> getHasBuyQuantity(Long activityId, Long itemId,Long userId);

    /**
     * 根据抢购活动模拟订单记录id校验该抢购号是否有效
     * @param id 抢购活动模拟订单记录id
     * @return 是否有效
     */
    public Response<Boolean> checkBuyingOrderId(Long id);
}
