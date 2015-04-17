package com.aixforce.collect.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.model.CollectedItem;
import com.aixforce.collect.model.CollectedShop;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 5:50 PM  <br>
 * Author: xiao
 */
public interface CollectedShopService {


    /**
     * 添加商品收藏记录
     *
     * @param userId    用户id
     * @param shopId    店铺id
     * @return  操作是否成功
     */
    Response<CollectedSummary> create(Long userId, Long shopId);



    /**
     * 删除商品收藏记录
     *
     * @param userId    用户id
     * @param shopId    店铺id
     * @return  操作是否成功
     */
    Response<Boolean> delete(Long userId, Long shopId);


    /**
     * 查询用户收藏的店铺
     *
     * @param shopName  店铺名称，选填，模糊匹配
     * @param pageNo    页码
     * @param size      分页大小
     * @param baseUser  查询用户
     * @return  收藏店铺分页
     */
    Response<Paging<CollectedShop>> findBy(@ParamInfo("shopName") @Nullable String shopName,
                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                           @ParamInfo("size") @Nullable Integer size,
                                           @ParamInfo("baseUser") BaseUser baseUser);


    /**
     *
     * 批量删除店铺收藏记录
     *
     * @param userId    用户id
     * @param shopIds   商品id列表
     * @return  操作是否成功
     */
    Response<Boolean> bulkDelete(Long userId, List<Long> shopIds);



}
