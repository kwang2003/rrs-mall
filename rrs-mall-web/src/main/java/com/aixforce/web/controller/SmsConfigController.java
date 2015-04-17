package com.aixforce.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aixforce.common.utils.CommonConstants;
import com.rrs.brand.model.SmsConfigDto;
import com.rrs.brand.service.SmsConfigService;

@Controller
@RequestMapping("/smsConfig")
public class SmsConfigController {

	@Autowired
	private SmsConfigService smsConfigService;

	@RequestMapping("/update")
	public String updateSmsConfig(SmsConfigDto smsConfigCndDto,
			HttpServletRequest request) {
		try {
			String userId = request.getSession()
					.getAttribute(CommonConstants.SESSION_USER_ID).toString();

			smsConfigCndDto.setUserId(userId);
			smsConfigCndDto.setUserType("1");

			smsConfigService.updateSmsConfig(smsConfigCndDto);
		} catch (Exception e) {
			
		}

		return "redirect:/user/sms-config";
	}
}
