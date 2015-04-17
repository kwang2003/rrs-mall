package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.model.BrandClubSd;

import java.util.List;

/**
 * Created by zhum01 on 2014/8/5.
 */
public interface BrandClubSlideService {

    Response<Boolean> updateBrandClubSlide(BrandClubSd brandClubSlide);

    Response<Boolean> deleteBrandClubSlide(BrandClubSd brandClubSlide);

    Response<Boolean> saveBrandClubSlide(BrandClubSd brandClubSlide);

//    Response<List<BrandClubSd>> findAllByType(Long imageType);

    Response<List<BrandClubSd>> findAllByIdAndType(int brandId, Long imageType);
}
