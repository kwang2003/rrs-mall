package com.aixforce.rrs.buying.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.model.BuyingItem;

import java.util.List;

/**
 * Created by songrenfei on 14-9-23
 */
public interface BuyingItemService {

    /**
     * 创建抢购活动商品关联
     * @param buyingItem 抢购活动商品关联对象
     * @return 参与活动的商品
     */
    Response<BuyingItem> create(BuyingItem buyingItem);

    /**
     * 更新 抢购活动商品关联
     * @param buyingItem 更新的抢购活动商品关联对象
     * @return 是否更新成功 true or false
     */
    Response<Boolean> update(BuyingItem buyingItem);

    /**
     * 删除抢购活动商品关联
     * @param id 抢购活动商品关联id
     * @return 是否删除成功 true or false
     */
    Response<Boolean> delete(Long id);


    /**
     * 根据抢购活动商品关联id查找该条记录
     * @param id 抢购活动商品关联id
     * @return 抢购活动商品关联
     */
    public Response<BuyingItem> findById(Long id);


    /**
     * 根据抢购活动id查找参与该活动的商品
     * @param id 抢购活动商品关联id
     * @return 抢购活动商品关联
     */
    public Response<List<BuyingItem>> findByActivityId(Long id);

    /**
     * 更新参与活动商品的虚拟销量
     * @param buyingItemList 更新的数据
     * @return 是否更新成功
     */
    public Response<Boolean> updateFakeSoldQuantity(List<BuyingItem> buyingItemList);

    /**
     * 根据活动id和商品id 查询唯一一条记录
     * @param activityId 活动id
     * @param itemId 商品id
     * @return buyingitem 记录
     */
    public Response<BuyingItem> findByActivityIdAndItemId(Long activityId,Long itemId);


    /**
     * 根据商品id 查询最新一期的活动商品
     * @param itemId
     * @return
     */
    public Response<BuyingItem> findLatestByItemId(Long itemId);
}
