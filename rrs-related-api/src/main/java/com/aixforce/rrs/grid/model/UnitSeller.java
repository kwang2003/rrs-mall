package com.aixforce.rrs.grid.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * For BrandsSellers
 *
 * Date: 4/26/14
 * Time: 10:12
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class UnitSeller implements Serializable {
    private static final long serialVersionUID = -3784227271300984690L;

    @Setter
    @Getter
    private Long sellerId;

    @Setter
    @Getter
    private String sellerName;

    @Override
    public String toString() {
        return "UnitSeller{" +
                "sellerId=" + sellerId +
                ", sellerName='" + sellerName + '\'' +
                '}';
    }
}
