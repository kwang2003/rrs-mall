package com.rrs.coupons.dao;

import com.aixforce.common.model.Paging;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsCouOrder;
import com.rrs.coupons.model.RrsCouOrderItem;
import com.rrs.coupons.model.ShopCoupons;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RrsCouponsDao extends SqlSessionDaoSupport {

	public List<RrsCouOrder> findByOrderIds(List<Long> ids) {
		return getSqlSession().selectList("RrsCou.findByOrderIds", ids);
	}
    public List<RrsCouOrderItem> findOrderItemsByOrderIds(List<Long> ids) {
        return getSqlSession().selectList("RrsCou.findOrderItemsByOrderIds", ids);
    }

    /**
     * 查询可用的优惠劵信息
     * 优惠券状态：未生效（0）暂停（1）生效（2）失效(3)
     * **/
    public List<RrsCou> queryRrsCouponsBy(String nowDate,Long status) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("startTime",nowDate);
        paramMap.put("endTime",nowDate);
        paramMap.put("status",status);
        return getSqlSession().selectList("RrsCou.queryRrsCouponsBy", paramMap);
    }

    public RrsCou queryCouponsById(Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponsId",couponsId);
        List<RrsCou> resultList = getSqlSession().selectList("RrsCou.queryCouponsById", paramMap);
        if(resultList!=null && resultList.size()>0){
            return resultList.get(0);
        }else{
            return null;
        }
    }

    public Boolean updateRrsCou(RrsCou rrsCou) {
        return getSqlSession().insert("RrsCou.updateRrsCou",rrsCou) == 1;
    }

    public Paging<RrsCou> queryCouponsByPage(Integer offset, Integer size, Map<String, Object> params) {
        Long total = getSqlSession().selectOne("RrsCou.countOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<RrsCou> shops = getSqlSession().selectList("RrsCou.pagination", params);
            return new Paging<RrsCou>(total, shops);
        }
        return new Paging<RrsCou>(0L, Collections.<RrsCou>emptyList());
    }

    public Paging<ShopCoupons> queryShopCouponsByPage(Integer offset, Integer size, Map<String, Object> params) {
        Long total = getSqlSession().selectOne("RrsCou.countShopOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<ShopCoupons> shops = getSqlSession().selectList("RrsCou.pageShopCou", params);
            return new Paging<ShopCoupons>(total, shops);
        }
        return new Paging<ShopCoupons>(0L, Collections.<ShopCoupons>emptyList());
    }

    public List<RrsCou> findAllSellCoupons(long userId,int pageCount){
        if(pageCount!=0){
            pageCount = pageCount*25;
        }
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("userId",userId);
        map.put("page",pageCount);

        return getSqlSession().selectList("RrsCou.findSellCoupons",map);
    }
    public List<RrsCou> findBySearch(Map<Object,Object> map){

        return getSqlSession().selectList("RrsCou.findBySearch",map);
    }
    public Integer countCou(long userId){
        return getSqlSession().selectOne("RrsCou.countCou",userId);
    }
    public Integer countCouBySearch(RrsCou rrsCou){
        return getSqlSession().selectOne("RrsCou.countBySearch",rrsCou);

    }
    public List<Map> findAdminAll(int page){
        if(page!=0){
            page = page*25;
        }
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("page",page);
        return getSqlSession().selectList("RrsCou.findAdminCoupons",map);
    }
    public void chexiaoCoupons(long couponsId){

         getSqlSession().update("RrsCou.chexiaoCoupons",couponsId);
    }
    public List<Map> searchAll(Map<String,Object> map){
        return getSqlSession().selectList("RrsCou.searchAll",map);

    }

    public RrsCou queryShopCouponsById(Long couponsId) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("couponsId",couponsId);
        List<RrsCou> resultList = getSqlSession().selectList("RrsCou.queryShopCouponsById", paramMap);
        if(resultList!=null && resultList.size()>0){
            return resultList.get(0);
        }else{
            return null;
        }
    }
    public void stopCoupons(Map<String, Object> map){

        getSqlSession().update("RrsCou.stopCoupons",map);
    }
    public RrsCou findEditById(long couponsId){
        return getSqlSession().selectOne("RrsCou.editById",couponsId);
    }

    public List<Map<String, String>> queryListCouName(Long couponsId){
        return getSqlSession().selectList("RrsCou.queryCouName",couponsId);

    }


}
