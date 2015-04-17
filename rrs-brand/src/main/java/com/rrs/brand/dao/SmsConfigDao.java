package com.rrs.brand.dao;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import com.rrs.brand.model.SmsConfigDto;

@Repository
public class SmsConfigDao extends SqlSessionDaoSupport {

	public SmsConfigDto select(SmsConfigDto smsConfigCndDto) {
		return getSqlSession().selectOne("SmsConfig.select", smsConfigCndDto);
	}

	public void insert(SmsConfigDto smsConfigCndDto) {
		getSqlSession().insert("SmsConfig.insert", smsConfigCndDto);
	}

	public void update(SmsConfigDto smsConfigCndDto) {
		getSqlSession().insert("SmsConfig.update", smsConfigCndDto);
	}
}
