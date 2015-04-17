package com.aixforce.rrs.settle.handle;

import com.aixforce.alipay.dto.AlipaySettlementResponse;
import com.aixforce.alipay.dto.settlement.AlipaySettlementDto;
import com.aixforce.alipay.request.PageQueryRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.rrs.settle.dao.AlipayTransDao;
import com.aixforce.rrs.settle.dao.ItemSettlementDao;
import com.aixforce.rrs.settle.dao.OrderAlipayCashDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.model.*;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.base.Preconditions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.rrs.settle.util.SettlementVerification.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-09-18 10:34 AM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class FixSettlementHandle extends JobHandle {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private AlipayTransDao alipayTransDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private AccountService<User> accountService;

    private int count = 0;

    @Autowired
    private Token token;


    @Autowired
    private OrderQueryService orderQueryService;

    public void fix(SettleJob job) {

        log.info("[ORDER-FIX] begin at {}", DFT.print(DateTime.now()));
        count = 0;

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {

            settleJobDao.ing(job.getId());
            Integer pageNo = 1;
            boolean next = batchProcessOrders(job.getTradedAt(), pageNo, BATCH_SIZE);
            while (next) {
                pageNo ++;
                next = batchProcessOrders(job.getTradedAt(), pageNo, BATCH_SIZE);
            }

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));

        } catch (IllegalStateException e) {
            log.error("[ORDER-FIX] failed with job:{}, error:{}", job, e.getMessage());
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[ORDER-FIX] failed with job:{}, cause:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[ORDER-FIX] successfully done, cost:{} min", stopwatch.elapsed(TimeUnit.MINUTES));
    }

    private boolean batchProcessOrders(Date tradedAt, Integer pageNo, Integer size) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Order> orders = getOrders(tradedAt, pageNo, size);
        for (Order order : orders) {
            try {
                doSettlement(order);
            } catch (IllegalStateException e) {
                log.error("fail to settle with order(id:{}, type:{}, payType:{}, status:{}, paymentCode:{}, fee:{}), error:{}",
                        order.getId(), order.getType(), order.getPaymentType(), order.getStatus(),
                        order.getPaymentCode(), order.getFee(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to settle with order(id:{}, type:{}, payType:{}, status:{}, paymentCode:{}, fee:{}), cause:{}",
                        order.getId(), order.getType(), order.getPaymentType(), order.getStatus(),
                        order.getPaymentCode(), order.getFee(), Throwables.getStackTraceAsString(e));
            }
        }

        stopwatch.stop();
        log.info("batch done cast:{}", stopwatch.elapsed(TimeUnit.SECONDS));

        int current = orders.size();
        return current != 0;
    }

    /**
     * 获取一个月之前至交易日期的订单列表
     */
    private List<Order> getOrders(Date tradedAt, Integer pageNo, Integer size) {
        Order criteria = new Order();
        Date createdStartAt = new DateTime(tradedAt).minusMonths(1).withTimeAtStartOfDay().toDate();
        Date createdEndAt = new DateTime(tradedAt).withTimeAtStartOfDay().plusDays(1).toDate();

        //todo
//        criteria.setPaymentType(1);

        Response<Paging<Order>> orderResult = orderQueryService.findBy(criteria, pageNo, size,createdStartAt, createdEndAt);
        Paging<Order> paging = orderResult.getResult();
        log.info("load {} of {}", pageNo * size, paging.getTotal());
        return paging.getData();
    }

    private void doSettlement(Order order) {
        if (needSettlement(order)) {
            Settlement exists = settlementDao.getByOrderId(order.getId());
            if (isNull(exists)) {
                count ++;
                log.info("{} order(id:{}, paymentCode:{}, fee:{}, type:{}, payType:{}, status:{} should create settlement)",  count,
                        order.getId(), order.getPaymentCode(), order.getFee(), order.getType(), order.getPaymentType(), order.getStatus());
                Long id = generate(order.getId(), Boolean.FALSE);
                checkState(notNull(id), "settlement.persist.fail");
                Settlement settlement = settlementDao.get(id);

                if (notNull(order.getFinishedAt())) {
                    log.info("order(id:{}) has been finished", order.getId());
                    settlement.setFinished(Settlement.Finished.DONE.value());
                    settlement.setFinishedAt(order.getFinishedAt());

                    if (isPlain(order) && isCod(order)) {  // 普通订单货到付款默认结算中
                        settlement.setCashed(Settlement.Cashed.DONE.value());
                        settlement.setSettleStatus(Settlement.SettleStatus.ING.value());
                    }

                }
                settlementManager.fixed(settlement);


                if (isPlain(settlement)&& isCod(settlement)) {
                    return;
                }

                //预售
                if (isPreSale(settlement)) {
                    updateThirdPartyCommissionForPresale(settlement);
                    //只产生定金提现
                    List<ItemSettlement> itemSettlementList = itemSettlementDao.findByOrderId(order.getId());
                    if(notNull(itemSettlementList)){
                        for (ItemSettlement itemSettlement : itemSettlementList){
                            //如果预售货到付款 尾款 不产生提现
                            if(Objects.equal(itemSettlement.getPayType(), OrderItem.PayType.COD.value())&&Objects.equal(itemSettlement.getType(), ItemSettlement.Type.PRESELL_REST.value())) continue;

                            if(Objects.equal(itemSettlement.getType(),ItemSettlement.Type.PRESELL_REST.value())){

                                persistPaidOrderCash(itemSettlement,itemSettlement.getFee(), OrderAlipayCash.CashType.PRESELL_REST);
                            }

                            if(Objects.equal(itemSettlement.getType(),ItemSettlement.Type.PRESELL_DEPOSIT.value())){
                                persistPaidOrderCash(itemSettlement,itemSettlement.getFee(), OrderAlipayCash.CashType.PRESELL_DEPOSIT);
                            }
                        }
                    }
                    return;
                }

                //普通
                createOrderCash(settlement);
            }
        }
    }

    private void createOrderCash(Settlement settlement) {
        updateThirdPartyCommission(settlement);

        Long totalFee = settlement.getFee();

        OrderAlipayCash creating = getOrderAlipayCash(settlement, totalFee);

        log.info("persist orderAlipayCash(oid:{}, total:{}, refund:{}, 3rd:{}, cash:{}",
                creating.getOrderId(), creating.getTotalFee(), creating.getRefundFee(), creating.getAlipayFee(), creating.getCashFee());
        orderAlipayCashDao.create(creating);
    }

    /**
     * 持久化订单提现明细到数据库 for 预售
     *
     * @param itemSettlement    子订单结算记录
     * @param totalFee      提现明细总金额（根据业务场景，并非一定等于货款)
     */
    private void persistPaidOrderCash(ItemSettlement itemSettlement, Long totalFee, OrderAlipayCash.CashType cashType) {

        OrderAlipayCash creating = new OrderAlipayCash();

        creating.setType(itemSettlement.getType());
        creating.setCashType(cashType.value());
        creating.setOrderId(itemSettlement.getOrderId());
        creating.setOrderItemId(itemSettlement.getOrderItemId());
        creating.setBuyerId(itemSettlement.getBuyerId());
        creating.setBuyerName(itemSettlement.getBuyerName());
        creating.setSellerId(itemSettlement.getSellerId());
        creating.setSellerName(itemSettlement.getSellerName());

        Shop shop  = getShopOf(itemSettlement.getSellerId());
        creating.setShopId(shop.getId());
        creating.setShopName(shop.getName());


        creating.setTotalFee(totalFee);
        creating.setAlipayFee(itemSettlement.getThirdPartyCommission());

        // 提现单没有退款金额
        Long cashFee = totalFee - itemSettlement.getThirdPartyCommission();
        creating.setCashFee(cashFee);
        creating.setRefundFee(0L);

        creating.setStatus(OrderAlipayCash.Status.NOT.value());
        creating.setTradedAt(itemSettlement.getPaidAt());
        creating.setFixed(Boolean.TRUE);

        log.info("persist orderAlipayCash(oid:{}, total:{}, refund:{}, 3rd:{}, cash:{}",
                creating.getOrderId(), creating.getTotalFee(), creating.getRefundFee(), creating.getAlipayFee(), creating.getCashFee());
        orderAlipayCashDao.create(creating);

    }


    private AlipayTrans getAlipayTrans(String paymentCode) {
        List<AlipayTrans> transes = alipayTransDao.findByTradeNo(paymentCode);
        if (isNullOrEmpty(transes)) {
            return null;
        }


        AlipayTrans payTrans = null;

        for (AlipayTrans trans : transes) {
            String subTransCodeMsg = trans.getSubTransCodeMsg();
            if (notEmpty(subTransCodeMsg) && equalWith(subTransCodeMsg, "收费")) {
                return trans;
            }

            String transCodeMsg = trans.getTransCodeMsg();
            if (notEmpty(transCodeMsg) && equalWith(transCodeMsg, "在线支付")) {
                payTrans = trans;
            }
        }

        if (payTrans != null) {
            return payTrans;
        }

        return null;
    }

    private void updateThirdPartyCommission(Settlement settlement) {
        if (isPlain(settlement) && isCod(settlement)) {
            log.info("update 3rd commission order (id:{}, type:{}, paymentType:{}) skipped",
                    settlement.getOrderId(), settlement.getType(), settlement.getPayType());
            return;
        }

        if (equalWith(settlement.getMultiPaid(), Settlement.MultiPaid.YES.value())) {
            log.info("update 3rd commission order (id:{}, type:{}, paymentType:{}, multi:{}) skipped",
                    settlement.getOrderId(), settlement.getType(), settlement.getPayType(), settlement.getMultiPaid());
            return;
        }

            Long commission = getCommission(settlement.getPaymentCode());
            if (isNull(commission)) {
                log.error("get trans from alipay failed, skipped order(id:{}) ", settlement.getOrderId());
                return;
            }

            updatingCommission(settlement, commission);
            settlement.setThirdPartyCommission(commission);
            log.info("update order (id:{}, type:{}, paymentType:{}) commission to {} ",
                    settlement.getOrderId(), settlement.getType(), settlement.getPayType(), commission);

    }


    private void updateThirdPartyCommissionForPresale(Settlement settlement) {

        if (equalWith(settlement.getMultiPaid(), Settlement.MultiPaid.YES.value())) {
            log.info("update 3rd commission order (id:{}, type:{}, paymentType:{}, multi:{}) skipped",
                    settlement.getOrderId(), settlement.getType(), settlement.getPayType(), settlement.getMultiPaid());
            return;
        }
        long total = 0L;

            List<ItemSettlement> itemSettlements = itemSettlementDao.list(settlement.getOrderId());
            for (ItemSettlement itemSettlement : itemSettlements) {
                if (isDeposit(itemSettlement) || ((isRest(itemSettlement) && isOnline(settlement)))) {
                    Long commission = getCommission(itemSettlement.getPaymentCode());
                    if (isNull(commission)) {
                        log.error("get trans from alipay failed, skipped order(id:{}, type:{}, payType:{}) orderItem(id:{}) ",
                                settlement.getOrderId(), settlement.getType(), settlement.getPayType(), itemSettlement.getOrderItemId());
                        return;
                    }

                    total += commission;
                    updatingItemCommission(itemSettlement, commission);
                    log.info("update orderItem (oid:{}, id:{}, type:{}, paymentType:{}) commission to {}",
                            itemSettlement.getOrderId(), itemSettlement.getId(), itemSettlement.getType(), itemSettlement.getPayType(), commission);
                } else {
                    total += 0L;
                    updatingItemCommission(itemSettlement, 0L);
                    log.info("update orderItem (oid:{}, id:{}, type:{}, paymentType:{}) commission to {}",
                            itemSettlement.getOrderId(), itemSettlement.getId(), itemSettlement.getType(), itemSettlement.getPayType(), 0);
                }

            }

            updatingCommission(settlement, total);
    }


    private Long getCommission(String paymentCode) {
        AlipayTrans trans = getAlipayTrans(paymentCode);

        if (notNull(trans)) {
            return trans.getOutcomeOfFen();

        } else {
            // 获取失败，这时候直接去支付宝那边查一把
            AlipaySettlementResponse response = PageQueryRequest.build(token)
                    .start(DateTime.now().toDate()).end(DateTime.now().toDate())
                    .tradeNo(paymentCode)
                    .pageNo(1).pageSize(200).query();

            if (response.isSuccess()) {
                return getServiceFee(response);
            }

        }

        return null;

    }

    private void updatingCommission(Settlement settlement, Long commission) {
        Settlement updating = new Settlement();
        updating.setId(settlement.getId());
        updating.setThirdPartyCommission(commission);
        settlementDao.update(updating);
    }

    private void updatingItemCommission(ItemSettlement itemSettlement, Long commission) {
        ItemSettlement updating = new ItemSettlement();
        updating.setId(itemSettlement.getId());
        updating.setThirdPartyCommission(commission);
        itemSettlementDao.update(updating);
    }



    private Long getServiceFee(AlipaySettlementResponse response) {

        List<AlipaySettlementDto> dtos = response.getResult().getPaging().getAccountLogList();

        for (AlipaySettlementDto dto : dtos) {
            if (notEmpty(dto.getSubTransCodeMsg()) && equalWith(dto.getSubTransCodeMsg(), "收费")) {
                return new BigDecimal(dto.getOutcome()).multiply(new BigDecimal(100)).longValue();
            }
        }

        return null;
    }



    private OrderAlipayCash getOrderAlipayCash(Settlement settlement, Long totalFee) {
        OrderAlipayCash creating = new OrderAlipayCash();
        creating.setOrderId(settlement.getOrderId());
        creating.setBuyerId(settlement.getBuyerId());
        creating.setBuyerName(settlement.getBuyerName());

        creating.setSellerId(settlement.getSellerId());
        creating.setSellerName(settlement.getSellerName());

        Shop shop  = getShopOf(settlement.getSellerId());
        creating.setShopId(shop.getId());
        creating.setShopName(shop.getName());


        creating.setTotalFee(totalFee);
        creating.setAlipayFee(settlement.getThirdPartyCommission());

        // 提现单没有退款金额

        Long cashFee = totalFee - settlement.getThirdPartyCommission();
        creating.setCashFee(cashFee);
        creating.setRefundFee(0L);
        creating.setCashType(OrderAlipayCash.CashType.PLAIN.value());//提现类型

        creating.setStatus(OrderAlipayCash.Status.NOT.value());
        creating.setTradedAt(settlement.getPaidAt());
        creating.setType(settlement.getType());

        creating.setFixed(Boolean.TRUE);
        return creating;
    }

    private Shop getShopOf(Long userId) {
        Response<Shop> shopResult = shopService.findByUserId(userId);
        checkState(shopResult.isSuccess(), shopResult.getError());
        return shopResult.getResult();
    }


    private boolean buyerNotPayRest(Settlement settlement) {
        return  equalWith(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value())
                || equalWith(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }


    /**
     * 以下订单跳过
     *
     * 1、普通货到付款订单-非成功状态
     * 2、预售订单定金支付超时
     * 3、支付时间为空
     * 4、尚未结束的预售订单
     *
     */
    public static boolean needSettlement(Order order) {
        if (isPlain(order) && isCod(order) && !done(order)) {
            return Boolean.FALSE;
        }

        if (isPreSale(order) && depositExpired(order)) {
            return Boolean.FALSE;
        }

        if (isNull(order.getPaidAt())) {
            return Boolean.FALSE;
        }

        if (isPreSale(order) && isNull(order.getFinishedAt()))  {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


    private Long generate(Long orderId, Boolean multi) {
        Order order = getOrder(orderId);

        // 填用户名
        Settlement settlement = new Settlement();
        User seller = getUser(order.getSellerId());
        settlement.setSellerId(seller.getId());
        settlement.setSellerName(seller.getName());

        User buyer = getUser(order.getBuyerId());
        settlement.setBuyerId(buyer.getId());
        settlement.setBuyerName(buyer.getName());
        settlement.setPaymentCode(order.getPaymentCode());

        // 订单相关
        settlement.setOrderId(orderId);
        settlement.setPaidAt(order.getPaidAt());
        settlement.setOrderedAt(order.getCreatedAt());

        settlement.setTradeStatus(order.getStatus());
        settlement.setPayType(order.getPaymentType());
        settlement.setType(order.getType());
        settlement.setFee(order.getFee().longValue());
        settlement.setBusiness(order.getBusiness());

        // 设定当前费率
        Response<ShopExtra> extraGetResult = shopService.getExtra(order.getSellerId());
        checkState(extraGetResult.isSuccess(), extraGetResult.getError());

        ShopExtra extra = extraGetResult.getResult();
        Double rate = extra.getRate() == null ? 0.0000 : extra.getRate();
        settlement.setCommissionRate(rate);

        // 是否联合支付
        settlement.setMultiPaid(multi ?  Settlement.MultiPaid.YES.value() : Settlement.MultiPaid.NOT.value());


        // 默认未结算状态
        settlement.setSettleStatus(Settlement.SettleStatus.NOT.value());
        return settlementManager.create(settlement, rate);
    }

    private Order getOrder(Long orderId) {
        Response<Order> orderQueryResult = orderQueryService.findById(orderId);
        checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());
        return orderQueryResult.getResult();
    }

    private User getUser(Long userId) {
        Response<User> userQueryResult = accountService.findUserById(userId);
        checkState(userQueryResult.isSuccess(), userQueryResult.getError());
        return userQueryResult.getResult();
    }

}
