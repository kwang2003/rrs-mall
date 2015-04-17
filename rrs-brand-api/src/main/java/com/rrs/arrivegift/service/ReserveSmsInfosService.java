package com.rrs.arrivegift.service;


import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.arrivegift.model.ReserveSmsInfos;

/**
 * Created by zhum01 on 2014/10/15.
 */
public interface ReserveSmsInfosService {
	public void create(ReserveSmsInfos reserveSmsInfos);
	
	public Response<Boolean> updateReserveSmsInfos(Long id);

    public Response<ReserveSmsInfos> checkSmsInfosBy(String sendTele, BaseUser baseUser, Long shopId, Long type);
}
