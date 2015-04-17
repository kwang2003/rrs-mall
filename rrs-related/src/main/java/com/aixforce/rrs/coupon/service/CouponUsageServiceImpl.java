package com.aixforce.rrs.coupon.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.coupon.service.CouponService;
import com.aixforce.rrs.coupon.service.CouponUsageService;
import com.aixforce.rrs.coupon.dao.CouponUsageDao;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.rrs.coupon.model.CouponUsage;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Created by Effet on 4/22/14.
 */
@Slf4j
@Service
public class CouponUsageServiceImpl implements CouponUsageService {

    @Autowired
    private CouponUsageDao couponUsageDao;

    @Autowired
    private CouponService couponService;

    @Override
    public Response<Boolean> update(CouponUsage couponUsage) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            Response<Coupon> couponR = couponService.findById(couponUsage.getCouponId(), couponUsage.getSellerId());
            if (!couponR.isSuccess()) {
                result.setError(couponR.getError());
                return result;
            }
            result.setResult(couponUsageDao.update(couponUsage));
            return result;
        } catch (Exception e) {
            log.error("failed to update couponUsage {}, cause:{}", couponUsage, Throwables.getStackTraceAsString(e));
            result.setError("coupon.usage.update.failed");
            return result;
        }
    }

    @Override
    public Response<CouponUsage> obtainACoupon(Long couponId, Long buyerId) {
        Response<CouponUsage> result = new Response<CouponUsage>();
        CouponUsage criteria = new CouponUsage();
        criteria.setCouponId(couponId);
        criteria.setBuyerId(buyerId);
        try {
            CouponUsage exist = couponUsageDao.findByCouponIdAndBuyerId(couponId, buyerId);
            if (exist != null) {
                exist.setUnused(exist.getUnused() + 1);
                couponUsageDao.update(exist);
            } else {
                exist = new CouponUsage();
                Response<Coupon> couponR = couponService.findById(couponId, exist.getSellerId());
                if (!couponR.isSuccess()) {
                    result.setError(couponR.getError());
                    return result;
                }
                Coupon cp = couponR.getResult();

                if (Objects.equal(cp.getSellerId(), buyerId)) {
                    log.error("can not obtain coupon that you released");
                    result.setError("coupon.usage.obtain.failed.its.yours");
                }

                exist.setCouponId(cp.getId());
                exist.setCouponName(cp.getName());
                exist.setSellerId(cp.getSellerId());
                exist.setShopName(cp.getShopName());
                exist.setAmount(cp.getAmount());
                exist.setUnused(1);
                exist.setUsed(0);
                exist.setEndAt(cp.getEndAt());
                couponUsageDao.create(exist);
            }
            result.setResult(couponUsageDao.findByCouponIdAndBuyerId(couponId, buyerId));
            return result;
        } catch (Exception e) {
            log.error("failed to obtain a coupon(id = {}), cause:{}", couponId, Throwables.getStackTraceAsString(e));
            result.setError("coupon.usage.update.failed");
            return result;
        }
    }

    @Override
    public Response<Paging<CouponUsage>> findByOrderBy(CouponUsage criteria, String orderBy,
                                                       Integer pageNo, Integer size) {
        Response<Paging<CouponUsage>> result = new Response<Paging<CouponUsage>>();

        pageNo = firstNonNull(pageNo, 1);
        size = firstNonNull(size, 20);
        pageNo = pageNo <= 0 ? 1 : pageNo;
        size = size <= 0 ? 20 : size;

        PageInfo pageInfo = new PageInfo(pageNo, size);

        try {
            result.setResult(couponUsageDao.findByOrderBy(criteria, orderBy, pageInfo.getOffset(), pageInfo.getLimit()));
            return result;
        } catch (Exception e) {
            log.error("failed to find coupon by criteria {}, order by {}, cause:{}", criteria, orderBy, Throwables.getStackTraceAsString(e));
            result.setError("coupon.usage.query.failed");
            return result;
        }
    }

    @Override
    public Response<Paging<CouponUsage>> findByNameOrderByAmountOrEndTime(String name, String orderBy,
                                                                          Integer pageNo, Integer size, BaseUser buyer) {
        Response<Paging<CouponUsage>> result = new Response<Paging<CouponUsage>>();

        if (buyer == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }

        CouponUsage criteria = new CouponUsage();

        if (!Strings.isNullOrEmpty(name)) {
            criteria.setShopName(name);
        }
        criteria.setBuyerId(buyer.getId());

        result = findByOrderBy(criteria, orderBy, pageNo, size);
        return result;
    }
}
