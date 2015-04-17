package com.rrs.brand.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by zhum01 on 2014/8/5.
 */
public class BrandClubSd implements Serializable {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String httpUrl;

    @Getter
    @Setter
    private String mainImage;

    @Getter
    @Setter
    private Long imageType;

    @Getter
    @Setter
    private Long brandId;
}
