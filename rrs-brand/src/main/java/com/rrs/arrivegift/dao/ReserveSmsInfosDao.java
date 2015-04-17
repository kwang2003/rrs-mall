package com.rrs.arrivegift.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.rrs.arrivegift.model.ReserveSmsInfos;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/10/15.
 */
@Repository
public class ReserveSmsInfosDao extends SqlSessionDaoSupport {

	public void create(ReserveSmsInfos reserveSmsInfos) {
		getSqlSession().selectOne("ReserveSmsInfos.create", reserveSmsInfos);
	}

	public Paging<ReserveSmsInfos> findModelReserveSmsInfo(Map<String, Object> params,Integer offset, Integer limit) {
		
		Long count = getSqlSession().selectOne("ReserveSmsInfos.countOf", params);
		count = Objects.firstNonNull(count, 0L);
		params.put("offset", offset);
		params.put("limit", limit);
		if (count > 0) {
			List<ReserveSmsInfos> ReserveSmsInfos = getSqlSession().selectList(
					"ReserveSmsInfos.findBy",params);
			return new Paging<ReserveSmsInfos>(count, ReserveSmsInfos);
		}
		return new Paging<ReserveSmsInfos>(0L,
				Collections.<ReserveSmsInfos> emptyList());
	}

	

	public Boolean updateReserveSmsInfos(Long id) {
		return getSqlSession().update("ReserveSmsInfos.updateSmsInfo", id) == 1;
	}

    public ReserveSmsInfos checkSmsInfosBy(String sendTele, BaseUser baseUser, Long shopId, Long type) {
        Map<String, Object> p = Maps.newHashMap();
        p.put("sendTele",sendTele);
        if(baseUser!=null){
            p.put("userId",baseUser.getId());
        }
        p.put("shopId",shopId);
        p.put("type",type);
        List<ReserveSmsInfos> reserveSmsInfosList = getSqlSession().selectList("ReserveSmsInfos.checkSmsInfosBy",p);
        if(reserveSmsInfosList!=null && reserveSmsInfosList.size()>0){
            return reserveSmsInfosList.get(0);
        }else{
            return null;
        }
    }
}
