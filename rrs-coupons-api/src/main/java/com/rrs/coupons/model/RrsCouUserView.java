package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/8/22.
 */
@ToString
@EqualsAndHashCode
public class RrsCouUserView implements Serializable {
    @Getter
    @Setter
    private  Long id;
    @Getter
    @Setter
    private  Long userId;
    @Getter
    @Setter
    private  Long couponId;
    @Getter
    @Setter
    private  String cpName;
    @Getter
    @Setter
    private  Long status;

    @Getter
    @Setter
    private Long amount;

    @Getter
    @Setter
    private  String categoryId;

    @Getter
    @Setter
    private Long term;
}
