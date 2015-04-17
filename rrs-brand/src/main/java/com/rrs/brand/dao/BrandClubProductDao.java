package com.rrs.brand.dao;

import com.rrs.brand.model.BrandClubProduct;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mark on 2014/7/31.
 */

@Repository
public class BrandClubProductDao extends SqlSessionDaoSupport{

    public Boolean updateBrandClubProduct(BrandClubProduct brandClubProduct) {
        return getSqlSession().update("BrandClubProduct.updateBrandClubProduct",brandClubProduct) == 1;
    }

    public Boolean deleteBrandClubProduct(BrandClubProduct brandClubProduct) {
        return getSqlSession().delete("BrandClubProduct.deleteBrandClubProduct",brandClubProduct) == 1;
    }

    public Boolean saveBrandClubProduct(BrandClubProduct brandClubProduct) {
        return getSqlSession().insert("BrandClubProduct.saveBrandClubProduct",brandClubProduct) == 1;
    }

    public List<BrandClubProduct> findByBrandId(Integer brandId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandId",brandId);
        return getSqlSession().selectList("BrandClubProduct.findAllBy",paramMap);
    }

    public int validateProduct(String userId,long productId){
        Map<Object,Object> map =new HashMap<Object,Object>();
        map.put("userId",userId);
        map.put("productId",productId);
        return (Integer)getSqlSession().selectOne("RrsBrand.vailProduct",map);
    }

    public int validateBrand(String userId,long productId){
        Map<Object,Object> map =new HashMap<Object,Object>();
        map.put("userId",userId);
        map.put("productId",productId);
        return (Integer)getSqlSession().selectOne("RrsBrand.vailBrand",map);

    }
}
