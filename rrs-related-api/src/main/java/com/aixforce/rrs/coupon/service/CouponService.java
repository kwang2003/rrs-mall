package com.aixforce.rrs.coupon.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.coupon.model.Coupon;
import com.aixforce.user.base.BaseUser;

import java.util.Date;
import java.util.List;

/**
 * Created by Effet on 4/21/14.
 */
public interface CouponService {

    Response<Coupon> create(Coupon coupon);

    Response<Coupon> create(String name, Integer amount, Integer useLimit,
                            Date startAt, Date endAt, Long sellerId);

    Response<Boolean> update(Coupon coupon);

    Response<Boolean> update(Coupon coupon, Long sellerId);

    /**
     * 商家未发布（INIT）时可修改优惠券信息
     * @param id       优惠券id
     * @param name     优惠券名
     * @param amount   面值
     * @param useLimit 使用限制
     * @param startAt  使用开始时间
     * @param endAt    结束时间
     * @param sellerId 商家id
     * @return 是否修改成功
     */
    Response<Boolean> update(Long id, String name, Integer amount, Integer useLimit,
                             Date startAt, Date endAt, Long sellerId);

    /**
     * 商家停发优惠券
     * @param id       优惠券id
     * @param sellerId 商家id
     * @return 是否挂起成功
     */
    Response<Boolean> suspend(Long id, Long sellerId);

    /**
     * 商家启用优惠券
     * @param id       优惠券id
     * @param sellerId 商家id
     * @return 是否启用成功
     */
    Response<Boolean> release(Long id, Long sellerId);

    Response<Coupon> findById(Long id);

    /**
     * 根据优惠券id查找
     * @param id     优惠券id
     * @param userId 查询的用户id
     * @return 优惠券
     */
    Response<Coupon> findById(Long id, Long userId);

    Response<List<Coupon>> findAllBy(Coupon criteria);

    Response<Paging<Coupon>> findBy(@ParamInfo("criteria") Coupon criteria,
                                    @ParamInfo("pageNo") Integer pageNo,
                                    @ParamInfo("size") Integer size);

    /**
     * 根据优惠券名称、面额、状态查询
     * @param name   优惠券名称
     * @param amount 面额
     * @param status 状态
     * @param pageNo
     * @param size
     * @param seller 商家
     * @return 优惠券分页列表
     */
    Response<Paging<Coupon>> findByNameAmountAndStatus(@ParamInfo("name") String name,
                                                       @ParamInfo("amount") Float amount,
                                                       @ParamInfo("status") Integer status,
                                                       @ParamInfo("pageNo") Integer pageNo,
                                                       @ParamInfo("size") Integer size,
                                                       @ParamInfo("seller") BaseUser seller);
}
