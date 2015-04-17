package com.rrs.brand.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by mark on 2014/7/30.
 */
@ToString
@EqualsAndHashCode
public class BrandClubType implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String brandTypeName;

    @Getter
    @Setter
    private Long typeOrder;
}
