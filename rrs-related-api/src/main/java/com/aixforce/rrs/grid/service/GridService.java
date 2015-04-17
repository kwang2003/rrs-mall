package com.aixforce.rrs.grid.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.trade.dto.FatOrder;

import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 14-1-18
 */
public interface GridService {

    /**
     * 验证授权信息
     * @param item 待发布商品
     * @param shopId 店铺id
     * @return  如果验证不通过，response为false，如果验证通过，response为true，data为brandId+regionIds
     */
    Response<List<Long>> authorize(Item item, Long shopId);

    Response<List<Long>> authorizeByInfos(Item item, List<ShopAuthorizeInfo> authorizeInfos);

    /**
     * 发商品前网格判断，内部和上面2个方法调用同一个方法
     */
    Response<List<Long>> authorizeBySpuIdAndShopId(Long spuId, Long shopId);

    /**
     * 从cookie里面获取regionId的值,如果未找到，返回error
     * @param cookies 请求cookie
     * @return  regionId
     */
    Response<Integer> findRegionFromCookie(Map<String, String> cookies);

    /**
     * 从cookie中获取当前区域信息的省份信息
     * @param cookies 请求cookie
     * @return Integer
     * 返回当前区域省份编号
     */
    Response<Integer> findProvinceFromCookie(Map<String , String> cookies);

    /**
     * 如果商品在当前区域不能购买，返回错误，不能创建订单
     * @param fatOrders fatOrders
     * @param regionId  区域id，从cookie中获取
     * @return   筛选过的fatOrders
     */
    Response<Boolean> verifyRegionWhenCreateOrder(List<? extends FatOrder> fatOrders, Integer regionId);

    /**
     * 如果商品在当前区域不能购买，返回错误，不能创建抢购虚拟订单
     * @param itemId 商品id
     * @param regionId  区域id，从cookie中获取
     * @return   是否可以买
     */
    Response<Boolean> verifyRegionWhenCreateTempOrder(Long itemId, Integer regionId);
}
