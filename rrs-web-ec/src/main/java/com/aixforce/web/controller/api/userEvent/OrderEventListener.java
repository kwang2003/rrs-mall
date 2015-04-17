package com.aixforce.web.controller.api.userEvent;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrdersPopularizeService;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

/**
 * Author: yjgsjone@163.com
 */
@Slf4j
@Component
public class OrderEventListener {

    private final UserEventBus eventBus;

    private final OrdersPopularizeService ordersPopularizeService;

    private final OrderQueryService orderQueryService;

    private final static String YIQIFA_CPS_COOKIE_NAME = "yiqifaCps";

    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired
    public OrderEventListener(UserEventBus eventBus, OrdersPopularizeService ordersPopularizeService, OrderQueryService orderQueryService) {
        this.eventBus = eventBus;
        this.ordersPopularizeService = ordersPopularizeService;
        this.orderQueryService = orderQueryService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    /**
     * 向亿起发推送订单信息
     * @param orderEvent
     */
    @Subscribe
    @SuppressWarnings("unused")
    public void sentOrderToYQF(OrderEvent orderEvent) {
        try {

            HttpServletRequest request = orderEvent.getRequest();
            Cookie[] cookies = request.getCookies();
            Set<Long> ids = orderEvent.getIds();

            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                // 检查Cookie中是否包含亿起发推广信息
                if (Objects.equal(cookieName, YIQIFA_CPS_COOKIE_NAME)) {
                    String cookieValue = cookie.getValue();
                    Map mapCookie = JSON_MAPPER.fromJson(cookieValue, Map.class);
                    for (Long id : ids) {
                        Response<List<OrderItem>> listResponse = orderQueryService.findSubsByOrderId(id);
                        Response<Order> orderResponse = orderQueryService.findById(id);
                        if (listResponse.isSuccess() && orderResponse.isSuccess()) {
                            List<OrderItem> orderItemList = listResponse.getResult();
                            Order order = orderResponse.getResult();
                            for (OrderItem orderItem:orderItemList) {
                                // 订单id
                                mapCookie.put("orderId", id);
                                // 子订单id
                                mapCookie.put("orderSubId", orderItem.getId());
                                // 推广者
                                mapCookie.put("promoter", "1");
                                // 创建时间
                                mapCookie.put("createdAt", orderItem.getCreatedAt());
                                // 修改时间
                                mapCookie.put("updatedAt", orderItem.getUpdatedAt());
                                ordersPopularizeService.create(mapCookie);
                                ordersPopularizeService.sendYqf(mapCookie, orderItem, order);
                            }
                        }
                    }
                }

            }

        } catch (IllegalStateException e) {
            log.error("fail to send data to yiqifa with registerEvent:{}, error:{}",
                    orderEvent, e.getMessage());
        } catch (Exception e) {
            log.error("fail to send data to yiqifa with registerEvent:{}, error:{}",
                    orderEvent, Throwables.getStackTraceAsString(e));
        }
    }


}
