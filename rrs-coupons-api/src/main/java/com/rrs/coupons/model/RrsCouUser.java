package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/8/19.
 */
@ToString
@EqualsAndHashCode
public class RrsCouUser implements Serializable {
    @Getter
    @Setter
    private  long id;
    @Getter
    @Setter
    private  long userId;
    @Getter
    @Setter
    private  long couponId;
    @Getter
    @Setter
    private  String status;


}
