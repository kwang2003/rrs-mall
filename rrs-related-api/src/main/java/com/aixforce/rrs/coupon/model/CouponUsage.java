package com.aixforce.rrs.coupon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Effet on 4/21/14.
 */
@ToString
@EqualsAndHashCode
public class CouponUsage implements Serializable {

    private static final long serialVersionUID = -4987918952838341562L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long couponId;

    @Getter
    @Setter
    private String couponName;

    @Getter
    @Setter
    private Long buyerId;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Integer amount;

    @Getter
    @Setter
    private Integer unused;

    @Getter
    @Setter
    private Integer used;

    @Getter
    @Setter
    private Date endAt;

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;
}
