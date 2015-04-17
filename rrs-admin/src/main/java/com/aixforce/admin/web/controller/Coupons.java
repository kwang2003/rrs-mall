package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.rrs.coupon.service.CouponService;
import com.aixforce.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Effet on 4/23/14.
 */
@Controller
@RequestMapping("/api/admin/coupons")
public class Coupons {

    private final static Logger log = LoggerFactory.getLogger(Coupons.class);

    @Autowired
    private CouponService couponService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Coupon findById(@PathVariable("id") Long id) {
        Response<Coupon> result = couponService.findById(id);
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to find coupon where id = {}, error code:{}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }
}
