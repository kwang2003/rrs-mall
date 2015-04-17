package com.rrs.brand.dao;

import com.rrs.brand.model.BrandClubType;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhum01 on 2014/7/30.
 */
@Repository
public class BrandClubTypeDao extends SqlSessionDaoSupport{
    public List<BrandClubType> findAllBy() {
        return getSqlSession().selectList("BrandClubType.findAllBy");
    }

}
