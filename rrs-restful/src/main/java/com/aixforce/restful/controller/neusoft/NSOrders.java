package com.aixforce.restful.controller.neusoft;

import com.aixforce.alipay.Bank;
import com.aixforce.alipay.exception.BankNotFoundException;
import com.aixforce.alipay.request.CallBack;
import com.aixforce.alipay.request.PayRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.NSSessionUID;
import com.aixforce.restful.util.Signatures;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.aixforce.rrs.code.service.ActivityBindService;
import com.aixforce.rrs.code.service.ActivityCodeService;
import com.aixforce.rrs.code.service.ActivityDefinitionService;
import com.aixforce.rrs.presale.dto.PreOrderPreSale;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.session.AFSession;
import com.aixforce.session.AFSessionManager;
import com.aixforce.trade.dto.HaierOrder;
import com.aixforce.trade.dto.OrderDescription;
import com.aixforce.trade.dto.PreOrder;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.service.CartService;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.rrs.settle.util.SettlementVerification.needSettlementAfterSuccess;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-19 1:19 PM  <br>
 * Author: xiao
 */
@Controller
@Slf4j
@RequestMapping("/api/extend/order")
public class NSOrders {

    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    private final static JavaType javaType= jsonMapper.createCollectionType(
            ArrayList.class, OrderComment.class);


    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private OrderCommentService orderCommentService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private ActivityBindService activityBindService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private Token token;

    @Value("#{app.alipayNotifySuffix}")
    private String notifyUrl;

    @Value("#{app.restkey}")
    private String key;

    private final AFSessionManager sessionManager = AFSessionManager.instance();

    private static final Splitter splitter = Splitter.on(" ").trimResults().omitEmptyStrings();


    private OrderDescription getOrderDescription(List<Long> ids) {
        try {

            Response<OrderDescription> descQueryResult = orderQueryService.getDescriptionOfOrders(ids);
            checkState(descQueryResult.isSuccess(), descQueryResult.getError());
            return descQueryResult.getResult();

        } catch (Exception e) {
            log.warn("fail to get order desc", e);
            return new OrderDescription();
        }

    }


    /**
     * 支付接口
     * @param orderId           订单号, 必填
     * @param sessionId         会话id, 必填
     * @param forwardUrl        前台跳转页面, 必填
     * @param title             商品标题, 必填
     * @param bank              如果要使用银行网关，需要传入银行的code, 选填, 默认支付宝
     * @param qr                是否启用二维码支付，选填, 默认不启用
     * @param uid               用户id, 必填
     * @param channel           渠道, 必填
     * @param sign              签名, 必填
     * @return                  支付宝即时到帐网关URL
     */
    @RequestMapping(value = "/pay/{uid}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    private HaierResponse<String> pay(@RequestParam("orderId") Long orderId,
                                      @RequestParam("session") String sessionId,
                                      @RequestParam("forward") String forwardUrl,
                                      @RequestParam(value = "title", defaultValue = "订单", required = false) String title,
                                      @RequestParam(value = "bank", required = false) String bank,
                                      @RequestParam(value = "qr", required = false) Boolean qr,
                                      @PathVariable Long uid,
                                      @RequestParam("channel") String channel,
                                      @RequestParam("sign") String sign,
                                      HttpServletRequest request) {

        HaierResponse<String> result = new HaierResponse<String>();

        try {
            checkArgument(notEmpty(forwardUrl), "forward.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.checkLogin(session, uid);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<Order> orderGetResult = orderQueryService.findById(orderId);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();
            checkState(equalWith(order.getBuyerId(), uidGetResult.getResult()), "order.not.belong.to.current.user");


            // 构建支付宝请求
            CallBack notify = new CallBack(notifyUrl);
            // 前台通知地址
            CallBack forward = new CallBack(forwardUrl);
            String tradeNo = order.getId() + "";



            OrderDescription description = getOrderDescription(Lists.newArrayList(order.getId()));


            PayRequest payRequest = PayRequest.build(token).title(description.getTitle())
                    .content(description.getContent())
                    .outerTradeNo(tradeNo).total(order.getFee())
                    .notify(notify).forward(forward);

            if (notNull(qr) && qr) {
                payRequest.enableQrCode();
            }

            if (!Strings.isNullOrEmpty(bank)) {
                try {
                    payRequest.defaultBank(Bank.from(bank));
                } catch (BankNotFoundException e) {
                    // ignore
                }
            }

            result.setResult(payRequest.url());

        } catch (IllegalArgumentException e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{}, sign:{}, error:{}",
                    orderId, title, bank, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{}, sign:{}, error:{}",
                    orderId, title, bank, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to create alipay url with orderId:{}, title:{}, bank:{}, sign:{}",
                    orderId, title, bank, sign, e);
            result.setError(messageSources.get("order.pay.fail"));
        }

        return result;
    }

    /**
     * 添加商品评价
     *
     * @param id            订单Id, 必填
     * @param json          json 字符串形势的评价对象, 必填
     * @param uid           用户的id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return              操作状态
     */
    @RequestMapping(value = "/{id}/comment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> comment(@PathVariable Long id,
                                          @RequestParam("data") String json,
                                          @RequestParam("uid") Long uid,
                                          @RequestParam("session") String sessionId,
                                          @RequestParam("channel") String channel,
                                          @RequestParam("sign") String sign,
                                          HttpServletRequest request) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(json), "data.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");


            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);

            checkState(idGet.isSuccess(), idGet.getError());
            List<OrderComment> comments = jsonMapper.fromJson(json, javaType);

            checkArgument(comments.size() > 0, "item.comment.can.not.be.empty");

            Response<User> userGetResult =  accountService.findUserById(uid);
            checkArgument(userGetResult.isSuccess(), userGetResult.getError());

            Response<List<Long>> commentCreateResult = orderCommentService.create(id, comments, userGetResult.getResult().getId());
            checkState(commentCreateResult.isSuccess(), commentCreateResult.getError());
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to create comment with itemId:{}, data:{}, uid:{}, sessionId:{}, error:{}",
                    id, json, uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to create comment with itemId:{}, data:{}, uid:{}, sessionId:{}, error:{}",
                    id, json, uid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to create comment with itemId:{}, data:{}, uid:{}, sessionId:{}",
                    id, json, uid, sessionId, e);
            result.setError(messageSources.get("item.comment.create.fail"));
        }

        return result;
    }


    /**
     * 获取单个订单信息
     *
     * @param orderId       订单id, 必填
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return 订单信息
     */
    @RequestMapping(value = "/{id}/detail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<HaierOrder> detail(@PathVariable(value = "id") Long orderId,
                                            @RequestParam("uid") Long uid,
                                            @RequestParam("session") String sessionId,
                                            @RequestParam("channel") String channel,
                                            @RequestParam(value = "sign") String sign,
                                            HttpServletRequest request) {

        HaierResponse<HaierOrder> result = new HaierResponse<HaierOrder>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());

            Response<HaierOrder> orderQueryResult = orderQueryService.findHaierOrderById(orderId);
            checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());
            HaierOrder haierOrder = orderQueryResult.getResult();


            checkState(equalWith(haierOrder.getBuyerId(), idGet.getResult()), "order.not.belong.to.current.user");

            result.setResult(haierOrder, key);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to query order detail with orderId:{}, uid:{}, session:{}, sign:{}, error:{}",
                    orderId, uid, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to query order detail with orderId:{}, uid:{}, session:{}, sign:{}, error:{}",
                    orderId, uid, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query order detail with orderId:{}, uid:{}, session:{}, sign:{}",
                    orderId, uid, sessionId, sign, e);
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }



    /**
     * 获取下单信息(订单确认页)
     *
     * @param skus          sku的json字符串, 必填
     * @param uid           用户id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return 订单信息
     */
    @RequestMapping(value = "/pre", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<PreOrder>> preOrder(@RequestParam(value = "skus") String skus,
                                                  @RequestParam("uid") Long uid,
                                                  @RequestParam("session") String sessionId,
                                                  @RequestParam("channel") String channel,
                                                  @RequestParam(value = "sign") String sign,
                                                  HttpServletRequest request) {
        HaierResponse<List<PreOrder>> result = new HaierResponse<List<PreOrder>>();

        try {

            checkArgument(notEmpty(skus), "skus.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> idGet = NSSessionUID.checkLogin(session, uid);
            checkState(idGet.isSuccess(), idGet.getError());


            BaseUser baseUser= UserUtil.getCurrentUser();//获取当前登陆用户

            Response<List<PreOrder>> preQueryResult = cartService.preOrder(baseUser,skus);
            checkState(preQueryResult.isSuccess(), preQueryResult.getError());

            result.setResult(preQueryResult.getResult(), key);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to pre order detail with skus:{}, uid:{}, session:{}, sign:{}, error:{}",
                    skus, uid, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to pre order with skus:{}, uid:{}, session:{}, sign:{}, error:{}",
                    skus, uid, sessionId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to pre order with skus:{}, uid:{}, session:{}, sign:{}",
                    skus, uid, sessionId, sign, e);
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }

    /**
     * 确认订单装状态，原始状态必须是卖家已发货
     *
     * @param oid           订单id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return              订单id
     */
    @RequestMapping(value = "/buyer/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> confirm(@RequestParam("id") Long oid,
                                       @RequestParam("session") String sessionId,
                                       @RequestParam("channel") String channel,
                                       @RequestParam("sign") String sign,
                                       HttpServletRequest request) {
        HaierResponse<Long> result = new HaierResponse<Long>();

        try {
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            // 校验用户是否存在
            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();



            Response<Order> orderGetResult = orderQueryService.findById(oid);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();
            // 校验订单的所有权
            checkState(equalWith(order.getBuyerId(), uid), "order.not.belong.to.current.user");
            // 校验订单的状态
            checkState(equalWith(order.getStatus(), Order.Status.DELIVERED.value()), "order.status.incorrect");

            // 确认订单
            Response<Boolean> confirmedResult = orderWriteService.confirm(order, uid);
            checkState(confirmedResult.isSuccess(), confirmedResult.getError());
            result.setResult(order.getId());

        } catch (IllegalArgumentException e) {
            log.error("failed to confirm order with oid:{}, session:{}, error:{}", oid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to confirm order with oid:{}, session:{}, error:{}", oid, sessionId, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to confirm order with oid:{}, session:{}", oid, sessionId, e);
            result.setError(messageSources.get("order.confirm.fail"));
        }

        return result;
    }

    /**
     * 卖家撤销订单，原始状态必须为待支付状态
     *
     * @param oid           订单id, 必填
     * @param sessionId     会话id, 必填
     * @param channel       渠道, 必填
     * @param sign          签名, 必填
     * @return              订单id
     */
    @RequestMapping(value = "/buyer/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> cancel(@RequestParam("id") Long oid,
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
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();


            Response<Order> orderGetResult = orderQueryService.findById(oid);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());
            Order order = orderGetResult.getResult();

            // 校验订单的所有权
            checkState(equalWith(order.getBuyerId(), uid), "order.not.belong.to.current.user");
            // 校验订单的状态
            checkState(equalWith(order.getStatus(), Order.Status.WAIT_FOR_PAY.value()), "order.status.incorrect");

            BaseUser buyer = new BaseUser();
            buyer.setId(uid);
            buyer.setTypeEnum(BaseUser.TYPE.BUYER);
            Response<Boolean> canceledResult = orderWriteService.cancelOrder(order, buyer);
            checkState(canceledResult.isSuccess(), canceledResult.getError());
            result.setResult(order.getId());

        } catch (IllegalArgumentException e) {
            log.error("failed to cancel order id:{}, session:{}, error:{}", oid, sessionId, e);
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("failed to cancel order id:{}, session:{}, error:{}", oid, sessionId, e);
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("failed to cancel order id:{}, session:{}", oid, sessionId, e);
            result.setError(messageSources.get("order.cancel.fail"));
        }

        return result;
    }

    /**
     * 预售下单预览页
     * @param skus 以json格式保存的Map<Long,Integer> key为skuId，value为sku数量
     * @param regionId 地区id
     */
    @RequestMapping(value = "/preSale/preOrder", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<PreOrderPreSale> preSalePreOrder(@RequestParam("skus") String skus,
                                                         @RequestParam("regionId") Integer regionId,
                                                         @RequestParam("channel") String channel,
                                                         @RequestParam("sign") String sign,
                                                         @RequestParam("session") String sessionId,
                                                         HttpServletRequest request) {
        HaierResponse<PreOrderPreSale> result = new HaierResponse<PreOrderPreSale>();

        try {

            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            // 需要登录验证
            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<User> userR = accountService.findUserById(uidGetResult.getResult());
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={},error code={}",uidGetResult.getResult(),userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }
            User user = userR.getResult();

            Response<PreOrderPreSale> preOrderPreSaleR = preSaleService.preOrderPreSale(skus, regionId, user);
            if(!preOrderPreSaleR.isSuccess()) {
                log.error("fail to find preSale preOrder by skus={}, regionId={}, error code:{}",
                        skus, regionId, preOrderPreSaleR.getError());
                result.setError(messageSources.get(preOrderPreSaleR.getError()));
                return result;
            }

            result.setResult(preOrderPreSaleR.getResult());
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to find preSale preOrder by skus={},regionId={},channel={},sign={},cause:{}",
                    skus, regionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.preOrder.fail"));
            return result;
        }
    }

    /**
     * 预售订单确认收货
     * @return 操作订单id
     */
    @RequestMapping(value = "/preSale/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> preSaleConfirm(@RequestParam("id") Long oid,
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
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            //这里不在做权限校验，因为service会校验
            Response<Order> orderR = orderQueryService.findById(oid);
            if(!orderR.isSuccess()) {
                log.error("fail to find order by id={}, error code;{}", oid, orderR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            Response<Boolean> orderUpdateR = orderWriteService.confirm(orderR.getResult(),uid);
            if(!orderUpdateR.isSuccess()) {
                log.error("fail to confirm order by id={}, buyerId={}, error code:{}",
                        oid, uid, orderUpdateR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            // 确认收货后需要创建结算信息
            try {
                Order confirmedOrder = getOrder(oid);
                createSettlementAfterConfirm(confirmedOrder);
            } catch (IllegalStateException e) {
                log.error("fail to create settlement of Order(id:{}), code:{}", oid, e.getMessage());
            }


            result.setResult(oid);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to confirm preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.confirm.fail"));
            return result;
        }
    }

    private Order getOrder(Long orderId) {
        Response<Order> orderR = orderQueryService.findById(orderId);
        checkState(orderR.isSuccess(), orderR.getError());
        return orderR.getResult();
    }


    private void createSettlementAfterConfirm(Order order) {
        //普通订单-货到付款-交易成功
        if (needSettlementAfterSuccess(order)) {
            Response<Long> createResult = settlementService.generate(order.getId());
            checkState(createResult.isSuccess(), createResult.getError());
        }
    }

    /**
     * 取消预售订单,买家取消或者卖家取消
     * 操作订单id
     */
    @RequestMapping(value = "/preSale/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> preSaleCancel(@RequestParam("id") Long oid,
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
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            //这里不在做权限校验，因为service会校验
            Response<Order> orderR = orderQueryService.findById(oid);
            if(!orderR.isSuccess()) {
                log.error("fail to find order by id={}, error code;{}", oid, orderR.getError());
                result.setError(messageSources.get(orderR.getError()));
                return result;
            }

            Response<Boolean> updateR = orderWriteService.cancelOrder(orderR.getResult(),userR.getResult());
            if(!updateR.isSuccess()) {
                log.error("fail to cancel order orderId={}, userId={}, error code:{}",
                        oid, uid, updateR.getError());
                result.setError(messageSources.get(updateR.getError()));
                return result;
            }

            //从预售订单列表移除
            Response<Boolean> removeR = preSaleService.removePreSaleOrder(oid);
            if (!removeR.isSuccess()) {
                log.error("fail to remove preSale order id={} from list, error code:{}",
                        oid, removeR.getError());
            }

            //对于分仓的预售还要恢复库存
            Response<Boolean> storageR = preSaleService.recoverPreSaleStorageIfNecessary(oid);
            if(!storageR.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", oid, storageR.getError());
            }

            //恢复预售购买限制
            Response<Boolean> preSaleBuyLimitR = preSaleService.recoverPreSaleBuyLimitIfNecessary(orderR.getResult());
            if(!preSaleBuyLimitR.isSuccess()) {
                log.error("failed to recover pre sale buy limit by order id={}, error code:{}",
                        orderR.getResult().getId(), preSaleBuyLimitR.getError());
            }

            result.setResult(oid);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to cancel preSale order orderId={}, session={}, channel={}, sign={}, cause:{}",
                    oid, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.cancel.fail"));
            return result;
        }
    }

    /**
     * 预售订单退货款
     * @return 操作子订单id
     */
    @RequestMapping(value = "/preSale/orderItem/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> preSaleOrderItemCancel(@RequestParam("orderItemId") Long orderItemId,
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
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());
            Long uid = uidGetResult.getResult();

            Response<User> userR = accountService.findUserById(uid);
            if(!userR.isSuccess()) {
                log.error("fail to find user by id={}, error code:{}", uid, userR.getError());
                result.setError(messageSources.get(userR.getError()));
                return result;
            }

            Response<Boolean> updateR = orderWriteService.cancelOrderItem(orderItemId);
            if(!updateR.isSuccess()) {
                log.error("fail to cancel preSale orderItem id={}, error code:{}",orderItemId, updateR.getError());
                result.setError(messageSources.get(updateR.getError()));
                return result;
            }

            result.setResult(orderItemId);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to cancel preSale orderItem id={}, session={}, channel={}, sign={}, cause:{}",
                    orderItemId, sessionId, channel, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("preSale.orderItem.refund.or.returnGood.fail"));
            return result;
        }
    }

    @RequestMapping(value = "/code/activities", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<List<ActivityDefinition>> getActivityByCode(@RequestParam("code") String code,
                                                      @RequestParam("skuIds") String skuString,
                                                      @RequestParam("session") String sessionId,
                                                      @RequestParam("channel") String channel,
                                                      @RequestParam("sign") String sign,
                                                      HttpServletRequest request) {
        HaierResponse<List<ActivityDefinition>> result = new HaierResponse<List<ActivityDefinition>>();

        try {

            checkArgument(notEmpty(sessionId), "session.id.can.not.be.empty");
            checkArgument(notEmpty(channel), "channel.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            // 校验签名, 先注释方便调试
            checkArgument(Signatures.verify(request, key), "sign.verify.fail");

            AFSession session = new AFSession(sessionManager, request, sessionId);
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Response<List<ActivityDefinition>> activityDefR = activityDefinitionService.findValidActivityDefinitionsByCode(code);
            if(!activityDefR.isSuccess() || activityDefR.getResult() == null) {
                log.error("fail to find activity def by code={}, error code={}",code, activityDefR.getError());
                result.setError(messageSources.get(activityDefR.getError()));
                return result;
            }
            List<ActivityDefinition> activityDefinitions = activityDefR.getResult();
            List<Long> skuIds = getSkuIds(skuString);

            Response<List<Sku>> skusR = itemService.findSkuByIds(skuIds);
            if(!skusR.isSuccess() || skusR.getResult() == null) {
                log.error("fail to find skus by ids={}, error code:{}", skuIds, skusR.getError());
                result.setError(messageSources.get(skusR.getError()));
                return result;
            }
            List<Sku> skus = skusR.getResult();
            List<Long> itemIds = Lists.transform(skus, new Function<Sku, Long>() {
                @Override
                public Long apply(Sku input) {
                    return input.getItemId();
                }
            });
            List<ActivityDefinition> filterActivityDef = Lists.newArrayList();
            for(ActivityDefinition activityDef : activityDefinitions) {
                Response<List<Long>> bindIdsR = activityBindService.findBindIdsByActivityId(activityDef.getId(), ActivityBind.TargetType.ITEM.toNumber());
                if(!bindIdsR.isSuccess()) {
                    log.error("fail to find bind ids by activityId={}, targetType=ITEM, error code:{}",
                            activityDef.getId(), bindIdsR.getError());
                    continue;
                }
                List<Long> bindIds = bindIdsR.getResult();
                //有适用商品，库存为空（不限制购买数量）或者库存大于0的才显示出来
                if(inUseRange(bindIds, itemIds) && (activityDef.getStock() == null || activityDef.getStock() > 0)) {
                    filterActivityDef.add(activityDef);
                }
            }

            result.setResult(filterActivityDef);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
                    code,skuString,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
                    code,skuString,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to find activity by code={},skuString={},session={},channel={},sign={}, cause:{}",
                    code,skuString,sessionId,channel,sign,Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("activityDefinition.select.failed"));
            return result;
        }
    }

    @RequestMapping(value = "/sku/discount", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Long> getSkuDiscount(@RequestParam("skuIds") String skuIds,
                                              @RequestParam("activityId") Long activityId,
                                              @RequestParam("code") String code,
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
            Response<Long> uidGetResult = NSSessionUID.getUserId(session);
            checkState(uidGetResult.isSuccess(), uidGetResult.getError());

            Map<Long, Integer> skuMap = jsonMapper.fromJson(skuIds, jsonMapper.createCollectionType(Map.class, Long.class, Integer.class));
            Response<List<Sku>> skusR = itemService.findSkuByIds(Lists.newArrayList(skuMap.keySet()));
            if(!skusR.isSuccess()) {
                log.error("fail to find skus by ids={},error code:{}", skuIds, skusR.getError());
                result.setError(messageSources.get(skusR.getError()));
                return result;
            }

            Response<ActivityDefinition> activityDefinitionR = activityDefinitionService.findActivityDefinitionById(activityId);
            if(!activityDefinitionR.isSuccess()) {
                log.error("fail to find activity def by id={}, error code:{}", activityId, activityDefinitionR.getError());
                result.setError(messageSources.get(activityDefinitionR.getError()));
                return result;
            }
            ActivityDefinition activityDef = activityDefinitionR.getResult();

            Response<List<Long>> itemIdsR = activityBindService.findBindIdsByActivityId(activityId, ActivityBind.TargetType.ITEM.toNumber());
            if(!itemIdsR.isSuccess()) {
                log.error("fail to find bind ids by activityId={}, TargetType=ITEM, error code:{}",
                        activityId, itemIdsR.getError());
                result.setError(messageSources.get(itemIdsR.getError()));
                return result;
            }
            List<Long> itemIds = itemIdsR.getResult();

            Long totalDiscount = 0l;
            Integer totalToBuy = 0;

            for(Sku sku : skusR.getResult()) {
                int quantity = skuMap.get(sku.getId());

                //sku在活动范围之内,sku价格高于优惠的金额
                if(itemIds.contains(sku.getItemId()) && sku.getPrice() > activityDef.getDiscount()) {
                    totalDiscount += quantity * activityDef.getDiscount();
                    totalToBuy += quantity;
                }
            }
            //如果库存为空，不需要做数量的判断
            if(activityDef.getStock() == null) {
                result.setResult(totalDiscount);
                return result;
            }

            Response<ActivityCode> activityCodeR = activityCodeService.findOneByActivityIdAndCode(activityId, code);
            if(!activityCodeR.isSuccess()) {
                log.error("fail to find usage by activityId={}, code={}, error code:{}",
                        activityId, code, activityCodeR.getError());
                result.setError(messageSources.get(activityCodeR.getError()));
                return result;
            }
            Integer usage = activityCodeR.getResult().getUsage();

            if((usage + totalToBuy) > activityDef.getStock()) {
                log.warn("activityDef id={} has used {}, want to use {}", activityDef.getId(), usage, totalToBuy);
                result.setError(messageSources.get("stock.not.enough"));
                return result;
            }
            result.setResult(totalDiscount);
            return result;

        }catch (IllegalStateException e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (IllegalArgumentException e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
            return result;
        }catch (Exception e) {
            log.error("fail to get sku discount by skuIds={}, activityId={}, code={}, session={},channel={},sign={},cause:{}",
                    skuIds,activityId,code,sessionId,channel,sign,Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("get.sku.discount.fail"));
            return result;
        }
    }

    private List<Long> getSkuIds(String skuString) {
        List<String> ids = splitter.splitToList(skuString);

        List<Long> skuIds = Lists.newArrayListWithCapacity(ids.size());
        for (String id : ids) {
            skuIds.add(Long.valueOf(id));
        }
        return skuIds;
    }

    private boolean inUseRange(List<Long> bindIds, List<Long> itemIds) {
        for(Long bindId : bindIds) {
            if(itemIds.contains(bindId))
                return true;
        }
        return false;
    }
}
