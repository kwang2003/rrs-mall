package com.rrs.coupons.dao;

import com.rrs.coupons.model.RrsCouponsItemList;

import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/12/1.
 */
@Repository
public class CouponsItemListDao extends SqlSessionDaoSupport {
	public List<RrsCouponsItemList> queryCouponsItemListBy(
			HashMap<Object, Object> paramMap) {

		return getSqlSession().selectList("RrsCouponsItemList.queryCouponsItemListBy", paramMap);

	}

	public List<RrsCouponsItemList> queryCouponsByShopId(Long shopId) {
			return getSqlSession().selectList("RrsCouponsItemList.queryCouponsByShopId", shopId);
		
	}

	public int queryUserShopCou(Map<String, Object> params) {

		Integer countNum = getSqlSession().selectOne(
				"RrsCouponsItemList.queryUserShopCou", params);

		return Integer.valueOf(countNum).intValue();
	}

	public int querySumUserCou(Map<String, Object> params) {

		Integer countNum = getSqlSession().selectOne(
				"RrsCouponsItemList.querySumUserCou", params);

		return Integer.valueOf(countNum).intValue();
	}

}
