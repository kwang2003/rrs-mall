package com.rrs.arrivegift.service;

import com.aixforce.common.model.Response;
import com.rrs.arrivegift.model.ShopGiftConfig;
	
/**
 * Created by zf on 2014/10/15.
 */
public interface ShopGiftConfigService {
	
	public Response<Integer> insertShopGift(ShopGiftConfig shopGift);
	public Response<Boolean> updateShopGift(ShopGiftConfig shopGift);
	public Response<ShopGiftConfig> findShopGift(Long shopId);

}
