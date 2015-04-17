package com.aixforce.rrs.coupon.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.coupon.service.CouponService;
import com.aixforce.rrs.coupon.dao.CouponDao;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Created by Effet on 4/22/14.
 */
@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private ShopService shopService;

    @Override
    public Response<Coupon> create(Coupon coupon) {
        Response<Coupon> result = new Response<Coupon>();
        if (coupon == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }
        if (coupon.getAmount() > coupon.getUseLimit()) {
            log.error("failed to create coupon {}, amount must less than useLimit", coupon);
            result.setError("coupon.limit.too.small");
            return result;
        }
        if (coupon.getStartAt().after(coupon.getEndAt())) {
            log.error("failed to create coupon {}, startAt must before endAt", coupon);
            result.setError("coupon.date.illegal");
            return result;
        }
        try {
            couponDao.create(coupon);
            result.setResult(coupon);
            return result;
        } catch (Exception e) {
            log.error("failed to create coupon {}, cause:{}", coupon, Throwables.getStackTraceAsString(e));
            result.setError("coupon.create.failed");
            return result;
        }
    }

    @Override
    public Response<Coupon> create(String name, Integer amount, Integer useLimit,
                                   Date startAt, Date endAt, Long sellerId) {
        Response<Coupon> result = new Response<Coupon>();

        try {

            Response<Shop> shopR = shopService.findByUserId(sellerId);
            if (!shopR.isSuccess()) {
                log.error("failed to find shop(user_id={}), error code:{}",sellerId, shopR.getError() );
                result.setError(shopR.getError());
                return result;
            }

            Shop shop = shopR.getResult();
            Coupon cp = new Coupon();
            cp.setName(name);
            cp.setShopId(shop.getId());
            cp.setShopName(shop.getName());
            cp.setSellerId(shop.getUserId());
            cp.setAmount(amount);
            cp.setUseLimit(useLimit);
            cp.setType(Coupon.Type.OBTAINED.value());
            cp.setStatus(Coupon.Status.INIT.value());
            cp.setTaken(0);
            cp.setUsed(0);
            cp.setClicked(0);
            cp.setStartAt(startAt);
            cp.setEndAt(endAt);

            return create(cp);

        } catch (Exception e) {
            log.error("failed to create coupon(name={}) for seller(id={}), cause:{}",
                    name, sellerId, Throwables.getStackTraceAsString(e));
            result.setError("coupon.create.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(Long id, String name, Integer amount, Integer useLimit,
                                    Date startAt, Date endAt, Long sellerId) {
        Response<Boolean> result = new Response<Boolean>();
        Coupon cp = new Coupon();

        Coupon exist = couponDao.findById(id);
        if (exist == null) {
            log.error("no coupon exist where id = {}", id);
            result.setError("illegal.param");
            return result;
        }
        if (exist.getStatus() != Coupon.Status.INIT.value()) {
            log.error("only coupon in INIT state can edit");
            result.setError("coupon.update.failed");
            return result;
        }

        cp.setId(id);
        cp.setName(name);
        cp.setAmount(amount);
        cp.setUseLimit(useLimit);
        cp.setStartAt(startAt);
        cp.setEndAt(endAt);

        return update(cp, sellerId);
    }

    @Override
    public Response<Boolean> update(Coupon coupon) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(couponDao.update(coupon));
            return result;
        } catch (Exception e) {
            log.error("failed to update coupon {}, cause:{}", coupon, Throwables.getStackTraceAsString(e));
            result.setError("coupon.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> update(Coupon coupon, Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        if (coupon == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        if (coupon.getId() == null) {
            log.error("coupon.id must be specified {}", coupon);
            result.setError("illegal.param");
            return result;
        }
        Coupon exist = couponDao.findById(coupon.getId());
        if (exist == null) {
            log.error("no coupon exist where id = {}", coupon.getId());
            result.setError("illegal.param");
            return result;
        }
        if (!Objects.equal(exist.getSellerId(), userId)) {
            log.error("authorize fail userId {}, coupon belong to {}", userId, exist.getId());
            result.setError("authorize.fail");
            return result;
        }
        if (firstNonNull(coupon.getAmount(), exist.getAmount())
                > firstNonNull(coupon.getUseLimit(), exist.getUseLimit())) {
            log.error("useLimit must less than amount");
            result.setError("illegal.param");
            return result;
        }
        if (firstNonNull(coupon.getStartAt(), exist.getStartAt())
                .after(firstNonNull(coupon.getEndAt(), exist.getEndAt()))) {
            log.error("startAt must before endAt");
            result.setError("illegal.param");
        }
        try {
            result.setResult(couponDao.update(coupon));
            return result;
        } catch (Exception e) {
            log.error("failed to update coupon {}, cause:{}", coupon, Throwables.getStackTraceAsString(e));
            result.setError("coupon.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> suspend(Long id, Long sellerId) {
        return changeStatus(id, Coupon.Status.SUSPEND.value(), sellerId);
    }

    @Override
    public Response<Boolean> release(Long id, Long sellerId) {
        return changeStatus(id, Coupon.Status.RELEASE.value(), sellerId);
    }

    private Response<Boolean> changeStatus(Long id, Integer status, Long sellerId) {
        Coupon criteria = new Coupon();
        criteria.setId(id);
        criteria.setStatus(status);
        return update(criteria, sellerId);
    }

    @Override
    public Response<Coupon> findById(Long id) {
        Response<Coupon> result = new Response<Coupon>();
        try {
            Coupon cp = couponDao.findById(id);
            if (cp == null) {
                log.error("no coupon(id = {}) found", id);
                result.setError("coupon.not.found");
                return result;
            }
            result.setResult(cp);
            return result;
        } catch (Exception e) {
            log.error("failed to find coupon(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("coupon.query.failed");
            return result;
        }
    }

    @Override
    public Response<Coupon> findById(Long id, Long userId) {
        Response<Coupon> result = new Response<Coupon>();
        try {
            Coupon cp = couponDao.findById(id);

            if (cp == null) {
                log.error("no coupon(id = {}) found", id);
                result.setError("coupon.not.found");
                return result;
            }

            boolean hasPerm = false;
            if (Objects.equal(userId, cp.getSellerId())) {
                hasPerm = true;
            } else if (Objects.equal(cp.getType(), Coupon.Type.OBTAINED.value()) &&
                    (Objects.equal(cp.getStatus(), Coupon.Status.RELEASE.value()) ||
                            Objects.equal(cp.getStatus(), Coupon.Status.VALID.value()))) {
                // everyone can obtain this coupon, and it now is released or valid to use
                hasPerm = true;
            }

            if (!hasPerm) {
                log.error("failed to find coupon(id = {}), user has no permission", id);
                result.setError("user.has.no.permission");
                return result;
            }

            result.setResult(cp);
            return result;

        } catch (Exception e) {
            log.error("failed to find coupon(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("coupon.query.failed");
            return result;
        }
    }

    @Override
    public Response<List<Coupon>> findAllBy(Coupon criteria) {
        Response<List<Coupon>> result = new Response<List<Coupon>>();
        try {
            List<Coupon> coupons = couponDao.findAllBy(criteria);
            result.setResult(coupons);
            return result;
        } catch (Exception e) {
            log.error("failed to find all coupons by {}, cause:{}", criteria, Throwables.getStackTraceAsString(e));
            result.setError("coupon.query.failed");
            return result;
        }
    }

    @Override
    public Response<Paging<Coupon>> findBy(Coupon criteria, Integer pageNo, Integer size) {
        Response<Paging<Coupon>> result = new Response<Paging<Coupon>>();

        pageNo = firstNonNull(pageNo, 1);
        size = firstNonNull(size, 20);
        pageNo = pageNo <= 0 ? 1 : pageNo;
        size = size <= 0 ? 20 : size;

        PageInfo pageInfo = new PageInfo(pageNo, size);
        try {
            result.setResult(couponDao.findBy(criteria, pageInfo.getOffset(), pageInfo.getLimit()));
            return result;
        } catch (Exception e) {
            log.error("failed to find coupon by criteria {}, cause:{}", criteria, Throwables.getStackTraceAsString(e));
            result.setError("coupon.query.failed");
            return result;
        }
    }

    @Override
    public Response<Paging<Coupon>> findByNameAmountAndStatus(String name, Float amount, Integer status,
                                                              Integer pageNo, Integer size, BaseUser seller) {
        Response<Paging<Coupon>> result = new Response<Paging<Coupon>>();
        if (seller == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        Coupon criteria = new Coupon();
        if (!Strings.isNullOrEmpty(name)) {
            criteria.setName(name);
        }
        if (amount != null) {
            Integer amountI = (int) (amount.doubleValue() * 100);
            criteria.setAmount(amountI);
        }
        criteria.setStatus(status);

        criteria.setSellerId(seller.getId()); // imply the user confirmation
        result = findBy(criteria, pageNo, size);
        return result;
    }
}