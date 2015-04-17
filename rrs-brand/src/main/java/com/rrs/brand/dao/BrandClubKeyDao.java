package com.rrs.brand.dao;

import com.rrs.brand.model.BrandsClubKey;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhua02 on 2014/7/31.
 */
@Repository
public class BrandClubKeyDao extends SqlSessionDaoSupport{

    public void addRl(int[] shopidlist,int brandClub_id) {
        for (int i = 0; i < shopidlist.length; i++) {
            BrandsClubKey brandsClubKey = new BrandsClubKey();
            brandsClubKey.setStatus(1);
            brandsClubKey.setBrandClubId(brandClub_id);
            brandsClubKey.setShopId(shopidlist[i]);
            getSqlSession().insert("BrandsClubKey.addRl", brandsClubKey);
        }
    }

    public void delRl(int[] idlist){
        int id=0;
        for(int i=0;i<idlist.length;i++){
            id=idlist[i];
            getSqlSession().delete("BrandsClubKey.delRl",id);
        }
    }


    public List<BrandsClubKey> findbrandKeyByShopId(Integer shopId){
        BrandsClubKey brandsClubKey=new BrandsClubKey();
        brandsClubKey.setShopId(shopId.intValue());
        return getSqlSession().selectList("BrandsClubKey.findbrandKeyByShopId",brandsClubKey);
    }

    public List<BrandsClubKey> findbrandKeyByBrandId(Long brandId) {
        BrandsClubKey brandsClubKey=new BrandsClubKey();
        brandsClubKey.setBrandClubId(brandId.intValue());
        return getSqlSession().selectList("BrandsClubKey.findbrandKeyByBrandId",brandsClubKey);
    }
}
