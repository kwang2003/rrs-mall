package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 9:54 AM  <br>
 * Author: xiao
 */
public interface OrderAlipayCashService {

    /**
     * 根据起止日期来查询该订单提现明细分页信息 <br/>
     * 若为空则查询所有商户信息（此项操作仅运营人能可以执行）
     *
     * @param sellerName            商家名称
     * @param startAt               查询起始日期（基于交易日期）
     * @param endAt                 查询截止日期（基于交易日期）
     * @param cashedAt              查询指定某一天的日期（基于提现日期）
     * @param tradedAt              查询指定某一天的日期（基于交易日期）
     * @param pageNo                页码
     * @param size                  每页大小
     * @param user                  用户
     * @return  订单提现分页明细
     */
    Response<Paging<OrderAlipayCash>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                             @ParamInfo("orderId") @Nullable Long orderId,
                                             @ParamInfo("type") @Nullable Integer type,
                                             @ParamInfo("status") @Nullable Integer status,
                                             @ParamInfo("startAt") @Nullable String startAt,
                                             @ParamInfo("endAt") @Nullable String endAt,
                                             @ParamInfo("cashedAt") @Nullable String cashedAt,
                                             @ParamInfo("tradedAt") @Nullable String tradedAt,
                                             @ParamInfo("pageNo") @Nullable Integer pageNo,
                                             @ParamInfo("size") @Nullable Integer size,
                                             @ParamInfo("baseUser") BaseUser user);


    /**
     * 确认提现
     *
     * @param id    提现明细的id
     * @param user  用户
     * @return  是否操作成功
     */
    Response<Boolean> cashing(Long id, BaseUser user);


    /**
     * 批量确认提现
     *
     * @param ids   提现明细的id列表
     * @param user  用户
     * @return  是否操作成功
     */
    Response<Boolean> batchCashing(List<Long> ids, BaseUser user);


}
