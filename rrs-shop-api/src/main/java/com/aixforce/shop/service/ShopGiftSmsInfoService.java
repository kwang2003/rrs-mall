package com.aixforce.shop.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;

import com.rrs.arrivegift.model.ReserveSmsInfos;
import java.util.Map;

/**
 * Author: zf 
 * Date: 2014-10-30
 */
public interface ShopGiftSmsInfoService {

	/**
	 * 到点有礼商家查询预约信息
	 * 
	 * @param baseUser
	 * @param pageNo
	 * @param size
	 * @return
	 */
	public Response<Paging<ReserveSmsInfos>> findUserReserveSmsInfo(@ParamInfo("baseUser") BaseUser baseUser, @ParamInfo("pageNo") Integer pageNo,
            @ParamInfo("size") Integer size, @ParamInfo("params") Map<String, String> params);

}
