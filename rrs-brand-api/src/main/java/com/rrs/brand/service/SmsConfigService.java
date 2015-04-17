package com.rrs.brand.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.model.SmsConfigDto;

public interface SmsConfigService {
		
	public void updateSmsConfig(SmsConfigDto smsConfigCndDto) throws Exception;

	public Response<SmsConfigDto> selectSmsConfig(
			@ParamInfo("baseUser") BaseUser baseUser);

}
