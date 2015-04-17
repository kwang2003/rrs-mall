package com.aixforce.admin.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.rrs.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by Effet on 4/26/14.
 */
@Slf4j
@Service
public class CouponJobService {

    @Autowired
    private CouponService couponService;

    public Response<Boolean> correctAllInvalid() {
        Response<Boolean> result = new Response<Boolean>();
        Response<List<Coupon>> couponsR = couponService.findAllBy(new Coupon()); //todo: too slow this method is
        if (!couponsR.isSuccess()) {
            result.setError(couponsR.getError());
            return result;
        }
        List<Coupon> coupons = couponsR.getResult();
        for (Coupon cp : coupons) {
            boolean isChanged = false;
            Date dateNow = DateTime.now().withTimeAtStartOfDay().toDate();
            boolean isBefore = dateNow.before(cp.getStartAt());
            boolean isAfter = dateNow.after(cp.getEndAt());
            if (cp.getStatus() == Coupon.Status.RELEASE.value()) {
                if (isBefore) {
                    // no need change
                } else if (isAfter) {
                    cp.setStatus(Coupon.Status.EXPIRE.value());
                    isChanged = true;
                } else {
                    cp.setStatus(Coupon.Status.VALID.value());
                    isChanged = true;
                }
            } else if (cp.getStatus() == Coupon.Status.VALID.value()) {
                if (isAfter) {
                    cp.setStatus(Coupon.Status.EXPIRE.value());
                    isChanged = true;
                }
            } else if (cp.getStatus() == Coupon.Status.SUSPEND.value()) {
                if (isAfter) {
                    cp.setStatus(Coupon.Status.EXPIRE.value());
                    isChanged = true;
                }
            }

            if (isChanged) {
                try {
                    couponService.update(cp);
                } catch (Exception e) {
                    log.error("Coupon update failed");
                    result.setError("coupon.update.failed");
                    return result;
                }
            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }
}
