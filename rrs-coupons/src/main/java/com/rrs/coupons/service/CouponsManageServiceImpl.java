package com.rrs.coupons.service;

import com.rrs.coupons.dao.RrsCouponsDao;
import com.rrs.coupons.model.RrsCou;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yea01 on 2014/11/27.
 */
@Service
public class CouponsManageServiceImpl implements CouponsManageService {
    @Autowired
    private RrsCouponsDao rrsCouponsDao;

    @Override
    public List<RrsCou> findAllRrsCou(long userId,int pageCount) {
        return rrsCouponsDao.findAllSellCoupons(userId,pageCount);
    }

    @Override
    public List<RrsCou> findAllBySearch(Map<Object,Object> map) {
        return rrsCouponsDao.findBySearch(map);
    }

    @Override
    public Integer countAllCou(long userId) {

        return rrsCouponsDao.countCou(userId);
    }

    @Override
    public Integer countCouBySearch(RrsCou rrsCou) {
            return rrsCouponsDao.countCouBySearch(rrsCou);
    }

    @Override
    public List<Map> findAll(int pageCount) {
        return rrsCouponsDao.findAdminAll(pageCount);
    }

    @Override
    public void chexiaoCoupons(long couponsId) {
         rrsCouponsDao.chexiaoCoupons(couponsId);
    }

    @Override
    public List<Map> searchAll(Map<String, Object> map) {
        return rrsCouponsDao.searchAll(map);
    }

    @Override
    public void stopCoupons(Map<String, Object> map) {
        rrsCouponsDao.stopCoupons(map);
    }

    //编辑查询商家优惠券信息接口
    @Override
    public RrsCou findEditById(long couponsId) {
        return rrsCouponsDao.findEditById(couponsId);
    }

}
