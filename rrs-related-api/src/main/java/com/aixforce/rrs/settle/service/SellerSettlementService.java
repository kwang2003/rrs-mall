package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dto.PrintableSettlementDto;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 2:18 PM  <br>
 * Author: xiao
 */
public interface SellerSettlementService {

    /**
     * 根据商户确认的起止日期来查询该商户日结算汇总分页信息 <br/>
     * 若为空则查询所有商户信息（此项操作仅运营和财务能可以执行）
     *
     * @param sellerName            商户名称,可为空
     * @param confirmedStartAt      起始时间(基于创建时间),可为空
     * @param confirmedEndAt        截止时间(基于创建时间),可为空
     * @param confirmedAt           创建时间(基于创建时间),可为空，如填写则此项则startAt,endAt失效
     * @param filter                是否过滤订单数为0的汇总
     * @param pageNo                页码，从1开始
     * @param size                  每页显示条目数
     * @return  满足条件的日结算信息列表，若查不到则返回空列表
     */
    Response<Paging<SellerSettlement>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                             @ParamInfo("startAt") @Nullable String confirmedStartAt,
                                             @ParamInfo("endAt") @Nullable String confirmedEndAt,
                                             @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                             @ParamInfo("filter") @Nullable Boolean filter,
                                             @ParamInfo("pageNo") @Nullable Integer pageNo,
                                             @ParamInfo("size") @Nullable Integer size,
                                             @ParamInfo("baseUser") BaseUser user);


    /**
     * 获取商家日汇总记录（结算单）
     *
     * @param id    日汇总id
     * @param user  当前操作用户
     * @return  商家日汇总记录
     */
    Response<PrintableSettlementDto> get(@ParamInfo("id") Long id,
                                   @ParamInfo("baseUser") BaseUser user);


    /**
     * 标记指定的商户日汇总记录为"已打印"
     *
     * @param id    日汇总记录id
     * @param user  当前用户
     * @return   是否操作成功
     */
    Response<Boolean> printing(Long id, BaseUser user);



}
