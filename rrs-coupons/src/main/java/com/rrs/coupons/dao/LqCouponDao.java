package com.rrs.coupons.dao;

import com.google.common.collect.ImmutableMap;
import com.rrs.coupons.model.LqCouponView;
import com.rrs.coupons.model.RrsCou;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhua02 on 2014/8/21.
 */
@Repository
public class LqCouponDao extends SqlSessionDaoSupport{

    public List<LqCouponView> findCouponAll(String today){
        return getSqlSession().selectList("LqCouponView.findCouponAll",today);
    }

    public int findUserLimit(int id){
        RrsCou rrsc=getSqlSession().selectOne("RrsCou.findUserLimit", id);
        return rrsc.getUseLimit();
    }

    public int findSendNum(int id){
        RrsCou rrsc=getSqlSession().selectOne("RrsCou.findSendNum",id);
        return rrsc.getSendNum();
    }

    public int findUseCount(int couponId){
        return (Integer)getSqlSession().selectOne("RrsCouponsUser.findUseCount",couponId);
    }

    public int findUserUseCount(int couponId,int userid){
        return (Integer)getSqlSession().selectOne("RrsCouponsUser.findUserUseCount", ImmutableMap.of("couponId", couponId, "userid", userid));
    }

    public void addUserCoupon(int couponId,int userid){
        getSqlSession().insert("RrsCouponsUser.addUserCoupon",ImmutableMap.of("userId", userid, "couponId", couponId));
    }

    public void updateCouponReceive(int id){
        getSqlSession().update("RrsCou.updateCouponReceive",id);
    }
}
