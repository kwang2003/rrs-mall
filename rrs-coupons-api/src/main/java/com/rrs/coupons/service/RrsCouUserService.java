package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.coupons.model.RrsCouUserView;

import java.util.List;

/**
 * Created by yea01 on 2014/8/22.
 */
public interface RrsCouUserService {
    /**
     * 获取当前用户的优惠劵信息
     * **/
    Response<List<RrsCouUserView>> queryCouponsAllByUser(@ParamInfo("baseUser") BaseUser baseUse,@ParamInfo("skus") String skus,@ParamInfo("status") Long status);
}
