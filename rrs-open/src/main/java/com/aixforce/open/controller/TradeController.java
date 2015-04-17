package com.aixforce.open.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.trade.dto.HaierOrder;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.OrdersPopularize;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrdersPopularizeService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 交易
 *
 * Created by neusoft on 14-8-4.
 */
@Slf4j
@Controller
@RequestMapping("/api/open/trade")
public class TradeController {

    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    private OrderQueryService orderQueryService;

    private static final int DEFAULT_SIZE = 50;

    private final static DateTimeFormatter dft = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    OrdersPopularizeService ordersPopularizeService;


    /**
     * 订单查询接口,返回订单的分页信息（根据updateAt字段查询起止范围)
     *
     * @param begin     开始时间,未输入则表示无开始时间
     * @param end       截止时间,未输入则表示无截止时间
     * @param pageNo    页号,缺省 1
     * @param size      页大小,缺省 50
     * @param sign      消息摘要,必输
     * @return          分页信息
     */
    @RequestMapping(value = "/orders/page", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<HaierOrder>> ordersPage(@RequestParam(value="channel", required = true) String channel,
                                                   @RequestParam(value = "begin", required = false) String begin,
                                                   @RequestParam(value = "end", required = false) String end,
                                                   @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                   @RequestParam(value = "size", required = false) Integer size,
                                                   @RequestParam(value = "sign", required = true) String sign,
                                                   HttpServletRequest request) {
        log.info("/orders/page accepted channel :{}, ip:{}, begin:{}, end:{}, pageNo:{}, size:{}",
                channel, RequestUtils.getIpAddr(request), begin, end, pageNo, size);

        Response<Paging<HaierOrder>> result = new Response<Paging<HaierOrder>>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            TreeMap<String, String> params = Maps.newTreeMap();   //参数按名称升序
            if (notNull(begin)) {
                params.put("begin", begin);
            }
            if (notNull(end)) {
                params.put("end", end);
            }
            if (notNull(pageNo)) {
                params.put("pageNo", pageNo.toString());
            } else {
                pageNo = 1;
            }

            if (notNull(size)) {
                params.put("size", size.toString());
            } else {
                size = DEFAULT_SIZE;
            }
            Date beginAt, endAt;

            if (notEmpty(begin)) {
                beginAt = dft.parseDateTime(begin).toDate();
            } else {
                beginAt = DateTime.now().minusDays(7).toDate();
            }

            if (notEmpty(end)) {
                endAt = dft.parseDateTime(end).toDate();
            } else {
                endAt = DateTime.now().toDate();
            }

            List<Long> userIds = null;
            Response<List<Long>> userIdsResult = channelShopsService.findUserIds(channel);
            if(userIdsResult.isSuccess()) {
                userIds = userIdsResult.getResult();
            }

            Response<List<Long>> bussinessIdsResult = channelShopsService.findBusinessIds(channel);
            checkState(bussinessIdsResult.isSuccess(), "fail.to.get.business.info");

            //修改接口,直接返回haier的DTO
            Response<Paging<HaierOrder>> ordersQuery = orderQueryService
                    .findHaierOrderByUpdatedAtAndSellerIds(beginAt, endAt, bussinessIdsResult.getResult(), pageNo, size, userIds);
            checkState(ordersQuery.isSuccess(), ordersQuery.getError());

            Paging<HaierOrder> paging = ordersQuery.getResult();

            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.error("fail to invoke 'orderPage' with channel :{}, ip:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request),begin, end, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to invoke 'orderPage' with channel :{}, ip:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), begin, end, pageNo, size, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to invoke 'orderPage' with channel :{}, ip:{}, begin:{}, end:{}, pageNo:{}, size:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), begin, end, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.invoke.orderPage"));
        }

        return result;
    }

    /**
     * 提供给亿起发查询订单接口
     * @param channel 频道ID
     * @param cid 活动ID
     * @param orderStartTime 订单生成开始时间
     * @param orderEndTime 订单生成结束时间
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 订单列表
     */
    @RequestMapping(value = "/yqf/orders/byCreatedAt", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Map>> yqfOrdersByCreatedAt(@RequestParam(value="channel", required = true) String channel,
                                                    @RequestParam(value = "cid", required = true) String cid,
                                                    @RequestParam(value = "orderStartTime", required = true) Long orderStartTime,
                                                    @RequestParam(value = "orderEndTime", required = true) Long orderEndTime,
                                                    @RequestParam(value = "sign", required = true) String sign,
                                                    HttpServletRequest request) {
        log.info("/yqf/orders/byCreatedAt accepted channel :{}, ip:{}, cid:{}, orderStartTime:{}, orderEndTime:{}",
                channel, RequestUtils.getIpAddr(request), cid, orderStartTime, orderEndTime);

        Response<List<Map>> result = new Response<List<Map>>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notNull(orderStartTime), "orderStartTime.can.not.be.empty");
            checkArgument(notNull(orderEndTime), "orderEndTime.can.not.be.empty");

            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            List<Map> orderItemList = Lists.newArrayList();

            Response<List<OrdersPopularize>> listResponse = ordersPopularizeService.findOrdersByCreatedAt(cid, new Date(orderStartTime*1000), new Date(orderEndTime*1000));
            checkState(listResponse.isSuccess(), listResponse.getError());
            List<OrdersPopularize> ordersPopularizeList = listResponse.getResult();


            for (OrdersPopularize ordersPopularize:ordersPopularizeList) {
                Long orderSubId = ordersPopularize.getOrderSubId();
                // 总单ID
                Long orderId = ordersPopularize.getOrderId();
                Response<Order> orderResponse = orderQueryService.findById(orderId);
                Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(orderSubId);
                if (orderItemResponse.isSuccess() && orderResponse.isSuccess()) {
                    OrderItem orderItem = orderItemResponse.getResult();
                    Order order = orderResponse.getResult();
                    Map map = Maps.newConcurrentMap();
                    // 订单编号
                    map.put("orderNo", orderItem.getOrderId()+"_"+orderItem.getId());
                    // 反馈标签
                    map.put("feedback", ordersPopularize.getWi());
                    // 下单时间
                    map.put("orderTime", sdf.format(orderItem.getCreatedAt()));
                    // 商品数量
                    map.put("amount", Objects.firstNonNull(orderItem.getQuantity(), ""));
                    // 商品金额
                    map.put("price", Objects.firstNonNull(orderItem.getFee(), ""));
                    // 商品名称
                    map.put("name", Objects.firstNonNull(orderItem.getItemName(), ""));
                    // 更新时间
                    map.put("updateTime", sdf.format(orderItem.getUpdatedAt()));
                    // 商品编号
                    map.put("productNo", Objects.firstNonNull(orderItem.getItemId(), ""));
                    // 商品类别
                    map.put("category", Objects.firstNonNull(orderItem.getBusinessId(), ""));
                    // 佣金类型
                    map.put("commissionType", Objects.firstNonNull(orderItem.getBusinessId(), ""));
                    // 运费
                    map.put("fare", Objects.firstNonNull(orderItem.getDeliverFee(), ""));
                    // 订单状态
                    map.put("orderStatus", Objects.firstNonNull(orderItem.getStatus(), ""));
                    // 支付状态
                    int paymentStatus = 0;
                    if (!isNull(order.getPaidAt())) {
                        paymentStatus = 1;
                    }
                    map.put("paymentStatus", paymentStatus);
                    // 支付方式
                    map.put("paymentType", Objects.firstNonNull(orderItem.getPayType(), ""));
                    orderItemList.add(map);
                }
            }

            result.setResult(orderItemList);
        } catch (IllegalArgumentException e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, orderStartTime:{}, orderEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request),cid, orderStartTime, orderEndTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, orderStartTime:{}, orderEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request),cid, orderStartTime, orderEndTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, orderStartTime:{}, orderEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), cid, orderStartTime, orderEndTime, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.yiqifa.orders"));
        }

        return result;
    }

    /**
     * 提供给亿起发查询订单接口
     * @param channel 频道ID
     * @param cid 活动ID
     * @param updateStartTime  订单更新开始时间
     * @param updateEndTime 订单更新结束时间
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 订单列表
     */
    @RequestMapping(value = "/yqf/orders/byUpdatedAt", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<Map>> yqfOrdersByUpdatedAt(@RequestParam(value="channel", required = true) String channel,
                                                    @RequestParam(value = "cid", required = true) String cid,
                                                    @RequestParam(value = "updateStartTime", required = true) Long updateStartTime,
                                                    @RequestParam(value = "updateEndTime", required = true) Long updateEndTime,
                                                    @RequestParam(value = "sign", required = true) String sign,
                                                    HttpServletRequest request) {
        log.info("/yqf/orders/byCreatedAt accepted channel :{}, ip:{}, cid:{}, updateStartTime:{}, updateEndTime:{}",
                channel, RequestUtils.getIpAddr(request), cid, updateStartTime, updateEndTime);

        Response<List<Map>> result = new Response<List<Map>>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notNull(updateStartTime), "updateStartTime.can.not.be.empty");
            checkArgument(notNull(updateEndTime), "updateEndTime.can.not.be.empty");

            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            List<Map> orderItemList = Lists.newArrayList();

            Response<List<OrdersPopularize>> listResponse = ordersPopularizeService.findOrdersByUpdatedAt(cid, new Date(updateStartTime*1000), new Date(updateEndTime*1000));
            checkState(listResponse.isSuccess(), listResponse.getError());
            List<OrdersPopularize> ordersPopularizeList = listResponse.getResult();


            for (OrdersPopularize ordersPopularize:ordersPopularizeList) {
                Long orderSubId = ordersPopularize.getOrderSubId();
                // 总单ID
                Long orderId = ordersPopularize.getOrderId();
                Response<Order> orderResponse = orderQueryService.findById(orderId);
                Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(orderSubId);
                if (orderItemResponse.isSuccess() && orderResponse.isSuccess()) {
                    OrderItem orderItem = orderItemResponse.getResult();
                    Order order = orderResponse.getResult();
                    Map map = Maps.newConcurrentMap();
                    // 订单编号
                    map.put("orderNo", orderItem.getOrderId()+"_"+orderItem.getId());
                    // 反馈标签
                    map.put("feedback", ordersPopularize.getWi());
                    // 更新时间
                    map.put("updateTime", sdf.format(orderItem.getUpdatedAt()));
                    // 订单状态
                    map.put("orderStatus", Objects.firstNonNull(orderItem.getStatus(), ""));
                    // 支付状态
                    int paymentStatus = 0;
                    if (!isNull(order.getPaidAt())) {
                        paymentStatus = 1;
                    }
                    map.put("paymentStatus", paymentStatus);
                    // 支付方式
                    map.put("paymentType", Objects.firstNonNull(orderItem.getPayType(), ""));
                    orderItemList.add(map);
                }
            }

            result.setResult(orderItemList);
        } catch (IllegalArgumentException e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, updateStartTime:{}, updateEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request),cid, updateStartTime, updateEndTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, updateStartTime:{}, updateEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request),cid, updateStartTime, updateEndTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get yiqifa orders by createdAt with channel :{}, ip:{}, cid:{}, updateStartTime:{}, updateEndTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), cid, updateStartTime, updateEndTime, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.yiqifa.orders"));
        }

        return result;
    }
}
