package com.aixforce.item.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-4-4
 */
public class FurnitureSiteItemDto implements Serializable {

    private static final long serialVersionUID = -3464949982642937721L;

    @Getter
    @Setter
    private List<RecommendSiteItem> fcid; //热销新品榜的商品

    @Getter
    @Setter
    private List<RecommendSiteItem> ids;  //id列表里面的商品
}
