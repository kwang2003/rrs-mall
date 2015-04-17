package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-20 12:50 PM  <br>
 * Author: xiao
 */
public interface DepositFeeCashService {


    /**
     * 查询列表
     *
     * @param sellerName        账户名称,可以为空
     * @param createdStartAt    创建起始时间,可以为空
     * @param createdEndAt      创建截止时间,可以为空
     * @return  基础费用提现记录分页记录
     */
    Response<Paging<DepositFeeCash>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                            @ParamInfo("id") @Nullable Long id,
                                            @ParamInfo("status") @Nullable Integer status,
                                            @ParamInfo("startAt") @Nullable String createdStartAt,
                                            @ParamInfo("endAt") @Nullable String createdEndAt,
                                            @ParamInfo("pageNo") @Nullable Integer pageNo,
                                            @ParamInfo("size") @Nullable Integer size,
                                            @ParamInfo("baseUser")BaseUser user);


    /**
     * 标记基础金提现为“已提现”
     *
     * @param id        要更新的保证金提现记录id
     * @param user      当前操作的用户
     * @return   执行结果
     */
    Response<Boolean> cashing(Long id, BaseUser user);

}
