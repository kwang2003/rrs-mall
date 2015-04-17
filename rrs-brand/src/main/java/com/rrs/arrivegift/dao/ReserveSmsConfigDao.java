package com.rrs.arrivegift.dao;

import com.aixforce.common.model.Paging;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.rrs.arrivegift.model.ReserveSmsConfig;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zf on 2014/10/16.
 */
@Repository
public class ReserveSmsConfigDao extends SqlSessionDaoSupport {

	public void insertReserveSmsConfig(ReserveSmsConfig smsInfo) {
		getSqlSession().selectOne("ReserveSmsConfig.create", smsInfo);
	}

	public Paging<ReserveSmsConfig> findReserveSmsInfo(Long userId,
			Integer offset, Integer limit) {
		Long count = getSqlSession().selectOne("ReserveSmsConfig.countOf",
				userId);
		count = Objects.firstNonNull(count, 0L);
		if (count > 0) {			
			Map<String, Object> params = Maps.newHashMap();

			params.put("userId", userId);
			params.put("offset", offset);
			params.put("limit", limit);

			List<ReserveSmsConfig> reserveSmsConfig = getSqlSession()
					.selectList("ReserveSmsConfig.findBy",params);	
			return new Paging<ReserveSmsConfig>(count, reserveSmsConfig);
		}

		return new Paging<ReserveSmsConfig>(0L,
				Collections.<ReserveSmsConfig> emptyList());
	}

	public Boolean updateReserveSmsConfig(ReserveSmsConfig smsInfo) {
		return getSqlSession()
				.update("ReserveSmsConfig.updateSmsInfo", smsInfo) == 1;
	}
	
	public Boolean checkSmsOnly(Long id) {
		return getSqlSession().update("ReserveSmsConfig.checkSmsOnly", id) == 1;
	}

	public Boolean delReserveSmsConfig(Long id) {
		return getSqlSession().update("ReserveSmsConfig.delSmsInfo", id) == 1;
	}

    public ReserveSmsConfig querySmsConfigInfo(Long type, Long shopId) {
        Map<String, Object> p = Maps.newHashMap();
        p.put("type",type);
        p.put("shopId",shopId);
        List<ReserveSmsConfig> reserveSmsConfigList = getSqlSession().selectList("ReserveSmsConfig.querySmsConfigInfo",p);
        if(reserveSmsConfigList!=null && reserveSmsConfigList.size()>0){
            return reserveSmsConfigList.get(0);
        }else{
            return null;
        }
    }
}
