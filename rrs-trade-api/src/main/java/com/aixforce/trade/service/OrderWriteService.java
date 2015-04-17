package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.FatOrder;
import com.aixforce.trade.dto.ItemBundleFatOrder;
import com.aixforce.trade.dto.OrderIdAndEarnestId;
import com.aixforce.trade.dto.OrderLogisticsInfoDto;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderExtra;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.user.base.BaseUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
public interface OrderWriteService{
    /**
     * 创建订单
     *
     * @param buyerId     买家id
     * @param tradeInfoId 收货地址id
     * @param fatOrders   预订单
     * @param skuIdAndDiscount skuId和优惠k-v对
     * @return 返回sellerId 和创建的订单id的对应关系
     */
    public Response<Map<Long,Long>> create(Long buyerId, Long tradeInfoId,
                                           List<FatOrder> fatOrders,Map<Long,Integer> skuIdAndDiscount,String bank);

    /**
     * 创建组合商品订单
     * @param buyerId       买家id
     * @param tradeInfoId   收货地址id
     * @param itemBundleFatOrder  预订单
     * @return  新创建的订单id
     */
    public Response<Long> createItemBundle(Long buyerId, Long tradeInfoId, ItemBundleFatOrder itemBundleFatOrder,String bank);

    /**
     * 为订单付款
     *
     * @param orderId 订单id
     * @param paymentCode 支付宝交易号
     * @return 是否付款成功
     */
    public Response<Boolean> normalOrderPaid(Long orderId, String paymentCode, Date paidAt);

    /**
     * 多个订单同时付款
     * @param orderIds 订单id列表
     * @param paymentCode 支付宝交易号
     * @return 是否付款成功
     */
    public Response<Boolean> batchNormalOrderPaid(List<Long> orderIds, String paymentCode, Date paidAt);

    /**
     * 卖家发货
     * @param order 订单
     * @param orderLogisticsInfoDto 订单物流信息
     * @return 是否发货成功
     */
    public Response<Boolean> deliver(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto, BaseUser user);


    /**
     * 买家确认收获
     *
     * @param order 订单
     * @param buyerId 确认者id
     * @return 是否确认成功
     */
    public Response<Boolean> confirm(Order order, Long buyerId);


    /**
     * 取消子订单,卖家同意退货，卖家收到退货
     *
     * @param orderItemId 子订单id
     * @return 是否取消成功
     */
    public Response<Boolean> cancelOrderItem(Long orderItemId);

    /**
     * 取消订单
     * @param order 订单id
     * @param user  用户
     * @return    是否取消成功
     */
    public Response<Boolean> cancelOrder(Order order, BaseUser user);

    /**
     * 拒绝退款或者拒绝退货
     *
     * @param orderItemId 子订单id
     * @param user  当前登录用户,该接口只有卖家能调用
     * @return 操作是否成功
     */
    public Response<Boolean> refuse(Long orderItemId, BaseUser user);

    /**
     * 卖家同意退货
     * @param orderItemId 子订单id
     * @param user   当前登录用户id，该接口只有卖家能调用
     * @return 操作是否成功
     */
    public Response<Boolean> agreeReturnGoods(Long orderItemId, BaseUser user);

    /**
     * 卖家同意退货-押金预授权订单
     * @param orderId 子订单id
     * @param user   当前登录用户id，该接口只有卖家能调用
     * @return 操作是否成功
     */
    public Response<Boolean> agreeReturnDeposit(Long orderId, BaseUser user);

    /**
     * 买家撤销退款或者退货申请
     * @param orderItemId 子订单id
     * @param user  当前登录用户id， 只有买家能调用
     * @return  操作是否成功
     */
    public Response<Boolean> undoRequest(Long orderItemId, BaseUser user);

    /**
     * 买家添加退货款理由和退款金额
     *
     * @param orderItemId  子订单id
     * @param user       当前登录用户，该接口只有买家能调用
     * @param reason       申请理由
     * @param refundAmount 申请退款金额
     * @return 是否添加成功
     */
    public Response<Boolean> addReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount);

    /**
     * 开放服务给rrs调用,抛出异常
     * @param order 订单
     * @return 创建后的orderId
     */
    public  Response<Long> createOrder(Order order);

    public  Response<Long> createOrderItem(OrderItem orderItem);

    public void createOrderExtra(OrderExtra orderExtra);

    /**
     * 更新订单状态，内部调用
     *
     * @param id        订单id
     * @param status    订单状态
     * @return  操作是否成功
     */
    Response<Boolean> updateStatus(Long id, Integer status);

    /**
     * 付定金或者尾款
     * @param orderItemId 子订单id
     * @param paymentCode 支付宝交易号
     * @param paidAt 付款时间
     * @return 操作是否成功
     */
    Response<Boolean> preSalePay(Long orderItemId, String paymentCode, Date paidAt);

    /**
     * 预售申请退款退货，需要分2笔钱到2个子订单
     * @param orderItemId 子订单id
     * @param user        当前登录用户id， 只有买家能调用
     * @param reason      退款理由
     * @param refundAmount 退款金额
     * @return 操作是否成功
     */
    Response<Boolean> preSaleAddReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount);

    /**
     * 押金申请退款退货，需要分2笔钱到2个子订单
     * @param orderItemId 子订单id
     * @param user        当前登录用户id， 只有买家能调用
     * @param reason      退款理由
     * @param refundAmount 退款金额
     * @return 操作是否成功
     */
    Response<Boolean> depositAddReasonAndRefund(Long orderItemId, BaseUser user, String reason, Integer refundAmount);

    /**
     * 退普通订单货款
     *
     * @param refundAt      退款时间
     * @param orderItem     子订单
     */
    Response<Boolean> refundPlainOrderItem(Date refundAt, OrderItem orderItem);

    /**
     * 退预售订单货款
     *
     * @param refundAt  退款时间
     * @param deposit   定金
     * @param rest      尾款
     */
    Response<Boolean> refundPresellOrderItem(Date refundAt, OrderItem deposit, OrderItem rest);

    /**
     * 更新预售总订单和属于它的子订单的状态
     * @param orderId 订单id
     * @param status  更新的状态
     * @return 操作是否成功
     */
    Response<Boolean> updateOrderAndOrderItems(Long orderId, Integer status);

    /**
     * 判断订单是否超时
     */
    Response<Boolean> verifyOrderExpire(Date date);

    /**
     * 判断订单超时未付款
     */
    Response<Boolean> verifyOrderNotPaidExpire(Date date);

    /**
     * 让某个订单失效
     * @param id    订单号
     */
    Response<Boolean> expireOrder(Long id);

    /**
     * 实现更改运费的处理（针对于子订单的运费更改）
     * @param sellerId      需要验证一下用户的权限
     * @param orderItemId   子订单编号
     * @param newFee        新的运费价格
     * @return  Boolean
     * 返回更改是否成功
     */
    Response<Boolean> updateDeliverFee(Long sellerId, Long orderItemId , Integer newFee);


    /**
     * 更新电子发票信息
     *
     * @param orderId       订单id
     * @param invoiceNo     电子发票号
     * @param url           电子发票地址
     * @return  更改是否成功
     */
    Response<Boolean> updateElectInvoice(Long orderId, String invoiceNo, String url);


    /**
     * 更新订单及子订单信息，仅供数据订正程序使用
     *
     * @param updating      需要更新的订单信息
     * @param orderItems    需要更新的子订单信息
     * @return 更改是否成功
     */
    Response<Boolean> update4Fix(Order updating, List<OrderItem> orderItems);

    /**
     * 下面4个方法都是为了执行订单超时暴露出来的
     */
    Response<Boolean> updateOrder(Order order);

    Response<Boolean> bathUpdateOrderItemStatusByOrderId(Integer fromStatus, Integer toStatus, Long orderId, String paymentCode);

    Response<Boolean> updateOrderItem(OrderItem orderItem);

    Response<Boolean> cancelOrder(Long orderId, Integer toStatus);

    /**
     * 重置一个子订单的id
     * @param oi        需要重置的子订单
     * @param channel   渠道
     * @return 重置后的子订单
     */
    Response<OrderItem> resetOrderItem(OrderItem oi, String channel);

    /**
     * 重置一个订单的id
     * @param order        需要重置的订单
     * @param channel   渠道
     * @return 重置后的子订单
     */
    Response<Order> resetOrder(Order order, String channel);
    /**
     * 更新子订单信息
     *
     * @return 更改是否成功
     */
    void updateRrsOrderItem(OrderItem orderItems);

    /**
     * 创建预售订单，保证事务
     * @param order 总订单
     * @param earnest 定金子订单
     * @param remain 尾款子订单
     * @param orderExtra 订单其他信息
     * @return 创建成功的订单id和定金id
     */
    Response<OrderIdAndEarnestId> preSaleOrderCreate(Order order, OrderItem earnest, OrderItem remain, OrderExtra orderExtra);

    /**
     * 创建抢购订单，保证事务
     * @param order  总订单
     * @param orderItem 子订单
     * @param orderExtra 订单其他信息
     * @return 创建成功的订单id
     */
    Response<Long> buyingOrderCreate(Order order, OrderItem orderItem, OrderExtra orderExtra);

    /**
     * 创建试金行动订单
     *
     * @param buyerId     买家id
     * @param tradeInfoId 收货地址id
     * @param fatOrders   预订单
     * @param skuIdAndDiscount skuId和优惠k-v对
     * @return 返回sellerId 和创建的订单id的对应关系
     */
    public Response<Map<Long,Long>> createForSku(Long buyerId, Long tradeInfoId,
                                           List<FatOrder> fatOrders,Map<Long,Integer> skuIdAndDiscount,String bank);

    /**
     * 更新退款订单状态，外部调用
     *
     * @param tempReturnId     临时逆向订单id
     * @param status 订单状态
     * @return 操作是否成功
     */
    public Response<Boolean> updateStatusForTempReturn(String tempReturnId, Integer status);

    /**
     * 按指定的订单编号将订单状态更新为未付款交易关闭
     * @param orderIds
     * @return
     */
    public Response<Boolean> notPaidExpire(List<Long> orderIds);

    /**
     * 退预授权订单货款
     *
     * @param refundAt      退款时间
     * @param orderItem     子订单
     */
    Response<Boolean> refundPlainOrderItem2(Date refundAt, OrderItem orderItem);

    /**
     * 卖家发货
     * @param order 订单
     * @param orderLogisticsInfoDto 订单物流信息
     * @return 是否发货成功
     */
    public Response<Boolean> deliver2(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto, BaseUser user);

    /**
     * 试金购买，退订
     * @param orderId 订单号
     * @return 是否发货成功
     */
    public Response<Boolean> updateOrderCallBack(Long orderId) ;

    Response<Boolean> updateOrderItemType(OrderItem orderItem);

    /**
     * @param orderId 订单号
     * @return 是否试金订单
     */
    public boolean checkPreDeposit(Long orderId);

    /**
     * @param orderId 订单号
     * @return 试金是否购买回调
     */
    public Boolean checkPreDepositPayOrBack(Long orderId);

    /**
     * 标记orders / orderItems 表中的支付平台和支付渠道
     * @param orders
     * @param orderItems
     * @param isPreSale 0 普通订单 1 预售/试用订单
     * @return
     */
    public Response<Boolean> setPaymentPlatform(List<Order> orders, List<OrderItem> orderItems, int isPreSale);

    /**
     * 微信退款
     * @param orderItem
     * @return
     */
    public Response<Boolean> wxPayRefund(OrderItem orderItem);
}
