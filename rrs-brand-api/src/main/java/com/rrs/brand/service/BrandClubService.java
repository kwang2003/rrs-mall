package com.rrs.brand.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.model.Addresses;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandClubVo;
import com.rrs.brand.model.ExperinceMall;

import java.util.List;
import java.util.Map;

/**
 * Created by temp on 2014/7/11.
 */
public interface BrandClubService {
    Response<List<BrandClubVo>> findAllBy(@ParamInfo("brandName") String brandName,
                                          @ParamInfo("brandTypeId") Integer brandTypeId,
                                          @ParamInfo("status") Integer status
    );

    Response<List<BrandClubVo>> findAllByUser(@ParamInfo("baseUser") BaseUser baseUse,@ParamInfo("brandName") String brandName,
                                          @ParamInfo("brandTypeId") Integer brandTypeId,
                                          @ParamInfo("status") Integer status
    );

    public Response<List<BrandClub>> findBrandClubBy(@ParamInfo("brandName") String brandName);


    Response<Boolean> updateBrandClub(BrandClub brandClub);

    BrandClub findById(int id);

    void updateBrandClubHttp2(BrandClub brandClub);


    Response<List<BrandClubVo>> findAllBy2(@ParamInfo("brandName") String brandName,
                                          @ParamInfo("brandTypeId") Integer brandTypeId,
                                          @ParamInfo("status") Integer status
    );

    BrandClub findByUser(int userId);

    void updateShopClubKey(long brandId);

    Response<List<BrandClub>> findAll();
    Response<BrandClub> findByBrandUser(@ParamInfo("baseUser") BaseUser baseUser);
    Response<BrandClub> queryBrandById(Long brandId);
    void updateBrandInfos(BrandClub brandClub);
    Response<List<Addresses>> findProvince();
    Response<List<Addresses>> findAddress(int provinceId);
    Response<List<ExperinceMall>>findMall(int provinceId,int cityId);
    Response<Integer>insertMall(int mallId,long userId);
    Response<Integer>findStatus(long userId);
    void exitMall(long userId);
    Response<List<ExperinceMall>> findAllExper(@ParamInfo("shopName") String shopName,@ParamInfo("sellerName") String sellerName);
    void insertQr(Map<Object,Object> map);
    void insertQc(Map<Object,Object>map);

    /**
     * 查找到店支付
     * @param userId
     * @return
     */
    Response<Long> findStorePay(long userId);
}
