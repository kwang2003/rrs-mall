package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.model.BrandClubProduct;

import java.util.List;

/**
 * Created by mark on 2014/7/31.
 */
public interface BrandClubProductService {

    Response<Boolean> updateBrandClubProduct(BrandClubProduct brandClubProduct);

    Response<Boolean> deleteBrandClubProduct(BrandClubProduct brandClubProduct);

    Response<Boolean> saveBrandClubProduct(BrandClubProduct brandClubProduct);

    Response<List<BrandClubProduct>> findByBrandId(Integer brandId);

    Response vaildateBrand(BaseUser baseUser,long productId);


}
