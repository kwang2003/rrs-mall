package com.rrs.coupons.dao;

import com.aixforce.common.model.Paging;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsShowCouponView;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/8/19.
 */


@Repository
public class CouponsRrsDao extends SqlSessionDaoSupport {
    public Integer adminCount(){

        return getSqlSession().selectOne("RrsCou.adminCount");
    }
    public List<RrsShowCouponView> getCouByUserId(Long userId){
        return getSqlSession().selectList("RrsCouponsViews.getList",userId);
    }
    public List<RrsCou> findCouponsAll(Map<String, Object> paramMap){
        return getSqlSession().selectList("RrsCou.findCouponsAll",paramMap);
    }

    public int addCoupon(Map<String, Object> paramMap){
        return getSqlSession().insert("RrsCou.addCoupon",paramMap);
    }
    public int updateCoupon(Map<String, Object> paramMap){
        return getSqlSession().update("RrsCou.updateCoupon",paramMap);
    }
    public int updateCouponStatus(Map<String, Object> paramMap){
        return getSqlSession().update("RrsCou.updateCouponStatus",paramMap);
    }
    public List<Map<String, Object>> findCategory(int categoryId){
        return getSqlSession().selectList("RrsCou.findCategory",categoryId);
    }

    public List<RrsCou> querySellerCouponsByParam(Map<String, Object> params) {
        return getSqlSession().selectList("RrsCou.querySellerCouponsByParam",params);
    }

    public Paging<RrsCou> queryCouponsByShopId(Map<String, Object> params){
        Long total = getSqlSession().selectOne("RrsCou.queryCouponsCountByShopId", params);
        if (total > 0L) {
            List<RrsCou> shops = getSqlSession().selectList("RrsCou.queryCouponsByShopId", params);
            return new Paging<RrsCou>(total, shops);
        }
        return new Paging<RrsCou>(0L, Collections.<RrsCou>emptyList());
    }
    public Integer insertItemIds(List<Map<String, Object>> items) {
        if (items.size() == 0)
            return 0;
        return getSqlSession().insert("RrsCou.insertItemIds",items);
    }
    public List<Map<String,Object>> findEditItems(String couponsId){
        return getSqlSession().selectList("RrsCou.findEditItems",couponsId);
    }
    public Integer deleteCouponsId(String couponsId) {
        return getSqlSession().delete("RrsCou.deleteCouponsId",couponsId);
    }

}
