package com.aixforce.open.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.open.dto.HaierResponse;
import com.aixforce.open.util.Channel;
import com.aixforce.open.util.NSSessionUID;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.sms.SmsService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.dto.UserProfileDto;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.UserProfileService;
import com.aixforce.web.controller.api.userEvent.LoginEvent;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.user.util.UserVerification.active;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 用户
 *
 * Created by yangjingang on 14-8-4.
 */
@Slf4j
@Controller
@RequestMapping("/api/open/user")
public class UserController {

    @Autowired
    MessageSources messageSources;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserEventBus userEventBus;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");

    private static final Splitter splitter = Splitter.on(" ");

    private static final int SMS_GROUP_MAX = 200;

    private final HashFunction md5 = Hashing.md5();

    /**
     * 获取用户信息
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return              用户信息
     */
    @RequestMapping(value = "/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<UserProfileDto> userInfo(@PathVariable Long uid,
                                                  @RequestParam(value="sessionId", required = true) String sessionId,
                                                  @RequestParam(value="channel", required = true) String channel,
                                                  @RequestParam(value="sign", required = true) String sign,
                                                  HttpServletRequest request) {
        log.info("/userInfo accepted request with channel:{}, ip:{}, uid:{}, sessionId:{}",
                channel, RequestUtils.getIpAddr(request), uid, sessionId);

        Response<UserProfileDto> result = new Response<UserProfileDto>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            String method = Thread.currentThread().getStackTrace()[1].getClassName()+'.'+Thread.currentThread().getStackTrace()[1].getMethodName();
            Response<Boolean> authResult = channelShopsService.isAuthRole(method, channel);
            checkState(authResult.isSuccess(), authResult.getError());
            // 检查是否需要授权验证
            checkArgument(!authResult.getResult(), "no.auth.to.access");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());


            BaseUser baseUser = new BaseUser();
            baseUser.setId(uid);
            Response<UserProfileDto> profileGet = userProfileService.findUserProfileByUser(baseUser);
            checkState(profileGet.isSuccess(), profileGet.getError());

            result.setResult(profileGet.getResult());

        } catch (IllegalArgumentException e) {
            log.error("failed to get user info with channel:{}, ip:{}, uid:{}, sessionId:{}, error:{}", channel, RequestUtils.getIpAddr(request), uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to get user info with channel:{}, ip:{}, uid:{}, sessionId:{}, error:{}", channel, RequestUtils.getIpAddr(request), uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to get user info with channel:{}, ip:{}, uid:{}, sessionId:{}, error:{}", channel, RequestUtils.getIpAddr(request), uid, sessionId, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.user.info"));
        }
        return result;
    }

    /**
     * 发送验证码短信
     *
     * @param id      短信ID
     * @param mobile    手机号, 必填
     * @param sign      签名, 必填
     * @return  是否发送成功
     */
    @RequestMapping(value = "/sms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> sendSms(@RequestParam(value="id", defaultValue = "1") Long id,
                                          @RequestParam(value="mobile",required = true) String mobile,
                                          @RequestParam(value="args",required = false) String args,
                                          @RequestParam(value="channel",required = true) String channel,
                                          @RequestParam(value="sign",required = true) String sign,
                                          HttpServletRequest request) {
        log.info("/sms accepted request with channel:{}, ip:{}, id:{}, mobile:{}",
        channel, RequestUtils.getIpAddr(request), id, mobile);

        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            String method = Thread.currentThread().getStackTrace()[1].getClassName()+'.'+Thread.currentThread().getStackTrace()[1].getMethodName();
            Response<Boolean> authResult = channelShopsService.isAuthRole(method, channel);
            checkState(authResult.isSuccess(), authResult.getError());
            // 检查是否需要授权验证
            checkArgument(!authResult.getResult(), "no.auth.to.access");

            Response<Long> roleResponse = channelShopsService.findRole1(channel);
            checkState(roleResponse.isSuccess(), roleResponse.getError());

            // 渠道 RRS PAD 只能使用默认的短信接口，这里负责生成验证码(手机验证码登陆的安全问题)
            if (Objects.equal(id.intValue(), 0)) {
                args = RandomStringUtils.randomNumeric(6);
            }

            Response<String> smsResult = channelShopsService.findSmsMessage(0==id.intValue()?1L:id);
            checkState(smsResult.isSuccess(), smsResult.getError());
            String smsMsg = "";
            if (!isEmpty(args)) {
                String[] smsArgs = args.split(" ");
                smsMsg = MessageFormat.format(smsResult.getResult(), smsArgs);
            } else {
                smsMsg = smsResult.getResult();
            }

            String[] mobileArr = mobile.split(" ");
            // 限制群发数量
            checkArgument(mobileArr.length<=SMS_GROUP_MAX, "mobile.numbers.too.large.");
            if (mobileArr.length <= 1) {
                checkArgument(mobilePattern.matcher(mobile).matches(), "incorrect.mobile");
                Response<Boolean> mobileCheckResult = channelShopsService.checkMobileSendable(mobile);
                checkState(mobileCheckResult.isSuccess(), mobileCheckResult.getError());

                Response<Boolean> smsSent = smsService.sendSingle("000000", mobile, smsMsg);
                checkState(smsSent.isSuccess(), smsSent.getError());

                channelShopsService.setMobileSent(mobile, args, id);
            } else {
                Iterable<String> iterable= splitter.split(mobile);
                Iterator iterator = iterable.iterator();
                while(iterator.hasNext()) {
                    checkArgument(mobilePattern.matcher((CharSequence) iterator.next()).matches(), "incorrect.mobile");
                }
                Response<Boolean> smsSent = smsService.sendGroup("000000", iterable, smsMsg);
                checkState(smsSent.isSuccess(), smsSent.getError());
            }

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("failed to sent msg with channel:{}, ip:{}, id:{}, mobile:{}, error:{}", channel, RequestUtils.getIpAddr(request), id, mobile, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to sent msg with channel:{}, ip:{}, id:{}, mobile:{}, error:{}", channel, RequestUtils.getIpAddr(request), id, mobile, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to sent msg with channel:{}, ip:{}, id:{}, mobile:{}, error:{}", channel, RequestUtils.getIpAddr(request), id, mobile, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.send.smg"));
        }
        return result;
    }

    /**
     * 用户登录
     *
     * @param loginId   登录凭证(登录名|邮箱|手机), 必填
     * @param password  密码, 必填
     * @param type      登录类型 1:邮箱 2:手机 3:登录名 99:验证码登陆
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     *
     * @return 用户id
     */
    @RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> login(@RequestParam("loginId") String loginId,
                                     @RequestParam("password") String password,
                                     @RequestParam(value = "type", defaultValue = "1") Integer type,
                                     @RequestParam("channel") String channel,
                                     @RequestParam("sign") String sign,
                                     HttpServletRequest request, HttpServletResponse response) {

        log.info("/login accepted request with channel:{}, ip:{}, loginId:{}, password:{}, type:{}",
                channel, RequestUtils.getIpAddr(request), loginId, password, type);

        HaierResponse<Long> result = new HaierResponse<Long>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            User user = new User();

            // 通过手机验证码登陆
            if (Objects.equal(type, 99)) {
                String method = Thread.currentThread().getStackTrace()[1].getClassName()+'.'+Thread.currentThread().getStackTrace()[1].getMethodName();
                Response<Boolean> authResult = channelShopsService.isAuthRole(method, channel);
                checkState(authResult.isSuccess(), authResult.getError());
                // 检查是否需要授权验证
                checkArgument(!authResult.getResult(), "no.auth.to.access");

                Response<Boolean> booleanResponse = channelShopsService.validateMobileCode(loginId, password);
                checkState(booleanResponse.isSuccess(), booleanResponse.getError());

                Response<User> userResponse = accountService.findUserByMobile(loginId);
                checkState(userResponse.isSuccess(), userResponse.getError());

                user = userResponse.getResult();
            } else {
                LoginType loginType = LoginType.from(type);
                checkArgument(notNull(loginType), "incorrect.login.type");


                Response<User> loginResult = accountService.userLogin(loginId, loginType, password);
                checkState(loginResult.isSuccess(), loginResult.getError());

                user = loginResult.getResult();
            }

            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
            LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
            userEventBus.post(loginEvent);

            result.setSessionId(request);
            result.setResult(user.getId());

        } catch (IllegalArgumentException e) {
            log.error("fail to login with channel:{}, ip:{}, loginId:{}, password:{}, type:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), loginId, password, type, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to login with channel:{}, ip:{}, loginId:{}, password:{}, type:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), loginId, password, type, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to login with channel:{}, ip:{}, loginId:{}, password:{}, type:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), loginId, password, type, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("user.login.fail"));

        }
        return result;
    }

    /**
     * 忘记密码
     *
     * @param mobile        手机号
     * @param password      新密码
     * @param code          短信验证码
     * @param code          会话ID
     */
    @RequestMapping(value = "/forget-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> forgetPassword(@RequestParam("mobile") String mobile,
                                                 @RequestParam("password") String password,
                                                 @RequestParam("captcha") String code,
                                                 @RequestParam("session") String sessionId,
                                                 @RequestParam("channel") String channel,
                                                 @RequestParam("sign") String sign,
                                                 HttpServletRequest request) {

        log.info("/forget-password accepted request with channel:{}, ip:{}, mobile:{}, new password:{}, code:{}, sessionId:{}",
                channel, RequestUtils.getIpAddr(request), mobile, password, code, sessionId);

        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(mobile), "mobile.can.not.be.empty");
            checkArgument(notEmpty(password), "password.can.not.be.empty");
            checkArgument(notEmpty(code), "code.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            String method = Thread.currentThread().getStackTrace()[1].getClassName()+'.'+Thread.currentThread().getStackTrace()[1].getMethodName();
            Response<Boolean> authResult = channelShopsService.isAuthRole(method, channel);
            checkState(authResult.isSuccess(), authResult.getError());
            // 检查是否需要授权验证
            checkArgument(!authResult.getResult(), "no.auth.to.access");


            Response<User> userGetResult = accountService.findUserBy(mobile, LoginType.MOBILE);
            checkArgument(userGetResult.isSuccess(), userGetResult.getError());
            User user = userGetResult.getResult();
            //对于未激活过的用户,不允许修改密码
            checkState(notNull(user) && active(user), "user.not.active");


            Response<Boolean> booleanResponse = channelShopsService.validateMobileCode(mobile, code);
            checkState(booleanResponse.isSuccess(), booleanResponse.getError());


            Response<Boolean> resetPassResult = accountService.resetPassword(user.getId(), password);
            checkState(resetPassResult.isSuccess(), resetPassResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to reset password with channel:{}, ip:{}, mobile:{}, new password:{}, code:{}, sessionId:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), mobile, password, code, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to reset password with channel:{}, ip:{}, mobile:{}, new password:{}, code:{}, sessionId:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), mobile, password, code, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to reset password with channel:{}, ip:{}, mobile:{}, new password:{}, code:{}, sessionId:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), mobile, password, code, sessionId, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("user.password.reset.fail"));

        }
        return result;

    }

    private void checkIfMobileUsed(String phone) throws IllegalStateException{
        // 检查用户是否被注册
        checkState(notNull(phone)||notEmpty(phone), "user.mobile.duplicate"); // now empty as duplicate
        Response<User> userGetResult = accountService.findUserByMobile(phone);
        checkState(userGetResult.isSuccess(), userGetResult.getError());
        checkState(isNull(userGetResult.getResult()), "user.mobile.duplicate");
    }
}
