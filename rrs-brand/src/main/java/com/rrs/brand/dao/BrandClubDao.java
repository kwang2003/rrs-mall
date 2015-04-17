package com.rrs.brand.dao;

import com.aixforce.user.base.BaseUser;
import com.rrs.brand.model.Addresses;
import com.rrs.brand.model.BrandClub;
import com.rrs.brand.model.BrandClubVo;
import com.rrs.brand.model.ExperinceMall;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/7/23.
 */

@Repository
public class BrandClubDao extends SqlSessionDaoSupport{
    public List<BrandClubVo> findAllBy(String brandName,Integer brandTypeId,Integer status) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandName",brandName);
        paramMap.put("brandTypeId",brandTypeId);
        paramMap.put("status",status);
        return getSqlSession().selectList("BrandClubVo.findAllBy",paramMap);

    }

    public List<BrandClubVo> findAllByUser(BaseUser user,String brandName,Integer brandTypeId,Integer status) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandName",brandName);
        paramMap.put("brandTypeId",brandTypeId);
        paramMap.put("status",status);
        paramMap.put("userId",user.getId());
        return getSqlSession().selectList("BrandClubVo.findAllBy",paramMap);

    }

    public List<BrandClub> findBrandClubBy(String brandName) {
        HashMap<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandName",brandName);
        return getSqlSession().selectList("BrandClub.findBrandClubBy",paramMap);

    }

    public BrandClub findBrandClubByUid(Long userid) {
        return getSqlSession().selectOne("BrandClub.findBrandClubByUid", userid);

    }

    public Boolean updateBrandClub(BrandClub brandClub) {
           return getSqlSession().update("BrandClub.updateBrandClub",brandClub) == 1;
    }

    public BrandClub findById(int id) {
        return getSqlSession().selectOne("BrandClub.findById",id);

    }

    public void updateBrandClubHttp2(BrandClub brandClub) {
        getSqlSession().update("BrandClub.updateBrandClubHttp2",brandClub);

    }

    public BrandClub findByUser(BrandClub brandClub) {
        return getSqlSession().selectOne("BrandClub.getBrandOutIdByUser",brandClub);
    }
    /**
     *查询店铺认领的品牌
     */
    public List<BrandClub> searchRl(long userId){
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("shopUser",userId);
        return getSqlSession().selectList("BrandClub.searchKeyByUser",map);
    }
    /**
     *修改认领店铺状态
     */
    public void updateRl(long userId){
        BrandClub brand= new BrandClub();
        brand.setId(userId);
        getSqlSession().update("BrandClub.updateKeyById",brand);
    }

    public List<BrandClub> findAll() {
        return getSqlSession().selectList("BrandClub.findAll");
    }

    public BrandClub findAllBrandUser(BrandClub brandClub) {
        return getSqlSession().selectOne("BrandClub.findBrandInfoByUser",brandClub);
    }

    public BrandClub queryBrandById(Long brandId) {
        Map<Object,Object> paramMap = new HashMap<Object,Object>();
        paramMap.put("brandId",brandId);
        return getSqlSession().selectOne("BrandClub.queryBrandById",paramMap);
    }
    public void updateBrandInfos(BrandClub brandClub){

        getSqlSession().update("BrandClub.updateInfos",brandClub);
    }
    public List<Addresses> findAddress(int provinceId){

      return   getSqlSession().selectList("Addresses.findAddress",provinceId);
    }
    public List<ExperinceMall> findMall(int provinceId,int cityId){
        Map<String,Integer> map =new HashMap<String,Integer>();
        map.put("provinceId",provinceId);
        map.put("cityId",cityId);

        return   getSqlSession().selectList("ExperinceMall.findMall",map);
    }
    public Long findShop(long userId){
    return getSqlSession().selectOne("ExperinceMall.findShop",userId);

    }
    public int findStatus(long userId){
        return (Integer)getSqlSession().selectOne("ExperinceMall.findStatus",userId);

    }
    public int findCount(long userId){
        return (Integer)getSqlSession().selectOne("ExperinceMall.findCount",userId);

    }
    public void insertMall( int mallId,long userId,long shopId,long experId){
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("mallId",mallId);
        map.put("userId",userId);
        map.put("shopId",shopId);
        map.put("experId",experId);
        getSqlSession().selectOne("ExperinceMall.create",map);

    }
    public void exitMall(long userId){
        getSqlSession().update("ExperinceMall.exitMall", userId);

    }

    public Long findExpId(int mallId) {
        return getSqlSession().selectOne("ExperinceMall.findMallUser",mallId);
    }

    public List<Addresses> findProvince() {
        return getSqlSession().selectList("Addresses.findProvince");
    }

    public List<ExperinceMall> findAllExper(Map map){
        return getSqlSession().selectList("ExperMallView.showAll",map);
    }
    public void experQr(Map map){

        getSqlSession().update("ExperinceMall.qianRu",map);
        getSqlSession().update("ExperinceMall.qianRuStatus",map);
    }
    public void experQc(Map map){
        getSqlSession().update("ExperinceMall.qianRu",map);
        getSqlSession().update("ExperinceMall.qianChuStatus",map);
    }
    public String findCode(Map map){

        return getSqlSession().selectOne("ExperinceMall.findCode",map);
    }
    public int findQcStatus(long userId){
        return (Integer)getSqlSession().selectOne("ExperinceMall.findQcStatus",userId);
    }

    /**
     * 查找支持到店支付的店铺
     * @param userId
     * @return
     */
    public Long findStorepPay(long userId){
        return getSqlSession().selectOne("ExperinceMall.findStorepPay",userId);

    }
}
