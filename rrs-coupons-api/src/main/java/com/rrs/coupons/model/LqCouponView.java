package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by zhua02 on 2014/8/21.
 */
@ToString
@EqualsAndHashCode
public class LqCouponView implements Serializable {
    private int couponId;
    private int couponLqCount;
    private String couponName;
    private String imgsrc;


    public int getCouponId() {
        return couponId;
    }

    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public int getCouponLqCount() {
        return couponLqCount;
    }

    public void setCouponLqCount(int couponLqCount) {
        this.couponLqCount = couponLqCount;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getImgsrc() {
        return imgsrc;
    }

    public void setImgsrc(String imgsrc) {
        this.imgsrc = imgsrc;
    }
}
