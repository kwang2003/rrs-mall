package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by zhum01 on 2014/12/1.
 */
@ToString
@EqualsAndHashCode
public class RrsCouponsItemList implements Serializable {

    @Getter
    @Setter
    public Long id;

    @Getter
    @Setter
    public Long couponsId;

    @Getter
    @Setter
    public Long itemId;

    @Getter
    @Setter
    public Long shopId;

    @Getter
    @Setter
    public Long sellerId;

    @Getter
    @Setter
    public String couponsCode;

    @Getter
    @Setter
    public String couponsName;

    @Getter
    @Setter
    public String shopName;
    
    @Getter
    @Setter
    public String startTime;
    
    @Getter
    @Setter
    public String endTime;
    
    @Getter
    @Setter
    public int amount;
    
    @Getter
    @Setter
    public int useLimit;
    
    @Getter
    @Setter
    public int userSum;
    
    
    
    
}
