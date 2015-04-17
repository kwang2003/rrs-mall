package com.rrs.arrivegift.service;

import java.util.Map;

import com.aixforce.common.model.Paging;
import com.rrs.arrivegift.model.ReserveSmsInfos;
;

/**
 * Created by zf on 2014/10/15.
 */
public interface ShopGiftSmsInfoDaoService {

	public Paging<ReserveSmsInfos> findModelReserveSmsInfo(Map<String, Object> params,Integer offset, Integer limit);

}
