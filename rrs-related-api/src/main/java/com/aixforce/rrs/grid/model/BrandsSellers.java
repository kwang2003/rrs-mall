package com.aixforce.rrs.grid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Date: 4/26/14
 * Time: 10:12
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@AllArgsConstructor
@NoArgsConstructor
public class BrandsSellers implements Serializable {

    private static final long serialVersionUID = 6588576107029886072L;

    @Setter
    @Getter
    private Long brandId;

    @Setter
    @Getter
    private String brandName;

    @Setter
    @Getter
    private Long sellerId;

    @Setter
    @Getter
    private String sellerName;

    @Getter
    @Setter
    private Long shopId;

    @Override
    public String toString() {
        return "BrandsSellers{" +
                "sellerId=" + sellerId +
                ", sellerName='" + sellerName + '\'' +
                ", brandId=" + brandId +
                ", brandName='" + brandName + '\'' +
                '}';
    }
}
