package com.aixforce.rrs.grid.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-16
 */
public interface ShopAuthorizeInfoService {

    Response<Long> create(ShopAuthorizeInfo shopAuthorizeInfo,Long sellerId, String sellerName);

    Response<Boolean> update(ShopAuthorizeInfo exist,ShopAuthorizeInfo toBeUpdated, Long sellerId, String sellerName);

    Response<Boolean> delete(ShopAuthorizeInfo exist, Long sellerId);

    Response<ShopAuthorizeInfo> findById(Long id);

    Response<List<ShopAuthorizeInfo>> findByShopId(Long shopId);

    Response<List<ShopAuthorizeInfo>> findByShopIdNoCache(Long shopId);

}
