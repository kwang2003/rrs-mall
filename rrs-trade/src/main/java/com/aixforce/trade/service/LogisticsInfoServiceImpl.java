package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.LogisticsInfoDao;
import com.aixforce.trade.dto.LogisticsOrder;
import com.aixforce.trade.model.LogisticsInfo;
import com.aixforce.trade.model.Order;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-22-上午10:13
 */
@Slf4j
@Service
public class LogisticsInfoServiceImpl implements LogisticsInfoService {

    @Autowired
    private LogisticsInfoDao logisticsInfoDao;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private ShopService shopService;


    public Response<Long> create(LogisticsInfo logisticsInfo) {

        Response<Long> result = new Response<Long>();

        if (logisticsInfo == null) {
            log.error("logistics information can not be null");
            result.setError("logistics.information.not.null.fail");
            return result;
        }

        if (logisticsInfo.getOrderId() == null) {
            log.error("logistics information orderId can not be null");
            result.setError("logistics.orderId.not.null.fail");
            return result;
        }

        if (logisticsInfo.getCompanyName() == null) {
            log.error("logistics information companyName can not be null");
            result.setError("logistics.companyName.not.null.fail");
            return result;
        }

        if (logisticsInfo.getFreightNote() == null) {
            log.error("logistics information freightNote can not be null");
            result.setError("logistics.freightNote.not.null.fail");
            return result;
        }

        if (logisticsInfo.getSenderId() == null) {
            log.error("logistics information senderId can not be null");
            result.setError("logistics.senderId.not.null.fail");
            return result;
        }

        if (logisticsInfo.getSenderName() == null) {
            log.error("logistics information senderName can not be null");
            result.setError("logistics.senderName.not.null.fail");
            return result;
        }

        if (logisticsInfo.getLogisticsStatus() == null) {
            log.error("logistics information logisticsStatus can not be null");
            result.setError("logistics.logisticsStatus.not.null.fail");
            return result;
        }

        try {

            //检查该订单是否早已录入物流信息
            LogisticsInfo existed = logisticsInfoDao.findByOrderId(logisticsInfo.getOrderId());
            if (existed != null) {
                log.error("logisticsInfo duplicated where order id={}", logisticsInfo.getOrderId());
                result.setError("logisticsInfo.duplicated");
                return result;
            }

            Response<Order> orderResponse = orderQueryService.findById(logisticsInfo.getOrderId());
            Order order = orderResponse.getResult();

            //检查订单是否存在
            if (order == null) {
                log.error("order(id={}) not found", logisticsInfo.getOrderId());
                result.setError("order.not.found");
                return result;
            } else {
                logisticsInfo.setSendFee(order.getDeliverFee());//设置运费
            }

            //检查该订单是否属于发货人
            if (!Objects.equal(order.getSellerId(), logisticsInfo.getSenderId())) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            //检查订单是否是货到付款，或者已付款
            if (!Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
                if (Objects.equal(order.getStatus(), Order.Status.WAIT_FOR_PAY)) {
                    log.error("buyer don't pay yet");
                    result.setError("logistics.buyer.not.pay.yet");
                    return result;
                }
            }

            Long id = logisticsInfoDao.create(logisticsInfo);
            result.setResult(id);
            return result;

        } catch (Exception e) {
            log.error("fail to record logistics information where order id={},cause:{}", logisticsInfo.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("logistics.information.record.fail");
            return result;
        }
    }


    public Response<LogisticsOrder> findOrderByOrderId(Long orderId, Long currentUserId) {

        Response<LogisticsOrder> result = new Response<LogisticsOrder>();

        try {

            if (orderId == null) {
                log.error("orderId can not be null");
                result.setError("logistics.orderId.not.null.fail");
                return result;
            }

            Response<Order> orderResponse = orderQueryService.findById(orderId);
            Order order = orderResponse.getResult();

            //检查订单是否存在
            if (order == null) {
                log.error("order(id={}) not found", orderId);
                result.setError("order.not.found");
                return result;
            }

            //只有买家和卖家能查看订单信息
            if (!Objects.equal(order.getSellerId(), currentUserId) && !Objects.equal(order.getBuyerId(), currentUserId)) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            Response<Shop> shopResponse = shopService.findByUserId(order.getSellerId());
            Shop shop = shopResponse.getResult();

            if (shop == null) {
                log.error("shop where userId={} not found", order.getSellerId());
                result.setError("shop.not.found");
                return result;
            }

            LogisticsOrder logisticsOrder = new LogisticsOrder();
            logisticsOrder.setOrderId(order.getId());
            logisticsOrder.setCreatedAt(order.getCreatedAt());
            logisticsOrder.setShopName(shop.getName());

            result.setResult(logisticsOrder);
            return result;
        } catch (Exception e) {
            log.error("fail to find logistics information where order id={},cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("logistics.information.query.fail");
            return result;
        }

    }

    public Response<Boolean> update(LogisticsInfo logisticsInfo, Long currentUserId) {
        Response<Boolean> result = new Response<Boolean>();

        if (logisticsInfo == null) {
            log.error("logistics information can not be null");
            result.setError("logistics.information.not.null.fail");
            return result;
        }

        if (logisticsInfo.getOrderId() == null) {
            log.error("orderId can not be null");
            result.setError("logistics.orderId.not.null.fail");
            return result;
        }

        if (currentUserId == null) {
            log.error("currentUserId can not be null");
            result.setError("user.id.not.null");
            return result;
        }


        try {

            LogisticsInfo existed = logisticsInfoDao.findByOrderId(logisticsInfo.getOrderId());

            if (existed == null) {
                log.error("logisticsInfo(id={}) not found", logisticsInfo.getOrderId());
                result.setError("logistics.information.not.found");
                return result;
            }

            //检查该物流信息是否属于当前登录用户
            if (!Objects.equal(existed.getSenderId(), currentUserId)) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            logisticsInfoDao.updateByOrderId(logisticsInfo);
            result.setResult(Boolean.TRUE);
            return result;

        } catch (Exception e) {
            log.error("fail to update logisticsInfo where orderId={},cause:{}", logisticsInfo.getOrderId(), Throwables.getStackTraceAsString(e));
            result.setError("logistics.information.update.fail");
            return result;
        }
    }


    public Response<LogisticsInfo> findByOrderId(Long orderId, Long currentUserId) {

        Response<LogisticsInfo> result = new Response<LogisticsInfo>();

        if (orderId == null) {
            log.error("orderId can not be null");
            result.setError("logistics.orderId.not.null.fail");
            return result;
        }

        if (currentUserId == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null");
            return result;
        }

        try {

            Response<Order> orderResponse = orderQueryService.findById(orderId);
            Order order = orderResponse.getResult();

            //检查订单是否存在
            if (order == null) {
                log.error("order(id={}) not found", orderId);
                result.setError("order.not.found");
                return result;
            }

            //只有买家和卖家能查看物流信息
            if (!Objects.equal(order.getSellerId(), currentUserId) && !Objects.equal(order.getBuyerId(), currentUserId)) {
                log.error("current user don't have the right");
                result.setError("authorize.fail");
                return result;
            }

            LogisticsInfo logisticsInfo = logisticsInfoDao.findByOrderId(orderId);
            if (logisticsInfo == null) {
                log.error("logisticsInfo not found where order id={}", orderId);
                result.setError("logisticsInfo.not.found");
                return result;
            }
            result.setResult(logisticsInfo);
            return result;

        } catch (Exception e) {
            log.error("fail to query logisticsInfo where orderId={},cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("logistics.information.query.fail");
            return result;
        }
    }

    public Response<LogisticsInfo> findByOrderId(Long orderId) {

        Response<LogisticsInfo> result = new Response<LogisticsInfo>();

        if (orderId == null) {
            log.error("orderId can not be null");
            result.setError("logistics.orderId.not.null.fail");
            return result;
        }

        try {

            Response<Order> orderResponse = orderQueryService.findById(orderId);
            Order order = orderResponse.getResult();

            //检查订单是否存在
            if (order == null) {
                log.error("order(id={}) not found", orderId);
                result.setError("order.not.found");
                return result;
            }

            LogisticsInfo logisticsInfo = logisticsInfoDao.findByOrderId(orderId);
            if (logisticsInfo == null) {
                log.error("logisticsInfo not found where order id={}", orderId);
                result.setError("logisticsInfo.not.found");
                return result;
            }
            result.setResult(logisticsInfo);
            return result;

        } catch (Exception e) {
            log.error("fail to query logisticsInfo where orderId={},cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("logistics.information.query.fail");
            return result;
        }
    }

}
