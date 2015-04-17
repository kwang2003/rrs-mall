package com.aixforce.rrs.coupon.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.coupon.model.Coupon;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Created by Effet on 4/21/14.
 */
@Repository
public class CouponDao extends SqlSessionDaoSupport {

    public Long create(Coupon coupon) {
        getSqlSession().insert("Coupon.create", coupon);
        return coupon.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("Coupon.delete", id) == 1;
    }

    public boolean update(Coupon coupon) {
        return getSqlSession().update("Coupon.update", coupon) == 1;
    }

    public Coupon findById(Long id) {
        return getSqlSession().selectOne("Coupon.findById", id);
    }

    public Paging<Coupon> findBy(Coupon criteria, Integer offset, Integer limit) {
        Long count = getSqlSession().selectOne("Coupon.countOf", criteria);
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<Coupon> coupons = getSqlSession().selectList("Coupon.findBy",
                    ImmutableMap.of("criteria", criteria, "offset", offset, "limit", limit));
            return new Paging<Coupon>(count, coupons);
        }
        return new Paging<Coupon>(0L, Collections.<Coupon>emptyList());
    }

    public List<Coupon> findAllBy(Coupon criteria) {
        return  getSqlSession().selectList("Coupon.findAllBy", criteria);
    }
}
