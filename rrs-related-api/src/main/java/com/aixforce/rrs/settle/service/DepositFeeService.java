package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dto.TechFeeSummaryDto;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * 保证金（技术服务费）服务
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
public interface DepositFeeService {

    /**
     * 缴纳或者扣除保证金或技术服务费
     *
     * @param fee    保证金（技术服务费）对象
     * @param user   当前用户
     * @return  如果创建成功则返回id
     */
    Response<Long> create(DepositFee fee, BaseUser user);

    /**
     * 更新一行记录
     * @param updating      要更新的保证金信息
     * @param user          当前操作的用户
     * @return  更新成功的数量
     */
    Response<Long> update(DepositFee updating, BaseUser user);



    /**
     * 根据商户名字分页查询各种费用
     * 由组件调用
     * @param name      商家 name, 可为空
     * @param bid       类目 id, 可为空
     * @param pageNo    页码, 可为空
     * @param size      每页记录数, 可为空
     * @param user      当前操作的用户
     *
     * @return  符合条件的查询列表
     */
    @SuppressWarnings("unused")
    Response<Paging<DepositFee>> findDepositDetailByName(@ParamInfo("sellerName") @Nullable String name,
                                                         @ParamInfo("businessId") @Nullable Long bid,
                                                         @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                         @ParamInfo("size") @Nullable Integer size,
                                                         @ParamInfo("baseUser") BaseUser user);


    /**
     * 根据商家名字分页查询各种费用
     * 由组件调用
     * @param name      商家标识
     * @param bid       类目ID
     * @param pageNo    页码
     * @param size      每页显示条数
     * @param user      用户
     * @return  符合查询条件的分页对象
     */
    @SuppressWarnings("unused")
    Response<Paging<DepositFee>> findTechFeeDetailByName(@ParamInfo("sellerName") @Nullable String name,
                                                     @ParamInfo("businessId") @Nullable Long bid,
                                                     @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                     @ParamInfo("size") @Nullable Integer size,
                                                     @ParamInfo("baseUser") BaseUser user);


    /**
     * 查询商家的保证金列表
     * 由组件调用
     *
     * @param pageNo    页码
     * @param size      每页显示
     * @param user      用户
     * @return 基础费用单
     */
    @SuppressWarnings("unused")
    Response<Paging<DepositFee>> findBaseDetailByName(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                      @ParamInfo("size") @Nullable Integer size,
                                                      @ParamInfo("baseUser") BaseUser user);


    /**
     * 根据ID找到实体
     *
     * @param id    标识
     * @return 保证金明细实体
     */
    Response<DepositFee> findDepositDetailByID(@ParamInfo("id") Long id);


    /**
     * 统计卖家已缴纳的技术服务费
     *
     * @param user  用户信息
     * @return 技术服务费汇总
     */
    Response<TechFeeSummaryDto> summaryOfTechFee(@ParamInfo("baseUser") BaseUser user);

}
