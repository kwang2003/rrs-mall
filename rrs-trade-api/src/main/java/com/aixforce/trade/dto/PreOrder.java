package com.aixforce.trade.dto;

import com.rrs.coupons.model.RrsCou;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * User: yangzefeng
 * Date: 13-12-5
 * Time: 上午10:00
 */
@ToString
public class PreOrder implements Serializable {

    private static final long serialVersionUID = 3696748779623394812L;

    @Getter
    @Setter
    private String sellerName;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private Boolean isCod;

    @Getter
    @Setter
    private Boolean isStorePay; //到店支付：0 不支持 1 支持

    @Getter
    @Setter
    private Boolean eInvoice;

    @Getter
    @Setter
    private Boolean vatInvoice;

    @Getter
    @Setter
    private Iterable<RichOrderItem> rois;

    @Getter
    @Setter
    private Boolean isEhaier;               //是否是ehaier商家

    @Getter
    @Setter
    private String systemDate; // 当前系统时间

    @Getter
    @Setter
    private Boolean isUserCoupons = false; // 是否有可使用的优惠券信息

    @Getter
    @Setter
    private Iterable<RrsCou> userCouponsList; // 是否有可使用的优惠券信息

}
