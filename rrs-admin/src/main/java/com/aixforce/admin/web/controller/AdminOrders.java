/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.rrs.code.service.CodeUsageService;
import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.trade.dto.RichOrder;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.trade.dto.RichOrderSellerView;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.UserExtra;
import com.aixforce.user.service.UserExtraService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.rrs.coupons.model.RrsCouOrderItem;
import com.rrs.coupons.service.CouponsRrsService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-11
 */
@Controller
@RequestMapping("/api")
public class AdminOrders {

    private final static Logger log = LoggerFactory.getLogger(AdminOrders.class);

    @Autowired
    private PreDepositService preDepositService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private CodeUsageService codeUsageService;

    @Autowired
    private MessageSources messageSources;


    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private UserExtraService userExtraService;

    @Autowired
    private CouponsRrsService couponsRrsService;

    /**
     * 运营分页查看所有卖家订单明细列表
     *
     * @param pageNo   页码
     * @param size     每页记录数
     * @param status   订单的交易状态
     * @param type     订单的类型
     * @param business 订单类目
     * @param name     卖家名字
     * @return 分页的订单明细列表
     */
    @RequestMapping(value = "/orders/adminView", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<RichOrderSellerView> viewOrders(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(value = "size", defaultValue = "20") Integer size,
                                                  @RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "type", required = false) Integer type,
                                                  @RequestParam(value = "business", required = false) Long business,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "orderId", required = false) Long orderId,
                                                  @RequestParam(value = "itemId", required = false) Long itemId) {

        BaseUser currentUser = UserUtil.getCurrentUser();
        if (Objects.equal(currentUser.getTypeEnum(), BaseUser.TYPE.SITE_OWNER)) {
            Response<UserExtra> userExtraR = userExtraService.findByUserId(currentUser.getId());
            if (!userExtraR.isSuccess()) {
                log.error("can't find user's extra record, user:{}", currentUser);
                throw new JsonResponseException(messageSources.get(userExtraR.getError()));
            }
            if (userExtraR.getResult().getBusinessId() != null) {
                business = (long) userExtraR.getResult().getBusinessId();
            } else {
                log.error("bad record, user extra's business id is null, user:{}", currentUser);
                throw new JsonResponseException(messageSources.get("user.null.business.id"));
            }
        }

        Response<Paging<RichOrderSellerView>> orderR = orderQueryService.adminFind(type, business, name, orderId, itemId, status, pageNo, size);
        if (orderR.isSuccess()) {
            Paging<RichOrderSellerView> richOrders = orderR.getResult();
            mixCouponcodeUsage(richOrders);
            addPreSaleOrderInfo(richOrders.getData());
            //add by zf 过滤试图 页面展示修改
            addCouponsSaleOrderinfo(richOrders.getData());
            // 增加预授权预置商品配置读取
            addPreDepositOrderInfo(richOrders.getData());
            return richOrders;
        } else {
            log.error("failed to find orders, error code:{}", orderR.getError());
            return new Paging<RichOrderSellerView>(0l, Collections.<RichOrderSellerView>emptyList());
        }
    }

    /** 将订单的优惠码使用信息放到卖家订单信息中  **/
    private void mixCouponcodeUsage(Paging<RichOrderSellerView> richOrders) {
        if(richOrders.getData()!=null){
            for(RichOrderSellerView richOrderSellerView: richOrders.getData()){
                Response<CodeUsage> codeUsage = codeUsageService.getCodeUsageByOrderId(richOrderSellerView.getOrderId());
                if(codeUsage!=null && codeUsage.getResult()!=null){
                    BeanMapper.copy(codeUsage.getResult(), richOrderSellerView);
                    //抢购活动优惠码使用
                    Boolean isBuying = richOrderSellerView.getIsBuying();
                    if (isBuying != null && isBuying.booleanValue()){
                        addOrderrSellerCodeUsageInfo(richOrderSellerView, codeUsage.getResult());
                    }
                }
            }
        }
    }

    /**
     * 查询抢购活动订单使用优惠码情况
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
                    if (!preSaleR.isSuccess() || preSaleR.getResult() == null) {
                        log.error("fail to find preSale by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
                        continue;
                    }
                    PreSale preSale = preSaleR.getResult();
                    richOrder.setRemainFinishAt(preSale.getRemainFinishAt());
                    Date date = new DateTime(richOrder.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).toDate();
                    richOrder.setEarnestPayTime(date);
                    richOrder.setPreSaleFinishAt(preSale.getPreSaleFinishAt());

                    List<RichOrderItem> richOrderItems = richOrder.getOrderItems();
                    richOrder.setCanPayEarnest(false);
                    if(DateTime.now().isBefore(new DateTime(richOrderItems.get(0).getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()))) {
                        richOrder.setCanPayEarnest(true);
                    }
                    richOrder.setCanPayRemain(false);
                    if(DateTime.now().isBefore(new DateTime(preSale.getRemainFinishAt())) &&
                            DateTime.now().isAfter(new DateTime(preSale.getPreSaleStartAt()))) {
                        richOrder.setCanPayRemain(true);
                    }
                    richOrder.setDepositType(0);
                } catch (Exception e) {
                    log.error("failed to handle order(id={}), cause:{}", richOrder.getOrderId(), Throwables.getStackTraceAsString(e));
                }
            }
        }
    }

    private void addCouponsSaleOrderinfo(List<RichOrderSellerView> data) {
        log.debug("init order coupon info begin ...");
        // 根据订单编号查询优惠券使用情况
        List<Long> ids = new ArrayList<Long>();
        for (RichOrderSellerView order : data) {
            ids.add(order.getOrderId());
        }

        List<RrsCouOrderItem> orderCouponList = couponsRrsService.findOrderItemsByOrderIds(ids);

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

            //System.err.println("=======RichOrderSellerView========:"+JSON_MAPPER.toJson(order).toString());
            order.setCouOrderList(couponList);
        }
        log.debug("init order coupon info end ...");
    }

    private void getOrderCouponKey(StringBuffer buff, Long orderId,
                                   Long itemId, Long skuId) {
        buff.setLength(0);
        buff.append("" + orderId).append("-").append("" + itemId).append("-")
                .append("" + skuId);
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

                    //增加预授权预置商品配置读取
                    Response<PreDeposit> preDepositR = preDepositService.findPreDepositByItemId(richOrder.getOrderItems().get(0).getSku().getItemId());

                    if (!preDepositR.isSuccess() || preDepositR.getResult() == null) {
                        log.error("fail to find preDepositR by itemId={}", richOrder.getOrderItems().get(0).getSku().getItemId());
                        continue;
                    }
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
}
