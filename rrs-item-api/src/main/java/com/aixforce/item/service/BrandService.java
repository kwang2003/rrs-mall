package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Brand;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by yangzefeng on 14-1-15
 */
public interface BrandService {

    Response<List<Brand>> findAll();

    Response<Brand> findById(Long id);

    Response<Long> create(Brand brand);

    Response<Boolean> update(Brand brand);

    Response<Paging<Brand>> paging(@ParamInfo("name") @Nullable String name,
                                       @ParamInfo("pageNo") @Nullable Integer pageNo,
                                       @ParamInfo("size") @Nullable Integer size);
}
