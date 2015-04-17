package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Date;


/**
 * Created by yea01 on 2014/8/20.
 */
@ToString
@EqualsAndHashCode
public class RrsShowCouponView implements Serializable {
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private String cpName;
    @Getter
    @Setter
    private Date startTime;
    @Getter
    @Setter
    private Date endTime;
    @Getter
    @Setter
    private long area;
    @Getter
    @Setter
    private int amount;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    private String areaName;
    @Getter
    @Setter
    private String categoryName;
    @Getter
    @Setter
    private int term;
    @Getter
    @Setter
    private String channel;
    @Getter
    @Setter
    private String shopName;
    @Getter
    @Setter
    private int indate;//优惠券是否即将过期
    @Getter
    @Setter
    private int couponsType; //1平台优惠券 2 商家优惠券

}
