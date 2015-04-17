package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.rrs.coupons.model.RrsCouponsItemList;

import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/12/1.
 */
public interface CouponsItemListService {
    Response<List<RrsCouponsItemList>> queryCouponsItemListBy(@ParamInfo("couponsId") Long couponsId, @ParamInfo("itemId") Long itemId, @ParamInfo("shopId") Long shopId, @ParamInfo("couponsCode") String couponsCode);
    
    List<RrsCouponsItemList> findCouponsbyShopId(@ParamInfo("shopId") Long shopId);
    
    int queryUserShopCou(Map<String, Object> params);	
    
    int sumUserCou(Map<String, Object> params);

}
