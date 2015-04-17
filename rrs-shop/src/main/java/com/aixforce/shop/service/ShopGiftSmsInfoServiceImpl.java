package com.aixforce.shop.service;


import static com.google.common.base.Objects.firstNonNull;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.shop.dao.ShopDao;
import com.aixforce.shop.model.Shop;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.rrs.arrivegift.model.ReserveSmsInfos;
import com.rrs.arrivegift.service.ShopGiftSmsInfoDaoService;

/**
 * Author: zf
 * Date: 2014-10-30
 */
@Service
@Slf4j
public class ShopGiftSmsInfoServiceImpl implements ShopGiftSmsInfoService {
	
	
	@Autowired			
	private ShopGiftSmsInfoDaoService shopGiftSmsInfoDaoService;
	
	@Autowired
	private ShopDao shopDao;

	@Override
	public Response<Paging<ReserveSmsInfos>> findUserReserveSmsInfo(
			BaseUser baseUser, Integer pageNo, Integer count,
			Map<String, String> params) {

		Response<Paging<ReserveSmsInfos>> result = new Response<Paging<ReserveSmsInfos>>();
		pageNo = firstNonNull(pageNo, 1);
		count = firstNonNull(count, 20);
		pageNo = pageNo > 0 ? pageNo : 1;
		count = count > 0 ? count : 20;
		int offset = (pageNo - 1) * count;

		Map<String, Object> builder = Maps.newHashMap();
		String userName = params.get("userName");
		if (!Strings.isNullOrEmpty(userName)) {
			builder.put("userName", userName.trim());
		}

		String shopName = params.get("shopName");
		if (!Strings.isNullOrEmpty(shopName)) {
			builder.put("shopName", shopName.trim());
		}

		String startTime = params.get("startTime");
		if (!Strings.isNullOrEmpty(startTime)) {
			builder.put("startTime", startTime.trim());
		}

		String endTime = params.get("endTime");
		if (!Strings.isNullOrEmpty(endTime)) {
			endTime =endTime.trim() +" 23:59:59";
			builder.put("endTime", endTime);
		}		

		String reserveStart = params.get("reserveStart");
		if (!Strings.isNullOrEmpty(reserveStart)) {
			builder.put("reserveStart", reserveStart.trim());
		}

		String reserveEnd = params.get("reserveEnd");
		if (!Strings.isNullOrEmpty(reserveEnd)) {
			reserveEnd =reserveEnd.trim() +" 23:59:59";
			builder.put("reserveEnd", reserveEnd);
		}

		String state = params.get("state");
		if (!Strings.isNullOrEmpty(state)) {
			builder.put("state", Integer.parseInt(state));
		}

		String sourceStype = params.get("sourceStype");
		if (!Strings.isNullOrEmpty(sourceStype)) {
			sourceStype = sourceStype.trim();
		}

		if (2 == baseUser.getType()) {// 卖家
			try {
				Shop shop = shopDao.findByUserId(baseUser.getId());
				if ("1".equals(sourceStype)) {// 商家短信预约到店管理
					builder.put("shopId", shop.getId());
					builder.put("type", 1);
				} else {
					builder.put("userId", baseUser.getId());
				}
			} catch (NullPointerException e) {
				log.error("failed to find shopid, cause:", e);
				result.setError("ReserveSmsInfosServiceImpl.shopDao.findByUserId.fail");
				return result;
			}
		} else {
			builder.put("userId", baseUser.getId());
		}

		try {
			result.setResult(shopGiftSmsInfoDaoService.findModelReserveSmsInfo(
					builder, offset, count));		
			return result;
		} catch (Exception e) {
			log.error("failed to find ReserveSmsInfo, cause:", e);
			result.setError("ReserveSmsInfosServiceImpl.findReserveSmsInfo.fail");
			return result;
		}
	}


}
