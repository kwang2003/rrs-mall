package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.dao.BrandClubTypeDao;
import com.rrs.brand.model.BrandClubType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by mark on 2014/7/11.
 */
@Service
public class BrandClubTypeServiceImpl implements BrandClubTypeService{

    private final static Logger log = LoggerFactory.getLogger(BrandClubTypeServiceImpl.class);

   @Autowired
   private BrandClubTypeDao brandClubTypeDao;

    @Override
    public Response<List<BrandClubType>> findAllBy() {
        Response<List<BrandClubType>> result = new Response<List<BrandClubType>>();
        try {
            List<BrandClubType> brandClubs = brandClubTypeDao.findAllBy();
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }
}
