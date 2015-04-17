package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.model.ShopExtra;

/**
 * Author:  songrenfei
 * Date: 2014-08-01
 */
@SuppressWarnings("unused")
public interface ShopExtraService {

    /**
     * 根据shopid查找该店铺的评分
     * @param shopId 店铺id
     * @return 店铺评分
     */
    Response<ShopExtra> findByShopId(Long shopId);

    /**
     * 根据shop更新店铺到店支付支持
     * @param extra 店铺扩展信息
     * @return
     */
    Response<Boolean> updateIsStorePayByShopId(ShopExtra extra);

    /**
     * 新增店铺扩展记录
     * @param extra 店铺扩展信息
     * @return
     */
    Response<Long> create(ShopExtra extra);
}
