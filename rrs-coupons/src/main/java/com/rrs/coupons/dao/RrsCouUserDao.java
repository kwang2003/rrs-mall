package com.rrs.coupons.dao;

import com.rrs.coupons.model.RrsCouUser;
import com.rrs.coupons.model.RrsCouUserView;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by yea01 on 2014/8/22.
 */

@Repository
public class RrsCouUserDao extends SqlSessionDaoSupport {

    public List<RrsCouUserView> queryCouponsAllByUser(Long userId, Long status,String nowDate){
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("startTime",nowDate);
        paramMap.put("endTime",nowDate);
        paramMap.put("status",status);
        paramMap.put("userId",userId);
        return getSqlSession().selectList("RrsCouponsUserView.queryCouponsAllByUser",paramMap);
    }

    public List<RrsCouUser> queryCouponsUserBy(Long userId,Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponId",couponsId);
        paramMap.put("userId",userId);
        return getSqlSession().selectList("RrsCouponsUser.queryCouponsUserBy",paramMap);
    }

    public void updateCouponUser(Long id) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("id",id);
        getSqlSession().update("RrsCouponsUser.updateCouponUser",paramMap);
    }
}
