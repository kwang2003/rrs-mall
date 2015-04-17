package com.aixforce.open.dto;

import com.rrs.coupons.model.RrsShowCouponView;
import lombok.Data;

/**
 * Created by neusoft on 14-9-9.
 */
@Data
public class RichCoupons extends RrsShowCouponView{

    private  Long userId;

    private  Long couponId;

    private  String cpName;

    private  String categoryId;

    private int term;
}
