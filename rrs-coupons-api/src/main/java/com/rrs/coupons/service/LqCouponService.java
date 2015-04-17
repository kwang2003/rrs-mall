package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.coupons.model.LqCouponView;
import com.rrs.coupons.model.LqMessage;

import java.util.List;

/**
 * Created by zhua02 on 2014/8/21.
 */
public interface LqCouponService {
    Response<List<LqCouponView>> findCouponAll();
    LqMessage LqCoupon(BaseUser baseUser,int couponId);
}
