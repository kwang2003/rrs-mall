package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.NSSessionUID;
import com.aixforce.restful.util.Signatures;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.trade.service.UserTradeInfoService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;


/**
 * Date: 4/22/14
 * Time: 16:41
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Slf4j
@Controller
@RequestMapping("/api/extend/address")
public class NSAddresses {
    
    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    UserTradeInfoService userTradeInfoService;

    @Autowired
    MessageSources messageSources;

    @Value("#{app.restkey}")
    String key;

    /**
     * 设置一个默认的收获地址
     *
     * @param id        默认收货地址id, 必填
     * @param sessionId 会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     *
     * @return          操作状态
     */
    @RequestMapping(value = "/{id}/default", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> setDefault(@PathVariable Long id,
                                             @RequestParam("session") String sessionId,
                                             @RequestParam("channel") String channel,
                                             @RequestParam("sign") String sign,
                                             HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            Long userId = ((Integer) session.getAttribute(CommonConstants.SESSION_USER_ID)).longValue();

            Response<Boolean> setDefaultResult = userTradeInfoService.makeDefault(userId, id);
            checkState(setDefaultResult.isSuccess(), setDefaultResult.getError());

            result.setResult(Boolean.TRUE);
            result.setSessionId(request);

        } catch (IllegalArgumentException e) {
            log.error("fail to set default trade info with tradeInfoId:{}, session:{}, error:{}", id, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to set default trade info with tradeInfoId:{}, session:{}, error:{}", id, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to set default trade info with tradeInfoId:{}, session:{}", id, sessionId, e);
            result.setError(messageSources.get("trade.info.set.default.fail"));
        }
        return result;

    }

    /**
     * 创建收货地址
     *
     * @param tradeInfo     收货地址信息, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return 收货地址id
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> create(UserTradeInfo tradeInfo,
                                      @RequestParam("session") String sessionId,
                                      @RequestParam("channel") String channel,
                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {
        HaierResponse<Long> response = new HaierResponse<Long>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            checkState(validateTradeInfo(tradeInfo), "trade.info.invalid");
            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            tradeInfo.setUserId(uid);
            Response<Long> createResult = userTradeInfoService.create(tradeInfo);
            checkState(createResult.isSuccess(), createResult.getError());

            response.setResult(createResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to add new tradeInfo with tradeInfo:{}, sessionId:{}, error:{}", tradeInfo, sessionId, e.getMessage());
            response.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to add new tradeInfo with tradeInfo:{}, sessionId:{}, error:{}", tradeInfo, sessionId, e.getMessage());
            response.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add new tradeInfo with tradeInfo:{}, sessionId:{}", tradeInfo, sessionId, e);
            response.setError(messageSources.get("trade.info.create.fail"));
        }
        return response;
    }

    /**
     * 逻辑删除收货地址
     * @param id         收货地址id, 必填
     * @param sessionId  会话id, 必填
     * @param channel    渠道, 必填
     * @param sign       签名, 必填
     *
     * @return  是否创建成功
     */
    @RequestMapping(value = "/{id}/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> delete(@PathVariable("id") Long id,
                                         @RequestParam("session") String sessionId,
                                         @RequestParam("channel") String channel,
                                         @RequestParam("sign") String sign,
                                         HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.id.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<Boolean> deleteResult = userTradeInfoService.invalidate(id);
            checkState(deleteResult.isSuccess(), deleteResult.getError());
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}, session:{}, error:{}", id, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}, session:{}, error:{}", id, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete tradeInfo with tradeInfoId:{}, session:{}", id, sessionId, e);
            result.setError(messageSources.get("trade.info.delete.fail"));
        }
        return result;
    }


    /**
     * 修改收货地址,逻辑删除再创建
     *
     * @param tradeInfo     收货地址信息, 必填
     * @param tradeInfoId   收货地址id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     *
     * @return              新的收货地址id
     */
    @RequestMapping(value = "/{id}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> update(UserTradeInfo tradeInfo,
                                      @PathVariable("id") Long tradeInfoId,
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

            checkState(validateTradeInfo(tradeInfo), "trade.info.invalid");
            tradeInfo.setId(tradeInfoId);

            AFSession session = new AFSession(sessionManager, request, sessionId);
            checkState(notNull(session.getAttribute(CommonConstants.SESSION_USER_ID)), "user.not.login.yet");
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();
            tradeInfo.setUserId(uid);

            Response<Long> updateResult = userTradeInfoService.update(tradeInfo, uid);
            checkState(updateResult.isSuccess(), updateResult.getError());
            result.setResult(updateResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to update userTradeInfo with tradeInfo:{}, session:{}, sign{}, error:{}",
                    tradeInfo, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to update userTradeInfo with tradeInfo:{}, session:{}, sign{}, error:{}",
                    tradeInfo, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update userTradeInfo with tradeInfo:{}, session:{}, sign{}", tradeInfo, sessionId, sign, e);
            result.setError(messageSources.get("trade.info.update.fail"));
        }

        return result;
    }


    
    private Boolean validateTradeInfo(UserTradeInfo tradeInfo) {
        return !Objects.equal(tradeInfo, null) &&
                !Objects.equal(tradeInfo.getCityCode(), null) &&
                !Objects.equal(tradeInfo.getDistrictCode(), null) &&
                !Objects.equal(tradeInfo.getProvinceCode(), null) &&
                !Strings.isNullOrEmpty(tradeInfo.getName()) &&
                !Strings.isNullOrEmpty(tradeInfo.getPhone()) &&
                !Strings.isNullOrEmpty(tradeInfo.getStreet()) &&
                !Strings.isNullOrEmpty(tradeInfo.getZip());
    }
}
