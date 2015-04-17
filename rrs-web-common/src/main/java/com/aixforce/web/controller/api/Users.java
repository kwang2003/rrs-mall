/*
 * Copyright (c) 2012 杭州端点网络科技有限公司¸
 */

package com.aixforce.web.controller.api;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.common.utils.NameValidator;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.sms.SmsService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.UserProfileService;
import com.aixforce.web.controller.api.userEvent.*;
import com.aixforce.web.controller.api.validator.SmsCountValidator;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-06
 */
@Controller
@RequestMapping("/api/user")
public class Users {

    private final static Logger log = LoggerFactory.getLogger(Users.class);

    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");

    private final Splitter splitter = Splitter.on('@').trimResults();

    private final static HashFunction md5 = Hashing.md5();

    private final static String CUSTOMER_LOGIN = "CustomerLogin";

    private final static String CUSTOMER_LOGIN_VALUE = "1";

    @Autowired
    private LoginRedirector loginRedirector;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserEventBus userEventBus;

    @Autowired
    private CommonConstants commonConstants;

    @Autowired
    private CaptchaGenerator captchaGenerator;

    @Autowired
    private SmsCountValidator smsCountValidator;

    @Autowired
    private MessageSources messageSources;

    @Value("#{app.domain}")
    private String domain;


    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String login(@RequestParam("loginId") String loginId, @RequestParam("password") String password,
                        @RequestParam(value = "type", defaultValue = "1") Integer type,
                        @RequestParam(value = "target", required = false) String target,
                        HttpServletRequest request, HttpServletResponse response) {

        LoginType loginType = LoginType.from(type);
        if (loginType == null) {
            throw new JsonResponseException("unknown login type:" + type);
        }
        Response<User> result = accountService.userLogin(loginId, loginType, password);
        if (!result.isSuccess()) {
            log.error("failed to login user by id={},type={},error code:{}", loginId, type, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        User user = result.getResult();
        request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
        //cookie CustomerLogin 设置为1,过期时间为30分钟
        Cookie customerLogin = new Cookie(CUSTOMER_LOGIN, CUSTOMER_LOGIN_VALUE);
        customerLogin.setMaxAge(30*60);
        customerLogin.setPath("/"); //在所有页面下都可见
        customerLogin.setDomain(domain);
        response.addCookie(customerLogin);

        String redirectUrl = loginRedirector.redirectTarget(target,user);

        LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
        userEventBus.post(loginEvent);
        return redirectUrl;
    }

    @RequestMapping(value = "/login-no-error", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String login(@RequestParam("loginId") String loginId, @RequestParam("password") String password,
                        @RequestParam(value = "type", defaultValue = "3") Integer type,
                        HttpServletRequest request, HttpServletResponse response) {

        LoginType loginType = LoginType.from(type);
        if (loginType == null) {
            throw new JsonResponseException("unknown login type:" + type);
        }
        Response<User> result = accountService.userLogin(loginId, loginType, password);
        if (!result.isSuccess()) {
            log.error("failed to login user by id={},type={},error code:{}", loginId, type, result.getError());
            return messageSources.get(result.getError());
        }
        User user = result.getResult();
        request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
        //cookie CustomerLogin 设置为1,过期时间为30分钟
        Cookie customerLogin = new Cookie(CUSTOMER_LOGIN, CUSTOMER_LOGIN_VALUE);
        customerLogin.setMaxAge(30*60);
        customerLogin.setPath("/"); //在所有页面下都可见
        customerLogin.setDomain(domain);
        response.addCookie(customerLogin);

        LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
        userEventBus.post(loginEvent);
        return "success";
    }


    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long signup(User user,
                       @RequestParam("password") String password,
                       @RequestParam("code") String code,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        try {
            checkArgument(notEmpty(user.getName()) || notEmpty(user.getEmail()) || notEmpty(user.getMobile()), "login.id.empty");

            // 校验token
            HttpSession session = request.getSession(true);
            String codeInSession = (String) session.getAttribute("code");
            checkArgument(notEmpty(codeInSession), "token.mismatch");
            String expectedCode = splitter.splitToList(codeInSession).get(0);
            checkArgument(equalWith(code, expectedCode), "token.mismatch");

            // 如果验证成功则删除之前的code
            session.removeAttribute("code");

            // 校验用户名是否重复
            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicated");

            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());

            // 创建用户
            Response<Long> result = accountService.createUser(user);
            checkState(result.isSuccess(), result.getError());
            Long userId = result.getResult();

            // 记录用户session
            session.setAttribute(CommonConstants.SESSION_USER_ID, result.getResult());


            // 异步记录注册统计信息
            userEventBus.post(new RegisterEvent(userId, user.getName(), request, response));
            return userId;

        } catch (IllegalArgumentException e) {
            log.error("failed to sign up {}, error:{}", user, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to sign up {}, error:{}", user, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to sign up {}, error:{}", user, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("user.signup.error"));
        }

    }

    @RequestMapping(value = "/sms", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean sendSms(@RequestParam("mobile") String mobile, HttpServletRequest request) {
        if (mobilePattern.matcher(mobile).matches()) {
            HttpSession session = request.getSession();
            String activateCode = (String) session.getAttribute("code");
            Response<Boolean> result;
            if (!Strings.isNullOrEmpty(activateCode)) {//判断是否需要重新发送激活码
                List<String> parts = splitter.splitToList(activateCode);
                long sendTime = Long.parseLong(parts.get(1));
                if (System.currentTimeMillis() - sendTime < TimeUnit.MINUTES.toMillis(1)) { //
                    throw new JsonResponseException(500, "1分钟内只能获取一次验证码");
                } else {
                    if (smsCountValidator.check(mobile)) {
                        String code =String.valueOf((int)((Math.random()*9+1)*100000));
                        String message = messageSources.get("sms.templates.register", code);
                        session.setAttribute("code", code + "@" + System.currentTimeMillis()+"@"+mobile);
                        result = smsService.sendSingle("000000", mobile, message);
                    } else {
                        throw new JsonResponseException(500, messageSources.get("sms.send.limit"));
                    }
                }
            } else { //新发送激活码
                if (smsCountValidator.check(mobile)) {
                    String code =String.valueOf((int)((Math.random()*9+1)*100000));
                    String message = messageSources.get("sms.templates.active", code);
                    session.setAttribute("code", code + "@" + System.currentTimeMillis()+"@"+mobile);
                    result = smsService.sendSingle("000000", mobile, message);
                } else {
                    throw new JsonResponseException(400, messageSources.get("sms.send.limit"));
                }
            }
            if(!result.isSuccess()) {
                log.error("send sms single fail, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            return result.getResult();
        } else {
            throw new JsonResponseException(400, "错误的手机号码");
        }
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public int userStatus(@RequestParam("mobile") String mobile) {
        Response<User> result = accountService.findUserBy(mobile, LoginType.MOBILE);
        if (!result.isSuccess()) {
            log.error("failed to get user(mobile={}) status,error code:{}", mobile, result.getError());
            return -100;
        }
        User user = result.getResult();
        return user.getStatus();
    }

    @RequestMapping(value = "/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean changePassword(String oldPassword, String newPassword) {
        Long userId = UserUtil.getUserId();

        Response<Boolean> result = accountService.changePassword(userId, oldPassword, newPassword);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to change password for user id={},error code:{}", userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    @RequestMapping(value = "/change_mobile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String changeMobile(@RequestParam("mobile") String mobile,
                               @RequestParam("password") String password,
                               @RequestParam("captcha") String code, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();

        HttpSession session = request.getSession();
        String temp = (String) session.getAttribute("code");
        if (Strings.isNullOrEmpty(temp)) {
            throw new JsonResponseException(500, "验证码不匹配");
        }

        List<String> parts = splitter.splitToList(temp);
        String expected = parts.get(0);
        if (!Objects.equal(expected, code)) {
            throw new JsonResponseException(500, "验证码不匹配");
        }
        //如果匹配了code,则删除在session中的值
        request.getSession().removeAttribute("code");
        Response<Boolean> cr = accountService.changeMobile(userId, mobile, password);
        if (!cr.isSuccess()) {
            log.error("failed to change mobile for user(id={}),error code:{}", userId, cr.getError());
            throw new JsonResponseException(500, messageSources.get(cr.getError()));
        }
        UserProfileEvent event = new UserProfileEvent(userId);
        event.setMobile(mobile);
        userEventBus.post(event);
        return "ok";

    }


    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    @ResponseBody
    public String captcha(HttpServletRequest request) {
        byte[] data = captchaGenerator.captcha(request.getSession());
        return BaseEncoding.base64().encode(data);
    }

    /**
     * 手机忘记密码
     *
     * @param mobile  手机号码
     * @param code    验证码
     * @param request 请求
     * @return 是否成功
     */
    @RequestMapping(value = "/forgot-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String handleForgotPasswordByMobile(@RequestParam("mobile") String mobile, @RequestParam("password") String password,
                                               @RequestParam("captcha") String code, HttpServletRequest request) {


        if (Strings.isNullOrEmpty(code)) {
            throw new JsonResponseException(500, messageSources.get("user.token.unmatch"));
        }

        HttpSession session = request.getSession();
        String temp = (String) session.getAttribute("code");
        if (Strings.isNullOrEmpty(temp)) {
            throw new JsonResponseException(500, messageSources.get("user.token.unmatch"));
        }

        List<String> parts = splitter.splitToList(temp);


        String expected = parts.get(0);
        if (!Objects.equal(expected, code)) {
            throw new JsonResponseException(500, messageSources.get("user.token.unmatch"));
        }

        String expectedMobile = parts.get(2);

        if(!Objects.equal(expectedMobile, mobile)){
            throw new JsonResponseException(500, messageSources.get("user.token.unmatch"));
        }
        //如果匹配了code,则删除在session中的值
        request.getSession().removeAttribute("code");

        Response<User> result = accountService.findUserBy(mobile, LoginType.MOBILE);

        if (!result.isSuccess()) {
            log.error("failed to find user where mobile={},error code:{}", mobile, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        User user = result.getResult();
        if (user == null || Objects.equal(user.getStatus(), User.STATUS.NOT_ACTIVATE.toNumber())) { //对于未激活过的用户,不允许修改密码
            throw new JsonResponseException(500, messageSources.get("user.not.found"));
        }


        Response<Boolean> r = accountService.resetPassword(user.getId(), password);
        if (r.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to reset password for user(mobile={}),error code:{}", mobile, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            BaseUser baseUser = UserUtil.getCurrentUser();
            if (baseUser != null) {
                //delete login token cookie
                LogoutEvent logoutEvent = new LogoutEvent(baseUser.getId(), request, response);
                userEventBus.post(logoutEvent);
            }
            return "redirect:" + commonConstants.getMainSite();
        } catch (Exception e) {
            log.error("failed to logout user,cause:", e);
            throw new JsonResponseException(500, messageSources.get("user.logout.fail"));
        }
    }

    @RequestMapping(value = "/update-profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateProfile(UserProfile userProfile,
                                @RequestParam("name") String name,
                                @RequestParam(value = "email", required = false) String email,
                                @RequestParam("avatar") String avatar) {


        BaseUser baseUser = UserUtil.getCurrentUser();

        User updated = new User();
        updated.setId(baseUser.getId());
        updated.setAvatar(avatar);
        if (!Strings.isNullOrEmpty(name)) {
            updated.setName(name);
        }
        if (!Strings.isNullOrEmpty(email)) {
            updated.setEmail(email);
        }
        Response<Boolean> ur = accountService.updateUser(updated);
        if (!ur.isSuccess()) {
            log.error("failed to update {},error code:{}", updated, ur.getError());
            throw new JsonResponseException(500, messageSources.get(ur.getError()));
        }

        userProfile.setUserId(baseUser.getId());
        ur = userProfileService.updateUserProfileByUserId(userProfile);
        if (!ur.isSuccess()) {
            log.error("failed to update {},error code:{}", userProfile, ur.getError());
            throw new JsonResponseException(500, messageSources.get(ur.getError()));
        }

        UserProfileEvent event = new UserProfileEvent(baseUser.getId());
        event.setName(baseUser.getName());
        event.setRealName(userProfile.getRealName());
        userEventBus.post(event);
        return "ok";

    }

    /**
     * 验证用户信息是否重复
     *
     * @param type      验证字段，有name，email，mobile
     * @param content   验证内容
     * @param operation 1为创建时验证，2为修改时验证
     * @return 是否已存在
     */
    @RequestMapping(value = "/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean verifyBy(@RequestParam("type") Integer type,
                            @RequestParam("content") String content,
                            @RequestParam(value = "operation", defaultValue = "1") Integer operation) {
        LoginType loginType = LoginType.from(type);
        Long userId = UserUtil.getUserId();
        if (loginType == null) {
            throw new JsonResponseException("unknown login type:" + type);
        }
        if (!Objects.equal(operation, 1) && !Objects.equal(operation, 2)) {
            throw new JsonResponseException("unknown operation");
        }
        Response<User> result = accountService.findUserBy(content, loginType);
        if (Objects.equal(operation, 1)) {
            if (result.isSuccess()) {
                log.warn("user info{} exist", content);
                return false;
            }
        } else {
            if (result.isSuccess() && !Objects.equal(result.getResult().getId(), userId)) {
                log.warn("user info{} exist", content);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断用户是否登录
     * @return 用户id
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long isLongIn(){
        Long userId = null;
        try {
            userId =UserUtil.getCurrentUser().getId();
        }catch (Exception e){
            log.error("current user not login");
        }
        return userId;
    }

    @RequestMapping(value = "/change_userInfo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean changeUserInfo(Long userId, String mobile, String newPassword) {
        Response<Boolean> result = accountService.changeUserInfo(userId, mobile, newPassword);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to change userInfo for user id={},error code:{}", userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

}