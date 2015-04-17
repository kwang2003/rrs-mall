package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.trade.dao.OrdersPopularizeDao;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.OrdersPopularize;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.support.nativejdbc.OracleJdbc4NativeJdbcExtractor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isNull;

/**
 * Created by yjgsjone@163.com on 14-9-15.
 */
@Service
@Slf4j
public class OrdersPopularizeServiceImpl implements OrdersPopularizeService{

    // 亿起发订单实时接口
    private static String YI_QI_FA_ORDER_SEND_URL = "http://o.yiqifa.com/servlet/handleCpsInterIn";
    // 亿起发分配标示ID
    private static String YI_QI_FA_INTER_ID = "54169ea99497777815b99186";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired
    OrdersPopularizeDao ordersPopularizeDao;

    @Override
    public Response<Integer> create(Map map) {

        Response<Integer> result = new Response<Integer>();

        try{
            int count = ordersPopularizeDao.create(map);
            result.setResult(count);
            return result;
        }  catch (Exception e) {
            log.error("failed to create ordersPopularize with:{}, cause:{}",
                    map, Throwables.getStackTraceAsString(e));
            result.setError("order.popularize.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> sendYqf(Map mapCookie, OrderItem orderItem, Order order) {


        Map map = Maps.newConcurrentMap();
        // 订单编号
        map.put("orderNo", orderItem.getOrderId()+"_"+orderItem.getId());
        // 活动ID
        map.put("campaignId", mapCookie.get("cid"));
        // 反馈标签
        map.put("feedback", mapCookie.get("wi"));
        // 下单时间
        map.put("orderTime", sdf.format(orderItem.getCreatedAt()));
        // 商品数量
        map.put("amount", Objects.firstNonNull(orderItem.getQuantity(), ""));
        // 商品金额
        map.put("price", Objects.firstNonNull(orderItem.getFee(), ""));
        // 商品名称
        //map.put("name", (Objects.firstNonNull(orderItem.getItemName(), "")).replaceAll("/","_"));
        map.put("name", ""); // 亿起发接口有bug，先传空
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


        String json = JSON_MAPPER.toJson(map);

        Response<Boolean> result = new Response<Boolean>();

        try{
            String code = HttpRequest
                    .post(YI_QI_FA_ORDER_SEND_URL, true,
                            "interId", YI_QI_FA_INTER_ID,
                            "json", json).body();
            if (!Objects.equal(code, "0")) {
                log.error("failed to sent order info to yiqifa with:{},return code:{}",
                        map, code);
                result.setError("failed.to.sent.order.info.to.yiqifa");

            }
            result.setResult(true);
        }  catch (Exception e) {
            log.error("failed to sent order info to yiqifa with:{}, cause:{}",
                    map, Throwables.getStackTraceAsString(e));
            result.setError("failed.to.sent.order.info.to.yiqifa");
            return result;
        }
        return result;
    }

    @Override
    public Response<Boolean> sendYqfOrderStatus(Map mapCookie, OrderItem orderItem, Order order) {
        Map map = Maps.newConcurrentMap();
        // 订单编号
        map.put("orderNo", orderItem.getOrderId()+"_"+orderItem.getId());
        // 反馈标签
        map.put("feedback", mapCookie.get("wi"));
        // 更新时间
        map.put("updateTime", sdf.format(orderItem.getUpdatedAt()));
        // 订单状态
        map.put("orderStatus", Objects.firstNonNull(orderItem.getStatus(), ""));
        // 支付状态 1：已支付 0：未支付
        int paymentStatus = 0;
        if (!isNull(order.getPaidAt())) {
            paymentStatus = 1;
        }
        map.put("paymentStatus", paymentStatus);
        // 支付方式
        map.put("paymentType", Objects.firstNonNull(orderItem.getPayType(), ""));

        String json = JSON_MAPPER.toJson(map);

        Response<Boolean> result = new Response<Boolean>();

        try{
            String code = HttpRequest
                    .post(YI_QI_FA_ORDER_SEND_URL, true,
                            "interId", YI_QI_FA_INTER_ID,
                            "json", json).body();
            if (!Objects.equal(code, "0")) {
                log.error("failed to update order status to yiqifa with:{},return code:{}",
                        map, code);
                result.setError("failed.to.update.order.status.to.yiqifa");

            }
            result.setResult(true);
        }  catch (Exception e) {
            log.error("failed to update order status to yiqifa with:{}, cause:{}",
                    map, Throwables.getStackTraceAsString(e));
            result.setError("failed.to.update.order.status.to.yiqifa");
            return result;
        }
        return result;
    }

    @Override
    public Response<List<OrdersPopularize>> findOrdersByCreatedAt(String cid, Date orderStartTime, Date orderEndTime) {

        Response<List<OrdersPopularize>> result = new Response<List<OrdersPopularize>>();

        try{

            Map map = Maps.newConcurrentMap();

            map.put("cid", cid);
            map.put("orderStartTime", orderStartTime);
            map.put("orderEndTime", orderEndTime);

            List<OrdersPopularize> ordersPopularizeList = ordersPopularizeDao.findOrdersByCreatedAt(map);

            result.setResult(ordersPopularizeList);

        }  catch (Exception e) {
            log.error("failed to get orders by createdAt with:orderStartTime:{},orderEndTime:{}, cause:{}",
                    orderStartTime, orderEndTime, Throwables.getStackTraceAsString(e));
            result.setError("failed.to.get.orders.by.createdAt");
            return result;
        }
        return result;
    }

    @Override
    public Response<List<OrdersPopularize>> findOrdersByUpdatedAt(String cid, Date updateStartTime, Date updateEndTime) {
        Response<List<OrdersPopularize>> result = new Response<List<OrdersPopularize>>();

        try{

            Map map = Maps.newConcurrentMap();

            map.put("cid", cid);
            map.put("updateStartTime", updateStartTime);
            map.put("updateEndTime", updateEndTime);

            List<OrdersPopularize> ordersPopularizeList = ordersPopularizeDao.findOrdersByUpdatedAt(map);

            result.setResult(ordersPopularizeList);

        }  catch (Exception e) {
            log.error("failed to get orders by createdAt with:updateStartTime:{},updateEndTime:{}, cause:{}",
                    updateStartTime, updateEndTime, Throwables.getStackTraceAsString(e));
            result.setError("failed.to.get.orders.by.updatedAt");
            return result;
        }
        return result;
    }

}
