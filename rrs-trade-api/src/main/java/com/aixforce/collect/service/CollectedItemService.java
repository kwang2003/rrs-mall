package com.aixforce.collect.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.collect.dto.CollectedBar;
import com.aixforce.collect.dto.CollectedItemInfo;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.model.CollectedItem;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 5:50 PM  <br>
 * Author: xiao
 */
public interface CollectedItemService {

    /**
     * 添加商品收藏记录
     *
     * @param userId    用户id
     * @param itemId    商品id
     */
    Response<CollectedSummary> create(Long userId, Long itemId,Long activityId);


    /**
     * 删除商品收藏记录
     *
     * @param userId    用户id
     * @param itemId    店铺id
     * @return  操作是否成功
     */
    Response<Boolean> delete(Long userId, Long itemId,Long activityId);

    /**
     * 删除商品收藏记录
     *
     * @param userId    用户id
     * @param id    店铺id
     * @return  操作是否成功
     */
    Response<Boolean> delete(Long userId, Long id);

    /**
     * 查询用户收藏的商品
     *
     * @param itemName  商品名称，选填，模糊匹配
     * @param pageNo    页码
     * @param size      分页大小
     * @param baseUser  查询用户
     * @return  收藏商品分页
     */
    Response<Paging<CollectedItemInfo>> findBy(@ParamInfo("itemName") @Nullable String itemName,
                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                           @ParamInfo("size") @Nullable Integer size,
                                           @ParamInfo("baseUser") BaseUser baseUser);



    /**
     *
     * 批量删除商品收藏记录
     *
     * @param userId    用户id
     * @param itemIds   商品id列表
     * @return  操作是否成功
     */
    Response<Boolean> bulkDelete(Long userId, List<Long> itemIds);


    /**
     * 商品收藏组件(显示在商品详情页)
     *
     * @param itemId    商品id
     * @return 商品id
     */
    @SuppressWarnings("unused")
    Response<Map<String,Long>> getBarOfItem(@ParamInfo("itemId") Long itemId,@ParamInfo("activityId") Long activityId);


    /**
     * 判断商品是否已收藏
     *
     * @param itemId    商品id
     * @param buyerId   买家id
     * @return  用户是否已经收藏这个商品
     */
    Response<CollectedBar> collected(@ParamInfo("itemId") Long itemId,
                                     @ParamInfo("buyerId") Long buyerId,@ParamInfo("activityId") Long activityId);

}
