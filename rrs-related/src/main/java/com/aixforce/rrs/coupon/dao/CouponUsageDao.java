package com.aixforce.rrs.coupon.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.coupon.model.CouponUsage;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Created by Effet on 4/22/14.
 */
@Repository
public class CouponUsageDao extends SqlSessionDaoSupport {

    public Long create(CouponUsage couponUsage) {
        getSqlSession().insert("CouponUsage.create", couponUsage);
        return couponUsage.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("CouponUsage.delete", id) == 1;
    }

    public boolean update(CouponUsage couponUsage) {
        return getSqlSession().update("CouponUsage.update", couponUsage) == 1;
    }

    public CouponUsage findById(Long id) {
        return getSqlSession().selectOne("CouponUsage.findById", id);
    }

    public CouponUsage findByCouponIdAndBuyerId(Long couponId, Long userId) {
        return getSqlSession().selectOne("CouponUsage.findByCouponIdAndBuyerId",
                ImmutableMap.of("couponId", couponId, "buyerId", userId));
    }

    public Paging<CouponUsage> findBy(CouponUsage criteria, Integer offset, Integer limit) {
        Long count = getSqlSession().selectOne("CouponUsage.countOf", criteria);
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<CouponUsage> couponUsages = getSqlSession().selectList("CouponUsage.findBy",
                    ImmutableMap.of("criteria", criteria, "offset", offset, "limit", limit));
            return new Paging<CouponUsage>(count, couponUsages);
        }
        return new Paging<CouponUsage>(0L, Collections.<CouponUsage>emptyList());
    }

    public Paging<CouponUsage> findByOrderBy(CouponUsage criteria, String orderBy, Integer offset, Integer limit) {
        Long count = getSqlSession().selectOne("CouponUsage.countOf", criteria);
        count = Objects.firstNonNull(count, 0L);
        if (count > 0) {
            List<CouponUsage> couponUsages = getSqlSession().selectList("CouponUsage.findByOrderBy",
                    ImmutableMap.of("criteria", criteria, "orderBy", orderBy, "offset", offset, "limit", limit));
            return new Paging<CouponUsage>(count, couponUsages);
        }
        return new Paging<CouponUsage>(0L, Collections.<CouponUsage>emptyList());
    }
}
