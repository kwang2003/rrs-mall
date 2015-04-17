/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-06
 */
@Controller
@RequestMapping("/api/user")
public class Sessions {
    @Value("#{app.mainSite}")
    private String mainSite;

    @Value("#{app.domain}")
    private String domain;

    private final static Logger log = LoggerFactory.getLogger(Sessions.class);

    public final static String CUSTOMER_LOGIN = "CustomerLogin";

    private final static String CUSTOMER_LOGIN_VALUE = "1";

    private final AccountService<User> accountService;

    private final CommonConstants commonConstants;

    private final MessageSources messageSources;

    @Autowired
    public Sessions(AccountService<User> accountService, CommonConstants commonConstants, MessageSources messageSources) {
        this.accountService = accountService;
        this.commonConstants = commonConstants;
        this.messageSources = messageSources;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("loginId") String loginId, @RequestParam("password") String password,
                        @RequestParam(value = "type", defaultValue = "1") Integer type,
                        @RequestParam(value = "target", required = false) String target,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {


        Response<User> result = accountService.login(loginId, LoginType.from(type), password);
        if (!result.isSuccess()) {
            log.error("failed to login user by loginId={} and loginType={},error code:{}", loginId, type, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        User user = result.getResult();
        BaseUser.TYPE t = user.getTypeEnum();

        // 东软对接需求，要求登录后跳转
        if (Objects.equal(t, BaseUser.TYPE.BUYER)) {
            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
                return Strings.isNullOrEmpty(target) ?
                        "http://" + mainSite + "/buyer/index" :
                        target;
        }

        if (!Objects.equal(t, BaseUser.TYPE.ADMIN)
                && !Objects.equal(t, BaseUser.TYPE.SITE_OWNER)
                && !Objects.equal(t, BaseUser.TYPE.WHOLESALER)
                && !Objects.equal(t, BaseUser.TYPE.FINANCE)) {
            log.error("user login with wrong privileges, needs to be admin, site owner, wholesaler or finance. actual:{}", user.getTypeEnum());
            throw new JsonResponseException(500, messageSources.get("authorize.fail"));
        }
        // 如果不是买家就进后台
        String redirectUrl = "http://" + commonConstants.getMainSite() + "/categories/backend";
        request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
        //cookie CustomerLogin 设置为1,过期时间为30分钟
        Cookie customerLogin = new Cookie(CUSTOMER_LOGIN, CUSTOMER_LOGIN_VALUE);
        customerLogin.setMaxAge(30*60);
        customerLogin.setPath("/"); //在所有页面下都可见
        customerLogin.setDomain(domain);
        response.addCookie(customerLogin);

        if (!Strings.isNullOrEmpty(target)) {
            redirectUrl = target;
        }
        return redirectUrl;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public String logout(@RequestParam(value = "target", required = false) String target,
                         HttpServletRequest request) {
        try {
            BaseUser baseUser = UserUtil.getCurrentUser();
            request.getSession().invalidate();

            if (baseUser!=null
                    && Objects.equal(baseUser.getTypeEnum(), BaseUser.TYPE.BUYER)) {
                return Strings.isNullOrEmpty(target) ?
                        "http://" + mainSite :
                        target;
            }
            return "ok";
        } catch (Exception e) {
            log.error("failed to logout user,cause:", e);
            throw new JsonResponseException(500, "user.logout.fail");
        }
    }
}
