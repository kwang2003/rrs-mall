package com.aixforce.item.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangzefeng on 13-12-17
 */
public class DefaultItem implements Serializable {

    private static final long serialVersionUID = 3330993765261957477L;
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long spuId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer price;

    @Getter
    @Setter
    private String mainImage;

    @Getter
    @Setter
    private String image1;

    @Getter
    @Setter
    private String image2;

    @Getter
    @Setter
    private String image3;

    @Getter
    @Setter
    private String image4;

    @Getter
    @Setter
    private String jsonSkus;  //以json形式存储的BaseSku列表
}
