package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.item.dto.ItemBundle.BundleItemDetails;
import com.aixforce.item.dto.ItemBundle.BundleItems;
import com.aixforce.item.model.ItemBundle;
import com.aixforce.user.base.BaseUser;

import java.util.List;

/**
 * Created by yangzefeng on 14-4-21
 */
public interface ItemBundleService {

    Response<ItemBundle> findById(Long id);

    Response<Long> create(ItemBundle itemBundle);

    Response<Boolean> update(ItemBundle itemBundle, Long userId);

    Response<List<BundleItems>> findBySellerId(@ParamInfo("baseUser")BaseUser user);

    /**
     * 组合商品详情页
     * @param id 组合商品id
     * @return   组合商品信息，商品信息，商品detail，sku信息
     */
    Response<BundleItemDetails> findBundleItemDetails(@ParamInfo("itemBundleId") Long id);
}
