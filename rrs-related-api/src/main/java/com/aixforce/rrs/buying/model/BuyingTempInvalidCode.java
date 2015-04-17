package com.aixforce.rrs.buying.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by wangyb on 15-1-31.
 */
public class BuyingTempInvalidCode implements Serializable {
    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private Long buyingOrderId;
}
