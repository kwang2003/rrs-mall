package com.rrs.brand.service;

import com.aixforce.common.model.Response;
import com.rrs.brand.dao.BrandClubSlideDao;
import com.rrs.brand.model.BrandClubSd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhum01 on 2014/8/5.
 */
@Service
public class BrandClubSlideServiceImpl implements BrandClubSlideService  {
    private final static Logger log = LoggerFactory.getLogger(BrandClubSlideServiceImpl.class);

    @Autowired
    private BrandClubSlideDao brandClubSlideDao;

    @Override
    public Response<Boolean> updateBrandClubSlide(BrandClubSd brandClubSlide) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubSlideDao.updateBrandClubSlide(brandClubSlide);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //      log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    public Response<Boolean> deleteBrandClubSlide(BrandClubSd brandClubSlide) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubSlideDao.deleteBrandClubSlide(brandClubSlide);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    public Response<Boolean> saveBrandClubSlide(BrandClubSd brandClubSlide) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubSlideDao.saveBrandClubSlide(brandClubSlide);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

//    public Response<List<BrandClubSd>> findAllByType(Long imageType) {
//        Response<List<BrandClubSd>> result = new Response<List<BrandClubSd>>();
//        try {
//            List<BrandClubSd> results = brandClubSlideDao.findAllByType(imageType);
//            result.setResult(results);
//            return result;
//        }catch (Exception e) {
//            //log.error("failed to find all brand, cause:", e);
//            result.setError("brand.query.fail");
//            return result;
//        }
//    }

    @Override
    public Response<List<BrandClubSd>> findAllByIdAndType(int brandId, Long imageType) {
        Response<List<BrandClubSd>> result = new Response<List<BrandClubSd>>();
        try {
            List<BrandClubSd> results = brandClubSlideDao.findAllByIdAndType(brandId,imageType);
            result.setResult(results);
            return result;
        }catch (Exception e) {
            //log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }
}
