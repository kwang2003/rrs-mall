package com.rrs.brand.dao;

import com.rrs.brand.model.BrandClubProductType;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by mark on 2014/7/31.
 */

@Repository
public class BrandClubProductTypeDao extends SqlSessionDaoSupport{

    public List<BrandClubProductType> findAllBy() {
        return getSqlSession().selectList("BrandClubProductType.findAllBy");
    }
}
