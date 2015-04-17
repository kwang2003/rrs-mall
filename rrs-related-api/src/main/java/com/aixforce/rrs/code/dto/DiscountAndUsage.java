package com.aixforce.rrs.code.dto;

import com.aixforce.rrs.code.model.CodeUsage;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by yangzefeng on 14-7-5
 */
@Data
public class DiscountAndUsage implements Serializable {
    private static final long serialVersionUID = -7589332314810332476L;

    private Map<Long,Integer> skuIdAndDiscount;

    private Map<Long,CodeUsage> sellerIdAndUsage;

    private Map<Long,Integer> activityCodeIdAndUsage;
}
