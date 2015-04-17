package com.aixforce.rrs.grid.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * For BrandsSellers
 *
 * Date: 4/26/14
 * Time: 10:11
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class UnitBrand implements Serializable {

    private static final long serialVersionUID = 7038062126100832768L;

    @Setter
    @Getter
    private Long brandId;

    @Setter
    @Getter
    private String brandName;

    /**
     * for jsonMapper, parse shopAuthorizeInfos json
     * @param id    brand's id
     */
    public void setId(Long id) {
        brandId = id;
    }

    /**
     * for jsonMapper, parse shopAuthorizeInfos json
     * @param name  brand's name
     */
    public void setName(String name) {
        brandName = name;
    }

    @Override
    public String toString() {
        return "UnitBrand{" +
                "brandId=" + brandId +
                ", brandName='" + brandName + '\'' +
                '}';
    }
}
