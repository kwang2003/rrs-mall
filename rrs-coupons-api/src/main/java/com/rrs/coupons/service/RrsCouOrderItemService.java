package com.rrs.coupons.service;

import com.aixforce.common.model.Response;
import com.rrs.coupons.model.RrsCouOrderItem;

/**
 * Created by yea01 on 2014/8/25.
 */
public interface RrsCouOrderItemService {

    public Response<Boolean> saveCouOrderItem(RrsCouOrderItem rrsCouOrderItem);
}
