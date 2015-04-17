package com.aixforce.open.controller;

import com.aixforce.common.model.Response;
import com.aixforce.open.util.NSSessionUID;
import com.aixforce.open.util.RequestUtils;
import com.aixforce.open.util.Signatures;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.shop.service.ChannelShopsService;
import com.aixforce.trade.model.DeliveryMethod;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderExtra;
import com.aixforce.trade.service.DeliveryMethodService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
/**
 * 物流
 *
 * Created by yjgsjone@163.com on 14-8-4.
 */
@Controller
@Slf4j
@RequestMapping("/api/open/logistics")
public class LogisticsController {


    @Autowired
    private ChannelShopsService channelShopsService;

    @Autowired
    MessageSources messageSources;

    @Autowired
    DeliveryMethodService deliveryMethodService;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    @Autowired
    private OrderQueryService orderQueryService;

    /**
     * 获取所有的配送方式
     * @param channel
     * @param sign
     * @param request
     * @return
     */
    @RequestMapping(value = "/delivery/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<List<DeliveryMethod>> deliveryAll(@RequestParam(value = "channel", required = true) String channel,
                                          @RequestParam(value = "sign", required = true) String sign,
                                          HttpServletRequest request) {
        log.info("/delivery/all accepted channel :{}, ip:{}",
                channel, RequestUtils.getIpAddr(request));

        Response<List<DeliveryMethod>> result = new Response<List<DeliveryMethod>>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");


            // 1送达时段
            result = deliveryMethodService.findBy(null, 1);
            checkState(result.isSuccess(), result.getError());
            List<DeliveryMethod> deliveryMethodList1 = result.getResult();

            // 2送达承诺
            result = deliveryMethodService.findBy(null, 2);
            checkState(result.isSuccess(), result.getError());
            List<DeliveryMethod> deliveryMethodList2 = result.getResult();


            List<DeliveryMethod> deliveryMethodList = Lists.newArrayList();
            for (DeliveryMethod deliveryMethod : deliveryMethodList1) {
                deliveryMethodList.add(deliveryMethod);
            }
            for (DeliveryMethod deliveryMethod : deliveryMethodList2) {
                deliveryMethodList.add(deliveryMethod);
            }

            result.setResult(deliveryMethodList);


        } catch (IllegalArgumentException e) {
            log.error("fail to get all delivery with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to get all delivery with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get all delivery with channel :{}, ip:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.get.all.delivery"));
        }
        return result;
    }

    /**
     * 修改配送方式
     * @param orderId 订单ID
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param deliverTime 送达时段
     * @param channel 频道ID
     * @param sign 签名密钥
     * @param request 请求对象
     * @return 修改结果true或者false
     */
    @RequestMapping(value = "/delivery/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> deliveryUpdate(@RequestParam Long orderId,
                                                         @RequestParam(value = "session", required = true) String sessionId,
                                                         @RequestParam Long userId,
                                                         @RequestParam String deliverTime,
                                                         @RequestParam(value = "channel", required = true) String channel,
                                                         @RequestParam(value = "sign", required = true) String sign,
                                                         HttpServletRequest request) {
        log.info("/delivery/update accepted channel :{}, ip:{}, orderId{}, sessionId:{}, userId:{}, deliverTime:{}",
                channel, RequestUtils.getIpAddr(request), orderId, sessionId, userId, deliverTime);

        Response<Boolean> result = new Response<Boolean>();

        try{

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            Response<String> keyResult = channelShopsService.findKey(channel);
            checkState(keyResult.isSuccess(), keyResult.getError());
            String key = keyResult.getResult();
            // 校验签名
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, userId);
            checkState(idGet.isSuccess(), idGet.getError());


            Response<Order> orderResponse = orderQueryService.findById(orderId);
            checkState(orderResponse.isSuccess(), orderResponse.getError());
            Order order = orderResponse.getResult();
            // 查看是否为当前用户的订单
            if (!Objects.equal(order.getBuyerId().intValue(), userId.intValue())) {
                throw new Exception("userId.not.match");
            }

            Response<OrderExtra> resultOrderExtra = orderQueryService.getOrderExtraByOrderId(orderId);
            checkState(resultOrderExtra.isSuccess(), resultOrderExtra.getError());

            OrderExtra exitOrderExtra = resultOrderExtra.getResult();
            if(exitOrderExtra==null){
                throw new Exception("OrderExtra.is.null");
            }
            OrderExtra orderExtra = new OrderExtra();
            orderExtra.setDeliverTime(deliverTime);
            orderExtra.setId(exitOrderExtra.getId());
            orderExtra.setOrderId(orderId);
            result=orderQueryService.updateOrderExtra(orderExtra);

            return result;
        } catch (IllegalArgumentException e) {
            log.error("fail to update delivery with channel :{}, ip:{}, orderId{}, sessionId:{}, userId:{}, deliverTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), orderId, sessionId, userId, deliverTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to update delivery with channel :{}, ip:{}, orderId{}, sessionId:{}, userId:{}, deliverTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), orderId, sessionId, userId, deliverTime, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update delivery with channel :{}, ip:{}, orderId{}, sessionId:{}, userId:{}, deliverTime:{}, error:{}",
                    channel, RequestUtils.getIpAddr(request), orderId, sessionId, userId, deliverTime, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("fail.to.update.delivery"));
        }
        return result;
    }

}
