package com.aixforce.rrs.coupon.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.coupon.model.CouponUsage;
import com.aixforce.user.base.BaseUser;

/**
 * Created by Effet on 4/21/14.
 */
public interface CouponUsageService {
    /**
     * 用户获得一张优惠券
     * @param couponId
     * @param buyerId
     * @return
     */
    Response<CouponUsage> obtainACoupon(Long couponId, Long buyerId);

    Response<Boolean> update(CouponUsage couponUsage);

    Response<Paging<CouponUsage>> findByOrderBy(@ParamInfo("criteria") CouponUsage criteria,
                                                @ParamInfo("orderBy") String orderBy,
                                                @ParamInfo("pageNo") Integer pageNo,
                                                @ParamInfo("size") Integer size);

    Response<Paging<CouponUsage>> findByNameOrderByAmountOrEndTime(@ParamInfo("name") String name,
                                                                   @ParamInfo("orderBy") String orderBy,
                                                                   @ParamInfo("pageNo") Integer pageNo,
                                                                   @ParamInfo("size") Integer size,
                                                                   @ParamInfo("baseUser") BaseUser buyer);
}
