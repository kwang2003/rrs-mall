package com.rrs.coupons.service;

import com.rrs.coupons.model.RrsCou;

import java.util.List;
import java.util.Map;

/**
 * Created by yea01 on 2014/11/27.
 */
public interface CouponsManageService {
    public List<RrsCou> findAllRrsCou(long userId, int pageCount);
    public List<RrsCou> findAllBySearch(Map<Object, Object> map);
    public Integer countAllCou(long userId);
    public Integer countCouBySearch(RrsCou rrsCou);
    public List<Map> findAll(int pageCount);
    public void chexiaoCoupons(long couponsId);
    public List<Map> searchAll(Map<String, Object> map);
    public void stopCoupons(Map<String, Object> map);
    public RrsCou findEditById(long couponsId);
}
