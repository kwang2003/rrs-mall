package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.model.BrandClubType;

import java.util.List;

/**
 * Created by mark on 2014/7/11.
 */
public interface BrandClubTypeService {
    Response<List<BrandClubType>> findAllBy();
    
}
