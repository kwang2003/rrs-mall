package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.HaierOrder;
import com.aixforce.trade.dto.ItemBundlePreOrder;
import com.aixforce.trade.dto.OrderDescription;
import com.aixforce.trade.dto.OrderItemTotalFee;
import com.aixforce.trade.dto.RichOrderBuyerView;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.trade.dto.RichOrderSellerView;
import com.aixforce.trade.dto.RichOrderWithDetail;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderExtra;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.user.base.BaseUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-10
 */
public interface OrderQueryService {

    /**
     * 根据id查找订单
     *
     * @param id 订单id
     * @return 订单
     */
    public Response<Order> findById(@ParamInfo("id") Long id);

    /**
     * 根据origin id查找订单
     *
     * @param id 订单原始id
     * @return 订单
     */
    public Response<Order> findByOriginId(Long id);


    /**
     * 根据id列表查找订单列表
     * @param ids id列表
     * @return  订单列表
     */
    public Response<List<Order>> findByIds(List<Long> ids);

    /**
     * 根据 criteria 查找订单，不会被组件直接调用
     *
     * @param criteria           查询条件
     * @param pageNo             页码
     * @param size               返回条数
     * @param createdStartAt     创建起始时间
     * @param createdEndAt       创建截止时间
     * @return 订单列表
     */
    public Response<Paging<Order>> findBy(Order criteria, Integer pageNo, Integer size, Date createdStartAt, Date createdEndAt);

    /**
     * 运营查询订单，按订单类型、行业（频道）、商家帐号、订单状态
     * important: admin 不能按照子订单状态筛选
     *
     * @param type 订单类型 可选
     * @param business 行业 可选
     * @param sellerName 商家帐号 可选
     * @param orderId 订单编号
     * @param status 主订单状态 可选
     * @param pageNo 起始页码 可选，默认 1
     * @param count 数量 可选，默认 20
     * @return 查询结果
     */
    public Response<Paging<RichOrderSellerView>> adminFind(@ParamInfo("type") Integer type,
                                             @ParamInfo("business") Long business,
                                             @ParamInfo("sellerName") String sellerName,
                                             @ParamInfo("orderId") Long orderId,
                                             @ParamInfo("itemId") Long itemId,
                                             @ParamInfo("status") Integer status,
                                             @ParamInfo("pageNo") Integer pageNo,
                                             @ParamInfo("count") Integer count);

    /**
     * 买家查看已买到的宝贝
     *
     * @param baseUser 买家,系统自动注入
     * @param pageNo   起始页码
     * @param count    数量
     * @return 从买家角度来看的订单列表
     */
    Response<Paging<RichOrderBuyerView>> findByBuyerId(@ParamInfo("baseUser") BaseUser baseUser,
                                                       @ParamInfo("pageNo") Integer pageNo,
                                                       @ParamInfo("count") Integer count,
                                                       @ParamInfo("status") Integer status,
                                                       @ParamInfo("orderId") Long orderId);

    /**
     * 卖家查看已卖出的宝贝
     *
     * @param baseUser 卖家,系统自动注入
     * @param pageNo   起始页码
     * @param count    数量
     * @param status   订单状态
     * @param orderId  订单编号
     * @return 从卖家角度来看的订单列表
     */
    Response<Paging<RichOrderSellerView>> findBySellerId(@ParamInfo("baseUser") BaseUser baseUser,
                                                         @ParamInfo("pageNo") Integer pageNo,
                                                         @ParamInfo("count") Integer count,
                                                         @ParamInfo("status") Integer status,
                                                         @ParamInfo("orderId") Long orderId);

    /**
     * 根据订单id查看收货信息
     *
     * @param baseUser 系统自动注入
     * @param orderId  订单id
     * @return 订单收获信息
     */
    @SuppressWarnings("unused")
    Response<UserTradeInfo> findUserTradeInfoByOrderId(@ParamInfo("baseUser") BaseUser baseUser,
                                                       @ParamInfo("orderId") Long orderId);


    /**
     * 卖家查看订单详情,包括发票和买家留言等信息
     *
     * @param seller 卖家
     * @param id     订单id
     * @return 订单详情
     */
    @SuppressWarnings("unused")
    Response<RichOrderWithDetail> sellerFindOrderById(@ParamInfo("seller") BaseUser seller, @ParamInfo("id") Long id);

    /**
     * 买家查看订单详情,包括发票和买家留言等信息
     *
     * @param buyer 买家
     * @param id    订单id
     * @return 订单详情
     */
    @SuppressWarnings("unused")
    Response<RichOrderWithDetail> buyerFindOrderById(@ParamInfo("buyer") BaseUser buyer, @ParamInfo("id") Long id);

    /**
     * 查看子订单详情
     *
     * @param orderItemId 子订单id,如果是预售订单，为尾款id
     * @return 子订单
     */
    Response<OrderItemTotalFee> findExtraByOrderItemId(@ParamInfo("orderItemId") Long orderItemId);

    /**
     * 查看子订单详情
     *
     * @param orderId 子订单id,如果是预售订单，为尾款id
     * @return 子订单
     */
    Response<OrderItemTotalFee> findDepositExtraByOrderItemId(@ParamInfo("orderId") Long orderId);

    /**
     * 根据OrderID查询OrderItem信息
     *
     * @param orderId 子订单id,如果是预售订单，为尾款id
     * @return 子订单
     */
    OrderItem findByMap(@ParamInfo("orderId") Long orderId,@ParamInfo("type") int type);

    /**
     * 根据订单号获取子订单号信息
     *
     * @param orderId 订单号
     * @return  子订单号列表
     */
    @SuppressWarnings("unused")
    Response<List<OrderItem>> findSubsByOrderId(Long orderId);


    /**
     * 根据订单列表获取所有子订单信息
     *
     * @param ids 订单列表
     * @return  子订单列表
     */
    @SuppressWarnings("unused")
    Response<List<OrderItem>> findSubsInOrderIds(Long... ids);

    /**
     * 根据订单号获取子订单详情消息用来评价
     *
     * @param orderId  订单号
     * @param baseUser 当前用户
     * @return  子订单详情消息
     */
    @SuppressWarnings("unused")
    public Response<List<RichOrderItem>> findOrderItemsByOrderIdForComment(@ParamInfo("orderId") Long orderId,
                                                                           @ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 获取在指定日期内完成的订单数量
     *
     * @param finishedAt 订单完成日期
     * @return 符合条件的订单数量，若没有则返回0
     */
    @SuppressWarnings("unused")
    Response<Long> countOfFinishedOrder(Date finishedAt);

    /**
     * 获取在指定日期内完成的订单数量
     *
     *
     * @param startAt  查询开始时间
     * @param endAt    查询截止时间
     * @param pageNo   页码
     * @param count    返回条数
     * @return 已完成订单列表
     */
    @SuppressWarnings("unused")
    Response<Paging<Order>> findByFinishAt(Date startAt, Date endAt, Integer pageNo, Integer count);


    /**
     *
     * 获取指定日期内更新的订单列表，封装成Page<HaierOrder>对象返回
     *
     * @param beginAt       查询开始时间
     * @param endAt         查询截止时间
     * @param businesses    行业范围筛选
     * @param pageNo        页码
     * @param size          返回条数
     * @return  更新的订单列表
     */
    Response<Paging<HaierOrder>> findHaierOrderByUpdatedAt(Date beginAt, Date endAt, List<Long> businesses,
                                                           Integer pageNo, Integer size);

    /**
     *
     * 获取指定卖家ID并且在指定日期内更新的订单列表，封装成Page<HaierOrder>对象返回
     *
     * @param beginAt       查询开始时间
     * @param endAt         查询截止时间
     * @param businesses    行业范围筛选
     * @param pageNo        页码
     * @param sellerIds    卖家ID
     * @param size          返回条数
     * @return  更新的订单列表
     */
    Response<Paging<HaierOrder>> findHaierOrderByUpdatedAtAndSellerIds(Date beginAt, Date endAt, List<Long> businesses,
                                                                       Integer pageNo, Integer size, List<Long> sellerIds);

    /**
     * 获取指定id的订单，封装成HaierOrder返回
     *
     * @param id    订单号
     * @return      订单
     */
    Response<HaierOrder> findHaierOrderById(Long id);

    /**
     * 根据订单id查找所有子订单
     * @param orderId 订单id
     * @return 子订单列表
     */
    Response<List<OrderItem>> findOrderItemByOrderId(Long orderId);

    /**
     * 由 OrderCommentJob 调用
     * 找到未评论过的已经过可评论时间的子订单
     * 根据 OrderID 分页
     * @param expireDays      订单可评论时长（默认15天）
     * @return 子订单列表
     */
    Response<List<OrderItem>> findExpiredUncommentedOrderItemId(Integer expireDays);

    /**
     * 根据子订单id找自订单
     * @param orderItemId 子订单id
     * @return 子订单
     */
    Response<OrderItem> findOrderItemById(Long orderItemId);

    /**
     * 根据子订单原始id找自订单
     * @param orderItemId 子订单原始id
     * @return 子订单
     */
    Response<OrderItem> findOrderItemByOriginId(Long orderItemId);

    /**
     * 根据组合商品id查找订单预览信息
     * @param id 组合商品id
     * @return 订单预览页
     */
    @SuppressWarnings("unused")
    Response<ItemBundlePreOrder> findItemBundlePreOrder(@ParamInfo("itemBundleId")Long id, @ParamInfo("skus") String skus);

    /**
     * 汇总指定商户某一天的退款金额
     *
     * @param sellerId  商户id
     * @param refundAt  退款时间
     * @return  退款总金额
     */
    Response<Long> sumRefundAmountOfSellerInDate(Long sellerId, Date refundAt);


    /**
     * todo 分页查询指定日期内退款的子订单
     *
     * @param refundAt  指定日期
     * @param pageNo    页码
     * @param size      每页大小
     * @return  分页信息
     */
    Response<Paging<OrderItem>> findRefundedOrderItemInDate(Date refundAt, Integer pageNo, Integer size);

    /**
     * 获取订单的描述信息
     *
     * @param ids   订单id列表
     * @return  订单的描述信息
     */
    Response<OrderDescription> getDescriptionOfOrders(List<Long> ids);


    /**
     * 下面4个方法都是为了订单超时任务暴露出来的
     */
    Response<Long> maxId();

    Response<Long> maxIdOfOrderItem();

    Response<List<Order>> findNotConfirmDeliverOrder(Long lastId, String startAt, String endAt, Integer size);

    Response<List<OrderItem>> findNotConfirmRefund(Long lastId, String startAt, String endAt, Integer size);

    /**
     * 根据订单id查找OrderExtra
     * @param orderId 订单id
     */
    Response<OrderExtra> getOrderExtraByOrderId(Long orderId);

    /**
     * 创建OrderExtra
     */
    Response<Long> createOrderExtra(OrderExtra orderExtra);

    /**
     * 更新OrderExtra
     */
    Response<Boolean> updateOrderExtra(OrderExtra orderExtra);
    /**
     * 判断商家或者用户是否启用短信通知
     * @param map userID,userType
     * @return
     */
    public boolean isUserStatus(Map<String, Object> map);
    /**
     * 
     * @return 获取预售活动结束所有用户的手机号
     */
    public List<Map<String, Object>> getMoblieList();
    /**
     * 修改预售商品发送短信状态
     */
    public Integer updateSmsFloag(Map<String, Object> map);


    /**
     * 取得押金试用失联（>=45天）的订单
     * @param ids id列表
     * @return  订单列表
     */
    public Response<List<Long>> findOnTrialTimeOutOrder(List<Long> ids);

    /**
     * 根据优惠券Id 查询优惠券对应的订单信息
     * **/
    Map<String, Object> queryOrderCouponsByCouponsId(@ParamInfo("baseUser") BaseUser baseUser, @ParamInfo("pageNo") Integer pageNo, @ParamInfo("count") Integer size, @ParamInfo("couponsId") Long couponsId);

    /**
     * 查看同一地址是否多次下单
     * @return  是否多次下单
     */
    public Response<Boolean> containByTradeInfoId(Long userId, Long tradeInfoId);
}
