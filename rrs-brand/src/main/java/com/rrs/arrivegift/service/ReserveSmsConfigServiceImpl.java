package com.rrs.arrivegift.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.arrivegift.dao.ReserveSmsConfigDao;
import com.rrs.arrivegift.model.ReserveSmsConfig;
import com.rrs.brand.service.BrandClubServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.google.common.base.Objects.firstNonNull;


/**
 * 到店有礼短信管理
 * Created by zf on 2014/10/16.
 *
 */
@Service
public class ReserveSmsConfigServiceImpl implements ReserveSmsConfigService {
	private final static Logger log = LoggerFactory
			.getLogger(BrandClubServiceImpl.class);

	@Autowired
	private ReserveSmsConfigDao reserveSmsConfig;

	@Override
	public Response<Integer> insertReserveSmsInfo(ReserveSmsConfig reserveSms) {
		Response<Integer> result = new Response<Integer>();
		try {
			reserveSmsConfig.insertReserveSmsConfig(reserveSms);
			result.setResult(200);
			return result;
		} catch (Exception e) {
			log.error("failed to insert reserveSms, cause:", e);
			result.setError("ReserveSmsConfigServiceImpl.insertReserveSmsInfo.fail");
			return result;
		}
	}

	@Override
	public Response<Paging<ReserveSmsConfig>> findReserveSmsInfo(BaseUser baseUser,
			Integer pageNo, Integer count) {
		
		Response<Paging<ReserveSmsConfig>> result = new Response<Paging<ReserveSmsConfig>>();
		pageNo = firstNonNull(pageNo, 1);
		count = firstNonNull(count, 20);
		pageNo = pageNo > 0 ? pageNo : 1;
		count = count > 0 ? count : 20;
		int offset = (pageNo - 1) * count;
		Long userId = baseUser.getId();
	
		try {
			result.setResult(reserveSmsConfig.findReserveSmsInfo(userId,
					offset, count));
			return result;
		} catch (Exception e) {
			log.error("failed to find ReserveSmsInfo, cause:", e);
			result.setError("ReserveSmsConfigServiceImpl.findReserveSmsInfo.fail");
			return result;
		}
	}

    @Override
    public Response<ReserveSmsConfig> querySmsConfigInfo(Long type, Long shopId) {
        Response<ReserveSmsConfig> result = new Response<ReserveSmsConfig>();
        try {
            result.setResult(reserveSmsConfig.querySmsConfigInfo(type,shopId));
            return result;
        } catch (Exception e) {
            log.error("failed to find ShopGift, cause:", e);
            result.setError("ShopGiftConfig.findShopGift.fail");
            return result;
        }
    }


    @Override
	public Response<Boolean> updateReserveSmsInfo(ReserveSmsConfig reserveSms) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			if(0==reserveSms.getEnable()){//启用
				Boolean istrue = reserveSmsConfig.updateReserveSmsConfig(reserveSms);
				istrue = reserveSmsConfig.checkSmsOnly(reserveSms.getId());
				result.setResult(istrue);
			}else{//停用
				Boolean istrue = reserveSmsConfig
						.updateReserveSmsConfig(reserveSms);
				result.setResult(istrue);
			}
			
			return result;
		} catch (Exception e) {
			log.error("failed to update ReserveSmsConfig, cause:", e);
			result.setError("ReserveSmsConfigServiceImpl.update.fail");
			return result;
		}
	}		

	@Override
	public Response<Boolean> delReserveSmsInfo(Long id) {
		Response<Boolean> result = new Response<Boolean>();
		try {
			Boolean istrue = reserveSmsConfig.delReserveSmsConfig(id);
			result.setResult(istrue);
			return result;
		} catch (Exception e) {
			log.error("failed to delete reserveSmsConfig, cause:", e);
			result.setError("ReserveSmsConfigServiceImpl.delReserveSmsInfo.fail");
			return result;
		}
	}

}
