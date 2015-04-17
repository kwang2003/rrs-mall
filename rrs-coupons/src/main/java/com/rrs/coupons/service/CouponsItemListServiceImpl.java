package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.rrs.coupons.dao.CouponsItemListDao;
import com.rrs.coupons.model.RrsCouponsItemList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/12/1.
 */
@Service
public class CouponsItemListServiceImpl implements CouponsItemListService {

	@Autowired
	private CouponsItemListDao couponsItemListDao;

	@Override
	public Response<List<RrsCouponsItemList>> queryCouponsItemListBy(
			@ParamInfo("couponsId") Long couponsId,
			@ParamInfo("itemId") Long itemId, 
			@ParamInfo("shopId") Long shopId,
			@ParamInfo("couponsCode") String couponsCode) {
		HashMap<Object, Object> paramMap = new HashMap<Object, Object>();
		paramMap.put("couponsId", couponsId);
		paramMap.put("itemId", itemId);
		paramMap.put("shopId", shopId);
		paramMap.put("couponsCode", couponsCode);

		Response<List<RrsCouponsItemList>> result = new Response<List<RrsCouponsItemList>>();
		try {
			List<RrsCouponsItemList> rrsCouponsItemLists = couponsItemListDao
					.queryCouponsItemListBy(paramMap);
			result.setResult(rrsCouponsItemLists);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setError("brand.update.fail");
			return result;
		}
	}
	
	@Override
	public List<RrsCouponsItemList> findCouponsbyShopId(
			@ParamInfo("shopId") Long shopId) {

		try {
			return couponsItemListDao.queryCouponsByShopId(shopId);

		} catch (Exception e) {
			e.printStackTrace();
			// log.error("failed to update brand, cause:", e);
			return null;
		}

	}

	@Override
	public int queryUserShopCou(Map<String, Object> params) {
		try {
			return couponsItemListDao.queryUserShopCou(params);
		} catch (Exception e) {
			e.printStackTrace();
			// log.error("failed to update brand, cause:", e);
			return 0;
		}
	}

	@Override
	public int sumUserCou(Map<String, Object> params) {
		try {
			return couponsItemListDao.querySumUserCou(params);
		} catch (Exception e) {
			e.printStackTrace();
			// log.error("failed to update brand, cause:", e);
			return 0;
		}
	}
	
	
}
