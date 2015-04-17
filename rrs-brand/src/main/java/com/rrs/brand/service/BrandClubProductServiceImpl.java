package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.dao.BrandClubProductDao;
import com.rrs.brand.model.BrandClubProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhum01 on 2014/7/31.
 */
@Service
public class BrandClubProductServiceImpl implements BrandClubProductService{

    private final static Logger log = LoggerFactory.getLogger(BrandClubProductServiceImpl.class);

    @Autowired
    private BrandClubProductDao brandClubProductDao;

    @Override
    public Response<Boolean> updateBrandClubProduct(BrandClubProduct brandClubProduct) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubProductDao.updateBrandClubProduct(brandClubProduct);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
      //      log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> deleteBrandClubProduct(BrandClubProduct brandClubProduct) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubProductDao.deleteBrandClubProduct(brandClubProduct);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> saveBrandClubProduct(BrandClubProduct brandClubProduct) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubProductDao.saveBrandClubProduct(brandClubProduct);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public Response<List<BrandClubProduct>> findByBrandId(Integer brandId) {
        Response<List<BrandClubProduct>> result = new Response<List<BrandClubProduct>>();
        try {
            List<BrandClubProduct> results = brandClubProductDao.findByBrandId(brandId);
            result.setResult(results);
            return result;
        }catch (Exception e) {
            //log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }

    @Override
    public Response vaildateBrand(BaseUser baseUser,long productId) {
        Response result = new Response();
       int count =  brandClubProductDao.validateProduct(baseUser.getId().toString(),productId);
       if(count!=0){
        result.setError("find the error about the product");
        result.setSuccess(false);
        return result;
       }
        int count2 =brandClubProductDao.validateBrand(baseUser.getId().toString(),productId);
        if(count2==0){
            result.setError("find the error about the product");
            result.setSuccess(false);
            return result;
       }
        result.setSuccess(true);
        return result;

    }
}
