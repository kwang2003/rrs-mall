package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.*;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.model.*;
import com.aixforce.shop.model.Shop;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.rrs.settle.util.SettlementVerification.*;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 10:04 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class CashSummaryHandle extends JobHandle {


    private static final Long NO_FEE = 0L;

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private AlipayCashDao alipayCashDao;

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private AccountService<User> accountService;

    private CashFactory cashFactory = new CashFactory();


    /**
     * 统计(T-1日)的支付宝提现金额
     *
     * @param job   任务信息
     */
    public void summaryAlipayCashes(SettleJob job) {

        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;     // 完成的任务无需再次处理
        log.info("[SUMMARY-ALIPAY-CASHES] job begin at {}", DFT.print(DateTime.now()));

        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());
            Date tradedAt = job.getTradedAt();     // 支付时间

            generateOrderAlipayCashOfPaid(tradedAt);        // T日的提现明细 for 普通订单

            generateOrderAlipayCashOfPaidForPresale(tradedAt);// T日的提现明细 for 预售 定金

            generateOrderAlipayCashOfPaidForPresaleRest(tradedAt);// T日的提现明细 for 预售 尾款

            generateOrderAlipayCashOfRefund(tradedAt);      // T日的退款明细

            summaryCashesOfSeller(tradedAt);    // 汇总T日商户日提现
            summaryCashesDaily(tradedAt);       // 汇总T日总提现

            settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));

        } catch (IllegalStateException e) {
            log.error("[SUMMARY-ALIPAY-CASHES] failed with job:{}, error:{}", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[SUMMARY-ALIPAY-CASHES] failed with job:{}, cause:{} ", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        log.info("[SUMMARY-ALIPAY-CASHES] done at {} cost {}",DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    /**
     * 生成指定日期内的商户日汇总提现金额
     * @param tradedAt  交易日期
     */
    private void generateOrderAlipayCashOfPaid(Date tradedAt) {
        int pageNo = 1;
        boolean next = batchGenerateCashOfPaid(tradedAt, pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchGenerateCashOfPaid(tradedAt, pageNo, BATCH_SIZE);
        }
    }

    /**
     * 生成指定日期内的商户日汇总提现金额 for 预售 定金
     * @param tradedAt  交易日期
     */
    private void generateOrderAlipayCashOfPaidForPresale(Date tradedAt) {
        int pageNo = 1;
        boolean next = batchGenerateCashOfPaidForPresale(tradedAt, pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchGenerateCashOfPaidForPresale(tradedAt, pageNo, BATCH_SIZE);
        }
    }

    /**
     * 生成指定日期内的商户日汇总提现金额 for 预售 尾款
     * @param tradedAt  交易日期
     */
    private void generateOrderAlipayCashOfPaidForPresaleRest(Date tradedAt) {
        int pageNo = 1;
        boolean next = batchGenerateCashOfPaidForPresaleRest(tradedAt, pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchGenerateCashOfPaidForPresaleRest(tradedAt, pageNo, BATCH_SIZE);
        }
    }

    private void generateOrderAlipayCashOfRefund(Date tradedAt) {
        int pageNo = 1;
        boolean next = batchGenerateCashOfRefund(tradedAt, pageNo, BATCH_SIZE);
        while (next) {
            pageNo ++;
            next = batchGenerateCashOfRefund(tradedAt, pageNo, BATCH_SIZE);
        }
    }

    /**
     * 统计指定日内的商户日汇总金额
     * @param cashedAt  提现日期
     */
    private void summaryCashesOfSeller(Date cashedAt) {
        int pageNo = 1;
        boolean next = batchSummarySellerAlipayCashes(cashedAt, pageNo, BATCH_SIZE);   // 拉取JDE数据
        while (next) {
            pageNo ++;
            next = batchSummarySellerAlipayCashes(cashedAt, pageNo, BATCH_SIZE);
        }
    }

    /**
     * 统计指定日内的支付宝提现汇总金额
     * @param cashedAt 提现日期
     */
    private void summaryCashesDaily(Date cashedAt) {
        // 统计当天所有商户的支付宝提现
        AlipayCash alipayCash = orderAlipayCashDao.summaryCashesDaily(cashedAt);

        alipayCash.setSummedAt(cashedAt);
        if (alipayCash.getCashTotalCount() == 0) {
            alipayCash.setStatus(AlipayCash.Status.DONE.value());
        } else {
            alipayCash.setStatus(AlipayCash.Status.NOT.value());
        }

        alipayCash.setStatus(AlipayCash.Status.DONE.value());
        alipayCashDao.create(alipayCash);
    }

    /**
     * 批量生成普通订单提现明细
     *
     * @param paidAt    支付日期
     * @return  批量生成提现明细
     */
    private boolean batchGenerateCashOfPaid(Date paidAt, Integer pageNo, Integer size) {
        List<Settlement> settlements = getSettlements(Settlement.Type.PLAIN, paidAt, pageNo, size);

        for (Settlement s : settlements) {

            try {   // 某个订单明细出问题不应该影响整个订单明细

                if (isPlain(s)&& isCod(s)) {
                    // 普通订单货到付款不计入提现，直接默认已提现状态, 此状态订单会汇总到 T+1 的汇总
                    log.info("settlement(id:{}, oid:{}) is plain cod order, so mark it as cashed directly", s.getId(), s.getOrderId());
                    markSettlementAsCashed(s);
                    continue;
                }

                Long totalFee = s.getFee();


                persistPaidOrderCash(s, totalFee, OrderAlipayCash.CashType.PLAIN);

            } catch (IllegalStateException e) {
                log.error("fail to create cash of settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, error:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to create cash of settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, cause:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), Throwables.getStackTraceAsString(e));
            }
        }

        Integer current = settlements.size();
        return equalWith(current, BATCH_SIZE);
    }

    private List<Settlement> getSettlements(Settlement.Type type, Date paidAt, Integer pageNo, Integer size) {
        Settlement criteria = new Settlement();
        criteria.setPaidAt(paidAt);
        criteria.setType(type.value());//这里只处理普通订单 与预售订单分开

        PageInfo pageInfo = new PageInfo(pageNo, size);
        Paging<Settlement> paging = settlementDao.findBy(criteria, pageInfo.offset, pageInfo.limit);
        return paging.getData();
    }



    /**
     * 批量生成提现明细 for 预售定金
     *
     * @param paidAt    支付日期
     * @return  批量生成提现明细
     */
    private boolean batchGenerateCashOfPaidForPresale(Date paidAt, Integer pageNo, Integer size) {
        //这里是根据paidAt查询需要提现的子结算
        Paging<ItemSettlement> paging = itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_DEPOSIT, paidAt, pageNo, size);
        List<ItemSettlement> settlements = paging.getData();

        for (ItemSettlement s : settlements) {
            try {
                Long totalFee = s.getFee();

                persistPaidOrderCash(s, totalFee, OrderAlipayCash.CashType.PRESELL_DEPOSIT);

            } catch (IllegalStateException e) {
                log.error("fail to create cash of deposit item settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, error:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to create cash of deposit item settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, cause:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), Throwables.getStackTraceAsString(e));
            }
        }

        Integer current = settlements.size();
        return equalWith(current, BATCH_SIZE);
    }


    /**
     * 批量生成提现明细 for 预售尾款
     *
     * @param paidAt    支付日期
     * @return  批量生成提现明细
     */
    private boolean batchGenerateCashOfPaidForPresaleRest(Date paidAt, Integer pageNo, Integer size) {
        Paging<ItemSettlement> paging = itemSettlementDao.findBy(ItemSettlement.Type.PRESELL_REST, paidAt, pageNo, size);
        List<ItemSettlement> settlements = paging.getData();

        for (ItemSettlement s : settlements) {
            try {
                //尾款货到付款不产生提现
                if(Objects.equal(s.getPayType(),OrderItem.PayType.COD)){
                    continue;
                }
                Long totalFee = s.getFee();
                persistPaidOrderCash(s, totalFee,OrderAlipayCash.CashType.PRESELL_REST);

            }catch(IllegalArgumentException e){
                log.error("fail to create cash of rest item settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, error:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), e.getMessage());
            }catch (IllegalStateException e) {
                log.error("fail to create cash of rest item settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, error:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to create cash of rest item settlement(id:{}, oid:{}, sid:{}, seller:{}, fee:{}, 3rd:{}, cause:{}",
                        s.getId(), s.getOrderId(), s.getSellerId(), s.getSellerName(),
                        s.getFee(), s.getThirdPartyCommission(), Throwables.getStackTraceAsString(e));
            }
        }

        Integer current = settlements.size();
        return equalWith(current, BATCH_SIZE);
    }



    /**
     * 持久化订单提现明细到数据库
     *
     * @param settlement    订单结算记录
     * @param totalFee      提现明细总金额（根据业务场景，并非一定等于货款)
     */
    private void persistPaidOrderCash(Settlement settlement, Long totalFee, OrderAlipayCash.CashType cashType) {
        log.info("start create order cash cashType={} for plain",cashType.value());
        OrderAlipayCash creating = cashFactory.newOrderCash(cashType);
        log.info("order cash order id={},type={}.cashType={} for plain",settlement.getOrderId(),creating.getType(),creating.getCashType());
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
        creating.setRefundFee(NO_FEE);

        creating.setStatus(OrderAlipayCash.Status.NOT.value());
        creating.setTradedAt(settlement.getPaidAt());

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

        log.info("start create order cash cashType={} for presale",cashType.value());
        OrderAlipayCash creating = cashFactory.newOrderCash(cashType);
        log.info("order cash order id={},type={}.cashType={} for presale",itemSettlement.getOrderId(),creating.getType(),creating.getCashType());

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
        creating.setRefundFee(NO_FEE);

        creating.setStatus(OrderAlipayCash.Status.NOT.value());
        creating.setTradedAt(itemSettlement.getPaidAt());

        log.info("persist orderAlipayCash(oid:{}, total:{}, refund:{}, 3rd:{}, cash:{}",
                creating.getOrderId(), creating.getTotalFee(), creating.getRefundFee(), creating.getAlipayFee(), creating.getCashFee());
        orderAlipayCashDao.create(creating);

    }

    /**
     * 获取预售订单定金子订单
     *
     * @param presaleSettlement    预售订单计算记录
     */
    private ItemSettlement getDepositOrderItem(Settlement presaleSettlement) {
        ItemSettlement itemCriteria = new ItemSettlement();
        itemCriteria.setType(ItemSettlement.Type.PRESELL_DEPOSIT.value());
        itemCriteria.setOrderId(presaleSettlement.getOrderId());

        // 定金单
        ItemSettlement orderOfDeposit = itemSettlementDao.getBy(itemCriteria);
        // 若这里的金额不对那么汇总就会有问题，直接返回错误
        checkState(notNull(orderOfDeposit.getFee()), "deposit.fee.is.empty");
        return orderOfDeposit;
    }

    /**
     * 标记指定的结算记录为已提现状态
     *
     * @param settlement    订单结算记录
     */
    private void markSettlementAsCashed(Settlement settlement) {
        Settlement updating = newSettlement();
        updating.setId(settlement.getId());
        updating.setCashed(Settlement.Cashed.DONE.value());
        settlementDao.update(updating);
    }

    protected Settlement newSettlement() {
        return new Settlement();
    }

    /**
     * 批量生成退款明细
     * @param refundAt  退款时间
     */
    private boolean batchGenerateCashOfRefund(Date refundAt, Integer pageNo, Integer size) {

        Response<Paging<OrderItem>> result = orderQueryService.findRefundedOrderItemInDate(refundAt, pageNo, size);
        checkState(result.isSuccess(), result.getError());

        Paging<OrderItem> paging = result.getResult();
        List<OrderItem> orderItems = paging.getData();

        for (OrderItem o : orderItems) {
            try {

                persistRefundOrderCash(o);

            } catch (IllegalStateException e) {
                log.error("fail to create cash of orderItem(id:{}, oid:{}, sid:{}, fee:{} error:{}",
                        o.getId(), o.getOrderId(), o.getSellerId(),
                        o.getFee(), e.getMessage());
            } catch (Exception e) {
                log.error("fail to create cash of orderItem(id:{}, oid:{}, sid:{}, fee:{} cause:{}",
                        o.getId(), o.getOrderId(), o.getSellerId(),
                        o.getFee(), Throwables.getStackTraceAsString(e));
            }
        }

        Integer current = orderItems.size();
        return equalWith(current, BATCH_SIZE);
    }

    private void persistRefundOrderCash(OrderItem orderItem) {

        OrderAlipayCash creating;

        if (equalWith(orderItem.getType(), OrderItem.Type.PLAIN.value())) {
            creating = cashFactory.newOrderCash(OrderAlipayCash.CashType.PLAIN_REFUND);
        } else {
            if(equalWith(orderItem.getType(),OrderItem.Type.PRESELL_DEPOSIT.value())){
                creating = cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_DEPOSIT_REFUND);
            }else {
                creating = cashFactory.newOrderCash(OrderAlipayCash.CashType.PRESELL_REST_REFUND);
            }
        }

        creating.setOrderId(orderItem.getOrderId());

        creating.setBuyerId(orderItem.getBuyerId());
        User buyer = getUser(orderItem.getBuyerId());
        creating.setBuyerName(buyer.getName());

        creating.setSellerId(orderItem.getSellerId());
        User seller = getUser(orderItem.getSellerId());
        creating.setSellerName(seller.getName());

        Shop shop  = getShopOf(orderItem.getSellerId());
        creating.setShopId(shop.getId());
        creating.setShopName(shop.getName());

        creating.setTotalFee(0L);
        creating.setAlipayFee(0L);

        checkState(notNull(orderItem.getRefundAmount()), "refund.amount.empty");
        creating.setRefundFee((long)orderItem.getRefundAmount());
        creating.setCashFee(0L - orderItem.getRefundAmount());

        creating.setOrderItemId(orderItem.getId());
        creating.setStatus(OrderAlipayCash.Status.NOT.value());
        creating.setTradedAt(orderItem.getRefundAt());


        orderAlipayCashDao.create(creating);
    }

    private User getUser(Long userId) {
        Response<User> userResult = accountService.findUserById(userId);
        checkState(userResult.isSuccess(), userResult.getError());
        return userResult.getResult();
    }

    private Shop getShopOf(Long userId) {
        Response<Shop> shopResult = shopService.findByUserId(userId);
        checkState(shopResult.isSuccess(), shopResult.getError());
        return shopResult.getResult();
    }


    /**
     *
     * 批量统计每个商户的日支付宝提现汇总
     *
     * @param cashedAt    支付时间（T-1日）
     * @param pageNo    批次号
     * @param size      批次数量
     * @return  是否存在下一批待处理数据
     */
    private boolean batchSummarySellerAlipayCashes(Date cashedAt, int pageNo, Integer size) {

        List<Integer> statuses = Lists.newArrayList(User.STATUS.FROZEN.toNumber(),
                User.STATUS.LOCKED.toNumber(), User.STATUS.NORMAL.toNumber());
        Response<Paging<Shop>> shopQueryResult = shopService.findBy(statuses, pageNo, size);
        checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());

        Paging<Shop> paging = shopQueryResult.getResult();
        List<Shop> shops = paging.getData();
        if (shops!=null && !shops.isEmpty()) {
            log.warn("not found processing shops");
            return Boolean.FALSE;
        }


        for (Shop shop : shops) {   // 汇总每日商户确认的数据
            try {
                // 汇总计算方式修改，逐条累加
                SellerAlipayCash sellerAlipayCash = summaryCashOfShop(shop, cashedAt);
                fillSellerAlipayCashWithZero(sellerAlipayCash);

                if (sellerAlipayCash.getCashTotalCount() == 0) {
                    log.info("shop(id:{}) cash is empty at {}, skipped", shop.getId(), DFT.print(new DateTime(cashedAt)));
                    continue;
                }

                // 提现金额 = 收入金额 - 支付宝手续费 - 退款金额
                sellerAlipayCash.setCashFee(sellerAlipayCash.getTotalFee()
                        - sellerAlipayCash.getAlipayFee() - sellerAlipayCash.getRefundFee());
                sellerAlipayCash.setSellerId(shop.getUserId());
                sellerAlipayCash.setSummedAt(cashedAt);

                String outerCode = getOuterCodeOfShop(shop);
                sellerAlipayCash.setOuterCode(outerCode);

                Long business = getBusinessOfShop(shop);
                sellerAlipayCash.setBusiness(business);

                // 创建的时候需要判断该商户ID有没有已经汇总提现，防止重复汇总
                sellerAlipayCash.setSellerName(shop.getUserName());


                sellerAlipayCash.setStatus(SellerAlipayCash.Status.DONE.value());
                sellerAlipayCash.setSynced(SellerAlipayCash.Synced.NOT.value());
                sellerAlipayCash.setVouched(SellerAlipayCash.Vouched.NOT.value());

                log.info("shop(id:{}) total:{}, 3rd:{}, refund:{}, cash:{} persisted",
                        shop.getId(),
                        sellerAlipayCash.getTotalFee(), sellerAlipayCash.getAlipayFee(),
                        sellerAlipayCash.getRefundFee(), sellerAlipayCash.getCashFee());

                sellerAlipayCashDao.create(sellerAlipayCash);
            } catch (IllegalStateException e) {
                log.error("fail to summary seller alipay cash with seller(id:{}, name:{}) paidAt:{}, error:{}",
                        shop.getUserId(), shop.getUserName(), cashedAt, e.getMessage());
            } catch (Exception e) {
                log.error("fail to summary seller alipay cash with seller(id:{}, name:{}) paidAt:{}, cause:{}",
                        shop.getUserId(), shop.getUserName(), cashedAt, Throwables.getStackTraceAsString(e));
            }
        }

        int total = paging.getTotal().intValue();
        int current = shops.size();
        return ((pageNo - 1) * size + current) < total;  // 判断是否存在下一个要处理的批次
    }


    private SellerAlipayCash summaryCashOfShop(Shop shop, Date cashedAt) {
        return orderAlipayCashDao.sumSellerAlipayCash(shop.getUserId(), cashedAt);
    }

    private boolean isPresaleAndCod(Settlement settlement) {
        return isPreSale(settlement) && isCod(settlement);
    }

    private boolean isPresaleAndOnlineAndNotPayRest(Settlement settlement) {
        return isPreSale(settlement) && isOnline(settlement) && buyerNotPayRest(settlement);
    }


    private boolean buyerNotPayRest(Settlement settlement) {
        return  equalWith(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value())
                || equalWith(settlement.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }

    /**
     * 将值为null的金额填充0
     *
     * @param sellerAlipayCash  商家每日支付宝提现记录
     */
    private void fillSellerAlipayCashWithZero(SellerAlipayCash sellerAlipayCash) {
        sellerAlipayCash.setTotalFee(sellerAlipayCash.getTotalFee() == null ? 0L : sellerAlipayCash.getTotalFee());
        sellerAlipayCash.setAlipayFee(sellerAlipayCash.getAlipayFee() == null ? 0L : sellerAlipayCash.getAlipayFee());
        sellerAlipayCash.setRefundFee(sellerAlipayCash.getRefundFee() == null ? 0L : sellerAlipayCash.getRefundFee());
    }

    static class CashFactory{
        public OrderAlipayCash newOrderCash(OrderAlipayCash.CashType cashType) {
            OrderAlipayCash orderAlipayCash = new OrderAlipayCash();
            if (isPlain(cashType)) {
                orderAlipayCash.setType(Order.Type.PLAIN.value());
            } else if (isPreSale(cashType)) {
                orderAlipayCash.setType(Order.Type.PRE_SELL.value());
            } else {
                throw new IllegalArgumentException("cash.type.not.valid");
            }

            orderAlipayCash.setCashType(cashType.value());
            return orderAlipayCash;
        }



        private boolean isPlain(OrderAlipayCash.CashType cashType) {
            return equalWith(OrderAlipayCash.CashType.PLAIN, cashType)
                    || equalWith(OrderAlipayCash.CashType.PLAIN_REFUND, cashType);
        }

        private boolean isPreSale(OrderAlipayCash.CashType cashType) {
            return equalWith(OrderAlipayCash.CashType.PRESELL_DEPOSIT, cashType)
                    || equalWith(OrderAlipayCash.CashType.PRESELL_DEPOSIT_REFUND, cashType)
                    || equalWith(OrderAlipayCash.CashType.PRESELL_REST, cashType)
                    || equalWith(OrderAlipayCash.CashType.PRESELL_REST_REFUND, cashType);
        }

    }
}
