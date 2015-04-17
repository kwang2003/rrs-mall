package com.aixforce.rrs.grid.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.grid.dto.BrandSellersDto;
import com.aixforce.rrs.grid.dto.SellerBrandsDto;
import com.aixforce.rrs.grid.model.UnitBrand;
import com.aixforce.rrs.grid.model.UnitSeller;

import java.util.List;

/**
 * Date: 4/26/14
 * Time: 13:33
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public interface BrandsSellersService {


    /**
     * 根据卖家的id找卖家的品牌
     *
     * @param sellerId  卖家id
     * @return          卖家的品牌列表 dto
     */
    public Response<SellerBrandsDto> findBrandsBySeller(Long sellerId);

    /**
     * 根据品牌的id找品牌的卖家
     *
     * @param brandId   品牌的id
     * @return          品牌的卖家列表 dto
     */
    public Response<BrandSellersDto> findSellersByBrand(Long brandId);

    /**
     * 根据品牌数组找到卖家
     *
     * @param brands    品牌的数组
     * @return          卖家dto 列表
     */
    public Response<List<UnitSeller>> findSellersByBrands(List<UnitBrand> brands);
}
