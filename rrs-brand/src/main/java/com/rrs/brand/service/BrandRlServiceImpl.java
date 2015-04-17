package com.rrs.brand.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.brand.dao.BrandClubDao;
import com.rrs.brand.dao.BrandClubKeyDao;
import com.rrs.brand.dao.BrandRlViewDao;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandRlView;
import com.rrs.brand.model.BrandWRlView;
import com.rrs.brand.model.BrandsClubKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/29.
 */
@Service
public class BrandRlServiceImpl implements BrandRlService {

    @Autowired
    private BrandRlViewDao brdao;

    @Autowired
    private BrandClubDao bcldao;

    @Autowired
    private BrandClubKeyDao bcdao;

    @Override
    public List<BrandRlView> findByPro(String shopname, int shopid, int businessid, int status, String starttime, String endtime) {
        return null;
    }

    @Override
    public List<BrandRlView> findByPro(String shopname, int shopid, int businessid, int status) {
        return null;
    }

    @Override
    public Response<List<BrandRlView>> findRlzj(BaseUser baseUser,String shopname) {
        BrandClub brandClub=bcldao.findBrandClubByUid(baseUser.getId());
        Response<List<BrandRlView>> result = new Response<List<BrandRlView>>();
        result.setResult(brdao.findYrl(brandClub.getId().intValue(),shopname));
        return result;
    }

    @Override
    public Response<List<BrandRlView>> findRl(int brandClubKey) {
        Response<List<BrandRlView>> result = new Response<List<BrandRlView>>();
        result.setResult(brdao.findYrl2(brandClubKey));
        return result;
    }

    @Override
    public Response<List<BrandWRlView>> findWRl(BaseUser baseUser,String shopname) {
        BrandClub brandClub=bcldao.findBrandClubByUid(baseUser.getId());
        String newbrand="{\"brands\":[{\"id\":"+brandClub.getBrandOutId()+",\"";
        Response<List<BrandWRlView>> result = new Response<List<BrandWRlView>>();
        result.setResult(brdao.findWrl(newbrand, brandClub.getBrandOutId().intValue(),shopname));
        return result;
    }

    @Override
    public void addRl_Key(int[] shopidlist, BaseUser baseUser) {
        BrandClub brandClub=bcldao.findBrandClubByUid(baseUser.getId());
        bcdao.addRl(shopidlist,brandClub.getId().intValue());
    }

    @Override
    public void delRl_Key(int[] idlist) {
        bcdao.delRl(idlist);
    }

    @Override
    public List<BrandsClubKey> findbrandKeyByShopId(Integer shopId) {
        return bcdao.findbrandKeyByShopId(shopId);
    }

    @Override
    public List<BrandsClubKey> findbrandKeyByBrandId(Long brandId) {
        return bcdao.findbrandKeyByBrandId(brandId);
    }


}
