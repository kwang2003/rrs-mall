package com.rrs.brand.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by mark on 2014/7/31
 */

@ToString
@EqualsAndHashCode
public class BrandClubProduct implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String productName;

    @Getter
    @Setter
    private String productImage;

    @Getter
    @Setter
    private Long price;

    @Getter
    @Setter
    private Long oriPrice;

    @Getter
    @Setter
    private Long brandClupId;

    @Getter
    @Setter
    private Long productType;

    @Getter
    @Setter
    private String productUrl;

    @Getter
    @Setter
    private Long productId;
    @Getter
    @Setter
    private String brandTypeName;
}