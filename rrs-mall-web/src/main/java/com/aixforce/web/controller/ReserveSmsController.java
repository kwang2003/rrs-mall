package com.aixforce.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.rrs.arrivegift.model.ReserveSmsConfig;
import com.rrs.arrivegift.service.ReserveSmsConfigService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * 商家预约短信后台管理
 * 
 * @author zhangv01 Created by zf on 2014/10/15.
 */
@Slf4j
@Controller
@RequestMapping("/api")
public class ReserveSmsController {

	@Autowired
	private ShopService shopService;

	@Autowired
	private ReserveSmsConfigService reserveSmsConfigService;

	@Autowired
	private MessageSources messageSources;

	@RequestMapping(value = "/seller/reserve-manage", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public Paging<ReserveSmsConfig> findSmsInfo(
			@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(value = "size", defaultValue = "20") Integer size) {

		BaseUser baseUser = UserUtil.getCurrentUser();
		Response<Paging<ReserveSmsConfig>> smsInfo = reserveSmsConfigService
				.findReserveSmsInfo(baseUser,pageNo, size);

		Paging<ReserveSmsConfig> result = smsInfo.getResult();
		if (!smsInfo.isSuccess()) {
			log.error(
					"failed to query orders for seller(id={}), error code:{}",
					baseUser.getId(), smsInfo.getError());
			throw new JsonResponseException(500, messageSources.get(smsInfo
					.getError()));
		}
		return result;
	}

	/**
	 * 是否启用短信
	 * 
	 * @param id
	 *            短信id
	 * @param enable
	 *            0 不启用 1 启用
	 * @return
	 */
	@RequestMapping(value = "/seller/updateSmsInfo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String updateSmsInfo(@RequestParam("id") Long id,
			@RequestParam("enable") int enable) {
		BaseUser currentSeller = UserUtil.getCurrentUser();
		ReserveSmsConfig reserveSms = new ReserveSmsConfig();
		reserveSms.setId(id);
		reserveSms.setEnable(enable);
		Response<Boolean> result = reserveSmsConfigService
				.updateReserveSmsInfo(reserveSms);

		if (!result.isSuccess()) {
			log.error("failed to update reserveSms info(id={}), error code:{}",
					currentSeller.getId(), result.getError());
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}

		return "ok";
	}

	/**
	 * 修改短信内容
	 * 
	 * @param id
	 *            短信id
	 * @param smsinfo
	 *            短息内容
	 * @return
	 */
	@RequestMapping(value = "/seller/editSmsInfo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String editSmsInfo(@RequestParam("id") Long id,
			@RequestParam("smsInfo") String smsinfo) {
		BaseUser currentSeller = UserUtil.getCurrentUser();
		ReserveSmsConfig reserveSms = new ReserveSmsConfig();
		reserveSms.setId(id);
		reserveSms.setEnable(1);
		reserveSms.setSmsInfo(smsinfo);
		Response<Boolean> result = reserveSmsConfigService
				.updateReserveSmsInfo(reserveSms);

		if (!result.isSuccess()) {
			log.error("failed to update reserveSms info(id={}), error code:{}",
					currentSeller.getId(), result.getError());
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}

		return "ok";
	}

	/**
	 * 删除短信 逻辑删除
	 * 
	 * @param id
	 *            短信id
	 * @return
	 */
	@RequestMapping(value = "/seller/delSmsInfo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String delSmsInfo(@RequestParam("id") Long id) {
		BaseUser currentSeller = UserUtil.getCurrentUser();

		Response<Boolean> result = reserveSmsConfigService
				.delReserveSmsInfo(id);
		if (!result.isSuccess()) {
			log.error("failed to delete reserveSms info(id={}), error code:{}",
					currentSeller.getId(), result.getError());
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}

		return "ok";
	}

	/**
	 * 添加短信
	 * 
	 * @param smsinfo
	 *            短信内容
	 * @return
	 */
	@RequestMapping(value = "/seller/addSmsInfo", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String addSmsInfo(@RequestParam("smsinfo") String smsinfo) {
		Long userId = UserUtil.getUserId();
		Response<Shop> shopInfo = shopService.findByUserId(userId);
		ReserveSmsConfig smsInfo = new ReserveSmsConfig();
		smsInfo.setUserId(userId);
		if (null != shopInfo && null != shopInfo.getResult()) {
			smsInfo.setShopId(shopInfo.getResult().getId());
		}
		smsInfo.setSmsInfo(smsinfo);
		smsInfo.setType(1);
		smsInfo.setUserType(2);
		smsInfo.setEnable(1);

		Response<Integer> result = reserveSmsConfigService
				.insertReserveSmsInfo(smsInfo);
		if (!result.isSuccess()) {
			log.error("failed to insert reserveSms info(id={}), error code:{}",
					userId, result.getError());
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}
		
		return "ok";
	}

}
