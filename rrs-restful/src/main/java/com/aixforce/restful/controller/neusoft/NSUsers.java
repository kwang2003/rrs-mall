package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.common.utils.NameValidator;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.dto.HaierUserDto;
import com.aixforce.restful.event.ThirdRegisterEvent;
import com.aixforce.restful.service.NeusoftHelperService;
import com.aixforce.restful.util.NSSessionUID;
import com.aixforce.restful.util.Signatures;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.rrs.code.service.ActivityBindService;
import com.aixforce.rrs.code.service.ActivityCodeService;
import com.aixforce.rrs.code.service.CodeUsageService;
import com.aixforce.rrs.presale.dto.FatOrderPreSale;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.sms.SmsService;
import com.aixforce.trade.dto.FatOrder;
import com.aixforce.trade.dto.RichOrderBuyerView;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.UserCart;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.trade.service.CartService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.trade.service.UserTradeInfoService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.dto.UserProfileDto;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.UserProfileService;
import com.aixforce.web.controller.api.CaptchaGenerator;
import com.aixforce.web.controller.api.userEvent.LoginEvent;
import com.aixforce.web.controller.api.userEvent.LogoutEvent;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.aixforce.web.controller.api.userEvent.UserProfileEvent;
import com.aixforce.web.controller.api.validator.SmsCountValidator;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsCouOrderItem;
import com.rrs.coupons.model.RrsCouUser;
import com.rrs.coupons.service.CouponsRrsService;
import com.rrs.coupons.service.RrsCouOrderItemService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.user.util.UserVerification.active;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 3/26/14
 * Time: 15:00
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping(value = "/api/extend/user")
public class NSUsers {

    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    private final Random random = new Random();

    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final Splitter splitter = Splitter.on('@').trimResults();

    @Autowired
    private NeusoftHelperService neusoftHelperService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private CaptchaGenerator captchaGenerator;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    OrderWriteService orderWriteService;

    @Autowired
    OrderQueryService orderQueryService;

    @Autowired
    GridService gridService;

    @Autowired
    private SmsService smsService;

    @Autowired
    CartService cartService;

    @Autowired
    PreSaleService preSaleService;

    @Autowired
    ActivityBindService activityBindService;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @Value("#{app.restkey}")
    private String key;

    @Value("#{app.mainSite}")
    private String mainSite;

    @Value("#{app.domain}")
    private String domain;

    @Autowired
    private UserEventBus userEventBus;

    @Autowired
    private SmsCountValidator smsCountValidator;

	@Autowired
    private CodeUsageService codeUsageService;
	
	@Autowired
    private ActivityCodeService activityCodeService;

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Autowired
    private CouponsRrsService couponsRrsService;

    @Autowired
    private RrsCouOrderItemService rrsCouOrderItemService;

    /**
     * 微信无密登录接口
     *
     *
     * @param channel       渠道, 必填
     * @param uid           用户id, 必填
     * @param timestamp     时戳,必填
     * @param token         登录令牌,必填
     * @param sign          签名,必填
     * @return  登录成功
     */
    @RequestMapping(value = "/weixin/{uid}/login", method = RequestMethod.POST ,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> loginWithNoPass(@RequestParam(value = "channel") String channel,
                                               @PathVariable("uid")  Long uid,
                                               @RequestParam("timestamp") String timestamp,
                                               @RequestParam("token") String token,
                                               @RequestParam("sign") String sign,
                                               HttpServletRequest request) {
        HaierResponse<Long> result = new HaierResponse<Long>();

        try {
            // 校验登录参数
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notNull(uid), "uid.can.not.be.empty");
            checkArgument(notNull(timestamp), "timestamp.can.not.be.empty");
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            DateTime date = DFT.parseDateTime(timestamp);

            // 校验时戳超时或失效
            checkState(Minutes.minutesBetween(date, DateTime.now()).isLessThan(Minutes.minutes(5)), "timestamp.ahead.or.expired");


            // 校验是否微信合法用户
            User user = getUser(uid);
            Integer thirdPartyType = user.getThirdPartType();
            checkState(equalWith(User.ThirdPartType.WEIXIN.value(), thirdPartyType), "third.party.account.type.incorrect");

            String thirdPartyId = user.getThirdPartId();
            checkState(notEmpty(thirdPartyId), "third.party.id.empty");

            // 校验登录令牌
            String expected = getEncryptedToken(uid, timestamp, thirdPartyId);
            checkState(equalWith(token, expected), "third.party.token.mismatch");

            // 持久化用户session
            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());


            result.setResult(user.getId());
            result.setSessionId(request);

        } catch (IllegalArgumentException e) {
            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, error:{}",
                    channel, uid, timestamp, token, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, error:{}",
                    channel, uid, timestamp, token, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to login with channel:{}, uid:{}, timestamp:{}, token:{}, sign:{}, cause:{}",
                    channel, uid, timestamp, token, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("user.login.fail"));
        }

        return result;
    }

    private User getUser(Long uid) {
        Response<User> userQueryResult = accountService.findUserById(uid);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }

    private String getEncryptedToken(Long uid, String timestamp, String thirdPartyId) {
        Map<String, Object> mappedToken = Maps.newTreeMap();
        mappedToken.put("uid", uid);
        mappedToken.put("timestamp", timestamp);

        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(mappedToken);

        return Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(thirdPartyId, Charsets.UTF_8).hash().toString();
    }

    @RequestMapping(value = "/improved/signup", method = RequestMethod.POST ,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<HaierUserDto> improvedSignUp(User user,
                                                      @RequestParam("password") String password,
                                                      @RequestParam(value = "channel") String channel,
                                                      @RequestParam(value = "active") String activity,
                                                      @RequestParam(value = "source") String from,
                                                      @RequestParam("sign") String sign,
                                                      @RequestParam(value = "type", defaultValue = "3") Integer loginType,
                                                      @RequestParam(value = "token", required = false) String token,
                                                      @RequestParam(value = "third", defaultValue = "false") Boolean third,
                                                      HttpServletRequest request) {


        HaierResponse<HaierUserDto> result = new HaierResponse<HaierUserDto>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notEmpty(user.getName()), "user.name.can.not.be.empty");
            checkArgument(notEmpty(password), "password.can.not.be.empty");
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");



            if (third) {  // 第三方登录时候需要token&mobile
                checkArgument(notEmpty(token) && notEmpty(user.getMobile()));
                user.setThirdPartId(token);
                user.setThirdPartType(User.ThirdPartType.WEIXIN.value());


                // 如果mobile已经存在的情况下，走微信绑定流程，返回应该是成功的标识
                User mobileUser = getUserByMobile(user.getMobile());
                if (notNull(mobileUser)) {   // 若手机已经被注册，则绑定token
                    mobileUser.setThirdPartId(token);
                    mobileUser.setThirdPartType(User.ThirdPartType.WEIXIN.value());
                    // 重新绑定不需要再次加密
                    mobileUser.setEncryptedPassword(null);

                    Response<Boolean> updatingResult = accountService.updateUser(mobileUser);
                    checkState(updatingResult.isSuccess(), updatingResult.getError());

                    HaierUserDto dto = new HaierUserDto(mobileUser.getId(), mobileUser.getName(),
                            mobileUser.getMobile(), mobileUser.getEmail(), token);
                    result.setResult(dto);
                    return result;
                }

            } else {  // 非第三方需要邮箱和手机至少有一个
                checkArgument(notEmpty(user.getEmail()) || notEmpty(user.getMobile()), "email.or.mobile.can.not.empty");
            }



            // 检测用户是否已注册
            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicate");
            Response<Boolean> existResult = accountService.userExists(user.getName(), LoginType.from(loginType));
            checkState(existResult.isSuccess(), existResult.getError());
            checkState(!existResult.getResult(), "user.name.duplicate");

            // 创建用户
            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());

            Response<Long> registerResult = accountService.createUser(user);
            checkState(registerResult.isSuccess(), registerResult.getError());
            Long userId = registerResult.getResult();

            // 异步记录注册统计信息
            userEventBus.post(new ThirdRegisterEvent(userId, user.getName(), channel, activity, from));
            HaierUserDto dto = new HaierUserDto(userId, user.getName(), user.getMobile(), user.getEmail(), token);
            result.setResult(dto);

        } catch (IllegalArgumentException e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to sign up with user:{}, channel:{}, active:{}, source:{}, sign:{}, token:{}, third:{}, error:{}",
                    user, channel, activity, from, sign, token, third, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("user.signup.fail"));
        }

        return result;
    }


    private User getUserByMobile(String mobile) {
        Response<User> userQueryResult = accountService.findUserByMobile(mobile);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }




    /**
     * 新增用户接口
     *
     * @param user      包含基本信息的用户对象, 必填
     * @param code      短信验证码, 必填
     * @param password  密码, 必填
     * @param sessionId 会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名
     *
     * @return 注册成功的用户id
     */
    @RequestMapping(value = "/signup", method = RequestMethod.POST ,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> signUp(User user,
                                      @RequestParam("code") String code,
                                      @RequestParam("password") String password,
                                      @RequestParam("session") String sessionId,
                                      @RequestParam("channel") String channel,
                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {
        HaierResponse<Long> result = new HaierResponse<Long>();

        try {

            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notEmpty(user.getName()), "user.name.can.not.be.empty");
            checkArgument(notEmpty(user.getEmail()) || notEmpty(user.getMobile()), "email.or.mobile.can.not.empty");
            checkArgument(notEmpty(user.getMobile()), "mobile.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request ,sessionId);
            String _code = (String) session.getAttribute("code");

            checkArgument(notEmpty(_code), "sms.code.not.presence");
            checkArgument(equalWith(code, _code), "sms.code.mismatch");
            session.removeAttribute("code");

            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicate");
            checkIfMobileUsed(user.getMobile());

            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());

            Response<Long> uidGet = accountService.createUser(user);
            checkState(uidGet.isSuccess(), uidGet.getError());

            //用户注册成功之后自动登录
            session.setAttribute(CommonConstants.SESSION_USER_ID, uidGet.getResult());
            result.setResult(uidGet.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to signup with user:{}, code:{}, sessionId:{}, error:{}", user, code, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to signup with user:{}, code:{}, sessionId:{}, error:{}", user, code, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to signup with user:{}, code:{}, sessionId:{}", user, code, sessionId, e);
            result.setError(messageSources.get("user.signup.fail"));
        }

        return result;
    }


    /**
     * 更新用户资料
     *
     * @param userProfile   用户资料, 必填
     * @param avatar        头像, 选填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return  用户id
     */
    @RequestMapping(value = "/update-profile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> updateUser(UserProfile userProfile,
                                          @RequestParam(value = "avatar", required = false) String avatar,
                                          @RequestParam("session") String sessionId,
                                          @RequestParam("channel") String channel,
                                          @RequestParam("sign") String sign,
                                          HttpServletRequest request) {
        HaierResponse<Long> result = new HaierResponse<Long>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            Object o = session.getAttribute(CommonConstants.SESSION_USER_ID);
            checkArgument(notNull(o), "user.not.login.yet");


            Long uid = ((Integer)o).longValue();
            Response<User> userGet = accountService.findUserById(uid);
            checkState(userGet.isSuccess(), userGet.getError());
            BaseUser baseUser = userGet.getResult();

            User updated = new User();
            updated.setId(baseUser.getId());
            updated.setAvatar(avatar);

            Response<Boolean> updatingResult = accountService.updateUser(updated);
            checkState(updatingResult.isSuccess(), updatingResult.getError());

            userProfile.setUserId(baseUser.getId());
            Response<Boolean> profileUpdatingResult = userProfileService.updateUserProfileByUserId(userProfile);
            checkState(profileUpdatingResult.isSuccess(), profileUpdatingResult.getError());

            UserProfileEvent event = new UserProfileEvent(baseUser.getId());
            event.setName(baseUser.getName());
            event.setRealName(userProfile.getRealName());
            userEventBus.post(event);

            result.setSessionId(request);
            result.setResult(baseUser.getId(), key);
        } catch (IllegalArgumentException e) {
            log.error("fail to update user profile with userProfile:{}, avatar:{}, session:{}, error:{}",
                    userProfile, avatar, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to update user profile with userProfile:{}, avatar:{}, session:{}, error:{}",
                    userProfile, avatar, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update user profile with userProfile:{}, avatar:{}, session:{}",
                    userProfile, avatar, sessionId, e);
            result.setError(messageSources.get("user.profile.update.fail"));
        }
        return result;
    }

    /**
     * 发送验证码短信
     *
     * @param code      短信验证码, 必填
     * @param mobile    手机号, 必填
     * @param sign      签名, 必填
     *
     * @return  是否发送成功
     */
    @RequestMapping(value = "/sms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> sendSms(@RequestParam("code") String code,
                                          @RequestParam("mobile") String mobile,
                                          @RequestParam("channel") String channel,
                                          @RequestParam("sign") String sign,
                                          HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(code), "sms.code.invalid");
            checkArgument(notEmpty(mobile), "mobile.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            // 校验短信发送次数
            checkArgument(smsCountValidator.check(mobile), "sms.send.limit");


            checkArgument(mobilePattern.matcher(mobile).matches(), "incorrect.mobile");
            Response<Boolean> mobileCheckResult = neusoftHelperService.checkMobileSendable(mobile);
            checkState(mobileCheckResult.isSuccess(), mobileCheckResult.getError());
            // 检测用户手机号是否被注册
            checkIfMobileUsed(mobile);


            String codeSms = smsHelper(code, TYPE.CODE);
            Response<Boolean> smsSent = smsService.sendSingle("000000", mobile, codeSms);
            checkState(smsSent.isSuccess(), smsSent.getError());

            neusoftHelperService.setMobileSent(mobile, code);
            request.getSession().setAttribute("code", code);
            result.setSessionId(request);
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to send sms with code:{}, mobile:{}, error:{}", code, mobile, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to send sms with code:{}, mobile:{}, error:{}", code, mobile, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to send sms with code:{}, mobile:{}", code, mobile, e);
            result.setError(messageSources.get("sms.send.fail"));

        }
        return result;
    }

    /**
     * 获取用户信息
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return              用户信息
     */
    @RequestMapping(value = "/{uid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<UserProfileDto> userInfo(@PathVariable Long uid,
                                                  @RequestParam("session") String sessionId,
                                                  @RequestParam("channel") String channel,
                                                  @RequestParam("sign") String sign,
                                                  HttpServletRequest request) {
        HaierResponse<UserProfileDto> result = new HaierResponse<UserProfileDto>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());


            BaseUser baseUser = new BaseUser();
            baseUser.setId(uid);
            Response<UserProfileDto> profileGet = userProfileService.findUserProfileByUser(baseUser);
            checkState(profileGet.isSuccess(), profileGet.getError());

            result.setSessionId(request);
            result.setResult(profileGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to get userInfo with uid:{}, session:{}, error:{}", uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get userInfo with uid:{}, session:{}", uid, sessionId, e);
            result.setError(messageSources.get("user.profile.get.fail"));
        }
        return result;
    }

    /**
     * 获取验证码图片
     *
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @return  图片的BASE64
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<String> captcha(@RequestParam("channel") String channel,
                                         @RequestParam("sign") String sign,
                                         HttpServletRequest request) {
        HaierResponse<String> result = new HaierResponse<String>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            byte[] data = captchaGenerator.captcha(request.getSession());
            result.setResult(BaseEncoding.base64().encode(data), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to get captcha with channel:{}, sign:{}", channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get captcha with channel:{}, sign:{}", channel, sign, e.getMessage());
            result.setError("captcha.get.fail");
        }
        return result;
    }

    /**
     * 用户登录
     *
     * @param loginId   登录凭证(登录名|邮箱|手机), 必填
     * @param password  密码, 必填
     * @param type      登录类型 1:邮箱 2:手机 3:登录名
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

        HaierResponse<Long> result = new HaierResponse<Long>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            LoginType loginType = LoginType.from(type);
            checkArgument(notNull(loginType), "incorrect.login.type");


            Response<User> loginResult = accountService.userLogin(loginId, loginType, password);
            checkState(loginResult.isSuccess(), loginResult.getError());
            User user = loginResult.getResult();
            request.getSession().setAttribute(CommonConstants.SESSION_USER_ID, user.getId());
            LoginEvent loginEvent = new LoginEvent(user.getId(), request, response);
            userEventBus.post(loginEvent);

            result.setSessionId(request);
            result.setResult(user.getId());

        } catch (IllegalArgumentException e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}, error:{}", loginId, type, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}, error:{}", loginId, type, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to login with loginId:{}, type:{}, sign:{}", loginId, type, sign, e);
            result.setError(messageSources.get("user.login.fail"));

        }
        return result;
    }


    /**
     * 注销用户
     *
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return  是否注销成功
     */
    @RequestMapping(value = "/logout/{uid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> logout(@PathVariable Long uid,
                                         @RequestParam(value = "session", required = false) String sessionId,
                                         @RequestParam(value = "channel") String channel,
                                         @RequestParam(value = "sign") String sign,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            BaseUser baseUser = new BaseUser();
            baseUser.setId(((Integer)session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue());
            session.invalidate();

            //delete login token cookie
            LogoutEvent logoutEvent = new LogoutEvent(baseUser.getId(), request, response);
            userEventBus.post(logoutEvent);

            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalStateException e) {
            log.error("failed to logout user with uid:{}, session:{}, error:{}", uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        } catch (Exception e) {
            log.error("failed to logout user with uid:{}, session:{}", uid, sessionId, e);
            result.setError(messageSources.get("user.logout.fail"));
            return result;
        }
    }


    /**
     * 此接口暂时先不校验渠道以及签名
     *
     * 判断指定session的用户是否处于登录状态
     *
     * @param sessionId  会话id, 必填
     * @return 登录中的用户id
     */
    @RequestMapping(value = "/check-session", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> checkSession(@RequestParam(value = "session") String sessionId,
                                            HttpServletRequest request) {
        HaierResponse<Long> result = new HaierResponse<Long>();

        try {

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGet = NSSessionUID.getUserId(session);
            checkState(uidGet.isSuccess(), "user.not.login");
            result.setSessionId(sessionId);
            result.setResult(uidGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to check session with session:{}, error:{}", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to check session with session:{}", sessionId, e);
            result.setError(messageSources.get("user.session.check.fail"));
        }

        return result;
    }


    /**
     * 登录并返回指定的URL地址
     *
     * @param url           前台跳转的URL
     * @param loginId       登录id, 必填
     * @param password      密码, 必填
     * @param type          登录类型 1:邮箱 2:手机 3:登录名
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return 跳转的地址
     *
     */
    @RequestMapping(value = "/login-return", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String loginReturn(@RequestParam("returnURL") String url,
                              @RequestParam("loginId") String loginId,
                              @RequestParam("password") String password,
                              @RequestParam("type") Integer type,
                              @RequestParam("channel") String channel,
                              @RequestParam("sign") String sign,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        HaierResponse<Long> result = login(loginId, password, type, channel, sign, request, response);
        String forward = mainSite;

        try {

            if (result.isSuccess()) {
                forward = url;
            }

        } catch (Exception e) {
            log.error("fail to login-return with returnUrl:{}, loginId:{}, type:{}, sign:{}",url, loginId, type, sign, e);
            result.setError(messageSources.get("user.login.fail"));
        }

        return forward;
    }


    /**
     * 注销后跳转的地址
     *
     * @param url           前台跳转的URL
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @param uid           用户id, 必填
     * @return 注销成功跳转的URL
     */
    @RequestMapping(value = "/{uid}/logout-return", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String logoutReturn(@RequestParam("returnURL") String url,
                               @RequestParam("session") String sessionId,
                               @RequestParam("channel") String channel,
                               @RequestParam("sign") String sign,
                               @PathVariable Long uid,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        HaierResponse<Boolean> result = logout(uid, sessionId, channel, sign, request, response);
        String forward = mainSite;

        try {
            if (result.isSuccess()) {
                forward = url;
            }

        } catch (Exception e) {
            log.error("fail to login-return with url:{}, sessionId:{}, uid:{}", url, sessionId, uid, e);
            result.setError(messageSources.get("user.login.fail"));
        }

        return forward;
    }

    /**
     * 获取用户的配送地址
     *
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return  用户配送地址列表
     */
    @RequestMapping(value = "/{uid}/trade-infos", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<UserTradeInfo>> userTradeInfos(@PathVariable Long uid,
                                                             @RequestParam("session") String sessionId,
                                                             @RequestParam("channel") String channel,
                                                             @RequestParam("sign") String sign,
                                                             HttpServletRequest request) {
        HaierResponse<List<UserTradeInfo>> result = new HaierResponse<List<UserTradeInfo>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());


            Long userId = idGet.getResult();
            Response<List<UserTradeInfo>> infoGet = userTradeInfoService.findTradeInfosByUserId(userId);
            result.setResult(infoGet.getResult(), key);

        } catch (IllegalStateException e) {
            log.error("fail to query trade info with uid:{}, sessionId:{}, error:{}", uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query trade info with uid:{}, sessionId:{}", uid, sessionId);
            result.setError(messageSources.get("user.trade.info.query.fail"));
        }
        return result;
    }


    /**
     *
     * 获取买家购物车
     *
     * @param id    用户id
     * @return      购物车列表
     */
    @RequestMapping(value = "/{id}/cart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<UserCart>> cartItems(@PathVariable Long id,
                                                   @RequestParam("channel") String channel,
                                                   @RequestParam("sign") String sign,
                                                   HttpServletRequest request) {
        HaierResponse<List<UserCart>> result = new HaierResponse<List<UserCart>>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            Response<List<UserCart>> cartGet = cartService.getPermanent(new BaseUser(id, "", 1));
            result.setResult(cartGet.getResult());
        } catch (Exception e) {
            log.error("fail to get user cart with uid:{}", id);
            result.setError(messageSources.get("cart.get.fail"));
        }

        return result;
    }

    /**
     * 快速注册
     *
     * @param phone  手机号
     */
    @RequestMapping(value = "/fast-register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> fastRegister(@RequestParam("channel") String channel,
                                            @RequestParam("phone") String phone,
                                            @RequestParam("sign") String sign) {

        HaierResponse<Long> result = new HaierResponse<Long>();

        try {
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            checkArgument(mobilePattern.matcher(phone).matches(), "incorrect.mobile");

            String i = String.valueOf(random.nextInt());
            int l = i.length();
            String password = i.substring(l-6,l);

            // start check and set user
            // 检查手机号是否已经被注册
            checkIfMobileUsed(phone);  // throw illegal state exception
            User user = new User();
            user.setName(phone);
            user.setMobile(phone);
            checkArgument(NameValidator.isAllowedUserName(user.getName()), "user.name.duplicate");

            user.setEncryptedPassword(password);
            user.setType(BaseUser.TYPE.BUYER.toNumber());
            user.setStatus(User.STATUS.NORMAL.toNumber());
            // start check and set user

            // start send sms
            Response<Boolean> resultGet = neusoftHelperService.checkMobileSendable(phone);
            checkState(resultGet.isSuccess(), resultGet.getError());

            String passwordSms = smsHelper(password, TYPE.PASSWORD);

            checkState(smsCountValidator.check(phone), "sms.send.limit");
            Response<Boolean> smsSent = smsService.sendSingle("000000", phone, passwordSms);
            checkState(smsSent.isSuccess(), smsSent.getError());

            neusoftHelperService.setMobileSent(phone, password);
            // end send sms


            // create user
            Response<Long> uidGet = accountService.createUser(user);
            checkState(uidGet.isSuccess(), uidGet.getError());
            result.setResult(uidGet.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}, error:{}", channel, phone, sign, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}, error:{}", channel, phone, sign, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to fast-register with channel:{}, phone:{}, sign:{}", channel, phone, sign, e);
            result.setError("user.register.faster.fail");
        }

        return result;
    }

    /**
     * 忘记密码
     *
     * @param mobile        手机号
     * @param password      新密码
     * @param code          短信验证码
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

        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(mobile), "mobile.can.not.be.empty");
            checkArgument(notEmpty(password), "password.can.not.be.empty");
            checkArgument(notEmpty(code), "code.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");



            Response<User> userGetResult = accountService.findUserBy(mobile, LoginType.MOBILE);

            checkArgument(userGetResult.isSuccess(), userGetResult.getError());
            User user = userGetResult.getResult();

            //对于未激活过的用户,不允许修改密码
            checkState(notNull(user) && active(user), "user.not.active");
            checkArgument(isEmpty(code), "user.code.can.not.be.empty");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            String temp = (String) session.getAttribute("code");
            checkState(isEmpty(temp), "user.code.not.found");


            List<String> parts = splitter.splitToList(temp);
            String expected = parts.get(0);
            checkState(equalWith(code, expected), "user.code.mismatch");


            //如果匹配了code,则删除在session中的值
            request.getSession().removeAttribute("code");

            Response<Boolean> resetPassResult = accountService.resetPassword(user.getId(), password);
            checkState(resetPassResult.isSuccess(), resetPassResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to reset password with mobile:{}, session:{}, captcha:{}, error:{}",
                    mobile, sessionId, code, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to reset password with mobile:{}, session:{}, captcha:{}, error:{}",
                    mobile, sessionId, code, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to reset password with mobile:{}, session:{}, captcha:{}",
                    mobile, sessionId, code, e);
            result.setError(messageSources.get("user.password.reset.fail"));

        }
        return result;

    }


    /**
     * 更改密码
     *
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     */
    @RequestMapping(value = "/change-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> changePassword(@RequestParam("old") String oldPassword,
                                                 @RequestParam("new") String newPassword,
                                                 @RequestParam("session") String sessionId,
                                                 @RequestParam("channel") String channel,
                                                 @RequestParam("sign") String sign,
                                                 HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();


        try {
            checkArgument(notEmpty(oldPassword), "user.old.pass.empty");
            checkArgument(notEmpty(newPassword), "user.new.pass.empty");

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");

            Long uid = ((Integer)session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue();

            Response<Boolean> pwdChangeResult = accountService.changePassword(uid, oldPassword, newPassword);
            checkState(pwdChangeResult.isSuccess(), pwdChangeResult.getError());


            result.setSessionId(request);
            result.setResult(true);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to change with sessionId:{}, error:{} ", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to change with sessionId:{}, error:{} ", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to change with sessionId:{} ", sessionId, e);
            result.setError(messageSources.get("user.password.change.fail"));
        }

        return result;
    }


    /**
     * 获取用户订单列表
     * @param id           用户id
     * @param status       订单状态，选填
     * @param sessionId    会话id, 必填
     * @param channel      渠道, 必填
     * @param sign         签名, 必填
     */
    @RequestMapping(value = "/{id}/orders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Paging<RichOrderBuyerView>> orders(@PathVariable Long id,
                                                            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                            @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                            @RequestParam(value = "status", required = false) Integer status,
                                                            @RequestParam(value = "orderId", required = false) Long orderId,
                                                            @RequestParam("session") String sessionId,
                                                            @RequestParam("channel") String channel,
                                                            @RequestParam("sign") String sign,
                                                            HttpServletRequest request) {
        HaierResponse<Paging<RichOrderBuyerView>> result = new HaierResponse<Paging<RichOrderBuyerView>>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");



            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");

            BaseUser baseUser = new BaseUser();
            baseUser.setId(((Integer)session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue());
            Response<Paging<RichOrderBuyerView>> ordersGet = orderQueryService.findByBuyerId(baseUser, pageNo, size, status, orderId);
            result.setResult(ordersGet.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query orders with uid:{}, pageNo:{}, size:{}, session:{}, status:{}, error:{}",
                    id, pageNo, size, sessionId, status, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query orders with uid:{}, pageNo:{}, size:{}, session:{}, status:{}, error:{}",
                    id, pageNo, size, sessionId, status, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query orders with uid:{}, pageNo:{}, size:{}, session:{}, status:{}",
                    id, pageNo, size, sessionId, status, e);
            result.setError(messageSources.get("order.query.fail"));
        }
        return result;
    }

    /**
     * 提交订单接口
     * @param uid       登录用户id
     * @param tradeId   收获地址id
     * @param region    地区id
     * @param data      订单的json字符串
     */
    @RequestMapping(value = "/{uid}/order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<SimpleOrderInfo>> order(@PathVariable Long uid,
                                                      @RequestParam("tradeInfoId") Long tradeId,
                                                      @RequestParam("session") String sessionId,
                                                      @RequestParam("region") Integer region,
                                                      @RequestParam("data") String data,
                                                      @RequestParam("channel") String channel,
                                                      @RequestParam("sign") String sign,
                                                      @RequestParam(value = "couponsId", required = false) String couponsId,
                                                      @RequestParam(value = "bank", required = false) String bank,
                                                      HttpServletRequest request) {

        HaierResponse<List<SimpleOrderInfo>> result = new HaierResponse<List<SimpleOrderInfo>>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request ,sessionId);
            Response<Long> uidGetResult = NSSessionUID.checkLogin(session, uid);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long userId = uidGetResult.getResult();

            List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));
            checkState(!CollectionUtils.isEmpty(fatOrders), "data.de.serialize.fail");

            Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateOrder(fatOrders, region);
            checkState(filterFatOrders.isSuccess(), filterFatOrders.getError());

            Response<User> userR = accountService.findUserById(userId);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }
            User user = userR.getResult();

            //计算优惠价
            Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(fatOrders, user);
            if (!discountAndUsageR.isSuccess()) {
                log.error("fail to process order code discount. fatOrders={}, buyerId={},error code:{}",
                        fatOrders, user.getId(), discountAndUsageR.getError());
                throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
            }
            DiscountAndUsage discountAndUsage = discountAndUsageR.getResult();

            Response<Map<Long,Long>> orderResult = orderWriteService.create(userId, tradeId, fatOrders,discountAndUsage.getSkuIdAndDiscount(),bank);
            checkState(orderResult.isSuccess(), orderResult.getError());

            //使用完优惠券后记录使用情况
            recordCodeUsage(discountAndUsage, orderResult.getResult());

            // 返回 id 和 应付金额
            Map<Long, Long> sellerIdAndOrderId = orderResult.getResult();
            List<Long> orderIds = Lists.newArrayListWithCapacity(sellerIdAndOrderId.keySet().size());
            for(Long sellerId : sellerIdAndOrderId.keySet()) {
                orderIds.add(sellerIdAndOrderId.get(sellerId));
            }

            Response<List<Order>> orderGet = orderQueryService.findByIds(orderIds);
            checkState(orderResult.isSuccess(), orderGet.getError());

            List<Order> orders = orderGet.getResult();


            int total = 0;
            Set<Long> ids = Sets.newHashSet();

            List<SimpleOrderInfo> simpleOrderInfo = Lists.newArrayList();
            for (Order order:orders) {
                SimpleOrderInfo info = new SimpleOrderInfo();
                info.setExpress(order.getDeliverFee());
                info.setId(order.getId());

                ids.add(order.getId());
                total += order.getFee();

                info.setTotal(order.getFee());
                simpleOrderInfo.add(info);
            }

            //ids 为拆分之后的订单ID add by cwf
            //获取是否选择了优惠券信息
            if(!StringUtils.isEmpty(couponsId) && !couponsId.equals("-1")){
                total = changeCouponsOrderItem(couponsId,total,ids,userId);
            }

            result.setResult(simpleOrderInfo);
        } catch (IllegalArgumentException e) {
            log.error("fail to submit order with id:{}, tradeId:{}, session:{}, region:{}, data:{}, error:{}",
                    uid, tradeId, sessionId, region, data, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to submit order with id:{}, tradeId:{}, session:{}, region:{}, data:{}, error:{}",
                    uid, tradeId, sessionId, region, data, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to submit order with id:{}, tradeId:{}, session:{}, region:{}, data:{}",
                    uid, tradeId, sessionId, region, data, e);
            result.setError(messageSources.get("order.submit.fail"));

        }
        return result;
    }

    private int changeCouponsOrderItem(String couponsId,int total,Set<Long> ids,Long userId){
        log.debug("init changeCouponsOrderItem begin ...");
        int resultTotal = total;
        if(!StringUtils.isEmpty(couponsId)) {
            Response<RrsCou> rrsCouObj = couponsRrsService.queryCouponsById(Long.valueOf(couponsId));//根据ID查询对应的对象
            RrsCou rrsCou;
            if(rrsCouObj!=null && rrsCouObj.isSuccess()){
                rrsCou = rrsCouObj.getResult();
                resultTotal = total - rrsCou.getAmount();//总额 减去 优惠券面额优惠金额
//                -----------------------------------------------------------------
                List<OrderItem> orderItemsAllList = new ArrayList<OrderItem>();//根据订单信息获取订单明细(产品) 计算产品的优惠百分比

                Iterator<Long> it = ids.iterator();
                while(it.hasNext()){
                    Long id = it.next();
                    Response<List<OrderItem>> orderItemList =  orderQueryService.findSubsByOrderId(id);
                    if(orderItemList.isSuccess()){
                        orderItemsAllList.addAll(orderItemList.getResult());
                    }
                }

//                for (Long id : ids) {
//                    Response<List<OrderItem>> orderItemList =  orderQueryService.findOrderItemByOrderId(id);
//                    if(orderItemList.isSuccess()){
//                        orderItemsAllList.addAll(orderItemList.getResult());
//                    }
//                }

                List<OrderItem> orderItemsArrayList = new ArrayList<OrderItem>();//根据订单信息获取订单明细(产品) 计算产品的优惠百分比
                int totalCoupons = 0;
                if(!orderItemsAllList.isEmpty()){
                    Iterator<OrderItem> its = orderItemsAllList.iterator();
                    while(its.hasNext()){
                        OrderItem orderItem = its.next();
//                        Response<Long>  resultValue =  couponsRrsService.checkJoin(orderItem.getItemId());//判断该产品是否参加优惠分配
                        Response<Long>  resultValue =  couponsRrsService.checkJoinAndUser(orderItem.getItemId(),userId);//判断该产品是否参加优惠分配
                        if(resultValue.isSuccess()){
                            if (Objects.equal(resultValue.getResult(), 1L)) {
                                totalCoupons += orderItem.getFee();
                                orderItemsArrayList.add(orderItem);
                            }
                        }
                    }
                }

                if(!orderItemsArrayList.isEmpty()){
                    RrsCouOrderItem rrsCouOrderItem = new RrsCouOrderItem();
                    int joinItems = orderItemsArrayList.size();//总共参加优惠产品
                    double freeAmount = 0;//优惠金额 = (产品金额 / 参加优惠的产品总额之和) * 优惠券金额  最后一个产品是前面几个产品剩余优惠
                    int allFreeAmount = 0;//计算总共
                    int orderFreeAmount = 0;//计算订单的优惠价格
                    HashMap<Long,Integer> orderFreeMap = new HashMap<Long,Integer>();
                    for(int i=0;i<joinItems;i++){
                        OrderItem orderItem = orderItemsArrayList.get(i);

                        if((i+1)==joinItems){//最后一个产品是前面几个产品剩余优惠
                            freeAmount =  rrsCou.getAmount() - allFreeAmount;
                        }else{
                            double modeV = Math.round((orderItem.getFee().doubleValue() / Double.valueOf(totalCoupons)) * 100000);
                            freeAmount = Math.round(modeV * rrsCou.getAmount() / 100000);
                            allFreeAmount+=freeAmount;
                        }

                        int freeAmouts = Double.valueOf(freeAmount).intValue();

                        Long mapKey = orderItem.getOrderId();
                        if(orderFreeMap.containsKey(mapKey)){
                            Integer freeA = orderFreeMap.get(mapKey);
                            orderFreeMap.put(orderItem.getOrderId(),freeAmouts+freeA);//总订单的优惠金额计算
                        }else{
                            orderFreeMap.put(orderItem.getOrderId(),freeAmouts);//总订单的优惠金额计算
                        }

                        orderItem.setFee(orderItem.getFee()-freeAmouts);
                        orderWriteService.updateOrderItem(orderItem);

                        //用户使用优惠券 的对应产品优惠信息
                        rrsCouOrderItem.setCouponsId(Long.valueOf(couponsId));//优惠券Id
                        rrsCouOrderItem.setItemId(orderItem.getItemId());//订单明细ID
                        rrsCouOrderItem.setOrderId(orderItem.getOrderId());//订单ID
                        rrsCouOrderItem.setSkuId(orderItem.getSkuId());//增加 skuid字段用户查询
                        rrsCouOrderItem.setUserId(userId);//用户ID
                        rrsCouOrderItem.setFreeAmount(BigDecimal.valueOf(freeAmount));//优惠金额

                        rrsCouOrderItemService.saveCouOrderItem(rrsCouOrderItem);
                    }
                    //用户购买时会进行拆单 需要计算拆单之后该订单的优惠价格 即需要计算该订单下的产品优惠金额
                    if(!orderFreeMap.isEmpty()){
                        for(Long orderId : orderFreeMap.keySet()) {
                            Integer freeMapAmoutn = Integer.valueOf(orderFreeMap.get(orderId));
                            Response<Order> getOrder = orderQueryService.findById(orderId);
                            if (getOrder.isSuccess()) {
                                Order order = getOrder.getResult();
                                order.setFee(order.getFee()-freeMapAmoutn);
                                orderWriteService.updateOrder(order);
                            }
                        }
                    }
                }
//                -----------------------------------------------------------------
                //修改已使用优惠券信息 couponUse
                rrsCou.setCouponUse(rrsCou.getCouponUse()+1);
                couponsRrsService.updateRrsCou(rrsCou);

                Response<RrsCouUser> resutUser = couponsRrsService.queryCouponsUserBy(userId,Long.parseLong(couponsId));
                if(resutUser.isSuccess()){
                    RrsCouUser rrsCouUser = resutUser.getResult();
                    log.info("init rrsCouUser end ...coupuons"+rrsCouUser.getId());
                    couponsRrsService.updateCouponUser(rrsCouUser.getId());
                }
            }
        }
        log.debug("init changeCouponsOrderItem end ...coupuons"+resultTotal);
        return resultTotal;
    }

    private void recordCodeUsage(DiscountAndUsage discountAndUsage, Map<Long, Long> sellerIdAndOrderId) {
        Map<Long, CodeUsage> sellerIdAndCodeUsage = discountAndUsage.getSellerIdAndUsage();
        Map<Long, Integer> activityCodeIdAndUsage = discountAndUsage.getActivityCodeIdAndUsage();

        //设置codeUsage的orderId
        List<CodeUsage> creates = Lists.newArrayList();
        for (Long sellerId : sellerIdAndCodeUsage.keySet()) {
            Long orderId = sellerIdAndOrderId.get(sellerId);
            CodeUsage cu = sellerIdAndCodeUsage.get(sellerId);
            cu.setOrderId(orderId);
            creates.add(cu);
        }
        //调用批量创建接口
        Response<Boolean> batchCreateR = codeUsageService.batchCreateCodeUsage(creates);
        if (!batchCreateR.isSuccess()) {
            log.error("fail to batch create codeUsage by creates={}, error code:{}", creates, batchCreateR.getError());
        }
        //调用批量更新优惠码使用数量的接口
        Response<Boolean> updateR = activityCodeService.batchUpdateByIds(activityCodeIdAndUsage);
        if (!updateR.isSuccess()) {
            log.error("fail to batch update activityCode usage by map={}, error code={},",
                    activityCodeIdAndUsage, updateR.getError());
        }

    }

    /**
     * 创建预售订单
     * @param uid 手机端登录的用户id
     * @param tradeId 收货地址id
     * @param region 区域信息
     * @param data   订单信息
     * @return 创建的订单id+定金id，组装成一个list返回
     */
    @RequestMapping(value = "/{uid}/preSale/order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<Long>> createPreSaleOrder(@PathVariable("uid") Long uid,
                                                  @RequestParam("tradeInfoId") Long tradeId,
                                                  @RequestParam("session") String sessionId,
                                                  @RequestParam("region") Integer region,
                                                  @RequestParam("data") String data,
                                                  @RequestParam(value = "bank", required = false) String bank,
                                                  @RequestParam("channel") String channel,
                                                  @RequestParam("sign") String sign,
                                                  HttpServletRequest request) {
        HaierResponse<List<Long>> result = new HaierResponse<List<Long>>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request ,sessionId);
            Response<Long> uidGetResult = NSSessionUID.checkLogin(session, uid);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long userId = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(userId);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }
            User user = userR.getResult();

            FatOrderPreSale fatOrderPreSale = JSON_MAPPER.fromJson(data, FatOrderPreSale.class);

            Long id = fatOrderPreSale.getPreSale().getId();

            Response<PreSale> pR = preSaleService.findById(id);
            if (!pR.isSuccess()) {
                log.error("failed to find presale by id({}), error code:{}", id, pR.getError());
                throw new JsonResponseException(pR.getError());
            }

            PreSale preSale = pR.getResult();
            if (!equalWith(preSale.getStatus(), PreSale.Status.RUNNING.value())) {
                log.error("presale(id={}) has not released", id);
                throw new JsonResponseException(500, messageSources.get("presell.not.release"));
            }

            //计算优惠码
            Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(
                    Lists.newArrayList(fatOrderPreSale), user);
            if(!discountAndUsageR.isSuccess()) {
                log.error("fail to process preSale order code discount, preSaleFatOrder={}, buyerId={},error code:{}",
                        fatOrderPreSale, user.getId(), discountAndUsageR.getError());
                throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
            }

            //创建预售订单
            Response<Long> createPreSaleR = preSaleService.createPreSaleOrder(userId, tradeId,region,fatOrderPreSale,
                    discountAndUsageR.getResult(), bank);
            if(!createPreSaleR.isSuccess()) {
                log.error("fail to create preSale order by userId={},tradeId={},region={},fatOrderPreSale={},discountAndUsage={},error code={}",
                        userId, tradeId, region, fatOrderPreSale, discountAndUsageR.getResult(), createPreSaleR.getError());
                result.setError(messageSources.get(createPreSaleR.getError()));
                return result;
            }

            Long earnestId = createPreSaleR.getResult();

            Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(earnestId);
            if(!orderItemR.isSuccess()) {
                log.error("fail to find orderItem by id={}, error code={}",earnestId, orderItemR.getError());
                result.setError(messageSources.get(orderItemR.getError()));
                return result;
            }

            Long orderId = orderItemR.getResult().getOrderId();

            result.setResult(Lists.newArrayList(orderId,earnestId));

            return result;

        }catch (IllegalStateException e) {
            log.error("fail to create preSale order by uid={}, tradeInfoId={}, session={}, region={}, data={}, channel={}, sign={}, cause:{}",
                    uid, tradeId, sessionId, region, data, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to create preSale order by uid={}, tradeInfoId={}, session={}, region={}, data={}, channel={}, sign={}, cause:{}",
                    uid, tradeId, sessionId, region, data, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to create preSale order by uid={}, tradeInfoId={}, session={}, region={}, data={}, channel={}, sign={}, cause:{}",
                    uid, tradeId, sessionId, region, data, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.order.create.fail"));
            return result;
        }
    }


    private String smsHelper(String sms, TYPE type) {
        switch (type) {
            case PASSWORD:
                return "您的初始登录密码是："+sms+"，请尽快更换。【日日顺商场】";
            case CODE:
                return "您的验证码是："+sms+"。【日日顺商场】";
            default:
                return null;
        }
    }

    private enum TYPE {
        PASSWORD,           // 用户的初始密码
        CODE         // 用来注册的验证码

    }

    private void checkIfMobileUsed(String phone) throws IllegalStateException{
        // 检查用户是否被注册
        checkState(notNull(phone)||notEmpty(phone), "user.mobile.duplicate"); // now empty as duplicate
        Response<User> userGetResult = accountService.findUserByMobile(phone);
        checkState(userGetResult.isSuccess(), userGetResult.getError());
        checkState(isNull(userGetResult.getResult()), "user.mobile.duplicate");
    }

    @ToString
    private static class SimpleOrderInfo {
        @Setter
        @Getter
        private Long id;

        @Setter
        @Getter
        private Integer total;

        @Setter
        @Getter
        private Integer express;
    }
}
