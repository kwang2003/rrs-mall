package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.DailySettlement;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 2:18 PM  <br>
 * Author: xiao
 */
public interface DailySettlementService {


    /**
     * 根据商户确认的起止日期来查询日结算汇总分页信息
     *
     * @param startAt   起始时间,必输项 yyyy-MM-dd
     * @param endAt     截止时间,必输项 yyyy-MM-dd
     * @return  满足条件的日结算信息列表，若查不到则返回空列表
     */
    Response<Paging<DailySettlement>> findBy(@ParamInfo("startAt") @Nullable String startAt,
                                         @ParamInfo("endAt") @Nullable String endAt,
                                         @ParamInfo("pageNo") @Nullable Integer pageNo,
                                         @ParamInfo("size") @Nullable Integer size,
                                         @ParamInfo("baseUser") BaseUser user);

}
