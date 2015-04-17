package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-7-4
 */
public class SkuAndDiscount implements Serializable {
    private static final long serialVersionUID = -4981776708486620913L;

    @Getter
    @Setter
    private Long skuId;

    @Getter
    @Setter
    private Integer discount;
}
