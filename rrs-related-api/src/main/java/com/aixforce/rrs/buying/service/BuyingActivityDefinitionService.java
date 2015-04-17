package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dto.BuyingActivityDto;
import com.aixforce.rrs.buying.dto.BuyingPreOrder;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.trade.dto.BuyingFatOrder;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by songrenfei on 14-9-23
 */
public interface BuyingActivityDefinitionService {

    /**
     * 创建抢购活动
     * @param buyingActivityDefinition 抢购活动对象
     * @return id
     */
    Response<Long> create(BuyingActivityDefinition buyingActivityDefinition);

    /**
     * 创建抢购活动
     * @param buyingActivityDto 抢购活动和活动商品关联对象
     * @return id
     */
    Response<Long> create(BuyingActivityDto buyingActivityDto,Long userId);

    /**
     * 更新 抢购活动
     * @param buyingActivityDefinition 更新的抢购活动对象
     * @return 是否更新成功
     */
    Response<Boolean> update(BuyingActivityDefinition buyingActivityDefinition);

    /**
     * 创建抢购活动
     * @param buyingActivityDto 抢购活动和活动商品关联对象
     * @return 是否更新成功
     */
    Response<Boolean> update(BuyingActivityDto buyingActivityDto,Long userId);

    /**
     * 删除抢购活动
     * @param id 抢购活动id
     * @return 是否删除成功
     */
    Response<Boolean> delete(Long id);


    /**
     * 根据抢购活动id查找该条记录
     * @param id 抢购活动id
     * @return 抢购活动
     */
    Response<BuyingActivityDefinition> findById(Long id);

    /**
     * 分页查询抢购活动定义
     * @param name 活动标题
     * @param sellerId 卖家id
     * @param businessId 频道
     * @param strDate 开始时间
     * @param endDate 结束时间
     * @param status 状态
     * @param pageNo 当前页码
     * @param size  每页显示多少条
     * @return  活动集合
     */
    Response<Paging<BuyingActivityDefinition>> paging(@ParamInfo("name") @Nullable String name,
                                   @ParamInfo("sellerId") @Nullable Long sellerId,
                                   @ParamInfo("businessId") @Nullable Long businessId,
                                   @ParamInfo("strDate") @Nullable String strDate,
                                   @ParamInfo("endDate") @Nullable String endDate,
                                   @ParamInfo("status") @Nullable String status,
                                   @ParamInfo("pageNo") @Nullable Integer pageNo,
                                   @ParamInfo("size") @Nullable Integer size);


    /**
     * 分页查询抢购活动定义
     * @param name 活动标题
     * @param sellerId 卖家id
     * @param businessId 频道
     * @param strDate 开始时间
     * @param endDate 结束时间
     * @param status 状态
     * @param pageNo 当前页码
     * @param size  每页显示多少条
     * @param user  当前用户
     * @return  活动集合
     */
    Response<Paging<BuyingActivityDefinition>> pagingBySeller(@ParamInfo("name") @Nullable String name,
                                                      @ParamInfo("sellerId") @Nullable Long sellerId,
                                                      @ParamInfo("businessId") @Nullable Long businessId,
                                                      @ParamInfo("strDate") @Nullable String strDate,
                                                      @ParamInfo("endDate") @Nullable String endDate,
                                                      @ParamInfo("status") @Nullable String status,
                                                      @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                      @ParamInfo("size") @Nullable Integer size,
                                                      @ParamInfo("baseUser") BaseUser user);



    /**
     * 根据活动id 查询活动信息和参与活动的商品信息
     * @param id 活动id
     * @return dto对象
     */
    Response<BuyingActivityDto> fingBuyingActivityDtoById(@ParamInfo("id") @Nullable String id,
                                                          @ParamInfo("preview") @Nullable Boolean preview,
                                                          @ParamInfo("baseUser") BaseUser baseUser);

    /**
     * job 更新活动状态
     */
    void batchUpdateStatus();

    /**
     * 查找抢购活动的全部信息,包括商品,图片,sku,属性等
     *
     * @param itemId 商品id
     * @return 商品的全部信息和模版商品信息和活动信息
     */
    Response<Map<String, Object>> findDetails(@ParamInfo("itemId") Long itemId,@ParamInfo("activityId") Long activityId);


    /**
     * 抢购订单预览页
     * @param skuId skuId
     * @param quantity sku购买数量
     * @param buyingTempOrderId 虚拟订单id
     * @return 预览详情
     */
    Response<BuyingPreOrder> buyingPreOrder(@ParamInfo("skuId") Long skuId,
                                            @ParamInfo("quantity") Integer quantity,
                                            @ParamInfo("buyingTempOrderId") Long buyingTempOrderId);

    /**
     * 创建抢购真实订单
     * @param buyer 买家
     * @param tradeInfoId 收货信息id
     * @param bank 银行编号
     * @param buyingFatOrder 预订单
     * @return 创建的订单id
     */
    Response<Long> createBuyingOrder(BaseUser buyer, Long tradeInfoId, String bank, BuyingFatOrder buyingFatOrder);
}
