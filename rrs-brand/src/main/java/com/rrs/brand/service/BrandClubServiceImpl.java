package com.rrs.brand.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.dao.BrandClubDao;
import com.rrs.brand.model.Addresses;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandClubVo;
import com.rrs.brand.model.ExperinceMall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by temp on 2014/7/11.
 */
@Service
public class BrandClubServiceImpl implements BrandClubService{

    private final static Logger log = LoggerFactory.getLogger(BrandClubServiceImpl.class);

   @Autowired
   private BrandClubDao brandClubDao;

    public Response<List<BrandClubVo>> findAllBy(String brandName,Integer brandTypeId,Integer status){
        Response<List<BrandClubVo>> result = new Response<List<BrandClubVo>>();
        try {
            List<BrandClubVo> brandClubs = brandClubDao.findAllBy(brandName,brandTypeId,status);
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }

    public Response<List<BrandClubVo>> findAllByUser(BaseUser user,String brandName,Integer brandTypeId,Integer status){
        Response<List<BrandClubVo>> result = new Response<List<BrandClubVo>>();
        try {
            List<BrandClubVo> brandClubs = brandClubDao.findAllByUser(user,brandName,brandTypeId,status);
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }
//    public Response<List<BrandClub>> findAllBy() {
//        Response<List<BrandClub>> result = new Response<List<BrandClub>>();
//        try {
//            List<BrandClub> brandClubs = brandClubDao.findAllBy();
//            result.setResult(brandClubs);
//            return result;
//        }catch (Exception e) {
//            log.error("failed to find all brand, cause:", e);
//            result.setError("brand.query.fail");
//            return result;
//        }
//    }

    @Override
    public Response<List<BrandClub>> findBrandClubBy(String brandName) {
        Response<List<BrandClub>> result = new Response<List<BrandClub>>();
        try {
            List<BrandClub> brandClubs = brandClubDao.findBrandClubBy(brandName);
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateBrandClub(BrandClub brandClub) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  brandClubDao.updateBrandClub(brandClub);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public BrandClub findById(int id) {
        return brandClubDao.findById(id);
    }

    @Override
    public Response<BrandClub> queryBrandById(Long brandId) {
        Response<BrandClub> result = new Response<BrandClub>();
        try{
            result.setResult(brandClubDao.queryBrandById(brandId));
            return result;
        }catch(Exception e){
            log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public void updateBrandInfos(BrandClub brandClub) {
        brandClubDao.updateBrandInfos(brandClub);
    }

    @Override
    public Response<List<Addresses>> findProvince() {
        List<Addresses> list = brandClubDao.findProvince();
        Response<List<Addresses>> result = new Response<List<Addresses>>();
        result.setResult(list);
        return result;
    }

    @Override
    public Response<List<Addresses>> findAddress(int provinceId) {
        List<Addresses> list = brandClubDao.findAddress(provinceId);
        Response<List<Addresses>> result = new Response<List<Addresses>>();
        result.setResult(list);
        return result;

    }

    @Override
    public Response<List<ExperinceMall>> findMall(int provinceId, int cityId) {
        List<ExperinceMall> list = brandClubDao.findMall(provinceId,cityId);
        Response<List<ExperinceMall>> result = new Response<List<ExperinceMall>>();
        result.setResult(list);
        return result;
    }

    @Override
    public Response<Integer> insertMall( int mallId,long userId) {
        Response<Integer> result =new Response<Integer>();
        Long shopId = brandClubDao.findShop(userId);
        if(shopId==0){
            result.setResult(402);
           return result;
        }
        Long  experId=brandClubDao.findExpId(mallId);
        brandClubDao.insertMall(mallId,userId,shopId,experId);
        result.setResult(200);
        return result;
    }

    @Override
    public Response<Integer> findStatus(long userId) {
        Response<Integer> result = new Response<Integer>();
        int tempStatus = brandClubDao.findQcStatus(userId);
        if(tempStatus!=0){
            result.setResult(405);
            return result;
        }
        int count = brandClubDao.findCount(userId);
        if(count==0){
            result.setResult(404);
            return result;
        }

        int status = brandClubDao.findStatus(userId);
        if(status==1){
            result.setResult(201);
            return result;
        }else if (status ==2){
            result.setResult(202);
            return result;
        }else if (status ==3){
            result.setResult(203);
            return result;

        }else{
            result.setResult(204);
            return result;

        }
    }

    @Override
    public void exitMall(long userId) {
        brandClubDao.exitMall(userId);
    }

    @Override
    public Response<List<ExperinceMall>> findAllExper(@ParamInfo("shopName") String shopName, @ParamInfo("sellerName") String sellerName) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("shopName",shopName);
        map.put("sellerName",sellerName);
        List<ExperinceMall> list = brandClubDao.findAllExper(map);
        Response<List<ExperinceMall>> result=new Response<List<ExperinceMall>>();
        result.setResult(list);
        return result;
    }

    @Override
    public void insertQr(Map<Object, Object> map) {
        String resourceCode = brandClubDao.findCode(map);
        map.put("resourceCode",resourceCode);
        brandClubDao.experQr(map);
    }

    @Override
    public void insertQc(Map<Object, Object> map) {
        brandClubDao.experQc(map);
    }

    @Override
    public void updateBrandClubHttp2(BrandClub brandClub) {
        brandClubDao.updateBrandClubHttp2(brandClub);
    }

    @Override
    public Response<List<BrandClubVo>> findAllBy2(String brandName,Integer brandTypeId,Integer status){
        Response<List<BrandClubVo>> result = new Response<List<BrandClubVo>>();
        try {
            List<BrandClubVo> brandClubs = brandClubDao.findAllBy(brandName,brandTypeId,status);
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }
    }

    @Override
    public BrandClub findByUser(int userId) {
        BrandClub bc = new BrandClub();
        bc.setUserId(new Long(userId));
        return this.brandClubDao.findByUser(bc);
    }

    @Override
    public void updateShopClubKey(long brandId) {
        brandClubDao.updateRl(brandId);
    }

    @Override
    public Response<List<BrandClub>> findAll() {
        Response<List<BrandClub>> result= new Response<List<BrandClub>>();
        result.setResult(brandClubDao.findAll());
        return result;
    }

    @Override
    public Response<BrandClub> findByBrandUser(@ParamInfo("baseUser") BaseUser baseUser) {
        BrandClub brandClub = new BrandClub();
        brandClub.setUserId(baseUser.getId());
        Response<BrandClub> result = new Response<BrandClub>();
       BrandClub brand =  brandClubDao.findAllBrandUser(brandClub);
        result.setResult(brand);
        result.setSuccess(true);
        return result;
    }

    @Override
    public Response<Long> findStorePay(long userId){

        Response<Long> result = new Response<Long>();
        Long shopid =  brandClubDao.findStorepPay(userId);
        result.setResult(shopid);
        result.setSuccess(true);
        return result;
    }
}
