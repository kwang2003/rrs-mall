package com.rrs.arrivegift.service;


import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.arrivegift.model.ReserveSmsConfig;
	
/**
 * Created by zf on 2014/10/16.
 */
public interface ReserveSmsConfigService {
		
	public Response<Integer> insertReserveSmsInfo(ReserveSmsConfig smsInfo);
	public Response<Boolean> updateReserveSmsInfo(ReserveSmsConfig smsInfo);
	public Response<Boolean> delReserveSmsInfo(Long id);	
	public Response<Paging<ReserveSmsConfig>> findReserveSmsInfo(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("pageNo") Integer pageNo,
            @ParamInfo("size") Integer size);

    public Response<ReserveSmsConfig> querySmsConfigInfo(Long type, Long shopId);
}