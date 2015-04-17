package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.dao.BrandClubProductTypeDao;
import com.rrs.brand.model.BrandClubProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhum01 on 2014/7/31.
 */

@Service
public class BrandClubProductTypeServiceImpl implements BrandClubProductTypeService{

    private final static Logger log = LoggerFactory.getLogger(BrandClubProductTypeServiceImpl.class);

    @Autowired
    private BrandClubProductTypeDao brandClubProductTypeDao;


    @Override
    public Response<List<BrandClubProductType>> findAllBy() {
        Response<List<BrandClubProductType>> result = new Response<List<BrandClubProductType>>();
        try {
            List<BrandClubProductType> typeList = brandClubProductTypeDao.findAllBy();
            result.setResult(typeList);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }
}
