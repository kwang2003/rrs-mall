package com.rrs.brand.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.rrs.brand.dao.SmsConfigDao;
import com.rrs.brand.model.SmsConfigDto;

@Service
public class SmsConfigServiceImpl implements SmsConfigService {

	@Autowired
	private SmsConfigDao smsConfigDao;

	@Override	
	public void updateSmsConfig(SmsConfigDto smsConfigCndDto) throws Exception {
		SmsConfigDto smsConfigCndDto2 = new SmsConfigDto();
		smsConfigCndDto2.setUserId(smsConfigCndDto.getUserId());
		smsConfigCndDto2.setUserType(smsConfigCndDto.getUserType());
		SmsConfigDto smsConfigRstDto = smsConfigDao.select(smsConfigCndDto2);

		if (StringUtils.isEmpty(smsConfigCndDto.getEnable())) {
			smsConfigCndDto.setEnable("1"); 
		}

		if (null == smsConfigRstDto) {
			smsConfigDao.insert(smsConfigCndDto);
		} else {
			smsConfigDao.update(smsConfigCndDto);
		}
	}

	@Override
	public Response<SmsConfigDto> selectSmsConfig(BaseUser baseUser) {
		Response<SmsConfigDto> response = new Response<SmsConfigDto>();
		try {
			SmsConfigDto smsConfigCndDto = new SmsConfigDto();
			smsConfigCndDto.setUserId(baseUser.getId().toString());
			SmsConfigDto smsConfigRstDto = smsConfigDao.select(smsConfigCndDto);

			if (null == smsConfigRstDto) {
				smsConfigRstDto = new SmsConfigDto();
				smsConfigRstDto.setEnable("1");
			} else {
				if (StringUtils.isEmpty(smsConfigRstDto.getEnable())) {
					smsConfigRstDto.setEnable("1");
				}
			}

			response.setResult(smsConfigRstDto);
		} catch (Exception e) {
			response.setError("smsConfig.select.fail");
		}	

		return response;
	}

}
