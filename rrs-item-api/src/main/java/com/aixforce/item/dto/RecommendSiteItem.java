package com.aixforce.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-1-17
 */
public class RecommendSiteItem implements Serializable {
    private static final long serialVersionUID = 497464688981475877L;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private Long spuId;

    @Getter
    @Setter
    private String mainImage;

    @Getter
    @Setter
    private Integer price;

    @Getter
    @Setter
    private String name;
}
