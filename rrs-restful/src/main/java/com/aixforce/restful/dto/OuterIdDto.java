package com.aixforce.restful.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-1-18
 */
@ToString
public class OuterIdDto implements Serializable{

    private static final long serialVersionUID = -1893738500647474922L;

    @Getter
    @Setter
    private String shopId;

    @Getter
    @Setter
    private List<SkuAndQuantity> skus;

    @ToString
    public static class SkuAndQuantity {
        @Getter
        @Setter
        private String skuId;

        @Getter
        @Setter
        private String stock;
    }
}
