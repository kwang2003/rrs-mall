package com.aixforce.restful.controller.neusoft;

import com.aixforce.common.model.Response;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.NSSessionUID;
import com.aixforce.restful.util.Signatures;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.trade.model.UserCart;
import com.aixforce.trade.service.CartService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 4/23/14
 * Time: 17:08
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@RequestMapping("/api/extend/cart")
@Slf4j
public class NSCarts {

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    CartService cartService;

    @Autowired
    MessageSources messageSources;

    @Value("#{app.restkey}")
    String key;

    /**
     * 当前用户的购物车内容
     *
     * @param sessionId 会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     *
     * @return          用户购物车内容对象列表
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<UserCart>> list(@RequestParam("session") String sessionId,
                                              @RequestParam("channel") String channel,
                                              @RequestParam("sign") String sign,
                                              HttpServletRequest request) {
        HaierResponse<List<UserCart>> result = new HaierResponse<List<UserCart>>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.getUserId(session);
            checkState(idGet.isSuccess(), idGet.getError());

            BaseUser baseUser = new BaseUser();
            baseUser.setId(idGet.getResult());
            Response<List<UserCart>> cartGetResult  = cartService.getPermanent(baseUser);
            checkState(cartGetResult.isSuccess(), cartGetResult.getError());
            result.setResult(cartGetResult.getResult(), key);

        } catch (IllegalArgumentException e) {
            log.error("fail to query cart list with session:{}, error:{}", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query cart list with session:{}, error:{} ", sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query cart list with session:{}, e:{} ", sessionId, e);
            result.setError(messageSources.get("user.cart.query.fail"));
        }
        return result;
    }

    /**
     * 批量删除用户购物车内容
     *
     * @param skuIds    将删除的 sku ID 数组字符串，用“，”分割, 必填
     * @param sessionId 登录会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     *
     * @return          操作状态
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> remove(@RequestParam("skuIds") String skuIds,
                                         @RequestParam("channel") String channel,
                                         @RequestParam("session") String sessionId,
                                         @RequestParam("sign") String sign,
                                         HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {

            checkArgument(notEmpty(skuIds), "skus.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");


            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.getUserId(session);
            checkState(idGet.isSuccess(), idGet.getError());

            List<String> parts = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(skuIds);
            List<Long> ids = Lists.newArrayListWithCapacity(parts.size());

            for(String _id : parts) {
                ids.add(Long.parseLong(_id));
            }

            checkState(!ids.isEmpty(), "skus.id.can.not.be.empty");

            Response<Boolean> cartDelResult = cartService.batchDeletePermanent(idGet.getResult(), ids);
            checkState(cartDelResult.isSuccess(), cartDelResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalStateException e) {
            log.error("fail to delete carts with skuIds:{}, session:{}, error:{}",
                    skuIds, sessionId, e.getMessage());
            result.setError(messageSources.get("user.cart.delete.fail"));
        } catch (Exception e) {
            log.error("fail to delete carts with skuIds:{}, session:{}",
                    skuIds, sessionId, e);
            result.setError(messageSources.get("user.cart.delete.fail"));
        }

        return result;
    }

    /**
     * 清空用户购物车
     *
     * @param sessionId 会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @return          操作状态, 必填
     */
    @RequestMapping(value = "/empty", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> empty(@RequestParam("session") String sessionId,
                                        @RequestParam("channel") String channel,
                                        @RequestParam("sign") String sign,
                                        HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");


            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<Boolean> emptyCartResult = cartService.empty(uidGetResult.getResult().toString());
            checkState(emptyCartResult.isSuccess(), emptyCartResult.getError());

            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to empty cart with session:{}, error:{}", sessionId, e.getMessage());
            result.setError(messageSources.get("user.cart.empty.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to empty cart with session:{}, error:{}", sessionId, e.getMessage());
            result.setError(messageSources.get("user.cart.empty.fail"));
        } catch (Exception e) {
            log.error("fail to empty cart with session:{}", sessionId, e);
            result.setError(messageSources.get("user.cart.empty.fail"));
        }
        return result;
    }


    /**
     * 变更购物车中的商品
     *
     * @param skuId     库存编号, 必填
     * @param quantity  数量, 必填
     * @param sessionId 会话id, 必填
     * @param channel   渠道, 必填
     * @param sign      签名, 必填
     * @return          操作状态, 必填
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Integer> add(@RequestParam("skuId") Long skuId,
                                      @RequestParam("quantity") Integer quantity,
                                      @RequestParam("session") String sessionId,
                                      @RequestParam("channel") String channel,
                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        HaierResponse<Integer> result = new HaierResponse<Integer>();

        try {
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");
            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<Integer> changeResult = cartService.changePermanentCart(uidGetResult.getResult(), skuId, quantity);
            checkState(changeResult.isSuccess(), changeResult.getError());
            result.setResult(changeResult.getResult());

        } catch (IllegalArgumentException e) {
            log.error("fail to add cart with sku:{}, session:{}, error:{}", skuId, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to add cart with sku:{}, session:{}, error:{}", skuId, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add cart with sku:{}, session:{}", skuId, sessionId, e);
            result.setError(messageSources.get("user.cart.empty.fail"));
        }


        return result;
    }



}
