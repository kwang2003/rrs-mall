package com.rrs.brand.dao;

import com.google.common.collect.ImmutableMap;
import com.rrs.brand.model.BrandRlView;
import com.rrs.brand.model.BrandWRlView;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhum01 on 2014/7/23.
 */

@Repository
public class BrandRlViewDao extends SqlSessionDaoSupport{

    public List<BrandRlView> findYrl(int id,String shopname) {
        if(shopname != null && !shopname.equals("")){
            return getSqlSession().selectList("BrandRlView.findYrltj", ImmutableMap.of("id", id,"shopname",shopname));
        }else {
            return getSqlSession().selectList("BrandRlView.findYrl", id);
        }
    }

    public List<BrandRlView> findYrl2(int id) {
        return getSqlSession().selectList("BrandRlView.findYrl2",id);
    }

    public List<BrandWRlView> findWrl(String brandid,int brand_id,String shopname) {
        System.out.println(brandid);
        System.out.println(brand_id);
        //return getSqlSession().selectList("BrandWRlView.findWrl", ImmutableMap.of("brandid", brandid, "brandClub_id", brandClub_id,"brand_id",brand_id));
        if(shopname != null && !shopname.equals("")){
            return getSqlSession().selectList("BrandWRlView.findWrltj", ImmutableMap.of("brandid", brandid,"brand_id",brand_id,"shopname",shopname));
        }else {
            return getSqlSession().selectList("BrandWRlView.findWrl", ImmutableMap.of("brandid", brandid, "brand_id", brand_id));
        }

    }
}
