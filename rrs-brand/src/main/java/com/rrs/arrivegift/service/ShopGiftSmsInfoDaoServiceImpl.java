package com.rrs.arrivegift.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aixforce.common.model.Paging;
import com.rrs.arrivegift.dao.ReserveSmsInfosDao;
import com.rrs.arrivegift.model.ReserveSmsInfos;

/**
 * 到店有礼短信管理 Created by zf on 2014/10/16.
 *
 */
@Service
public class ShopGiftSmsInfoDaoServiceImpl implements ShopGiftSmsInfoDaoService {
	@Autowired
	private ReserveSmsInfosDao reserveSmsInfosDao;

	@Override
	public Paging<ReserveSmsInfos> findModelReserveSmsInfo(
			Map<String, Object> params, Integer offset, Integer limit) {

		return reserveSmsInfosDao
				.findModelReserveSmsInfo(params, offset, limit);
	}

}
