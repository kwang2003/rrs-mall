/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api;

import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.agreements.service.PreAuthorizationDepositOrderService;
import com.aixforce.alipay.Bank;
import com.aixforce.alipay.dto.AlipayRefundData;
import com.aixforce.alipay.event.AlipayEventBus;
import com.aixforce.alipay.event.TradeCloseEvent;
import com.aixforce.alipay.exception.BankNotFoundException;
import com.aixforce.alipay.mpiPay.MPIRequest;
import com.aixforce.alipay.request.*;
import com.aixforce.api.service.CpsService;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.buying.service.BuyingActivityDefinitionService;
import com.aixforce.rrs.buying.service.BuyingItemService;
import com.aixforce.rrs.buying.service.BuyingOrderRecordService;
import com.aixforce.rrs.buying.service.BuyingTempOrderService;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.rrs.code.service.ActivityBindService;
import com.aixforce.rrs.code.service.ActivityCodeService;
import com.aixforce.rrs.code.service.ActivityDefinitionService;
import com.aixforce.rrs.code.service.CodeUsageService;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.predeposit.dto.FatOrderPreDeposit;
import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.dto.FatOrderPreSale;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.trade.dto.*;
import com.aixforce.trade.model.*;
import com.aixforce.trade.service.*;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.components.Express100;
import com.aixforce.web.controller.api.userEvent.CpsEvent;
import com.aixforce.web.controller.api.userEvent.SmsEvent;
import com.aixforce.web.controller.api.userEvent.SmsEventBus;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsCouOrderItem;
import com.rrs.coupons.model.RrsCouUser;
import com.rrs.coupons.model.RrsCouponsItemList;
import com.rrs.coupons.service.CouponsItemListService;
import com.rrs.coupons.service.CouponsRrsService;
import com.rrs.coupons.service.RrsCouOrderItemService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.rrs.settle.util.SettlementVerification.*;
import static com.google.common.base.Preconditions.*;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-11
 */
@Controller
@RequestMapping("/api")
public class Orders {

    private final static Logger log = LoggerFactory.getLogger(Orders.class);
    public final static JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();


    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private GridService gridService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private PreSaleService rreSaleService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CouponsRrsService couponsRrsService;

    @Autowired
    private BuyingOrderRecordService buyingOrderRecordService;

    @Autowired
    private CodeUsageService codeUsageService;

    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private ActivityBindService activityBindService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private RrsCouOrderItemService rrsCouOrderItemService;


    @Autowired
    private BuyingActivityDefinitionService buyingActivityDefinitionService;

    @Autowired
    private BuyingItemService buyingItemService;


    @Autowired
    private OrderLogisticsInfoService orderLogisticsInfoService;

    @Autowired
    private OrderInstallInfoService orderInstallInfoService;

    @Autowired
    private OrderJobOverDayConfigService orderJobOverDayConfigService;

    @Autowired
    private Express100 express100;


    private static final Joiner joiner = Joiner.on(",").skipNulls();

    private static final Splitter splitter = Splitter.on(" ");

    @Autowired
    private Token token;

    @Autowired
    private AlipayEventBus alipayEventBus;


    @Value("#{app.alipayNotifySuffix}")
    private String notifyUrl;

    @Value("#{app.alipayFreezeNotifySuffix}")
    private String freezeNotifyUrl;

    @Value("#{app.alipayReturnSuffix}")
    private String returnUrl;

    @Value("#{app.mpiGateway}")
    private String mpiGateway;

    @Value("#{app.merchantID}")
    private String merchantID;

    @Autowired
    UserEventBus orderEventBus;
    
    @Autowired
    SmsEventBus smsEventBus;

    @Value("#{app.plazaKey}")
    private String plazaKey;

    @Autowired
    private BuyingTempOrderService buyingTempOrderService;

    @Autowired
    private PreDepositService preDepositService;

    @Autowired
    private PreAuthorizationDepositOrderService preAuthorizationDepositOrderService;

    @Value("#{app.alipayRefundSuffix}")
    private String refundNotifyUrl;

    @Autowired
    private CouponsItemListService couponsItemListService;
    /**
     * 根据 主订单号 获取订单描述信息
     *
     * @param ids 订单id列表
     * @return 订单描述信息
     */
    private OrderDescription getDescriptionOfOrders(List<Long> ids) {
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
     * 创建订单,然后跳转到付款的url
     *
     * @param data        FatOrder List的json表示形式
     * @param tradeInfoId 收获地址id
     * @return 新建订单的id
     */
    @RequestMapping(value = "/buyer/orders2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> directCreateOrdersAndPay2(@RequestParam("data") String data,
                                                         @RequestParam("tradeInfoId") Long tradeInfoId,
                                                         @CookieValue(value = "bank", required = false) String bank,
                                                         HttpServletRequest request) {


        // 试金行动 START
        List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));

        // 试金行动订单，sku自动收货天数设定。
        Map<String, Object> params = Maps.newLinkedHashMap();
        Response<Paging<OrderJobDayConfig>> orderJobDayConfigR = orderJobOverDayConfigService.findBy(params);

        if (orderJobDayConfigR.isSuccess()) {

            Map<Long, Integer> orderJobDayConfigMap = Maps.newLinkedHashMap();
            if (notNull(orderJobDayConfigR.getResult().getTotal()) && orderJobDayConfigR.getResult().getTotal() > 0 ) {

                for (OrderJobDayConfig orderJobDayConfig : orderJobDayConfigR.getResult().getData()) {

                    orderJobDayConfigMap.put(orderJobDayConfig.getSkuId(), orderJobDayConfig.getExpireDay());
                }

            }

            for (FatOrder fatOrder : fatOrders) {

                for (Long skuId : fatOrder.getSkuIdAndQuantity().keySet()) {
                    if (notNull(orderJobDayConfigMap.get(skuId))) {
                        return createOrdersAndReturnUrlAndOrderIdsForSku(data, tradeInfoId, bank, request);
                    }
                }
            }
        }
        // 试金行动 END

        return createOrdersAndReturnUrlAndOrderIds(data, tradeInfoId, bank, request);
    }


    /**
     * 创建订单,然后跳转到付款的url
     *
     * @param data        FatOrder List的json表示形式
     * @param tradeInfoId 收获地址id
     * @return 新建订单的id
     */
    @RequestMapping(value = "/buyer/orders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String directCreateOrdersAndPay(@RequestParam("data") String data,
                                           @RequestParam("tradeInfoId") Long tradeInfoId,
                                           @CookieValue(value = "bank", required = false) String bank,
                                           HttpServletRequest request) {

        Map<String, Object> mappedResult = createOrdersAndReturnUrlAndOrderIds(data, tradeInfoId, bank, request);
        return (String) mappedResult.get("url");
    }


    private Map<String, Object> createOrdersAndReturnUrlAndOrderIds(String data, Long tradeInfoId,
                                                                    String bank, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();
        User user = checkUserStatus(userId);
        List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));

        //remove if region not match
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionIdR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionIdR.isSuccess()) {
            log.error("fail to get region from cookie, error code:{}", regionIdR.getError());
            throw new JsonResponseException(500, messageSources.get(regionIdR.getError()));
        }
        Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateOrder(fatOrders, regionIdR.getResult());
        if (!filterFatOrders.isSuccess()) {
            log.error("fail to filter fatOrders, error code:{}", filterFatOrders.getError());
            throw new JsonResponseException(500, messageSources.get(filterFatOrders.getError()));
        }

        //计算优惠价
        Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(fatOrders, user);
        if (!discountAndUsageR.isSuccess()) {
            log.error("fail to process order code discount. fatOrders={}, buyerId={},error code:{}",
                    fatOrders, user.getId(), discountAndUsageR.getError());
            throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
        }
        DiscountAndUsage discountAndUsage = discountAndUsageR.getResult();

        Response<Map<Long, Long>> result = orderWriteService.create(userId, tradeInfoId,
                fatOrders, discountAndUsage.getSkuIdAndDiscount(), bank);
        if (!result.isSuccess()) {
            log.error("fail to create order for  {},error code:{}", data, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        //使用完优惠券后记录使用情况
        recordCodeUsage(discountAndUsage, result.getResult());

        boolean needRedirect = false;
        Map<Long, Long> maps = result.getResult();
        Set<Long> ids = Sets.newHashSet();
        for (Long id : maps.keySet()) {
            ids.add(maps.get(id));
        }
        int total = 0;
        List<Long> paidIds = Lists.newArrayList();
        for (Long id : ids) {
            Response<Order> getOrder = orderQueryService.findById(id);
            if (!getOrder.isSuccess()) {
                log.error("fail to get order for  {},error code:{}", data, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            Order order = getOrder.getResult();

            if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {
                needRedirect = true;
                total += order.getFee();
                paidIds.add(order.getId());
              }else{
            	  try {
            		 /*Shop shop=shopService.findByUserId(order.getSellerId()).getResult();
                 	 User user1=accountService.findUserById(order.getBuyerId()).getResult();
     	           	 if(isUserStatus(String.valueOf(order.getSellerId()),"2")){
     	              	sendSms(shop.getPhone(),
     	              	"您有一个新订单【"+order.getId()+"】，买家【"+user1.getMobile()+"】选择货到付款，请尽快安排发货。");       	
     	              }*/
            		  smsEventBus.post(new SmsEvent(order.getId(), order.getId(), "8"));
				} catch (Exception e) {
					log.error("fail to get sms exception"+e.getMessage());
				}
            	 
	          }
        }

        //商家优惠券的使用方法 平台优化是需要将平台券的使用和商家券的使用 方法 进行合并
        total =  getsellerCouonsByUse(fatOrders,total,ids,userId);

        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", ids);
        //ids 为拆分之后的订单ID add by cwf
        String couponsId =  request.getParameter("couponsId");//获取是否选择了优惠券信息
        if(!StringUtils.isEmpty(couponsId) && !couponsId.equals("-1")){
            total = changeCouponsOrderItem(couponsId,total,ids,userId);
        }
        log.info(" total == = "+total);
        // 亿起发CPS处理
//        orderEventBus.post(new OrderEvent(ids, request));
        orderEventBus.post(new CpsEvent( request,ids, CpsService.EventType.ORDER));


        if (needRedirect) {  // 有在线支付的订单时需要跳转支付页面
            String url = buildAlipayUrl(bank, total, paidIds);
            mappedResult.put("url", url);

            return mappedResult;
        } else { // 全订单都为货到付款时只需要返回空即可
            mappedResult.put("url", "");
        }

        return mappedResult;
    }

    /**
     * 创建商品套餐订单,然后跳转到付款的url
     *
     * @param data        FatOrder List的json表示形式
     * @param tradeInfoId 收获地址id
     * @return 新建订单的id
     */
    @RequestMapping(value = "/buyer/itemBundle/orders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> directCreateItemBundleOrderAndPay(@RequestParam("data") String data,
                                                    @RequestParam("tradeInfoId") Long tradeInfoId,
                                                    @CookieValue(value = "bank", required = false) String bank,
                                                    HttpServletRequest request) {
        return createItemBundleOrderAndReturnUrlAndOrderId(data, tradeInfoId, bank, request);
    }

    private Map<String,Object> createItemBundleOrderAndReturnUrlAndOrderId(String data, Long tradeInfoId,
                                                                           String bank, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();
        ItemBundleFatOrder itemBundleFatOrder = JSON_MAPPER.fromJson(data, ItemBundleFatOrder[].class)[0];

        //remove if region not match
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionIdR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionIdR.isSuccess()) {
            log.error("fail to get region from cookie, error code:{}", regionIdR.getError());
            throw new JsonResponseException(500, messageSources.get(regionIdR.getError()));
        }
        Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateOrder(Lists.newArrayList(itemBundleFatOrder), regionIdR.getResult());
        if (!filterFatOrders.isSuccess()) {
            log.error("fail to filter fatOrders, error code:{}", filterFatOrders.getError());
            throw new JsonResponseException(500, messageSources.get(filterFatOrders.getError()));
        }

        //创建组合商品订单
        Response<Long> result = orderWriteService.createItemBundle(userId, tradeInfoId, itemBundleFatOrder,bank);
        if(!result.isSuccess()) {
            log.error("fail to create item bundle order by userId={}, tradeInfoId={}, itemBundleFatOrder={}, error code:{}",
                    userId, tradeInfoId, itemBundleFatOrder, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        boolean needRedirect = false;
        Long orderId = result.getResult();
        int total = 0;
        List<Long> paidIds = Lists.newArrayList();

        Response<Order> getOrder = orderQueryService.findById(orderId);
        if (!getOrder.isSuccess()) {
            log.error("fail to get order for  {},error code:{}", data, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Order order = getOrder.getResult();

        if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {
            needRedirect = true;
            total += order.getFee();
            paidIds.add(order.getId());
        }

        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", orderId);

        if (needRedirect) {  // 有在线支付的订单时需要跳转支付页面
            String url = buildAlipayUrl(bank, total, paidIds);
            mappedResult.put("url", url);

            return mappedResult;
        } else { // 全订单都为货到付款时只需要返回空即可
            mappedResult.put("url", "");
        }

        return mappedResult;
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

    private User checkUserStatus(Long userId) throws JsonResponseException {

        //check user status, if user status is abnormal, return directly
        Response<User> ru = accountService.findUserById(userId);


        if (!ru.isSuccess()) {
            log.error("failed to find user(id={}), error code: {}", userId, ru.getError());
            throw new JsonResponseException(500, messageSources.get(ru.getError()));
        }

        User user = ru.getResult();

        if (!Objects.equal(User.STATUS.NORMAL.toNumber(), user.getStatus())) { //只有正常状态的用户才能下单
            log.error("user(id={}) status is abnormal, status={}", userId, user.getStatus());
            throw new JsonResponseException(500, messageSources.get("user.status.abnormal"));
        }
        return user;
    }


    private String buildAlipayUrl(String bank, String title, String content, int total, String tradeNo) {

        String orderTitleTemp = title;

        // 订单标题超长截取
        if (title != null && !title.equals("")) {

            if (title.length() > 15) {

                orderTitleTemp = title.substring(0, 14) + "...";
            }
        }

        // 电子钱包支付逻辑
        if ("E-WALLET".equals(bank)) {
            MPIRequest mpiPay = new MPIRequest();
            mpiPay.setMerOrderNum(tradeNo);
            mpiPay.setTranAmt(String.valueOf(total));
            mpiPay.setMerchantID(merchantID);
            return mpiPay.pay() + "@" + returnUrl + "@" + mpiGateway;
        }

        CallBack notify = new CallBack(notifyUrl);
        CallBack forward = new CallBack(returnUrl);

        PayRequest payRequest = PayRequest.build(token)
                .title(orderTitleTemp)
                .content(content)
                .outerTradeNo(tradeNo).total(total)
                .notify(notify).forward(forward);

        if (!Strings.isNullOrEmpty(bank)) {
            try {
                payRequest.defaultBank(Bank.from(bank));
            } catch (BankNotFoundException e) {
                // ignore
            }
        }
        return payRequest.url();
    }

    private String buildAlipayUrl(String bank, int total, List<Long> ids) {
        OrderDescription description = getDescriptionOfOrders(ids);
        String outerIds = Joiner.on(",").skipNulls().join(ids);

        return buildAlipayUrl(bank, description.getTitle(), description.getContent(), total, outerIds);
    }


    /**
     * 直接创建预售订单并支付定金
     *
     * @param data        提交数据
     * @param tradeInfoId 配送信息标识
     * @return 需要跳转的支付页面链接
     */
    @RequestMapping(value = "/buyer/pre-orders2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> directCreatePresellOrderAndPay2(@RequestParam("data") String data,
                                                               @RequestParam("tradeInfoId") Long tradeInfoId,
                                                               @CookieValue(value = "bank", required = false) String bank,
                                                               @RequestParam(value = "regionId") Integer regionId,
                                                               HttpServletRequest request) {

        return createPresellOrdersAndReturnUrlAndOrderIds(data, tradeInfoId, regionId, bank, request);
    }

    /**
     * 直接创建预售订单并支付定金
     *
     * @param data        提交数据
     * @param tradeInfoId 配送信息标识
     * @return 需要跳转的支付页面链接
     */
    @RequestMapping(value = "/buyer/pre-orders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String directCreatePresellOrderAndPay(@RequestParam("data") String data,
                                                 @RequestParam("tradeInfoId") Long tradeInfoId,

                                                 @CookieValue(value = "bank", required = false) String bank,
                                                 @RequestParam(value = "regionId") Integer regionId,
                                                 HttpServletRequest request) {


        Map<String, Object> mappedResult = createPresellOrdersAndReturnUrlAndOrderIds(data, tradeInfoId, regionId, bank, request);
        return (String) mappedResult.get("url");
    }


    private Map<String, Object> createPresellOrdersAndReturnUrlAndOrderIds(String data, Long tradeInfoId, Integer regionId, String bank, HttpServletRequest request) {


        Long userId = UserUtil.getUserId();

        User buyer = checkUserStatus(userId);

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
                Lists.newArrayList(fatOrderPreSale), buyer);
        if (!discountAndUsageR.isSuccess()) {
            log.error("fail to process preSale order code discount, preSaleFatOrder={}, buyerId={},error code:{}",
                    fatOrderPreSale, buyer.getId(), discountAndUsageR.getError());
            throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
        }


        Response<Long> createPreSale = preSaleService.createPreSaleOrder(userId, tradeInfoId, regionId, fatOrderPreSale,
                discountAndUsageR.getResult(), bank);

        if (!createPreSale.isSuccess()) {
            log.error("fail to create preSale order{}, error code={}", data, createPreSale.getError());
            throw new JsonResponseException(500, messageSources.get(createPreSale.getError()));
        }

        Long orderItemId = createPreSale.getResult();
        Response<OrderItemTotalFee> getOrderItem = orderQueryService.findExtraByOrderItemId(orderItemId);
        if (!getOrderItem.isSuccess()) {
            log.error("fail to get deposit orderItem{}, error code={}", orderItemId, getOrderItem.getError());
            throw new JsonResponseException(500, messageSources.get(getOrderItem.getError()));
        }
        OrderItem oi = getOrderItem.getResult();
        int total = oi.getFee();
        List<Long> ids = Lists.newArrayList(oi.getOrderId(), oi.getId());


        Set<Long> idsSet = Sets.newHashSet();
        idsSet.add(oi.getOrderId());
        // 亿起发CPS处理
//        orderEventBus.post(new OrderEvent(idsSet, request));
        orderEventBus.post(new CpsEvent(request,idsSet, CpsService.EventType.ORDER));

        String tradeNo = joiner.join(ids);
        String subject = getOrderItemSubject(oi);

        String url = buildAlipayUrl(bank, subject, oi.getItemName(), total, tradeNo);

        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", oi.getOrderId());
        mappedResult.put("url", url);
        return mappedResult;
    }

    @RequestMapping(value = "/buyer/buying-order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String buyingOrderCreate(@RequestParam("data") String data,
                                    @RequestParam("tradeInfoId") Long tradeInfoId,
                                    @CookieValue(value = "bank", required = false) String bank,HttpServletRequest request) {

        Map<String, Object> orderIdAndReturnUrl = buyingOrderCreateReturnUrlAndOrderId(data, tradeInfoId, bank,request);

        return (String) orderIdAndReturnUrl.get("url");
    }

    private Map<String, Object> buyingOrderCreateReturnUrlAndOrderId(String data, Long tradeInfoId, String bank,HttpServletRequest request) {

        Long userId = UserUtil.getUserId();

        checkUserStatus(userId);

        // 将前台传递的data数据转换成BuyingFatOrder对象
        BuyingFatOrder buyingFatOrder = JSON_MAPPER.fromJson(data, BuyingFatOrder.class);

        // 创建抢购订单
        Response<Long> orderIdR = buyingActivityDefinitionService.createBuyingOrder(UserUtil.getCurrentUser(),tradeInfoId,
                bank,buyingFatOrder);
        if(!orderIdR.isSuccess()) {
            log.error("fail to create buying order by data={},tradeInfoId={},bank={},error code:{}",
                    data, tradeInfoId, bank, orderIdR.getError());
            throw new JsonResponseException(500, messageSources.get(orderIdR.getError()));
        }

        Long orderId = orderIdR.getResult();

        Map<String, Object> resultMap = Maps.newHashMap();

        resultMap.put("orderIds", orderId);

        Response<Order> orderR = orderQueryService.findById(orderId);
        if (!orderR.isSuccess()) {
            log.error("fail to get order by id {},error code:{}", orderId, orderR.getError());
            throw new JsonResponseException(500, messageSources.get(orderR.getError()));
        }
        Order order = orderR.getResult();

        //CPS推送
        Set<Long> idsSet = Sets.newHashSet();
        idsSet.add(order.getId());
        orderEventBus.post(new CpsEvent(request,idsSet, CpsService.EventType.ORDER));

        // 有在线支付的订单时需要跳转支付页面
        if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {

            String url = buildAlipayUrl(bank, order.getFee(), Lists.newArrayList(orderId));

            resultMap.put("url", url);

        } else { // 全订单都为货到付款时只需要返回空即可
            resultMap.put("url", "");
        }

        return resultMap;
    }

    /**
     * 直接支付预售定金或者尾款
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/buyer/pre-orders/{id}/pay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String presellOrderPay(@PathVariable("id") Long orderItemId,
                                  @CookieValue(value = "bank", required = false) String bank,
                                  @RequestParam(value = "deliverTime", required = false) String deliverTime) {
        Response<OrderItemTotalFee> getOrderItem = orderQueryService.findExtraByOrderItemId(orderItemId);
        if (!getOrderItem.isSuccess()) {
            log.error("fail to get orderItem(id={}), error={}", orderItemId, getOrderItem.getError());
            throw new JsonResponseException(500, messageSources.get(getOrderItem.getError()));
        }



        OrderItem oi = getOrderItem.getResult();
        if (oi == null) {
            log.error("orderItem(id={}) not exist", orderItemId);
            throw new JsonResponseException(500, messageSources.get("order.item.not.exist"));
        }
        Long orderId = oi.getOrderId();

        Response<OrderExtra> resultOrderExtra = orderQueryService.getOrderExtraByOrderId(orderId);
        if (!resultOrderExtra.isSuccess()) {
            log.error("fail to get OrderExtra by order(id={}), error={}", orderId, resultOrderExtra.getError());
            throw new JsonResponseException(500, messageSources.get(resultOrderExtra.getError()));
        }
        OrderExtra exitOrderExtra = resultOrderExtra.getResult();
        if(exitOrderExtra!=null && notEmpty(deliverTime)){
            OrderExtra orderExtra = new OrderExtra();
            orderExtra.setDeliverTime(deliverTime);
            orderExtra.setId(exitOrderExtra.getId());
            orderExtra.setOrderId(orderId);
            Response<Boolean> result=orderQueryService.updateOrderExtra(orderExtra);
            if (!result.isSuccess()) {
                log.error("fail to update OrderExtra by order(id={}), error={}", orderId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        } else if(exitOrderExtra == null && notEmpty(deliverTime)) {
            exitOrderExtra = new OrderExtra();
            exitOrderExtra.setDeliverTime(deliverTime);
            exitOrderExtra.setOrderId(orderId);
            Response<Long> result = orderQueryService.createOrderExtra(exitOrderExtra);
            if (!result.isSuccess()) {
                log.error("fail to create OrderExtra by order(id={}), error={}", orderId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        }


        if (!Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value()) &&
                !Objects.equal(oi.getType(), OrderItem.Type.PRESELL_REST.value())) {
            log.error("orderItem(id={}) is not presell order", orderItemId);
            throw new JsonResponseException(500, messageSources.get("order.item.status.incorrect"));
        }

        Long userId = UserUtil.getUserId();
        if (!Objects.equal(oi.getBuyerId(), userId)) {
            log.error("current user(id={}) is not real buyer(id={}), orderItem({})",
                    userId, oi.getBuyerId(), oi);
            throw new JsonResponseException(messageSources.get("order.item.buyer.not.match"));
        }

        //判断当前支付渠道和子订单中的支付渠道是否相等
        if(needResetOrder(oi, bank)) { // 如果渠道号发生了变化，则修改子订单的id
            // 新建子订单
            Response<OrderItem> resetResult = orderWriteService.resetOrderItem(oi, bank);
            checkState(resetResult.isSuccess(), resetResult.getError());
            oi = resetResult.getResult();//重新赋值
            //尾款订单变更支付方式 要同步 尾款子结算中得订单id
            if(Objects.equal(oi.getType(),OrderItem.Type.PRESELL_REST.value())){
                Response<ItemSettlement> settlementResponse =settlementService.findByOrderIdAndType(oi.getOrderId(),OrderItem.Type.PRESELL_REST.value());
                if(!settlementResponse.isSuccess()){
                    log.error("find item settlement fail error:{}",settlementResponse.getError());
                    throw new JsonResponseException(500, messageSources.get(settlementResponse.getError()));
                }
                ItemSettlement itemSettlement = new ItemSettlement();
                itemSettlement.setId(settlementResponse.getResult().getId());
                itemSettlement.setOrderItemId(oi.getId());
                Response<Boolean> updateRes =settlementService.updateItemSettlement(itemSettlement);
                if(!updateRes.getResult()){
                    log.error("update item settlement order item id fail");
                    throw new JsonResponseException(500, messageSources.get(updateRes.getError()));
                }

            }

            // 通知关闭订单,这里的子订单号要使用修改前的子订单号
            alipayEventBus.post(new TradeCloseEvent(token, oi.getOrderId() + "," + orderItemId));
            orderItemId = oi.getId();

        }

        // 判断当前订单是否处于可以支付的状态
        if (!canPay(orderItemId)) {
            log.error("current presell orderItem(id={}) may expired or presell not finished", orderItemId);
            if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                throw new JsonResponseException(500, messageSources.get("presell.order.deposit.expire"));
            } else {
                throw new JsonResponseException(500, messageSources.get("presell.order.rest.expire.or.not.finish"));
            }
        }

        List<Long> ids = Lists.newArrayList(oi.getOrderId(), oi.getId());
        String tradeNo = joiner.join(ids);
        String subject = getOrderItemSubject(oi);
        return buildAlipayUrl(bank, subject, oi.getItemName(), oi.getFee(), tradeNo);
    }

    private boolean needResetOrder(OrderItem oi, String bank) {
        if (isEmpty(oi.getChannel()) && isEmpty(bank)) {  // 无需修改订单号
            return Boolean.FALSE ;
        }
        return !equalWith(oi.getChannel(), bank);
    }


    /**
     * 普通订单支付
     *
     * @param orderId 订单id
     */
    @RequestMapping(value = "/buyer/orders/{id}/pay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String pay(@PathVariable("id") Long orderId,
                      @CookieValue(value = "bank", required = false) String bank) {
        Response<Order> getOrder = orderQueryService.findById(orderId);
        if (!getOrder.isSuccess()) {
            log.error("fail to get order(id={}), error={}", orderId, getOrder.getError());
            throw new JsonResponseException(500, messageSources.get(getOrder.getError()));
        }

        Order exitOrder = getOrder.getResult();
        //判断订单是否抢购订单，如果是，判断是否在付款时间内
        if(equalWith(exitOrder.getIsBuying(), Boolean.TRUE)) {
            Response<BuyingTempOrder> tempOrderR = buyingTempOrderService.getByOrderId(orderId);
            if(!tempOrderR.isSuccess()) {
                log.error("fail to find temp order by order id={}, when pay buying order, error code:{}", orderId, tempOrderR.getError());
                throw new JsonResponseException(500, messageSources.get(tempOrderR.getError()));
            }
            BuyingTempOrder tempOrder = tempOrderR.getResult();
            Integer payLimit = tempOrder.getPayLimit();
            if(DateTime.now().isAfter(new DateTime(exitOrder.getCreatedAt()).plusHours(payLimit))) {
                log.warn("order id={} pay expire", exitOrder.getId());
                throw new JsonResponseException(500, messageSources.get("buying.order.pay.expire"));
            }

        }
        //判断当前支付渠道和子订单中的支付渠道是否相等
        //下单时是支付宝此时不是支付宝 或 下单时不是支付宝此时是其他支付渠道 （支付渠道已改变）
        if((isEmpty(exitOrder.getChannel())&&!Strings.isNullOrEmpty(bank)) || (!isNull(exitOrder.getChannel())&&!equalWith(exitOrder.getChannel(), bank))) { // 如果渠道号发生了变化，则修改子订单的id
            // 新建订单
            Response<Order> resetResult = orderWriteService.resetOrder(exitOrder, bank);
            if (!resetResult.isSuccess()) {
                log.error("fail to create order(oldId={}), error={}", orderId, resetResult.getError());
                throw new JsonResponseException(500, messageSources.get(resetResult.getError()));
            }

            Order newOrder = resetResult.getResult();
            //更新优惠码使用中关联的订单id
            Response<Boolean> updateResult = codeUsageService.updateOrderId(orderId,newOrder.getId());
            if(!updateResult.isSuccess()){
                log.error("fail to update codeUsages(oldId={}), error={}", orderId, resetResult.getError());
                throw new JsonResponseException(500, messageSources.get(updateResult.getError()));
            }

            //如果是抢购订单
            if(equalWith(exitOrder.getIsBuying(), Boolean.TRUE)) {

                //更新抢购订单中关联的订单id
                Response<Boolean> tempOrderUpdateResult = buyingTempOrderService.updateOrderId(orderId,newOrder.getId());
                if(!tempOrderUpdateResult.isSuccess()){
                    log.error("fail to update buyingTempOrder(oldId={}), error={}", orderId, tempOrderUpdateResult.getError());
                    throw new JsonResponseException(500, messageSources.get(tempOrderUpdateResult.getError()));
                }

                //更新抢购订单记录中关联的订单id
                Response<Boolean> orderRecordUpdateResult = buyingOrderRecordService.updateOrderId(orderId,newOrder.getId());
                if(!orderRecordUpdateResult.isSuccess()){
                    log.error("fail to update buyingOrderRecord(oldId={}), error={}", orderId, orderRecordUpdateResult.getError());
                    throw new JsonResponseException(500, messageSources.get(orderRecordUpdateResult.getError()));
                }
            }


            // 通知关闭订单,这里的订单号要使用修改前的订单号
            alipayEventBus.post(new TradeCloseEvent(token, exitOrder.getId().toString()));


            return buildAlipayUrl(bank, newOrder.getFee(), Lists.newArrayList(newOrder.getId()));
        }

        Order order = getOrder.getResult();
        return buildAlipayUrl(bank, order.getFee(), Lists.newArrayList(orderId));
    }


    /**
     * 根据子订单的类型获取描述信息
     *
     * @param orderItem 子订单信息
     * @return 支付描述信息
     */
    private String getOrderItemSubject(OrderItem orderItem) {
        String subject = orderItem.getItemName();

        if (subject.length() > 200) {
            subject = subject.substring(0, 200) + "...";
        }

        if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
            subject += "定金";
        } else if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())) {
            subject += "尾款";
        }

        return subject;
    }


    /**
     * 改接口包含多个操作，买家取消交易，卖家取消交易
     *
     * @param orderId 订单id
     * @return 操作是否成功
     */
    @RequestMapping(value = "/order/{orderId}/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String cancel(@PathVariable("orderId") Long orderId) {
        BaseUser currentUser = UserUtil.getCurrentUser();
        Order order = getOrder(orderId);

        Response<Boolean> result = orderWriteService.cancelOrder(order, currentUser);
        if (!result.isSuccess()) {
            log.error("fail to cancel for order(id={}) by user({}),error code:{}",
                    orderId, currentUser, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        //remove from preSale order list
        if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
            Response<Boolean> removeR = preSaleService.removePreSaleOrder(orderId);
            if (!removeR.isSuccess()) {
                log.error("fail to remove preSale order id={} from list, error code:{}",
                        orderId, removeR.getError());
            }

            //对于分仓的预售还要恢复库存
            Response<Boolean> storageR = preSaleService.recoverPreSaleStorageIfNecessary(orderId);
            if(!storageR.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", orderId, storageR.getError());
            }

            //恢复预售购买限制
            Response<Boolean> preSaleBuyLimitR = preSaleService.recoverPreSaleBuyLimitIfNecessary(order);
            if(!preSaleBuyLimitR.isSuccess()) {
                log.error("failed to recover pre sale buy limit by order id={}, error code:{}",
                        order.getId(), preSaleBuyLimitR.getError());
            }
        }

        // 取消订单后需要创建结算信息
        /*try {
            order = getOrder(orderId);
            createSettlementAfterCancel(order);
        } catch (IllegalStateException e) {
            log.error("fail to create settlement of Order({}), code:{}", order, e.getMessage());
        }*/

        // 通知订单关闭
        //如果是到店支付，不需要通知支付宝关闭交易
        if(Order.PayType.STORE_PAY.value() != order.getPaymentType().intValue()){
            notifyTradeHasClosed(order);
        }

        return "ok";
    }

    private Order getOrder(Long orderId) {
        Response<Order> orderR = orderQueryService.findById(orderId);
        if (!orderR.isSuccess()) {
            log.error("fail to find order by id={}, error code:{}", orderId, orderR.getError());
            throw new JsonResponseException(500, messageSources.get(orderR.getError()));
        }
        return orderR.getResult();
    }

    private void createSettlementAfterCancel(Order order) {
        if (needSettlementAfterCancel(order)) {
            Response<Long> createResult = settlementService.generate(order.getId());
            checkState(createResult.isSuccess(), createResult.getError());
        }
    }

    /**
     *  通知关闭交易(异步通知支付宝关闭交易)
     */
    private void notifyTradeHasClosed(Order order) {
        if (isPlain(order)) {
            alipayEventBus.post(new TradeCloseEvent(token, order.getId().toString()));
            return;
        }

        if (isPreSale(order)) {
            Response<List<OrderItem>> orderItemQueryResult = orderQueryService.findOrderItemByOrderId(order.getId());
            if (orderItemQueryResult.isSuccess()) {
                List<OrderItem> orderItems = orderItemQueryResult.getResult();
                OrderItem deposit = getAssignedTypeOrderItemFrom(OrderItem.Type.PRESELL_DEPOSIT, orderItems);
                if (equalWith(deposit.getStatus(), OrderItem.Status.CANCELED_BY_BUYER.value())) {   // 若定金未支付则通知定金交易关闭
                    alipayEventBus.post(new TradeCloseEvent(token, order.getId().toString() + "," + deposit.getId().toString()));
                    return;
                }

                OrderItem rest = getAssignedTypeOrderItemFrom(OrderItem.Type.PRESELL_REST, orderItems);
                if (equalWith(rest.getStatus(), OrderItem.Status.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value())) {   // 若尾款未支付则通知尾款交易关闭
                    alipayEventBus.post(new TradeCloseEvent(token, order.getId().toString() + "," + rest.getId().toString()));
                }
            }
        }
    }


    private OrderItem getAssignedTypeOrderItemFrom(OrderItem.Type type , List<OrderItem> orderItems) {
        for (OrderItem oi : orderItems) {
            if (equalWith(oi.getType(), type.value())) {
                return oi;
            }
        }

        return null;
    }


    /**
     * 卖家退货(确认收到退货)，退款
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/order/{orderItemId}/cancelOrderItem", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void cancelOrderItem(@PathVariable("orderItemId") Long orderItemId) {
        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderItemId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderItemId,
                    orderItemR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemR.getError()));
        }
        OrderItem orderItem = orderItemR.getResult();
        Date now = DateTime.now().toDate();
        Response<Boolean> refund = orderWriteService.refundPlainOrderItem(now, orderItem);
        if (!refund.isSuccess()) {
            log.error("fail to do refund by date {}, orderItem {}, error {}",
                    now, orderItem, refund.getError());
            throw new JsonResponseException(500, messageSources.get(refund.getError()));
        }
        
        try {
        	int status=orderQueryService.findOrderItemById(orderItemId).getResult().getStatus();
        	int payType=orderItem.getPayType();
        	/*long buyerId=orderItem.getBuyerId();
        	Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
        	UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getId()).getResult();
        	User user=accountService.findUserById(buyerId).getResult();*/
        	if(payType==1){
        		if(status==-3){
            		/*if(isUserStatus(String.valueOf(buyerId),"1")){
                    	sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getMobile()+"】，您的退款申请已审核通过，请留意您的账户信息。");
                	}*/
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"6"));
            	}else if(status==-4){
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"7"));
            		/*if(isUserStatus(String.valueOf(buyerId),"1")){
                    	sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getMobile()+"】，您的退货申请已审核通过，请留意您的账户信息。");       	            	
                	}*/
            	}else{
            		log.error("fail to get sms status"+status);
            	}
        	}
        	
		} catch (Exception e2) {
			log.error("fail to get sms exception"+e2.getMessage());
		}
        
    }

    /**
     * 预售订单卖家退货(确认收到退货)，退款
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/order/{orderItemId}/preSaleCancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void preSaleCancelOrderItem(@PathVariable("orderItemId") Long orderItemId) {
        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderItemId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderItemId,
                    orderItemR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemR.getError()));
        }
        OrderItem orderItem = orderItemR.getResult();
        Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(orderItem.getOrderId());
        if (!orderItemsR.isSuccess()) {
            log.error("fail to find orderItems by orderId={}, error code:{}", orderItem.getOrderId(),
                    orderItemsR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemsR.getError()));
        }
        List<OrderItem> orderItems = orderItemsR.getResult();
        OrderItem earnest = null;
        OrderItem remain = null;
        for (OrderItem oi : orderItems) {
            if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                earnest = oi;
            } else {
                remain = oi;
            }
        }

        Date now = DateTime.now().toDate();
        Response<Boolean> refund = orderWriteService.refundPresellOrderItem(now, earnest, remain);
        if (!refund.isSuccess()) {
            log.error("fail to do refund by date {}, earnest {}, remain {}, error {}",
                    now, earnest, remain, refund.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemsR.getError()));
        }
        //remove from preSale order list
        Response<Boolean> removeR = preSaleService.removePreSaleOrder(orderItem.getOrderId());
        if (!removeR.isSuccess()) {
            log.error("fail to remove preSale order id={} from list, error code:{}",
                    orderItem.getOrderId(), removeR.getError());
            throw new JsonResponseException(500, messageSources.get(removeR.getError()));
        }
        try {
        	int status=orderQueryService.findOrderItemById(orderItemId).getResult().getStatus();
        	/*long buyerId=orderQueryService.findOrderItemById(orderItemId).getResult().getBuyerId();
        	User user=accountService.findUserById(buyerId).getResult();*/
        	if(status==-3){
                smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"9"));
        		/*if(isUserStatus(String.valueOf(buyerId),"1")){
                	sendSms(user.getMobile(),"亲爱的【"+user.getMobile()+"】，您的退款申请已审核通过，请留意您的账户信息。");       	            	
            	}	*/
        	}else if(status==-4){
                smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"10"));
        	/*	if(isUserStatus(String.valueOf(buyerId),"1")){
                	sendSms(user.getMobile(),"亲爱的【"+user.getMobile()+"】，您的退货申请已审核通过，请留意您的账户信息。");       	            	
            	}*/
        	}else{
        		log.error("fail to get sms status"+status);
        	}
		} catch (Exception e) {
			log.error("fail to get sms exception"+e.getMessage());
		}
        
    }

    @RequestMapping(value = "/order/{orderId}/deliver", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deliver(@PathVariable("orderId") Long orderId, @RequestBody OrderLogisticsInfoDto orderLogisticsInfoDto) {
        Order order = getOrder(orderId);
        if (!preSaleOrderCanDeliver(order)) {
            log.error("preSale not finish, can not deliver");
            throw new JsonResponseException(500, messageSources.get("preSale.not.finish"));
        }
        BaseUser currentUser = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.deliver(order, orderLogisticsInfoDto, currentUser);
        if (!result.isSuccess()) {
            log.error("fail to deliver for order(id={}) by user({}),error code:{}",
                    orderId, currentUser, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        try {
        	int type=order.getType();
           // User user=accountService.findUserById(order.getBuyerId()).getResult();
           // LogisticsInfo logisticsInfo=logisticsInfoService.findByOrderId(orderId).getResult();
            if(type==1){
            	smsEventBus.post(new SmsEvent(orderId, orderId, "2"));
            	/*if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
            		String smsContent="亲爱的【"+user.getMobile()+"】，您的订单【"+orderId+"】已发货";
            		if(logisticsInfo!=null){
            			smsContent+="，为您安排【"+logisticsInfo.getCompanyName()+"】（快递单号：【"+logisticsInfo.getFreightNote()+"】）送至府上，请检查后签收。";
            		}
                	sendSms(user.getMobile(),smsContent);       	
                }*/	
            }else{
            	smsEventBus.post(new SmsEvent(orderId, orderId, "3"));
            	/*if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
            	  List<OrderItem> list=orderQueryService.findOrderItemByOrderId(orderId).getResult();
            		String orderName=list.size()>0?list.get(0).getItemName():"";
            		String smsContent="亲爱的【"+user.getMobile()+"】，您的预购商品【"+orderName+"】已发货";
            		if(logisticsInfo!=null){
            			smsContent+="，为您安排【"+logisticsInfo.getCompanyName()+"】（快递单号：【"+logisticsInfo.getFreightNote()+"】）送至府上，请检查后签收。";
            		}
                	sendSms(user.getMobile(),smsContent);       	
                }*/
            }
		} catch (Exception e) {
			log.error("fail to get sms exception"+e.getMessage());
		}        
        return "ok";
    }

    private boolean preSaleOrderCanDeliver(Order order) {
        if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())
                && Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
            Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(order.getId());
            if (!orderItemsR.isSuccess()) {
                log.error("find orderItems by orderId={} fail, error code:{}",
                        order.getId(), orderItemsR.getError());
                return false;
            }
            Long itemId = orderItemsR.getResult().get(0).getItemId();
            Response<PreSale> preSaleR = preSaleService.findPreSaleByItemId(itemId);
            if (!preSaleR.isSuccess()) {
                log.error("fail to find preSale by itemId={}, error code:{}",
                        itemId, preSaleR.getError());
                return false;
            }
            PreSale preSale = preSaleR.getResult();
            return !new DateTime(preSale.getPreSaleFinishAt()).isAfterNow();
        }
        return true;
    }

    @RequestMapping(value = "/buyer/order/{orderId}/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String confirm(@PathVariable("orderId") Long orderId) {
        Long userId = UserUtil.getUserId();
        Order order = getOrder(orderId);
        Response<Boolean> result = orderWriteService.confirm(order, userId);
        if (!result.isSuccess()) {
            log.error("fail to confirm for order(id={}) by user(id={}),error code:{}",
                    orderId, userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        // 确认收货后需要创建结算信息
        try {
            order = getOrder(orderId);
            createSettlementAfterConfirm(order);
        } catch (IllegalStateException e) {
            log.error("fail to create settlement of Order({}), code:{}", order, e.getMessage());
        }

        //remove from preSale order list
        if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
            Response<Boolean> removeR = preSaleService.removePreSaleOrder(orderId);
            if (!removeR.isSuccess()) {
                log.error("fail to remove preSale order id={} from list, error code:{}",
                        orderId, removeR.getError());
                throw new JsonResponseException(500, messageSources.get(removeR.getError()));
            }
        }
        return "ok";
    }

    @RequestMapping(value = "/seller/order/collection/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response collect(@RequestParam("orderId") Long orderId,
                          @RequestParam("paymentCode") String paymentCode,
                          @RequestParam("userId") Long userId,
                          @RequestParam("sign") String sign,
                          HttpServletRequest request,
                          HttpServletResponse response) {


        Response ret = new Response();
        try {
            // 校验签名
            checkArgument(verify(request, plazaKey), "sign.verify.fail");
            Response<Boolean> result = orderWriteService.normalOrderPaid(orderId, paymentCode, new Date());
            if (!result.isSuccess()) {
                log.error("fail to confirm collection for order(id={}) by user(id={}),error code:{}",
                        orderId, userId, result.getError());


                ret.setError(result.getError());
            }
            ret.setSuccess(true);
            log.info("succeed to confirm collection for order(id={}) by user(id={})");

        }catch(Exception e){
            ret.setError(e.getMessage());
        }

        response.setHeader("Access-Control-Allow-Origin", "http://home.rrs.cn/*");
        return ret;
    }



    private void createSettlementAfterConfirm(Order order) {
        //普通订单-货到付款-交易成功
        if (needSettlementAfterSuccess(order)) {
            Response<Long> createResult = settlementService.generate(order.getId());
            checkState(createResult.isSuccess(), createResult.getError());
        }
    }


    @RequestMapping(value = "/order/{orderItemId}/refuse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String refuse(@PathVariable("orderItemId") Long orderItemId) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.refuse(orderItemId, user);
        if (!result.isSuccess()) {
            log.error("fail to refuse refund or returnGoods for orderItem({}) by user(id={}),error code:{}",
                    orderItemId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    @RequestMapping(value = "/buyer/order/{orderItemId}/undo", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String undo(@PathVariable("orderItemId") Long orderItemId) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.undoRequest(orderItemId, user);
        if (!result.isSuccess()) {
            log.error("fail to undo refund or returnGoods for orderItem(id={}) by user({}),error code:{}",
                    orderItemId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 卖家同意退货
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/order/{orderItemId}/agree", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String agreeReturnGoods(@PathVariable("orderItemId") Long orderItemId) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.agreeReturnGoods(orderItemId, user);
        if (!result.isSuccess()) {
            log.error("fail to agree return goods for orderItem for orderItem(id={}) by user({}), error code:{}",
                    orderItemId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 添加退货款理由和金额
     *
     * @param orderItemId  子订单id
     * @param reason       理由
     * @param refundAmount 金额
     * @return 是否成功
     */
    @RequestMapping(value = "/buyer/order/{orderItemId}/extra", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String addExtra(@PathVariable("orderItemId") Long orderItemId,
                           @RequestParam(value = "reason") String reason,
                           @RequestParam(value = "refundAmount") Integer refundAmount) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.addReasonAndRefund(orderItemId, user, reason, refundAmount);
        if (!result.isSuccess()) {
            log.error("fail to add reason or refund for orderItemId(id={}) by user({}),cause:{}",
                    orderItemId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        try {
        	OrderItem orderItem=orderQueryService.findOrderItemById(orderItemId).getResult();
        	int status=orderItem.getStatus();
        	int payType=orderItem.getPayType();
           /* long sellerId=orderItem.getSellerId();
            long buyerId=orderItem.getBuyerId();
            long orderId=orderItem.getOrderId();
            Shop shop=shopService.findByUserId(sellerId).getResult();
            User user1=accountService.findUserById(buyerId).getResult();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");  */
            if(payType==1){
            	if(status==4){
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"4"));
                	/*if(isUserStatus(String.valueOf(sellerId),"2")){
                    	sendSms(shop.getPhone(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getMobile()+"】"
                    			+ "于【"+format.format(new Date())+"】申请退款，请尽快审核。");       	
                    }*/
                }else if(status==5){
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"5"));
                	/*if(isUserStatus(String.valueOf(sellerId),"2")){
                    	sendSms(shop.getPhone(),"您有一项新的业务要处理，贵商铺订单【"+orderId+"】买家【"+user1.getMobile()+"】于"
                    			+ "【"+format.format(new Date())+"】申请退货，请尽快审核。");       	
                    }*/
                }else{
                	log.error("fail to get sms status"+status);
                }	
            }
            
		} catch (Exception e) {
			log.error("fail to get sms exception"+e.getMessage());
		}
        
        return "ok";
    }

    /**
     * 预售添加退货款理由和金额
     *
     * @param orderItemId  子订单id
     * @param reason       理由
     * @param refundAmount 金额
     * @return 是否成功
     */
    @RequestMapping(value = "/buyer/order/{orderItemId}/preSaleExtra", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String preSaleAddExtra(@PathVariable("orderItemId") Long orderItemId,
                                  @RequestParam(value = "reason") String reason,
                                  @RequestParam(value = "refundAmount") Integer refundAmount) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.preSaleAddReasonAndRefund(orderItemId, user, reason, refundAmount);
        if (!result.isSuccess()) {
            log.error("fail to add reason or refund for orderItemId(id={}) by user({}),cause:{}",
                    orderItemId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 分页现实买家的详细订单界面
     *
     * @param pageNo 分页的页码
     * @param size   分页大小
     * @param status 订单的状态
     * @return 分页后的订单列表
     */
    @RequestMapping(value = "/orders/buyerView", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<RichOrderBuyerView> viewBuyerOrders(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                      @RequestParam(value = "status", required = false) Integer status,
                                                      @RequestParam(value = "orderId", required = false) Long orderId) {
        // 买家不需要权限验证
        BaseUser currentBuyer = UserUtil.getCurrentUser();
        Response<Paging<RichOrderBuyerView>> orderGet =  orderQueryService.findByBuyerId(currentBuyer, pageNo, size, status, orderId);

        Paging<RichOrderBuyerView> result = orderGet.getResult();
        if (result == null || result.getTotal() == 0) {
            return result;
        } else {
            addPreSaleOrderInfo(result.getData());
            addOrderCouponInfo(result.getData());
            addBuyingInfo(result.getData());
            // 增加预授权预置商品配置读取
            addPreDepositOrderInfo(result.getData());
            //添加活动优惠相关信息
            for (RichOrderBuyerView richOrderBuyerView : result.getData()) {
                Response<CodeUsage> codeUsage = codeUsageService.getCodeUsageByOrderId(richOrderBuyerView.getOrderId());
                if (codeUsage != null && codeUsage.getResult() != null) {
                    BeanMapper.copy(codeUsage.getResult(), richOrderBuyerView);
                    //抢购活动优惠码使用
                    if (equalWith(richOrderBuyerView.getIsBuying(),Boolean.TRUE)) {
                        addOrderCodeUsageInfo(richOrderBuyerView, codeUsage.getResult());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 查询抢购活动订单使用优惠码情况（客户个人中心）
     * @param richOrderBuyerView
     * @param codeUsage
     */
    private void addOrderCodeUsageInfo(RichOrderBuyerView richOrderBuyerView, CodeUsage codeUsage) {
        log.debug("init order coupon info begin ...");
        List<RrsCouOrderItem> couponList = richOrderBuyerView.getCouponList();
        if (null == couponList || couponList.size() < 1) {
            couponList = new ArrayList<RrsCouOrderItem>();
        }
        for(RichOrderItem item : richOrderBuyerView.getOrderItems()) {
            RrsCouOrderItem couOrder = new RrsCouOrderItem();
            couOrder.setAmount(codeUsage.getDiscount());
            couOrder.setId(codeUsage.getActivityId());
            couOrder.setCpName(codeUsage.getActivityName());
            item.setCouponAmount(new BigDecimal(codeUsage.getDiscount()));
            couponList.add(couOrder);
        }
        richOrderBuyerView.setCouponList(couponList);
        log.debug("init order coupon info end ...");
    }

    private void addBuyingInfo(List<? extends RichOrder> richOrders) {
        for(RichOrder roi : richOrders) {
            try {
                if (roi.getIsBuying() == null || !roi.getIsBuying()) {
                    continue;
                }
                Response<BuyingTempOrder> tempOrderR = buyingTempOrderService.getByOrderId(roi.getOrderId());
                if (!tempOrderR.isSuccess()) {
                    log.error("fail to find temp order by order id={}, when view buyer order list, error code={}, skip",
                            roi.getOrderId(), tempOrderR.getError());
                    continue;
                }
                BuyingTempOrder tempOrder = tempOrderR.getResult();
                if(notNull(tempOrder.getPayLimit())){
                    DateTime payDeadLine = new DateTime(roi.getCreatedAt()).plusHours(tempOrder.getPayLimit());
                    if (DateTime.now().isAfter(payDeadLine)) {
                        roi.setBuyingCanPay(Boolean.FALSE);
                    } else {
                        roi.setBuyingCanPay(Boolean.TRUE);
                    }
                }

                roi.setBuyingActivityId(tempOrder.getBuyingActivityId());
            }catch (Exception e) {
                log.error("fail to add buying info for order id={}, cause:{}, skip", roi.getOrderId(), Throwables.getStackTraceAsString(e));
                //ignore
            }
        }
    }

    @RequestMapping(value = "/orders/sellerView", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<RichOrderSellerView> viewSellerOrders(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                        @RequestParam(value = "status", required = false) Integer status,
                                                        @RequestParam(value = "orderId", required = false) Long orderId) {
        BaseUser currentSeller = UserUtil.getCurrentUser();
        Response<Paging<RichOrderSellerView>> orderGet = orderQueryService.findBySellerId(currentSeller, pageNo, size, status, orderId);
        if (!orderGet.isSuccess()) {
            log.error("failed to query orders for seller(id={}), error code:{}", currentSeller.getId(), orderGet.getError());
            throw new JsonResponseException(500, messageSources.get(orderGet.getError()));
        }

        Paging<RichOrderSellerView> result = orderGet.getResult();
        if (result.getTotal() == 0) {
            return result;
        } else {
            for (RichOrderSellerView richOrderSellerView : result.getData()) {
                Response<CodeUsage> codeUsage = codeUsageService.getCodeUsageByOrderId(richOrderSellerView.getOrderId());
                if (codeUsage != null && codeUsage.getResult() != null) {
                    BeanMapper.copy(codeUsage.getResult(), richOrderSellerView);
                    //抢购活动优惠码使用
                    if (equalWith(richOrderSellerView.getIsBuying(),Boolean.TRUE)) {
                        addOrderrSellerCodeUsageInfo(richOrderSellerView, codeUsage.getResult());
                    }
                }
            }
            addBuyingInfo(result.getData());
            addPreSaleOrderInfo(result.getData());
//          add by zf 2014-08-22
            addCouponsSaleOrderinfo(result.getData());
            // 增加预授权预置商品配置读取
            addPreDepositOrderInfo(result.getData());
        }
        return result;
    }

    /**
     * 查询抢购活动订单使用优惠码情况（商家中心）
     * @param richOrderSellerView
     * @param codeUsage
     */
    private void addOrderrSellerCodeUsageInfo(RichOrderSellerView richOrderSellerView, CodeUsage codeUsage) {
        log.debug("init order sell coupon info begin ...");
        List<RrsCouOrderItem> couponList = richOrderSellerView.getCouOrderList();
        if (null == couponList || couponList.size() < 1) {
            couponList = new ArrayList<RrsCouOrderItem>();
        }
        for(RichOrderItem item : richOrderSellerView.getOrderItems()) {
            RrsCouOrderItem couOrder = new RrsCouOrderItem();
            couOrder.setAmount(codeUsage.getDiscount());
            couOrder.setId(codeUsage.getActivityId());
            couOrder.setCpName(codeUsage.getActivityName());
            item.setCouponAmount(new BigDecimal(codeUsage.getDiscount()));
            couponList.add(couOrder);
        }
        richOrderSellerView.setCouOrderList(couponList);
        log.debug("init order sell coupon info end ...");
    }

    @RequestMapping(value = "/seller/deliverFee", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateDeliverFee(@RequestParam("orderItemId") Long orderItemId, @RequestParam("newFee") Integer newFee) {
        BaseUser seller = UserUtil.getCurrentUser();

        Response<Boolean> result = orderWriteService.updateDeliverFee(seller.getId(), orderItemId, newFee);

        if (!result.isSuccess()) {
            log.error("failed to update order item deliver fee, sellerId={}, orderItemId={}, newFee={}, error code={}",
                    seller.getId(), orderItemId, newFee, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    @RequestMapping(value = "/buyer/sku/discount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long getSkuDiscount(@RequestParam("skuIds") String skuIds,
                               @RequestParam("activityId") Long activityId,
                               @RequestParam("code") String code) {
        Map<Long, Integer> skuMap = JSON_MAPPER.fromJson(skuIds, JSON_MAPPER.createCollectionType(Map.class, Long.class, Integer.class));
        Response<List<Sku>> skusR = itemService.findSkuByIds(Lists.newArrayList(skuMap.keySet()));
        if (!skusR.isSuccess()) {
            log.error("fail to find skus by ids={},error code:{}", skuIds, skusR.getError());
            throw new JsonResponseException(500, messageSources.get(skusR.getError()));
        }

        Response<ActivityDefinition> activityDefinitionR = activityDefinitionService.findActivityDefinitionById(activityId);
        if (!activityDefinitionR.isSuccess()) {
            log.error("fail to find activity def by id={}, error code:{}", activityId, activityDefinitionR.getError());
            throw new JsonResponseException(500, messageSources.get(activityDefinitionR.getError()));
        }
        ActivityDefinition activityDef = activityDefinitionR.getResult();

        Response<List<Long>> itemIdsR = activityBindService.findBindIdsByActivityId(activityId, ActivityBind.TargetType.ITEM.toNumber());
        if (!itemIdsR.isSuccess()) {
            log.error("fail to find bind ids by activityId={}, TargetType=ITEM, error code:{}",
                    activityId, itemIdsR.getError());
            throw new JsonResponseException(500, messageSources.get(itemIdsR.getError()));
        }
        List<Long> itemIds = itemIdsR.getResult();

        Long totalDiscount = 0l;
        Integer totalToBuy = 0;

        for (Sku sku : skusR.getResult()) {
            int quantity = skuMap.get(sku.getId());

            //sku在活动范围之内,sku价格高于优惠的金额
            if (itemIds.contains(sku.getItemId()) && sku.getPrice() > activityDef.getDiscount()) {
                totalDiscount += quantity * activityDef.getDiscount();
                totalToBuy += quantity;
            }
        }
        //如果库存为空，不需要做数量的判断
        if (activityDef.getStock() == null) {
            return totalDiscount;
        }

        Response<ActivityCode> activityCodeR = activityCodeService.findOneByActivityIdAndCode(activityId, code);
        if (!activityCodeR.isSuccess()) {
            log.error("fail to find usage by activityId={}, code={}, error code:{}",
                    activityId, code, activityCodeR.getError());
            throw new JsonResponseException(500, messageSources.get(activityCodeR.getError()));
        }
        Integer usage = activityCodeR.getResult().getUsage();

        if ((usage + totalToBuy) > activityDef.getStock()) {
            log.warn("activityDef id={} has used {}, want to use {}", activityDef.getId(), usage, totalToBuy);
            throw new JsonResponseException(500, messageSources.get("stock.not.enough"));
        }
        return totalDiscount;
    }

    @RequestMapping(value = "/code/activities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ActivityDefinition> getActivitiesByCode(@RequestParam("code") String code,
                                                        @RequestParam("skuIds") String skuString) {
        //根据优惠码 code 查询相关的优惠活动定义
        Response<List<ActivityDefinition>> result = activityDefinitionService.findValidActivityDefinitionsByCode(code);
        if (!result.isSuccess()) {
            log.error("fail to find activity def by code={}, error code={}", code, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        List<ActivityDefinition> activityDefinitions = result.getResult();
        List<Long> skuIds = getSkuIds(skuString);

        //根据sku id 列表获取库存列表
        Response<List<Sku>> skusR = itemService.findSkuByIds(skuIds);
        if (!skusR.isSuccess()) {
            log.error("fail to find skus by ids={}, error code:{}", skuIds, skusR.getError());
            throw new JsonResponseException(500, messageSources.get(skusR.getError()));
        }
        List<Sku> skus = skusR.getResult();
        List<Long> itemIds = Lists.transform(skus, new Function<Sku, Long>() {
            @Override
            public Long apply(Sku input) {
                return input.getItemId();
            }
        });
        List<ActivityDefinition> filterActivityDef = Lists.newArrayList();
        for (ActivityDefinition activityDef : activityDefinitions) {
            //根据优惠活动id查找绑定的所有绑定（sku,spu,品类）id
            Response<List<Long>> bindIdsR = activityBindService.findBindIdsByActivityId(activityDef.getId(), ActivityBind.TargetType.ITEM.toNumber());
            if (!bindIdsR.isSuccess()) {
                log.error("fail to find bind ids by activityId={}, targetType=ITEM, error code:{}",
                        activityDef.getId(), bindIdsR.getError());
                continue;
            }
            List<Long> bindIds = bindIdsR.getResult();
            //有适用商品，库存为空（不限制购买数量）或者库存大于0的才显示出来
            if (inUseRange(bindIds, itemIds) && (activityDef.getStock() == null || activityDef.getStock() > 0)) {
                filterActivityDef.add(activityDef);
            }
        }

        return filterActivityDef;
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
        for (Long bindId : bindIds) {
            if (itemIds.contains(bindId))
                return true;
        }
        return false;
    }

    private void addPreSaleOrderInfo(List<? extends RichOrder> richOrders) {
        for (RichOrder richOrder : richOrders) {
            if (Objects.equal(richOrder.getOrderType(), Order.Type.PRE_SELL.value())) {
                try {
                    if (richOrder.getOrderItems() == null || richOrder.getOrderItems().isEmpty()) {
                        log.warn("pre-sell order(id={}) doesn't has any orderItems", richOrder.getOrderId());
                        continue;
                    }
                    Integer totalRefundAmount = 0;
                    for (RichOrderItem roi : richOrder.getOrderItems()) {
                        if (roi.getRefundAmount() != null)
                            totalRefundAmount += roi.getRefundAmount();
                    }
                    richOrder.setTotalRefundAmount(totalRefundAmount);
                    Response<PreSale> preSaleR = preSaleService.findPreSaleByItemId(richOrder.getOrderItems().get(0).getSku().getItemId());
                    //增加预授权预置商品配置读取
//                    Response<PreDeposit> preDepositR = preDepositService.findPreDepositByItemId(richOrder.getOrderItems().get(0).getSku().getItemId());
                    if (!preSaleR.isSuccess() || preSaleR.getResult() == null) {
                        log.error("fail to find preSale by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
                        continue;
                    }
//                    //增加预授权预置商品配置读取
//                    if (!preDepositR.isSuccess() || preDepositR.getResult() == null) {
//                        log.error("fail to find preDepositR by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
//                        continue;
//                    }
                    PreSale preSale = preSaleR.getResult();
                    richOrder.setRemainFinishAt(preSale.getRemainFinishAt());
                    List<RichOrderItem> richOrderItems = richOrder.getOrderItems();
                    richOrder.setCanPayEarnest(false);
                    if(DateTime.now().isBefore(new DateTime(richOrderItems.get(0).getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()))) {
                        richOrder.setCanPayEarnest(true);
                    }
                    richOrder.setCanPayRemain(false);
                    if(DateTime.now().isBefore(new DateTime(preSale.getRemainFinishAt())) &&
                            DateTime.now().isAfter(new DateTime(preSale.getRemainStartAt()))) {
                        richOrder.setCanPayRemain(true);
                    }

                    richOrder.setRemainFinishAt(preSale.getRemainFinishAt());
                    Date date = new DateTime(richOrder.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).toDate();
                    richOrder.setEarnestPayTime(date);
                    richOrder.setPreSaleFinishAt(preSale.getPreSaleFinishAt());
                    richOrder.setDepositType(0);
                    //增加预授权预置商品配置读取
//                    if (preDepositR.isSuccess()) {
//                        PreDeposit preDeposit = preDepositR.getResult();
//                        richOrder.setRemainFinishAt(preDeposit.getRemainFinishAt());
//                        if(DateTime.now().isBefore(new DateTime(richOrderItems.get(0).getCreatedAt()).plusHours(preDeposit.getEarnestTimeLimit()))) {
//                            richOrder.setCanPayEarnest(true);
//                        }
//                        if(DateTime.now().isBefore(new DateTime(preDeposit.getRemainFinishAt())) &&
//                                DateTime.now().isAfter(new DateTime(preDeposit.getRemainStartAt()))) {
//                            richOrder.setCanPayRemain(true);
//                        }
//                        richOrder.setRemainFinishAt(preDeposit.getRemainFinishAt());
//                        date = new DateTime(richOrder.getCreatedAt()).plusHours(preDeposit.getEarnestTimeLimit()).toDate();
//                        richOrder.setEarnestPayTime(date);
//                        richOrder.setPreSaleFinishAt(preDeposit.getPreSaleFinishAt());
//                    }
                } catch (Exception e) {
                    log.error("failed to handle order(id={}), cause:{}", richOrder.getOrderId(), Throwables.getStackTraceAsString(e));
                }
            }
        }
    }

    /**
     * 判断预售订单是否能付款
     */
    private boolean canPay(Long orderItemId) {
        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderItemId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderItemId, orderItemR.getError());
        }
        OrderItem orderItem = orderItemR.getResult();
        Long itemId = orderItem.getItemId();
        Response<PreSale> preSaleR = preSaleService.findPreSaleByItemId(itemId);
        if (!preSaleR.isSuccess()) {
            log.error("fail to find preSale by itemId={}, error code:{}", itemId, preSaleR.getError());
            return false;
        }
        PreSale preSale = preSaleR.getResult();
        //定金
        if (Objects.equal(OrderItem.Type.PRESELL_DEPOSIT.value(), orderItem.getType())) {
            //付定金超时
            return !new DateTime(orderItem.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).isBeforeNow();
        }
        //尾款
        if (Objects.equal(OrderItem.Type.PRESELL_REST.value(), orderItem.getType())) {
            //付尾款时间未到不能付款
            if (new DateTime(preSale.getRemainStartAt()).isAfterNow())
                return false;
            //付尾款超时
            return !new DateTime(preSale.getRemainFinishAt()).isBeforeNow();
        }
        log.error("orderItem type not correct");
        return false;
    }

    /**
     * 查询订单使用优惠券情况
     *
     * @param data  订单编号
     */
    private void addOrderCouponInfo(List<RichOrderBuyerView> data) {
        log.debug("init order coupon info begin ...");
        // 根据订单编号查询优惠券使用情况
        List<Long> ids = new ArrayList<Long>();
        for (RichOrderBuyerView order : data) {
            ids.add(order.getOrderId());
        }

        List<RrsCouOrderItem> orderCouponList = couponsRrsService
                .findOrderItemsByOrderIds(ids);

        if (CollectionUtils.isEmpty(orderCouponList)) {
            return;
        }

        // 初始化订单的优惠券使用情况
        Map<String, RrsCouOrderItem> orderCouponMap = new HashMap<String, RrsCouOrderItem>();

        StringBuffer buff = new StringBuffer();
        for (RrsCouOrderItem couOrder : orderCouponList) {
            getOrderCouponKey(buff, couOrder.getOrderId() , couOrder.getItemId() , couOrder.getSkuId());
            orderCouponMap.put(buff.toString(), couOrder);
        }

        for (RichOrderBuyerView order : data) {
            List<RrsCouOrderItem> couponList = order.getCouponList();
            if (null == couponList||couponList.size()<1) {
                couponList = new ArrayList<RrsCouOrderItem>();
            }

            for(RichOrderItem item : order.getOrderItems()){
                getOrderCouponKey(buff, order.getOrderId() , item.getSku().getItemId() , item.getSku().getId());
                RrsCouOrderItem couOrder = orderCouponMap.get(buff.toString());
                if (null != couOrder) {
                    item.setCouponAmount(couOrder.getFreeAmount());
                    couponList.add(couOrder);
                }
            }

            order.setCouponList(couponList);
        }
        log.debug("init order coupon info end ...");
    }

    private void addCouponsSaleOrderinfo(List<RichOrderSellerView> data) {
        log.debug("init order coupon info begin ...");
        // 根据订单编号查询优惠券使用情况
        List<Long> ids = new ArrayList<Long>();
        for (RichOrderSellerView order : data) {
            ids.add(order.getOrderId());
        }

        List<RrsCouOrderItem> orderCouponList = couponsRrsService
                .findOrderItemsByOrderIds(ids);

        if (CollectionUtils.isEmpty(orderCouponList)) {
            return;
        }

        // 初始化订单的优惠券使用情况
        Map<String, RrsCouOrderItem> orderCouponMap = new HashMap<String, RrsCouOrderItem>();

        StringBuffer buff = new StringBuffer();
        for (RrsCouOrderItem couOrder : orderCouponList) {
            getOrderCouponKey(buff, couOrder.getOrderId() , couOrder.getItemId() , couOrder.getSkuId());
            orderCouponMap.put(buff.toString(), couOrder);
        }

        for (RichOrderSellerView order : data) {
            List<RrsCouOrderItem> couponList = order.getCouOrderList();
            if (null == couponList||couponList.size()<1) {
                couponList = new ArrayList<RrsCouOrderItem>();
            }

            for(RichOrderItem item : order.getOrderItems()){
                getOrderCouponKey(buff, order.getOrderId() , item.getSku().getItemId() , item.getSku().getId());
                RrsCouOrderItem couOrder = orderCouponMap.get(buff.toString());
                if (null != couOrder) {
                    item.setCouponAmount(couOrder.getFreeAmount());
                    couponList.add(couOrder);
                }
            }
            order.setCouOrderList(couponList);
        }
        log.debug("init order coupon info end ...");
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
                log.info("rrsCou.getAmount()"+rrsCou.getAmount());
                log.info(total+"totalCoupons"+totalCoupons);
                //盘旋选择的优惠券是否可以使用
                if(rrsCou.getTerm() > totalCoupons){//不能使用返回原价
                    return total;
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

    private void getOrderCouponKey(StringBuffer buff, Long orderId,
                                   Long itemId, Long skuId) {
        buff.setLength(0);
        buff.append("" + orderId).append("-").append("" + itemId).append("-")
                .append("" + skuId);
    }

    /**
     * 创建抢购虚拟订单
     * @return 虚拟订单id
     */
    @RequestMapping(value = "/buyer/buying-temp-order", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createTempOrder(@RequestBody BuyingTempOrder buyingTempOrder) {

        BaseUser user = UserUtil.getCurrentUser();
        buyingTempOrder.setBuyerId(user.getId());

        Response<Long> idRes = buyingTempOrderService.createTempOrder(buyingTempOrder);
        if(!idRes.isSuccess()){
            if(Objects.equal(idRes.getError(),"out.of.buy.limit")){
                Long itemId = buyingTempOrder.getItemId();
                Long activityId =buyingTempOrder.getBuyingActivityId();
                Response<BuyingItem> buyingItemResponse = buyingItemService.findByActivityIdAndItemId(activityId,itemId);
                Response<Integer> hasBuyQuantityRes = buyingTempOrderService.getHasBuyQuantity(buyingTempOrder.getBuyingActivityId(),buyingTempOrder.getItemId(),buyingTempOrder.getBuyerId());
                throw new JsonResponseException(500, messageSources.get(idRes.getError(),buyingItemResponse.getResult().getBuyLimit(),hasBuyQuantityRes.getResult()));
            }
            throw new JsonResponseException(500, messageSources.get(idRes.getError()));
        }

        return idRes.getResult();

    }

    /**
     * 查询订单物流信息
     * @param id 订单id
     * @return 订单物流信息id
     */
    @RequestMapping(value = "/orders/{id}/logistics", method = RequestMethod.GET)
    @ResponseBody
    public OrderLogisticsInfoDetail findOrderLogisticsInfo(@PathVariable("id") Long id){
        Response<OrderLogisticsInfo> resp = orderLogisticsInfoService.findByOrderId(id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        OrderLogisticsInfoDetail orderLogisticsInfoDetail = new OrderLogisticsInfoDetail();
        OrderLogisticsInfo orderLogisticsInfo = resp.getResult();
        orderLogisticsInfoDetail.setOrderLogisticsInfo(orderLogisticsInfo);
        if (Objects.equal(OrderLogisticsInfo.Type.THIRD.value(), orderLogisticsInfo.getType())){
            String expressDetail;
            if (express100.isSpecial(orderLogisticsInfo.getExpressCode())){
                orderLogisticsInfoDetail.setIsSepcial(Boolean.TRUE);
                expressDetail = express100.expressByHtmlApi(orderLogisticsInfo.getExpressCode(), orderLogisticsInfo.getExpressNo());
            } else {
                expressDetail = express100.expressByApi(orderLogisticsInfo.getExpressCode(), orderLogisticsInfo.getExpressNo());

            }
            orderLogisticsInfoDetail.setExpressDetail(expressDetail);
        }
        return orderLogisticsInfoDetail;
    }

    /**
     * 查询订单安装信息
     * @param id 订单id
     * @return 订单安装信息id
     */
    @RequestMapping(value = "/orders/{id}/installs", method = RequestMethod.GET)
    @ResponseBody
    public List<OrderInstallInfo> findOrderInstallInfo(@PathVariable("id") Long id){
        Response<List<OrderInstallInfo>> resp = orderInstallInfoService.findByOrderId(id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }
    /*private void sendSms(String mobile,String content){
    	smsService.sendSingle("000000", mobile, content);
    }
    private boolean isUserStatus(String userId,String userType){
    	boolean flog=false;
    	Map<String, Object> map=new HashMap<String, Object>();
    	map.put("userId", userId);
    	map.put("userType", userType);
    	flog=orderQueryService.isUserStatus(map);
    	return flog;
    }*/

    /**
     * 创建订单,然后跳转到付款的url
     *
     * @param data        FatOrder List的json表示形式
     * @param tradeInfoId 收获地址id
     * @return 新建订单的id
     */
    @RequestMapping(value = "/buyer/orders3", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> directCreateOrdersAndPay3(@RequestParam("data") String data,
                                                         @RequestParam("tradeInfoId") Long tradeInfoId,
                                                         @CookieValue(value = "bank", required = false) String bank,
                                                         HttpServletRequest request) {

        return createOrdersAndReturnUrlAndOrderIdsForSku(data, tradeInfoId, bank, request);
    }

    private Map<String, Object> createOrdersAndReturnUrlAndOrderIdsForSku(String data, Long tradeInfoId,

                                                                    String bank, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();
        User user = checkUserStatus(userId);
        List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));

        //remove if region not match
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionIdR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionIdR.isSuccess()) {
            log.error("fail to get region from cookie, error code:{}", regionIdR.getError());
            throw new JsonResponseException(500, messageSources.get(regionIdR.getError()));
        }
        Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateOrder(fatOrders, regionIdR.getResult());
        if (!filterFatOrders.isSuccess()) {
            log.error("fail to filter fatOrders, error code:{}", filterFatOrders.getError());
            throw new JsonResponseException(500, messageSources.get(filterFatOrders.getError()));
        }

        //计算优惠价
        Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(fatOrders, user);
        if (!discountAndUsageR.isSuccess()) {
            log.error("fail to process order code discount. fatOrders={}, buyerId={},error code:{}",
                    fatOrders, user.getId(), discountAndUsageR.getError());
            throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
        }
        DiscountAndUsage discountAndUsage = discountAndUsageR.getResult();

        Response<Map<Long, Long>> result = orderWriteService.createForSku(userId, tradeInfoId,
                fatOrders, discountAndUsage.getSkuIdAndDiscount(), bank);
        if (!result.isSuccess()) {
            log.error("fail to create order for  {},error code:{}", data, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        //使用完优惠券后记录使用情况
        recordCodeUsage(discountAndUsage, result.getResult());

        boolean needRedirect = false;
        Map<Long, Long> maps = result.getResult();
        Set<Long> ids = Sets.newHashSet();
        for (Long id : maps.keySet()) {
            ids.add(maps.get(id));
        }
        int total = 0;
        List<Long> paidIds = Lists.newArrayList();
        for (Long id : ids) {
            Response<Order> getOrder = orderQueryService.findById(id);
            if (!getOrder.isSuccess()) {
                log.error("fail to get order for  {},error code:{}", data, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            Order order = getOrder.getResult();

            if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {
                needRedirect = true;
                total += order.getFee();
                paidIds.add(order.getId());
            }
        }



        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", ids);
        //ids 为拆分之后的订单ID add by cwf
        String couponsId =  request.getParameter("couponsId");//获取是否选择了优惠券信息
        if(!StringUtils.isEmpty(couponsId) && !couponsId.equals("-1")){
            total = changeCouponsOrderItem(couponsId,total,ids,userId);
        }

        // 亿起发CPS处理
//        orderEventBus.post(new OrderEvent(ids, request));
        orderEventBus.post(new CpsEvent( request,ids, CpsService.EventType.ORDER));


        if (needRedirect) {  // 有在线支付的订单时需要跳转支付页面
            String url = buildAlipayUrl(bank, total, paidIds);
            mappedResult.put("url", url);

            return mappedResult;
        } else { // 全订单都为货到付款时只需要返回空即可
            mappedResult.put("url", "");
        }

        return mappedResult;
    }



    /**
     * 验证签名
     * @param request   请求
     * @param restKey   密钥
     * @return  校验通过
     */
    private boolean verify(HttpServletRequest request,  String restKey) {

        checkNotNull(restKey, "plaza key doesn't exists!");

        String sign = request.getParameter("sign");
        Map<String, String> params = Maps.newTreeMap();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (isValueEmptyOrSignRelatedKey(key, value)) {
                continue;
            }
            params.put(key, value);
        }


        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

        String expect = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(restKey, Charsets.UTF_8).hash().toString();
        return Objects.equal(expect, sign);
    }

    private static boolean isValueEmptyOrSignRelatedKey(String key, String value) {
        return Strings.isNullOrEmpty(value) || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(key, "sign")
                || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(key, "sign_type");
    }

    /**
     * 创建订单,然后跳转到预授权的付款的url
     *
     * @param data        FatOrder List的json表示形式
     * @param tradeInfoId 收获地址id
     * @return 新建订单的id
     */
    @RequestMapping(value = "/buyer/orders4", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> directCreateOrdersAndPay4(@RequestParam("data") String data,
                                                         @RequestParam("tradeInfoId") Long tradeInfoId,
                                                         @CookieValue(value = "bank", required = false) String bank,
                                                         HttpServletRequest request) {

        return createOrdersAndReturnUrlAndOrderIdsByFreezePay(data, tradeInfoId, bank, request);
    }

    /**
     * 预授权退款(确认收到退货)
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/order/{orderItemId}/cancelOrderItem2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void cancelOrderItem2(@PathVariable("orderItemId") Long orderItemId) {
        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderItemId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderItemId,
                    orderItemR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemR.getError()));
        }
        OrderItem orderItem = orderItemR.getResult();
        Date now = DateTime.now().toDate();
        Response<Boolean> refund = orderWriteService.refundPlainOrderItem2(now, orderItem);
        if (!refund.isSuccess()) {
            log.error("fail to do refund by date {}, orderItem {}, error {}",
                    now, orderItem, refund.getError());
            throw new JsonResponseException(500, messageSources.get(refund.getError()));
        }

        try {
            int status=orderQueryService.findOrderItemById(orderItemId).getResult().getStatus();
            int payType=orderItem.getPayType();
        	/*long buyerId=orderItem.getBuyerId();
        	Order order=orderQueryService.findById(orderItem.getOrderId()).getResult();
        	UserTradeInfo userTradeInfo=userTradeInfoService.findById(order.getId()).getResult();
        	User user=accountService.findUserById(buyerId).getResult();*/
            if(payType==1){
                if(status==-3){
            		/*if(isUserStatus(String.valueOf(buyerId),"1")){
                    	sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getMobile()+"】，您的退款申请已审核通过，请留意您的账户信息。");
                	}*/
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"6"));
                }else if(status==-4){
                    smsEventBus.post(new SmsEvent(orderItem.getOrderId(),orderItem.getId(),"7"));
            		/*if(isUserStatus(String.valueOf(buyerId),"1")){
                    	sendSms(userTradeInfo.getPhone(),"亲爱的【"+user.getMobile()+"】，您的退货申请已审核通过，请留意您的账户信息。");
                	}*/
                }else{
                    log.error("fail to get sms ststus"+status);
                }
            }

        } catch (Exception e2) {
            log.error("fail to get sms exception"+e2.getMessage());
        }

    }



    /**
     * 预授权扣款(确认收到货)
     *
     * @param orderId 订单id
     */
    @RequestMapping(value = "/buyer/order/{orderId}/confirm2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String confirm2(@PathVariable("orderId") Long orderId) {
        Long userId = UserUtil.getUserId();
        Order order = getOrder(orderId);
        Response<Boolean> result = orderWriteService.confirm(order, userId);
        if (!result.isSuccess()) {
            log.error("fail to confirm for order(id={}) by user(id={}),error code:{}",
                    orderId, userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        // 确认收货后需要创建结算信息
        try {
            order = getOrder(orderId);
            createSettlementAfterConfirm(order);
        } catch (IllegalStateException e) {
            log.error("fail to create settlement of Order({}), code:{}", order, e.getMessage());
        }

        //remove from preSale order list
        if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
            Response<Boolean> removeR = preSaleService.removePreSaleOrder(orderId);
            if (!removeR.isSuccess()) {
                log.error("fail to remove preSale order id={} from list, error code:{}",
                        orderId, removeR.getError());
                throw new JsonResponseException(500, messageSources.get(removeR.getError()));
            }
        }
        return "ok";
    }

    /**
     * 构建支付宝资金授权 - 付款 请求url
     * @param bank                  银行（为空默认选择支付宝）
     * @param orderTitle           订单标题 （如产品描述 最长50汉字）
     * @param tradeNo               RRS商城订单号
     * @param amount                金额
     * @param outRequestNo         流水号（唯一 最长64位）
     * @param productCode          业务产品码（支付宝签约接口时分配）
     * @param sceneCode            业务场景码（支付宝签约接口时分配）
     * @return
     */
    public String buildFreezePayUrl(String bank,
                                    String orderTitle,
                                    String tradeNo,
                                    int amount,
                                    String outRequestNo,
                                    String productCode,
                                    String sceneCode) {


        String orderTitleTemp = orderTitle;

        // 订单标题超长截取
        if (orderTitle != null && !orderTitle.equals("")) {

            if (orderTitle.length() > 15) {

                orderTitleTemp = orderTitle.substring(0, 14) + "...";
            }
        }

        CallBack notify = new CallBack(freezeNotifyUrl);
        CallBack forward = new CallBack(returnUrl);
        FreezePayRequest payRequest = FreezePayRequest.build(token)
                .orderTitle(orderTitleTemp)
                .outOrderNo(tradeNo)
                .amount(amount)
                .outRequestNo(outRequestNo)
                .notify(notify)
                .forward(forward)
                .productCode(productCode)
                .sceneCode(sceneCode);
        if (!Strings.isNullOrEmpty(bank)) {
            try {
                payRequest.defaultBank(Bank.from(bank));
            } catch (BankNotFoundException e) {
                // ignore
            }
        }
        return payRequest.url();
    }

    /**
     * 构建支付宝资金授权 - 退款 请求url
     * @param authNo            RRS商城订单号
     * @param outRequestNo     流水号（唯一 最长64位）
     * @param amount            金额
     * @param remark            业务描述（最多50汉字）
     * @return
     */
    public String buildUnFreezePayUrl (String authNo,
                                       String outRequestNo,
                                       int amount,
                                       String remark) {
        CallBack notify = new CallBack(notifyUrl);
        CallBack forward = new CallBack(returnUrl);
        UnFreezeRequest payRequest = UnFreezeRequest.build(token)
                .authNo(authNo)
                .outRequestNo(outRequestNo)
                .amount(amount)
                .remark(remark)
                .notify(notify);
        return payRequest.url();
    }

    private Map<String, Object> createOrdersAndReturnUrlAndOrderIdsByFreezePay(String data, Long tradeInfoId,
                                                                               String bank, HttpServletRequest request) {
        Long userId = UserUtil.getUserId();
        User user = checkUserStatus(userId);
        List<FatOrder> fatOrders = JSON_MAPPER.fromJson(data, JSON_MAPPER.createCollectionType(List.class, FatOrder.class));

        //remove if region not match
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionIdR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionIdR.isSuccess()) {
            log.error("fail to get region from cookie, error code:{}", regionIdR.getError());
            throw new JsonResponseException(500, messageSources.get(regionIdR.getError()));
        }
        Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateOrder(fatOrders, regionIdR.getResult());
        if (!filterFatOrders.isSuccess()) {
            log.error("fail to filter fatOrders, error code:{}", filterFatOrders.getError());
            throw new JsonResponseException(500, messageSources.get(filterFatOrders.getError()));
        }

        //计算优惠价
        Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(fatOrders, user);
        if (!discountAndUsageR.isSuccess()) {
            log.error("fail to process order code discount. fatOrders={}, buyerId={},error code:{}",
                    fatOrders, user.getId(), discountAndUsageR.getError());
            throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
        }
        DiscountAndUsage discountAndUsage = discountAndUsageR.getResult();

        Response<Map<Long, Long>> result = orderWriteService.create(userId, tradeInfoId,
                fatOrders, discountAndUsage.getSkuIdAndDiscount(), bank);
        if (!result.isSuccess()) {
            log.error("fail to create order for  {},error code:{}", data, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        //使用完优惠券后记录使用情况
        recordCodeUsage(discountAndUsage, result.getResult());

        boolean needRedirect = false;
        Map<Long, Long> maps = result.getResult();
        Set<Long> ids = Sets.newHashSet();
        for (Long id : maps.keySet()) {
            ids.add(maps.get(id));
        }
        int total = 0;
        List<Long> paidIds = Lists.newArrayList();
        for (Long id : ids) {

            Response<Order> getOrder = orderQueryService.findById(id);
            if (!getOrder.isSuccess()) {
                log.error("fail to get order for  {},error code:{}", data, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            Order order = getOrder.getResult();

            if (Objects.equal(order.getPaymentType(), Order.PayType.ONLINE.value())) {
                needRedirect = true;
                total += order.getFee();
                paidIds.add(order.getId());
            }else{
                try {
            		 /*Shop shop=shopService.findByUserId(order.getSellerId()).getResult();
                 	 User user1=accountService.findUserById(order.getBuyerId()).getResult();
     	           	 if(isUserStatus(String.valueOf(order.getSellerId()),"2")){
     	              	sendSms(shop.getPhone(),
     	              	"您有一个新订单【"+order.getId()+"】，买家【"+user1.getMobile()+"】选择货到付款，请尽快安排发货。");
     	              }*/
                    smsEventBus.post(new SmsEvent(order.getId(), order.getId(), "8"));
                } catch (Exception e) {
                    log.error("fail to get sms exception"+e.getMessage());
                }

            }
        }



        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", ids);
        //ids 为拆分之后的订单ID add by cwf
        String couponsId =  request.getParameter("couponsId");//获取是否选择了优惠券信息
        if(!StringUtils.isEmpty(couponsId) && !couponsId.equals("-1")){
            total = changeCouponsOrderItem(couponsId,total,ids,userId);
        }

        // 亿起发CPS处理
//        orderEventBus.post(new OrderEvent(ids, request));
        orderEventBus.post(new CpsEvent( request,ids, CpsService.EventType.ORDER));


        if (needRedirect) {  // 有在线支付的订单时需要跳转支付页面
            String url = null;

            if ("ALIPAY-FREEZE".equals(bank)) {

                OrderDescription description = getDescriptionOfOrders(paidIds);
                String outerIds = Joiner.on(",").skipNulls().join(ids);
                String productCode = "FUND_PRE_AUTH";
                String sceneCode = "PRESALE";
                // 当前毫秒 + 5位随机数
                String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
                url = buildFreezePayUrl(bank, description.getTitle(), outerIds, total, outRequestNo, productCode, sceneCode);
            } else {
                url = buildAlipayUrl(bank, total, paidIds);
            }

            mappedResult.put("url", url);

//            Response<Boolean> refundByAlipay = UnFreezeRequest.build(token).authNo("2014121500002001790000000016").outRequestNo("2014121500002001790000000016").remark("ceshi").amount(7).refund();

            return mappedResult;
        } else { // 全订单都为货到付款时只需要返回空即可
            mappedResult.put("url", "");
        }

        return mappedResult;
    }

    /**
     * 直接创建预售订单并支付定金
     *
     * @param data        提交数据
     * @param tradeInfoId 配送信息标识
     * @return 需要跳转的支付页面链接
     */
    @RequestMapping(value = "/buyer/pre-orders3", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> directCreatePresellOrderAndPay3(@RequestParam("data") String data,
                                                               @RequestParam("tradeInfoId") Long tradeInfoId,
                                                               @CookieValue(value = "bank", required = false) String bank,
                                                               @RequestParam(value = "regionId") Integer regionId,
                                                               HttpServletRequest request) {

        return createPresellOrdersAndReturnUrlAndOrderIds2(data, tradeInfoId, regionId, bank, request);
    }

    private Map<String, Object> createPresellOrdersAndReturnUrlAndOrderIds2(String data, Long tradeInfoId, Integer regionId, String bank, HttpServletRequest request) {

        Long userId = UserUtil.getUserId();

        User buyer = checkUserStatus(userId);

        // tradeInfoId 相同不能多次下单
        // 押金订单同一地址 不能多次下单
        Response<Boolean> tradeInfoIdR = orderQueryService.containByTradeInfoId(userId, tradeInfoId);
        if (!tradeInfoIdR.isSuccess()) {
            log.error("failed to create predepositOrder by tradeInfoId({}), error code:{}", tradeInfoId, tradeInfoIdR.getError());
            throw new JsonResponseException(500,messageSources.get("tradeInfo.not.pay.order"));
        }
        if (tradeInfoIdR.getResult()) {
            log.error("failed to create predepositOrder by tradeInfoId({}), error code:{}", tradeInfoId, "tradeInfo.not.pay.order");
            throw new JsonResponseException(500,messageSources.get("tradeInfo.not.pay.order"));
        }

        FatOrderPreDeposit fatOrderPreSale = JSON_MAPPER.fromJson(data, FatOrderPreDeposit.class);

        Long id = fatOrderPreSale.getPreDeposit().getId();

        Response<PreDeposit> pR = preDepositService.findById(id);
        if (!pR.isSuccess()) {
            log.error("failed to find predeposit by id({}), error code:{}", id, pR.getError());
            throw new JsonResponseException(500,pR.getError());
        }

        PreDeposit preSale = pR.getResult();
        if (!equalWith(preSale.getStatus(), PreDeposit.Status.RUNNING.value())) {
            log.error("presale(id={}) has not released", id);
            throw new JsonResponseException(500, messageSources.get("presell.not.release"));
        }

        //计算优惠码
        Response<DiscountAndUsage> discountAndUsageR = activityBindService.processOrderCodeDiscount(
                Lists.newArrayList(fatOrderPreSale), buyer);
        if (!discountAndUsageR.isSuccess()) {
            log.error("fail to process preSale order code discount, preSaleFatOrder={}, buyerId={},error code:{}",
                    fatOrderPreSale, buyer.getId(), discountAndUsageR.getError());
            throw new JsonResponseException(500, messageSources.get(discountAndUsageR.getError()));
        }


        Response<Long> createPreSale = preDepositService.createPreDepositOrder(userId, tradeInfoId, regionId, fatOrderPreSale,
                discountAndUsageR.getResult(), bank);

        if (!createPreSale.isSuccess()) {
            log.error("fail to create preSale order{}, error code={}", data, createPreSale.getError());
            throw new JsonResponseException(500, messageSources.get(createPreSale.getError()));
        }

        Long orderItemId = createPreSale.getResult();
        Response<OrderItemTotalFee> getOrderItem = orderQueryService.findExtraByOrderItemId(orderItemId);
        if (!getOrderItem.isSuccess()) {
            log.error("fail to get deposit orderItem{}, error code={}", orderItemId, getOrderItem.getError());
            throw new JsonResponseException(500, messageSources.get(getOrderItem.getError()));
        }
        OrderItem oi = getOrderItem.getResult();
        int total = oi.getFee();
        List<Long> ids = Lists.newArrayList(oi.getOrderId(), oi.getId());


        Set<Long> idsSet = Sets.newHashSet();
        idsSet.add(oi.getOrderId());
        // 亿起发CPS处理
//        orderEventBus.post(new OrderEvent(idsSet, request));
        orderEventBus.post(new CpsEvent(request,idsSet, CpsService.EventType.ORDER));

        String tradeNo = joiner.join(ids);
        String subject = getOrderItemSubject(oi);

        String url = null;
        Integer type = 2;

        if ("ALIPAY-FREEZE".equals(bank)) {

            String outerIds = Joiner.on(",").skipNulls().join(ids);
            String productCode = "FUND_PRE_AUTH";
            String sceneCode = "PRESALE";
            // 当前毫秒 + 5位随机数
            String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
            url = buildFreezePayUrl(bank, oi.getItemName(), outerIds, total, outRequestNo, productCode, sceneCode);
            type = 1;
        } else {
            url = buildAlipayUrl(bank, subject, oi.getItemName(), total, tradeNo);
        }

        PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
        preAuthorizationDepositOrder.setOrderId(oi.getOrderId());
        preAuthorizationDepositOrder.setType(type);

        Response<Boolean> response = preAuthorizationDepositOrderService.createPreDepositOrder(preAuthorizationDepositOrder);
        if (!response.isSuccess()) {
            log.error("fail to create preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
            throw new JsonResponseException(500, messageSources.get(response.getError()));
        }

        Map<String, Object> mappedResult = Maps.newHashMap();
        mappedResult.put("orderIds", oi.getOrderId());
        mappedResult.put("url", url);
        return mappedResult;
    }

    /**
     * 直接支付预售定金或者尾款
     *
     * @param orderItemId 子订单id
     */
    @RequestMapping(value = "/buyer/pre-orders2/{id}/pay", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String presellOrderPay2(@PathVariable("id") Long orderItemId,
                                  @CookieValue(value = "bank", required = false) String bank,
                                  @RequestParam(value = "deliverTime", required = false) String deliverTime) {
        //默认支付定金
        log.info("succeed to start presellOrderPay2");

        int orderItemType =2;
        Response<PreAuthorizationDepositOrder> agreementsR = preAuthorizationDepositOrderService.findPreDepositByOrderId(orderItemId);
        if (!agreementsR.isSuccess()) {
            log.error("fail to get orderItem(id={}), error={}", orderItemId, agreementsR.getError());
            throw new JsonResponseException(500, messageSources.get(agreementsR.getError()));
        }
        //判断支付押金还是支付尾款，如果是1，说明已经支付了定金或者是预授权，则支付的是订单尾款
        if(Objects.equal(agreementsR.getResult().getStatus(),1))
        {
             orderItemType =3;
        }
        else if(Objects.equal(agreementsR.getResult().getStatus(),0))
        {//未付押金状态时，判断押金付款时间是否超时
            Response<Order> resOrder = orderQueryService.findById(orderItemId);
            if (!resOrder.isSuccess()) {
                log.error("fail to get order(id={}), error={}", orderItemId, resOrder.getError());
            }
            Order orderDao = resOrder.getResult();
            Response<List<OrderItem>> respOrderItem = orderQueryService.findSubsByOrderId(orderItemId);
            if (!respOrderItem.isSuccess()) {
                log.error("fail to get orderItem(id={}), error={}", orderItemId, respOrderItem.getError());
            }
            List<OrderItem> orderItemList = respOrderItem.getResult();
            if(orderItemList!=null && orderItemList.size()>0)
            {
                Response<PreDeposit>  resPreDeposit = preDepositService.findPreDepositByItemId(orderItemList.get(0).getItemId());
                if (!resPreDeposit.isSuccess()) {
                    log.error("fail to get PreDeposit(id={}), error={}", orderItemId, resPreDeposit.getError());
                }

                PreDeposit preDeposit = resPreDeposit.getResult();
                if(orderDao!=null && preDeposit!=null){
                    if (new DateTime(orderDao.getCreatedAt()).plusHours(preDeposit.getEarnestTimeLimit()).isBeforeNow()) {
                        throw new JsonResponseException(500, "押金付款超时，请重新下订单操作。");
                    }
                }
            }
        }

        OrderItem depositOrderItem = orderQueryService.findByMap(orderItemId,orderItemType);
        orderItemId = depositOrderItem.getId();

        Response<OrderItem> getOrderItem = orderQueryService.findOrderItemById(orderItemId);
        if (!getOrderItem.isSuccess()) {
            log.error("fail to get orderItem(id={}), error={}", orderItemId, getOrderItem.getError());
            throw new JsonResponseException(500, messageSources.get(getOrderItem.getError()));
        }


        OrderItem oi = getOrderItem.getResult();
        if (oi == null) {
            log.error("orderItem(id={}) not exist", orderItemId);
            throw new JsonResponseException(500, messageSources.get("order.item.not.exist"));
        }
        Long orderId = oi.getOrderId();

        Response<OrderExtra> resultOrderExtra = orderQueryService.getOrderExtraByOrderId(orderId);
        if (!resultOrderExtra.isSuccess()) {
            log.error("fail to get OrderExtra by order(id={}), error={}", orderId, resultOrderExtra.getError());
            throw new JsonResponseException(500, messageSources.get(resultOrderExtra.getError()));
        }
        OrderExtra exitOrderExtra = resultOrderExtra.getResult();
        if(exitOrderExtra!=null && notEmpty(deliverTime)){
            OrderExtra orderExtra = new OrderExtra();
            orderExtra.setDeliverTime(deliverTime);
            orderExtra.setId(exitOrderExtra.getId());
            orderExtra.setOrderId(orderId);
            Response<Boolean> result=orderQueryService.updateOrderExtra(orderExtra);
            if (!result.isSuccess()) {
                log.error("fail to update OrderExtra by order(id={}), error={}", orderId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        } else if(exitOrderExtra == null && notEmpty(deliverTime)) {
            exitOrderExtra = new OrderExtra();
            exitOrderExtra.setDeliverTime(deliverTime);
            exitOrderExtra.setOrderId(orderId);
            Response<Long> result = orderQueryService.createOrderExtra(exitOrderExtra);
            if (!result.isSuccess()) {
                log.error("fail to create OrderExtra by order(id={}), error={}", orderId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
        }


        if (!Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value()) &&
                !Objects.equal(oi.getType(), OrderItem.Type.PRESELL_REST.value())) {
            log.error("orderItem(id={}) is not presell order", orderItemId);
            throw new JsonResponseException(500, messageSources.get("order.item.status.incorrect"));
        }

        Long userId = UserUtil.getUserId();
        if (!Objects.equal(oi.getBuyerId(), userId)) {
            log.error("current user(id={}) is not real buyer(id={}), orderItem({})",
                    userId, oi.getBuyerId(), oi);
            throw new JsonResponseException(messageSources.get("order.item.buyer.not.match"));
        }

        //判断当前支付渠道和子订单中的支付渠道是否相等
        if(needResetOrder(oi, bank)) { // 如果渠道号发生了变化，则修改子订单的id
            // 新建子订单
            Response<OrderItem> resetResult = orderWriteService.resetOrderItem(oi, bank);
            checkState(resetResult.isSuccess(), resetResult.getError());
            oi = resetResult.getResult();//重新赋值

            // 通知关闭订单,这里的子订单号要使用修改前的子订单号
            alipayEventBus.post(new TradeCloseEvent(token, oi.getOrderId() + "," + orderItemId));
            orderItemId = oi.getId();

        }
        bank = oi.getChannel();

        // 判断当前订单是否处于可以支付的状态
        if (!canPay2(orderItemId)) {
            log.error("current presell orderItem(id={}) may expired or presell not finished", orderItemId);
            if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                throw new JsonResponseException(500, messageSources.get("presell.order.deposit.expire"));
            } else {
                throw new JsonResponseException(500, messageSources.get("presell.order.rest.expire.or.not.finish"));
            }
        }

        List<Long> ids = Lists.newArrayList(oi.getOrderId(), oi.getId());
        String tradeNo = joiner.join(ids);
        String subject = getOrderItemSubject(oi);

        String url = null;
        Integer type = 2;
        if ("ALIPAY-FREEZE".equals(bank)) {

            String outerIds = Joiner.on(",").skipNulls().join(ids);
            String productCode = "FUND_PRE_AUTH";
            String sceneCode = "PRESALE";
            // 当前毫秒 + 5位随机数
            String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
            url = buildFreezePayUrl(bank, oi.getItemName(), outerIds, oi.getFee(), outRequestNo, productCode, sceneCode);
            type = 1;
        } else {
            url = buildAlipayUrl(bank, subject, oi.getItemName(), oi.getFee(), tradeNo);
        }

//        Response<Order>  orderR = orderQueryService.findByOriginId(orderId);
//        if (!orderR.isSuccess()) {
//            log.error("fail to create preAuthorizationDepositOrder data{}, error code={}", orderId, orderR.getError());
//            throw new JsonResponseException(500, messageSources.get(orderR.getError()));
//        }
//        Long newOrderId = orderR.getResult().getId();

        Long newOrderId = orderId;
        if(2==orderItemType){
        PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
        preAuthorizationDepositOrder.setOrderId(orderId);
        preAuthorizationDepositOrder.setType(type);

        Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
        if (!response.isSuccess()) {
            log.error("fail to create preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
            throw new JsonResponseException(500, messageSources.get(response.getError()));
        }
        }
        PreAuthorizationDepositOrder agereemnts = agreementsR.getResult();
        agereemnts.setOrderId(newOrderId);
        Response<Boolean> booleanResponse = preAuthorizationDepositOrderService.updatePreDepositById(agereemnts);
        if (!booleanResponse.isSuccess()) {
            log.error("fail to create preAuthorizationDepositOrder data{}, error code={}", agereemnts, booleanResponse.getError());
            throw new JsonResponseException(500, messageSources.get(booleanResponse.getError()));
        }

        return url;
    }

    @RequestMapping(value = "/order/{orderId}/deliver2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deliver2(@PathVariable("orderId") Long orderId, @RequestBody OrderLogisticsInfoDto orderLogisticsInfoDto) {
        Order order = getOrder(orderId);
        if (!preDepositOrderCanDeliver(order)) {
            log.error("preSale not finish, can not deliver");
            throw new JsonResponseException(500, messageSources.get("preSale.not.finish"));
        }
        BaseUser currentUser = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.deliver2(order, orderLogisticsInfoDto, currentUser);
        if (!result.isSuccess()) {
            log.error("fail to deliver for order(id={}) by user({}),error code:{}",
                    orderId, currentUser, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
        preAuthorizationDepositOrder.setOrderId(order.getId());
//        preAuthorizationDepositOrder.setStatus(2);
        preAuthorizationDepositOrder.setDeliverStatus(1);

        Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
        if (!response.isSuccess()) {
            log.error("fail to deliver for preAuthorizationDepositOrder(id={}) by user({}),error code:{}",
                    orderId, currentUser, result.getError());
            throw new JsonResponseException(500, messageSources.get(response.getError()));
        }

        try {
            int type=order.getType();
            // User user=accountService.findUserById(order.getBuyerId()).getResult();
            // LogisticsInfo logisticsInfo=logisticsInfoService.findByOrderId(orderId).getResult();
            if(type==1){
                smsEventBus.post(new SmsEvent(orderId, orderId, "2"));
            	/*if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
            		String smsContent="亲爱的【"+user.getMobile()+"】，您的订单【"+orderId+"】已发货";
            		if(logisticsInfo!=null){
            			smsContent+="，为您安排【"+logisticsInfo.getCompanyName()+"】（快递单号：【"+logisticsInfo.getFreightNote()+"】）送至府上，请检查后签收。";
            		}
                	sendSms(user.getMobile(),smsContent);
                }*/
            }else{
                smsEventBus.post(new SmsEvent(orderId, orderId, "3"));
            	/*if(isUserStatus(String.valueOf(order.getBuyerId()),"1")){
            	  List<OrderItem> list=orderQueryService.findOrderItemByOrderId(orderId).getResult();
            		String orderName=list.size()>0?list.get(0).getItemName():"";
            		String smsContent="亲爱的【"+user.getMobile()+"】，您的预购商品【"+orderName+"】已发货";
            		if(logisticsInfo!=null){
            			smsContent+="，为您安排【"+logisticsInfo.getCompanyName()+"】（快递单号：【"+logisticsInfo.getFreightNote()+"】）送至府上，请检查后签收。";
            		}
                	sendSms(user.getMobile(),smsContent);
                }*/
            }
        } catch (Exception e) {
            log.error("fail to get sms exception"+e.getMessage());
        }
        return "ok";
    }

    private boolean preDepositOrderCanDeliver(Order order) {
        if (Objects.equal(order.getType(), Order.Type.PRE_SELL.value())
                && Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
            Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(order.getId());
            if (!orderItemsR.isSuccess()) {
                log.error("find orderItems by orderId={} fail, error code:{}",
                        order.getId(), orderItemsR.getError());
                return false;
            }
            Long itemId = orderItemsR.getResult().get(0).getItemId();
            Response<PreDeposit> preSaleR = preDepositService.findPreDepositByItemId(itemId);
            if (!preSaleR.isSuccess()) {
                log.error("fail to find preSale by itemId={}, error code:{}",
                        itemId, preSaleR.getError());
                return false;
            }
            PreDeposit preSale = preSaleR.getResult();
//            return !new DateTime(preSale.getPreSaleFinishAt()).isAfterNow();
        }
        return true;
    }

    /**
     * 判断预售订单是否能付款
     */
    private boolean canPay2(Long orderItemId) {
        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderItemId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderItemId, orderItemR.getError());
        }
        OrderItem orderItem = orderItemR.getResult();
        Long itemId = orderItem.getItemId();
        Response<PreDeposit> preSaleR = preDepositService.findPreDepositByItemId(itemId);
        if (!preSaleR.isSuccess()) {
            log.error("fail to find preSale by itemId={}, error code:{}", itemId, preSaleR.getError());
            return false;
        }
        PreDeposit preSale = preSaleR.getResult();
        //定金
        if (Objects.equal(OrderItem.Type.PRESELL_DEPOSIT.value(), orderItem.getType())) {
            //付定金超时
            return !new DateTime(orderItem.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).isBeforeNow();
        }
        //尾款
        if (Objects.equal(OrderItem.Type.PRESELL_REST.value(), orderItem.getType())) {
            //付尾款时间未到不能付款
            if (new DateTime(preSale.getRemainStartAt()).isAfterNow())
                return false;
            //付尾款超时
//            return !new DateTime(preSale.getRemainFinishAt()).isBeforeNow();
            return true;
        }
        log.error("orderItem type not correct");
        return false;
    }

    private void addPreDepositOrderInfo(List<? extends RichOrder> richOrders) {
        for (RichOrder richOrder : richOrders) {
            if (Objects.equal(richOrder.getOrderType(), Order.Type.PRE_SELL.value())) {
                try {
                    if (richOrder.getOrderItems() == null || richOrder.getOrderItems().isEmpty()) {
                        log.warn("pre-sell order(id={}) doesn't has any orderItems", richOrder.getOrderId());
                        continue;
                    }
                    Integer totalRefundAmount = 0;
                    for (RichOrderItem roi : richOrder.getOrderItems()) {
                        if (roi.getRefundAmount() != null)
                            totalRefundAmount += roi.getRefundAmount();
                    }
                    richOrder.setTotalRefundAmount(totalRefundAmount);
//                    Response<PreSale> preSaleR = preSaleService.findPreSaleByItemId(richOrder.getOrderItems().get(0).getSku().getItemId());
                    //增加预授权预置商品配置读取
                    Response<PreDeposit> preDepositR = preDepositService.findPreDepositByItemId(richOrder.getOrderItems().get(0).getSku().getItemId());
//                    if (!preSaleR.isSuccess() || preSaleR.getResult() == null) {
//                        log.error("fail to find preSale by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
//                        continue;
//                    }
                    //增加预授权预置商品配置读取
                    if (!preDepositR.isSuccess() || preDepositR.getResult() == null) {
                        log.error("fail to find preDepositR by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
                        continue;
                    }
//                    PreSale preSale = preSaleR.getResult();
//                    richOrder.setRemainFinishAt(preSale.getRemainFinishAt());
//                    List<RichOrderItem> richOrderItems = richOrder.getOrderItems();
//                    richOrder.setCanPayEarnest(false);
//                    if(DateTime.now().isBefore(new DateTime(richOrderItems.get(0).getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()))) {
//                        richOrder.setCanPayEarnest(true);
//                    }
//                    richOrder.setCanPayRemain(false);
//                    if(DateTime.now().isBefore(new DateTime(preSale.getRemainFinishAt())) &&
//                            DateTime.now().isAfter(new DateTime(preSale.getRemainStartAt()))) {
//                        richOrder.setCanPayRemain(true);
//                    }
//
//                    richOrder.setRemainFinishAt(preSale.getRemainFinishAt());
//                    Date date = new DateTime(richOrder.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).toDate();
//                    richOrder.setEarnestPayTime(date);
//                    richOrder.setPreSaleFinishAt(preSale.getPreSaleFinishAt());
                    //增加预授权预置商品配置读取
                    if (preDepositR.isSuccess()) {
                    List<RichOrderItem> richOrderItems = richOrder.getOrderItems();
                        PreDeposit preDeposit = preDepositR.getResult();
                        richOrder.setRemainFinishAt(preDeposit.getRemainFinishAt());
                        if(DateTime.now().isBefore(new DateTime(richOrderItems.get(0).getCreatedAt()).plusHours(preDeposit.getEarnestTimeLimit()))) {
                            richOrder.setCanPayEarnest(true);
                        }
                        if(DateTime.now().isBefore(new DateTime(preDeposit.getRemainFinishAt())) &&
                                DateTime.now().isAfter(new DateTime(preDeposit.getRemainStartAt()))) {
                            richOrder.setCanPayRemain(true);
                        }
                        richOrder.setRemainFinishAt(preDeposit.getRemainFinishAt());
                        Date date = new DateTime(richOrder.getCreatedAt()).plusHours(preDeposit.getEarnestTimeLimit()).toDate();
                        richOrder.setEarnestPayTime(date);
                        richOrder.setPreSaleFinishAt(preDeposit.getPreSaleFinishAt());
                        richOrder.setDepositType(1);
                        for(RichOrderItem richOrderItem:richOrderItems){
                            if (Objects.equal(richOrderItem.getOrderItemType(),OrderItem.Type.PRESELL_REST.value())){
                                richOrder.setTotalPrice(richOrderItem.getCount()*richOrderItem.getUnitFee());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("failed to handle order(id={}), cause:{}", richOrder.getOrderId(), Throwables.getStackTraceAsString(e));
                }
            }
        }
    }

    /**
     * 预授权押金订单卖家退货(确认收到退货)，退款
     *
     * @param orderId 子订单id
     */
    @RequestMapping(value = "/order/{orderId}/depositCancel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void depositCancelOrderItem(@PathVariable("orderId") Long orderId) {

        Response<OrderItem> orderItemR = orderQueryService.findOrderItemById(orderId);
        if (!orderItemR.isSuccess()) {
            log.error("fail to find orderItem by id={}, error code:{}", orderId,
                    orderItemR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemR.getError()));
        }
        OrderItem orderItem = orderItemR.getResult();

        Response<Order> orderResponse = orderQueryService.findById(orderItem.getOrderId());
        if (isNull(orderItem)){
            log.error("fail to find orders by orderId={}, error code:{}", orderItem.getOrderId(),
                    orderResponse.getError());
            throw new JsonResponseException(500, messageSources.get(orderResponse.getError()));
        }
        Order order = orderResponse.getResult();

        Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(orderItem.getOrderId());
        if (!orderItemsR.isSuccess()) {
            log.error("fail to find orderItems by orderId={}, error code:{}", orderItem.getOrderId(),
                    orderItemsR.getError());
            throw new JsonResponseException(500, messageSources.get(orderItemsR.getError()));
        }

        try {
            Response<PreAuthorizationDepositOrder> authDepositR =
                    preAuthorizationDepositOrderService.findPreDepositByOrderId(order.getId());
            PreAuthorizationDepositOrder authDeposit = authDepositR.getResult();

            /*
            //支付宝回调方法中处理
            if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                // 首款子订单
                PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
                preAuthorizationDepositOrder.setOrderId(order.getId());
                preAuthorizationDepositOrder.setStatus(PreAuthorizationDepositOrder.DepositPayType.DELIVERFINNSH.value());

                Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
                if (!response.isSuccess()) {
                    log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
                    throw new JsonResponseException(500, response.getError());
                }
            } else
            */
            if (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {

                // 添加微信支付逻辑
                if (Objects.equal(orderItem.getPaymentPlatform(), "2")) {
                    Response<Boolean> wxRefundRes = orderWriteService.wxPayRefund(orderItem);
                    if (!wxRefundRes.isSuccess()) {
                       log.error(wxRefundRes.getError());
                    }
                    return;
                } else {
                    log.info("ordert not paid by wx");
                }

                // 尾款子订单
                // 退押金
                // 向支付宝发出退款请求
                log.info("authDeposit.getType()：{}",authDeposit.getType());
                if (authDeposit.getType().equals(1)) {
                    log.info("authDeposit.getType=1");
                    // 预授权
                    String batchNo = authDeposit.getTradeNo();
//                    AlipayRefundData refund = new AlipayRefundData(orderItem.getPaymentCode(),
//                            orderItem.getRefundAmount(), orderItem.getReason());

                    CallBack notify = new CallBack(freezeNotifyUrl);
//                    Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
//                            .detail(Lists.newArrayList(refund)).notify(notify).refund();

                    try
                    {
                        String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
                        String remark = "日日顺净水预授权订单";
                        Response<Boolean> refundByAlipay = UnFreezeRequest.build(token)
                                .authNo(batchNo)
                                .outRequestNo(outRequestNo)
                                .amount(orderItem.getRefundAmount())
                                .remark(remark)
                                .notify(notify).refund();

                        log.info("freezeNotify return value：{}",refundByAlipay.isSuccess());
                        if (!refundByAlipay.isSuccess()) {
                            log.error("fail to refund to {}, error code:{}", orderItem, refundByAlipay.getError());
                            return;
                        }
                    }
                    catch (Exception ex)
                    {
                        log.error("fail to cancel depositOrder:{}",ex.getMessage());
//                        throw new JsonResponseException(500, ex.getMessage());
                    }
                } else if (authDeposit.getType().equals(2)) {
                    log.info("authDeposit.getType=2");
                    // 押金
                    Long orderItemForDeposit = 0L;
                    for (OrderItem orderI : orderItemsR.getResult()) {
                        if (Objects.equal(orderI.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                            orderItemForDeposit = orderI.getId();
                        }
                    }
                    Date refundAt = DateTime.now().toDate();
                    String batchNo = RefundRequest.toBatchNo(refundAt, orderItemForDeposit);
                    AlipayRefundData refund = new AlipayRefundData(orderItem.getPaymentCode(),
                            orderItem.getRefundAmount(), orderItem.getReason());

                    CallBack notify = new CallBack(refundNotifyUrl);
                    Response<Boolean> refundByAlipay = RefundRequest.build(token).batch(batchNo)
                            .detail(Lists.newArrayList(refund)).notify(notify).refund();
                    if (!refundByAlipay.isSuccess()) {
                        log.error("fail to refund to {}, error code:{}", orderItem, refundByAlipay.getError());
                        return;
                    }
                }

               /*
                //支付宝回调方法中处理
                PreAuthorizationDepositOrder preAuthorizationDepositOrder = new PreAuthorizationDepositOrder();
                preAuthorizationDepositOrder.setOrderId(order.getId());
                preAuthorizationDepositOrder.setStatus(PreAuthorizationDepositOrder.DepositPayType.DELIVERFINNSH.value());

                Response<Boolean> response = preAuthorizationDepositOrderService.updatePreDepositOrder(preAuthorizationDepositOrder);
                if (!response.isSuccess()) {
                    log.error("fail to update preAuthorizationDepositOrder data{}, error code={}", preAuthorizationDepositOrder, response.getError());
                    throw new JsonResponseException(500, response.getError());
                }
                */
            }
        } catch (JsonResponseException e) {
            log.error("fail to cancel depositOrder");
            throw new JsonResponseException(500, e.getMessage());
        }
    }


    /**
     * 预授权押金订单卖家同意退货
     *
     * @param orderId 订单id
     */
    @RequestMapping(value = "/depositOrder/{orderId}/agree", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String agreeReturnDeposit(@PathVariable("orderId") Long orderId) {
        BaseUser user = UserUtil.getCurrentUser();
        Long originOrderId = 0L;
        if(!isNull(orderId)){
            Response<OrderItem> orderItemRes = orderQueryService.findOrderItemById(orderId);
            if(orderItemRes.isSuccess()){
                originOrderId = orderItemRes.getResult().getOrderId();
            }
        }

        Response<Boolean> result = orderWriteService.agreeReturnDeposit(originOrderId, user);
        if (!result.isSuccess()) {
            log.error("fail to agree return goods for order for order(id={}) by user({}), error code:{}",
                    orderId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    /**
     * 预授权押金订单添加退货款理由和金额
     *
     * @param orderId  订单id
     * @param reason       理由
     * @param refundAmount 金额
     * @return 是否成功
     */
    @RequestMapping(value = "/buyer/order/{orderId}/depositExtra", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String depositAddExtra(@PathVariable("orderId") Long orderId,
                                  @RequestParam(value = "reason") String reason,
                                  @RequestParam(value = "refundAmount") Integer refundAmount) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Boolean> result = orderWriteService.depositAddReasonAndRefund(orderId, user, reason, refundAmount);
        if (!result.isSuccess()) {
            log.error("fail to add reason or refund for orderItemId(id={}) by user({}),cause:{}",
                    orderId, user, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }


    /**
     * 使用商家优惠券之后的统计整体订单的优惠价格
     * **/
    private int getsellerCouonsByUse(List<FatOrder> fatOrders,int total,Set<Long> ids,Long userId){
        //拆单之后 针对 每个订单中的产品需要进行优惠计算  add by cwf
        HashMap<Long,Long> sellerCouponMap = new HashMap<Long, Long>();//一个商家选择一个优惠券
        for(FatOrder fatOrder : fatOrders) {
            Long couponsId = fatOrder.getCouponIds();
            sellerCouponMap.put(fatOrder.getSellerId(),couponsId);
        }
        int orderFreeAmount = 0;//计算订单的优惠价格
        for (Long id : ids) {
            Response<Order> getOrder = orderQueryService.findById(id);
            if (!getOrder.isSuccess()) {
                log.error("fail to get order for  {},error code:{}", getOrder, getOrder.getError());
                throw new JsonResponseException(500, messageSources.get(getOrder.getError()));
            }
            Order order = getOrder.getResult();
            if(sellerCouponMap.containsKey(order.getSellerId())){ //判断该店铺是否选择了优惠券
                Long couponsId = sellerCouponMap.get(order.getSellerId());
                if(Objects.equal(couponsId,-1L) || couponsId==null){
                    continue;
                }
                Response<List<OrderItem>> orderItemList =  orderQueryService.findSubsByOrderId(order.getId());
                if(!orderItemList.isSuccess()){
                    log.error("fail to get orderItem for  {},error code:{}", orderItemList, orderItemList.getError());
                    throw new JsonResponseException(500, messageSources.get(orderItemList.getError()));
                }

                ///////////////////////////////计算参加优惠券的产品的价格之和/////////////////////////////////////
                /******************************************************************/
                Response<List<RrsCouponsItemList>> couponsItemListBy = couponsItemListService.queryCouponsItemListBy(couponsId, null, null, "");
                Iterator<RrsCouponsItemList> its =  couponsItemListBy.getResult().iterator();
                HashMap<String,RrsCouponsItemList> allItemMap = new HashMap<String,RrsCouponsItemList>();
                while(its.hasNext()){
                    RrsCouponsItemList rrsCouponsItemList = its.next();
                    String keyCode = String.valueOf(rrsCouponsItemList.getItemId());
                    allItemMap.put(keyCode,rrsCouponsItemList);
                }
                /******************************************************************/

                /***********************************************/
                Integer totalPrice = 0;
                Iterator<OrderItem> itemOrderList = orderItemList.getResult().iterator();
                HashMap<String,OrderItem> itemPriceMap = new HashMap<String,OrderItem>();
                List<OrderItem> orderItemCouponsList = new ArrayList<OrderItem>();
                while(itemOrderList.hasNext()){
                    OrderItem orderItem = itemOrderList.next();
                    String keyCode = String.valueOf(orderItem.getItemId());//+"-"+String.valueOf(orderItem.getSkuId());
                    if(allItemMap.containsKey(keyCode)){
                        totalPrice += orderItem.getFee().intValue();
                        orderItemCouponsList.add(orderItem);
                    }
                }
                /***********************************************/

//                Integer totalPrice = 0;
//                List<OrderItem> orderItemCouponsList = new ArrayList<OrderItem>();
//                while(its.hasNext()){
//                    RrsCouponsItemList rrsCouponsItemList = its.next();
//                    Long keyCode = rrsCouponsItemList.getItemId();
//                    if(itemPriceMap.containsKey(keyCode)){
//                        OrderItem orderItem  = itemPriceMap.get(keyCode);
//                        totalPrice += orderItem.getFee().intValue();
//                        orderItemCouponsList.add(orderItem);
//                    }
//                }
                ///////////////////////////////////计算参加优惠券的产品的价格之和/////////////////////////////////////////////////
                Response<RrsCou> rrsCouResponse = new Response<RrsCou>();
                log.info("====couponsId====="+couponsId);
                rrsCouResponse = couponsRrsService.queryCouponsById(couponsId);
                if(rrsCouResponse.isSuccess() && rrsCouResponse.getResult()!=null){
                    int couponsTerm = rrsCouResponse.getResult().getTerm();
                    log.info("item Total Price size"+totalPrice+"coupons term price"+couponsTerm+" result :"+(totalPrice.intValue() > couponsTerm));
                    if((totalPrice.intValue()) >= couponsTerm){  //产品价格大于优惠券的最低消费价格 则可以使用 否则不能使用
                        int joinItems = orderItemCouponsList.size();
                        double freeAmount = 0;//优惠金额 = (产品金额 / 参加优惠的产品总额之和) * 优惠券金额  最后一个产品是前面几个产品剩余优惠
                        int allFreeAmount = 0;//计算总共

                        int couponsAmount = rrsCouResponse.getResult().getAmount();
                        orderFreeAmount +=couponsAmount;
                        HashMap<Long,Integer> orderFreeMap = new HashMap<Long,Integer>();
                        for(int i=0;i<joinItems;i++){
                            OrderItem orderItem = orderItemCouponsList.get(i);

                            if((i+1)==joinItems){//最后一个产品是前面几个产品剩余优惠
                                freeAmount =  couponsAmount - allFreeAmount;
                            }else{
                                double modeV = Math.round((orderItem.getFee().doubleValue() / Double.valueOf(totalPrice)) * 100000);
                                freeAmount = Math.round(modeV * couponsAmount / 100000);
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
                            RrsCouOrderItem rrsCouOrderItem = new RrsCouOrderItem();
                            rrsCouOrderItem.setCouponsId(Long.valueOf(couponsId));//优惠券Id
                            rrsCouOrderItem.setItemId(orderItem.getItemId());//订单明细ID
                            rrsCouOrderItem.setOrderId(orderItem.getOrderId());//订单ID
                            rrsCouOrderItem.setSkuId(orderItem.getSkuId());//增加 skuid字段用户查询
                            rrsCouOrderItem.setUserId(userId);//用户ID
                            rrsCouOrderItem.setFreeAmount(BigDecimal.valueOf(freeAmount));//优惠金额

                            rrsCouOrderItemService.saveCouOrderItem(rrsCouOrderItem);
                        }

                        //以上为订单明细  一下计算优惠后的总的订单优惠价格
                        //用户购买时会进行拆单 需要计算拆单之后该订单的优惠价格 即需要计算该订单下的产品优惠金额
                        if(!orderFreeMap.isEmpty()){
                            for(Long orderId : orderFreeMap.keySet()) {
                                Integer freeMapAmoutn = Integer.valueOf(orderFreeMap.get(orderId));
                                Response<Order> orderObj = orderQueryService.findById(orderId);
                                if (orderObj.isSuccess()) {
                                    Order objOrder = orderObj.getResult();
                                    objOrder.setFee(objOrder.getFee()-freeMapAmoutn);
                                    orderWriteService.updateOrder(objOrder);
                                }
                            }
                        }
                        //修改已使用优惠券信息 couponUse
                        RrsCou rrscouObj = rrsCouResponse.getResult();
                        rrscouObj.setCouponUse(rrscouObj.getCouponUse()+1);
                        couponsRrsService.updateRrsCou(rrscouObj);

                        Response<RrsCouUser> resutUser = couponsRrsService.queryCouponsUserBy(userId,couponsId);
                        if(resutUser.isSuccess()){
                            RrsCouUser rrsCouUser = resutUser.getResult();
                            log.info("init rrsCouUser end ...coupuons"+rrsCouUser.getId());
                            couponsRrsService.updateCouponUser(rrsCouUser.getId());
                        }
                    }
                }
            }
        }
        return total - orderFreeAmount;
    }

}
