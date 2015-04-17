package com.aixforce.open.controller;

import com.aixforce.common.utils.JsonMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 推广
 * Created by yjgsjone@163.com on 14-9-11.
 */
@Controller
@Slf4j
@RequestMapping("/api/open/popu")
public class PopuController {

    private final static JsonMapper jsonMapper = JsonMapper.JSON_NON_DEFAULT_MAPPER;

    private final static String YIQIFA_CPS_COOKIE_NAME = "yiqifaCps";

    @Value("#{app.domain}")
    private String domain;

    /**
     * 广告入口是广告主提供给亿起发的在广告跳转过程中能记录联盟以及网站主信息的链接，
     * 这些信息保存在COOKIE中，采用last click win（即效果算做最后一次带来流量的联盟和网站主）的方式更新COOKIE的值.
     * @param source 数据来源
     * @param channel 推广渠道
     * @param cid 活动ID
     * @param wi 反馈标签
     * @param target 目标地址
     * @return target指向的URL
     */
    @RequestMapping(value = "/yqf/trace", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String yqfTrace(@RequestParam String source,
                           @RequestParam(value="channel", defaultValue = "cps") String channel,
                           @RequestParam String cid,
                           @RequestParam String wi,
                           @RequestParam(value="target", required = false) String target,
                           HttpServletRequest request,
                           HttpServletResponse response){

        Map map = Maps.newConcurrentMap();
        // 数据来源
        map.put("source", source);
        // 推广渠道
        map.put("channel", channel);
        // 活动ID
        map.put("cid", cid);
        // 反馈标签
        map.put("wi", wi);

        String cpsCookieStr = jsonMapper.toJson(map);

        //cookie 亿起发CPS,过期时间为30天
        Cookie customerLogin = new Cookie(YIQIFA_CPS_COOKIE_NAME, cpsCookieStr);
        customerLogin.setMaxAge(30*24*60*60);
        customerLogin.setPath("/"); //在所有页面下都可见
        customerLogin.setDomain(domain);
        response.addCookie(customerLogin);

        if (!Strings.isNullOrEmpty(target)) {
            try {
                response.sendRedirect(target);
            } catch (IOException e) {
                log.error("fail to redirect yiqifa request with error:{}", e);
                throw new RuntimeException("fail to redirect yiqifa request");
            }
        }
        return "true";
    }


}
