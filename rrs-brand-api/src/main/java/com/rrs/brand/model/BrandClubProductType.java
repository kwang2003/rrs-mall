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
public class BrandClubProductType implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String productTypeName;

    @Getter
    @Setter
    private Long typeOrder;
}