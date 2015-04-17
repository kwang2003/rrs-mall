package com.rrs.arrivegift.dao;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import com.rrs.arrivegift.model.ShopGiftConfig;

		
/**
 * Created by zf on 2014/10/15.
 */
@Repository
public class ShopGiftConfigDao extends SqlSessionDaoSupport {

	public void insertShopGift(ShopGiftConfig shopGift) {
		getSqlSession().selectOne("ShopGiftConfig.create", shopGift);
	}
	
	public ShopGiftConfig findByShopId(Long shopid) {
		return getSqlSession().selectOne("ShopGiftConfig.findByShopId", shopid);	
	}
			
	public Boolean updateShopGift(ShopGiftConfig shopGift) {	
		return getSqlSession().update("ShopGiftConfig.updateShopGift", shopGift)== 1;
	}

}
