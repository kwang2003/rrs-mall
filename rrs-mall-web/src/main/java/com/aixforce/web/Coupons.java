package com.aixforce.web;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.rrs.coupon.model.CouponUsage;
import com.aixforce.rrs.coupon.service.CouponService;
import com.aixforce.rrs.coupon.service.CouponUsageService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.util.UserVerification;
import com.aixforce.web.misc.MessageSources;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Effet on 4/21/14.
 */
@Controller
@RequestMapping("/api/coupons")
public class Coupons {

    private final static Logger log = LoggerFactory.getLogger(Coupons.class);

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUsageService couponUsageService;

    @Autowired
    private MessageSources messageSources;

    /**
     * 商家创建优惠券
     * @param name
     * @param amount
     * @param useLimit
     * @param startAtS
     * @param endAtS
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Coupon createCouponBySeller(@RequestParam("name") String name,
                                       @RequestParam("amount") Integer amount,
                                       @RequestParam("useLimit") Integer useLimit,
                                       @RequestParam("startAt") String startAtS,
                                       @RequestParam("endAt") String endAtS) {
        if (!UserVerification.isSeller(UserUtil.getCurrentUser())) {
            log.warn("user has no permission to create coupon");
            throw new JsonResponseException(403, messageSources.get("authorize.fail"));
        }
        Date startAt, endAt;
        try {
            startAt = DFT.parseDateTime(startAtS).toDate();
            endAt = DFT.parseDateTime(endAtS).toDate();
        } catch (Exception e) {
            throw new JsonResponseException(400, messageSources.get("illegal.params"));
        }
        Response<Coupon> result = couponService.create(name, amount, useLimit, startAt, endAt, UserUtil.getUserId());
        if (!result.isSuccess()) {
            log.error("failed to create coupon {}, error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 优惠券信息
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Coupon findById(@PathVariable("id") Long id) {
        Response<Coupon> result = couponService.findById(id, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to find coupon where id = {}, error code:{}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 商家更新优惠券
     * @param id
     * @param name
     * @param amount
     * @param useLimit
     * @param startAtS
     * @param endAtS
     * @return
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean update(@PathVariable("id") Long id,
                          @RequestParam("name") String name,
                          @RequestParam("amount") Integer amount,
                          @RequestParam("useLimit") Integer useLimit,
                          @RequestParam("startAt") String startAtS,
                          @RequestParam("endAt") String endAtS) {
        Date startAt, endAt;
        try {
            startAt = DFT.parseDateTime(startAtS).toDate();
            endAt = DFT.parseDateTime(endAtS).toDate();
        } catch (Exception e) {
            throw new JsonResponseException(400, messageSources.get("illegal.params"));
        }
        Response<Boolean> result = couponService.update(id, name, amount, useLimit, startAt, endAt, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to create coupon {}, error code:{}", result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 商家停发优惠券（用户不能领取，已领取的可在规定时间内继续使用）
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/suspend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean suspend(@PathVariable("id") Long id) {
        Response<Boolean> result = couponService.suspend(id, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to suspend coupon(id = {}), error code:{}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 商家启用/发行优惠券
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/release", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean release(@PathVariable("id") Long id) {
        Response<Boolean> result = couponService.release(id, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to release coupon(id = {}), error code:{}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 商家所发行优惠券列表
     * @param name
     * @param amount
     * @param status
     * @param pageNo
     * @param size
     * @return
     */
    @RequestMapping(value = "/seller/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Coupon> findByNameAmountAndStatus(@RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "amount", required = false) Float amount,
                                                  @RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                                  @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Response<Paging<Coupon>> result = couponService.findByNameAmountAndStatus(name, amount, status, pageNo, size, UserUtil.getCurrentUser());
        if (result.isSuccess()) {
            return result.getResult().getData();
        }
        log.error("failed to find coupon by (name = {}, amount = {}, status = {}), error code:{}", name, amount, status, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 用户获得的优惠券列表
     * @param name
     * @param orderBy
     * @param pageNo
     * @param size
     * @return
     */
    @RequestMapping(value = "/buyer/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<CouponUsage> findByNameOrderByAmountOrEndTime(@RequestParam(value = "name", required = false) String name,
                                                              @RequestParam(value = "orderBy", defaultValue = "endAt") String orderBy,
                                                              @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
                                                              @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Response<Paging<CouponUsage>> result = couponUsageService.findByNameOrderByAmountOrEndTime(name, orderBy, pageNo, size, UserUtil.getCurrentUser());
        if (result.isSuccess()) {
            return result.getResult().getData();
        }
        log.error("failed to find couponUsage where name = {}, order by {}, error code:{}", name, orderBy, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * 用户获得优惠券
     * @param couponId
     * @return
     */
    @RequestMapping(value = "/buyer/obtain/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CouponUsage obtainACoupon(@PathVariable("id") Long couponId) {
        Response<CouponUsage> result = couponUsageService.obtainACoupon(couponId, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to getACoupon");
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }
}
