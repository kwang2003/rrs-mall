package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.RichOrderCommentForBuyer;
import com.aixforce.trade.dto.RichOrderCommentForSeller;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 仅通过 Controller 调用，不暴露 sevice
 * Date: 14-2-12
 * Time: PM2:21
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */


public interface OrderCommentService {

    /**
     * 用户创建订单的评价
     *
     * @param   orderId 主订单ID
     * @param   comment 评价对象
     * @param   userId 评论者id
     * @return  成功创建的评价 id
     */
    public Response<List<Long>> create(Long orderId, List<OrderComment> comment, Long userId);

    /**
     * 根据评价对象的 id，返回评价具体内容
     * @param   id 评价对象的 id
     * @return  指定 ID 的评价对象
     */
    public Response<OrderComment> findById(Long id);

    /**
     * 根据子订单对象的 ID 返回评价
     * @param   id  子订单ID
     * @return      OrderComment 对象
     */
    public Response<OrderComment> findByOrderItemId(Long id);

    /**
     * 根据店铺的ID查找评价
     *
     * @param ids    店铺的ID列表
     * @return      对应的 OrderComment 对象
     */
    public Response<Boolean> sumUpCommentScoreByShopIds(List<Long> ids);

    /**
     * 根据店铺的ID查找店铺昨天所有评价
     *
     * @param ids    店铺的ID列表
     * @return      店铺积分是否更新成功
     */
    public Response<Boolean> sumUpCommentScoreYestDayByShopIds(List<Long> ids);

    /**
     * 根据店铺的ID查找评价 并对该店铺评价积分进行全量统计
     *
     * @param id 店铺的ID
     * @return      对应的 店铺评价积分更新是否成功
     */
    public Response<Boolean> sumUpCommentScoreByShopId(Long id);

    /**
     * 根据卖家 ID 返回分页的评介
     * @param pageNo
     * @param size
     * @param currentSeller 自动注入当前用户，买家或者管理员
     * @return 分页的详细订单评价
     */
    public Response<Paging<RichOrderCommentForSeller>> findBySellerId(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                         @ParamInfo("size") @Nullable Integer size,
                                                         @ParamInfo("baseUser") @Nullable BaseUser currentSeller);

    /**
     * 根据买家 ID 返回分页的评价
     * @param pageNo
     * @param size
     * @param currentBuyer 自动注入当前的用户
     * @return  分页的详细订单评价
     */
    public Response<Paging<RichOrderCommentForBuyer>> findByBuyerId(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                    @ParamInfo("size") @Nullable Integer size,
                                                                    @ParamInfo("baseUser") @Nullable BaseUser currentBuyer);

    /**
     * 分页查看评价列表
     * @param   pageNo
     * @param   size
     * @return  分页后的评价列表
     */
    public Response<Paging<OrderComment>> viewItemComments(@ParamInfo("itemId") Long itemId,
                                               @ParamInfo("pageNo") @Nullable Integer pageNo,
                                               @ParamInfo("size") @Nullable Integer size);



    /**
     * 由 OrderCommentJobService 调用
     * @param avaliableOI   过评论期未评论订单ID
     * @return              未评论可关闭的订单列表
     */
    public Response<List<Long>> createCommentForExpiredOrderItem(List<OrderItem> avaliableOI);


    /**
     * 由 OrderCommentJobService 调用
     * 返回昨天新增的评价
     *
     * @param shopId    店铺的ID
     * @return 新增评价列表
     */
    public Response<List<OrderComment>> getYesterdayCommentForShop(Long shopId);

    /**
     * 由 OrderCommentJobService 调用
     * 返回昨天新增的评价
     *
     * @return 新增评价列表
     */
    public Response<List<OrderComment>> getYesterdayComment();

    /**
     * 得到昨天所有的评价订单4种评价分统计根据shopid分组
     * @return 评价订单集合
     */
    public Response<List<OrderComment>> sumUpForYesterdayGroupByShop();

    /**
     * 分页获取用来统计的评论。
     *
     * @param lastId    上次获取的最后一个评论的id
     * @param limit     每次获取评论的数量
     * @return          评论列表
     */
    Response<List<OrderComment>> forDump(Long lastId, Integer limit);

    /**
     * 分页获取用来关闭评论的id列表。
     *
     * @param lastId    上次获取的最后一个评论的id
     * @param limit     每次获取评论的数量
     * @return          评论的订单id列表
     */
    Response<List<Long>> forExpire(Long lastId, Integer limit);

    /**
     * 获取最后一个评论的id
     *
     * @return  最有一个评论的id
     */
    Response<Long> maxId();

    /**
     * 重新计算某一个店铺的评分
     * @param shopId 店铺id
     */
    Response<Boolean> shopDump(Long shopId);

    /**
     * 全量sum店铺的评价积分
     * @param shopId 店铺id
     * @return sum后OrderComment
     */
    Response<OrderComment> sumUpShopScore(Long shopId);


    /**
     * sum店铺昨天的评价积分
     * @param shopId 店铺id
     * @return sum后OrderComment
     */
    Response<OrderComment> sumUpShopScoreYesterday(Long shopId);


    /**
     * 更新评价回复
     * @param id 评价记录id
     * @param commentReply 回复内容
     * @return 是否更新成功 true 更新成功 false 更新失败
     */
    public Response<Boolean> updateCommentReply(Long id,String commentReply);
}
