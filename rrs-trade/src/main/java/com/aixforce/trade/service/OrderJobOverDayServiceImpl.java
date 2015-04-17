package com.aixforce.trade.service;

import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.alipay.event.AlipayEventBus;
import com.aixforce.alipay.event.TradeCloseEvent;
import com.aixforce.alipay.request.CallBack;
import com.aixforce.alipay.request.RefundRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.constant.ExpireTimes;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.OrderDao;
import com.aixforce.trade.dao.OrderExtraDao;
import com.aixforce.trade.dao.OrderItemDao;
import com.aixforce.trade.dao.OrderJobDayDao;
import com.aixforce.trade.dto.*;
import com.aixforce.trade.manager.OrderManager;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderExtra;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.OrderJobOverDay;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
@Service
public class OrderJobOverDayServiceImpl implements OrderJobOverDayService {

    private final static Logger log = LoggerFactory.getLogger(OrderJobOverDayServiceImpl.class);

    @Autowired
    private OrderJobDayDao orderJobDayDao;

    @Override
    public Response<Paging<OrderJobOverDay>> findByOrderIds(List<Long> orderIds) {
        Response<Paging<OrderJobOverDay>> result = new Response<Paging<OrderJobOverDay>>();
        if (orderIds.isEmpty()) {
            log.warn("ids is empty, return directly");
            return result;
        }
        try {
            Paging<OrderJobOverDay> orderJobOverDayPaging = orderJobDayDao.findByOrderIds(orderIds);
            result.setResult(orderJobOverDayPaging);
        } catch (Exception e) {
            log.error("failed to find OrderJobOverDay by orderIds, cause:", e);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Paging<OrderJobOverDay>> findBy(OrderJobOverDay orderJobOverDay) {
        Response<Paging<OrderJobOverDay>> result = new Response<Paging<OrderJobOverDay>>();

        try {
            Paging<OrderJobOverDay> orderJobOverDayPaging = orderJobDayDao.findBy(orderJobOverDay);
            result.setResult(orderJobOverDayPaging);

        } catch (Exception e) {
            log.error("failed to find OrderJobOverDay by criteria, cause:", e);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Boolean> updateStatusByOrderIds(List<Long> orderIds) {
        Response<Boolean> result = new Response<Boolean>();

        try {

            orderJobDayDao.updateByOrderIds(orderIds);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderIds:{} , cause:", orderIds, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }

        return result;
    }

    @Override
    public Response<Boolean> create(OrderJobOverDay orderJobOverDay) {

        Response<Boolean> result = new Response<Boolean>();

        try {

            Long orderJobDayId = orderJobDayDao.create(orderJobOverDay);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderJobDayId:{} , cause:", orderJobOverDay, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }


        return result;
    }

    @Override
    public Response<Boolean> update(OrderJobOverDay orderJobOverDay) {
        Response<Boolean> result = new Response<Boolean>();

        try {

            orderJobDayDao.update(orderJobOverDay);
            result.setResult(true);

        } catch (Exception e) {
            log.error("failed to update OrderJobOverDay by orderJobDayId:{} , cause:", orderJobOverDay, e);
            result.setResult(false);
            result.setError("OrderJobOverDay.query.fail");
            return result;
        }


        return result;
    }
}
