package com.aixforce.web.controller.api.userEvent;

import com.aixforce.api.service.CpsService;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

/**
 * @Description: CPS listener<br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/10/14 <br/>
 */
@Slf4j
@Component
public class CpsEventListener {

    private final UserEventBus eventBus;

    private final CpsService cpsService;

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Autowired
    public CpsEventListener(UserEventBus eventBus, CpsService cpsService) {
        this.eventBus = eventBus;
        this.cpsService = cpsService;
    }

    public final static int cookiesLength = 1;


    /**
     * 发送订单信息至CPS。
     *
     * @param cpsEvent
     */
    @Subscribe
    @SuppressWarnings("unused")
    public void sendCps(CpsEvent cpsEvent) {

        try {
            HttpServletRequest request = cpsEvent.getRequest();
            Cookie[] cookies = request.getCookies();

            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (Objects.equal(cookieName, CpsService.CPS_COOKIE_NAME)) {
                    Map map = Maps.newHashMap();
                    Set<Long> ids = null;
                    CpsService.EventType eventType = cpsEvent.getEventType();

                    map.put(CpsService.PARAM_CPS_EVENT_TYPE, eventType);
                    //支付， 取out_trade_no
                    if (Objects.equal(eventType, CpsService.EventType.PAY)) {
                        String tradeNos = request.getParameter("out_trade_no");
                        List<String> identities = Splitter.on(",").splitToList(tradeNos);
                        ids = convertToLong(identities);
                    } else {
                        ids = cpsEvent.getIds();
                    }
                    map.put(CpsService.PARAM_CPS_ORDER_IDS, ids);
                    map.put(CpsService.PARAM_CPS_COOKIE, cookie.getValue());


                    cpsService.processCps(map);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("fail to send data to CPS with registerEvent:{}, error:{}",
                    cpsEvent, Throwables.getStackTraceAsString(e));
        }

    }

    /**
     * 转换为Long
     *
     * @param identities
     * @return
     */
    private Set<Long> convertToLong(List<String> identities) {
        Set<Long> ids = Sets.newHashSet();
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }

    /**
     * 获取IP
     * PS:code copy by RequestUtils.java
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim();
        }
        return ip;
    }
}
