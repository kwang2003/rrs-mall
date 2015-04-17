package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * 支付宝提现
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 2:40 PM  <br>
 * Author: xiao
 */
public interface AlipayCashService {


    /**
     * 根据条件查询所有查询支付宝提现记录
     *
     * @param sellerName    商户账户
     * @param startAt       确认起始时间
     * @param endAt         确认截止时间
     * @param summedAt      统计时间
     * @param filter        是否过滤订单数为0的订单
     * @param pageNo        页码
     * @param size          每页显示数量
     * @param user          用户信息
     * @return 符合条件的查询记录
     */
    Response<Paging<SellerAlipayCash>> findSellerAlipayCashesBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                                @ParamInfo("startAt") @Nullable String startAt,
                                                                @ParamInfo("endAt") @Nullable String endAt,
                                                                @ParamInfo("summedAt") @Nullable String summedAt,
                                                                @ParamInfo("filter") @Nullable Boolean filter,
                                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                @ParamInfo("size") @Nullable Integer size,
                                                                @ParamInfo("baseUser") BaseUser user);

    /**
     * 根据起止日期分页查询所有的支付宝提现记录
     *
     * @param startAt  开始日期
     * @param endAt    截止日期
     * @param pageNo   页码，从1开始
     * @param size     每页显示条目数
     * @return 符合条件的查询记录
     */
    Response<Paging<AlipayCash>> findBy(@ParamInfo("startAt") @Nullable String startAt,
                                        @ParamInfo("endAt") @Nullable String endAt,
                                        @ParamInfo("pageNo") @Nullable Integer pageNo,
                                        @ParamInfo("size") @Nullable Integer size,
                                        @ParamInfo("baseUser") BaseUser user);

}
