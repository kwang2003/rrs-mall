package com.rrs.brand.dao;

import com.rrs.brand.model.BrandClubSd;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhum01 on 2014/8/5.
 */
@Repository
public class BrandClubSlideDao extends SqlSessionDaoSupport {
    public Boolean updateBrandClubSlide(BrandClubSd brandClubSlide) {
        return getSqlSession().update("BrandClubSlide.updateBrandClubSlide",brandClubSlide) == 1;
    }

    public Boolean deleteBrandClubSlide(BrandClubSd brandClubSlide) {
        return getSqlSession().delete("BrandClubSlide.deleteBrandClubSlide",brandClubSlide) == 1;
    }

    public Boolean saveBrandClubSlide(BrandClubSd brandClubSlide) {
        return getSqlSession().insert("BrandClubSlide.insertBrandClubSlide",brandClubSlide) == 1;
    }

//    public List<BrandClubSd> findAllByType(Long imageType) {
//        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
//        paramMap.put("imageType",imageType);
//        return getSqlSession().selectList("BrandClubSlide.findAllByType",paramMap);
//    }

    public List<BrandClubSd> findAllByIdAndType(int brandId, Long imageType) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandId",brandId);
        paramMap.put("imageType",imageType);
        return getSqlSession().selectList("BrandClubSlide.findAllByIdAndType",paramMap);
    }
}
