package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.model.BrandClubProductType;

import java.util.List;

/**
 * Created by mark on 2014/7/31
 */
public interface BrandClubProductTypeService {
    Response<List<BrandClubProductType>> findAllBy();
}
