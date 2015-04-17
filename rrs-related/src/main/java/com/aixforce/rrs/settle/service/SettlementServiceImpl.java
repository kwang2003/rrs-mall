package com.aixforce.rrs.settle.service;

import com.aixforce.alipay.dto.AlipaySettlementResponse;
import com.aixforce.alipay.dto.settlement.AlipaySettlementDto;
import com.aixforce.alipay.request.PageQueryRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.dao.AlipayTransDao;
import com.aixforce.rrs.settle.dao.ItemSettlementDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.dto.FatSettlement;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.manager.SettlementManager;
import com.aixforce.rrs.settle.model.AlipayTrans;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.aixforce.rrs.settle.util.SettlementVerification.checkOrderStatus;
import static com.aixforce.user.util.UserVerification.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@Slf4j
@Service
public class SettlementServiceImpl implements SettlementService {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private SettlementManager settlementManager;

    @Autowired
    private DepositManager depositManager;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private AlipayTransDao alipayTransDao;

    @Autowired
    private Token token;


    @Value("#{app.permitDay}")
    private Integer permitDay;

    @Value("#{app.threshold}")
    private Integer threshold;

    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * 商户标记订单确认状态为“已确认”
     *
     * @param id        订单结算记录id
     * @param userId    用户id
     * @return 是否更新成功
     */
    @Override
    public Response<Boolean> confirmed(Long id, Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            checkArgument(notNull(id), "settlement.confirmed.id.null");
            checkArgument(notNull(userId), "settlement.confirmed.user.id.null");
            Settlement settlement = settlementDao.get(id);
            checkState(notNull(settlement), "settlement.not.exist");
            checkState(equalWith(settlement.getSellerId(), userId), "settlement.user.has.no.permission");

            Boolean locked = depositManager.isAccountLocked(settlement.getSellerId(), threshold);
            // 若当前商户押金账户余额不足，则禁止商户确认
            checkState(not(locked), "deposit.account.balance.not.enough");
            boolean success = settlementDao.confirmed(id);
            checkState(success, "settle.confirmed.persist.fail");

            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalArgumentException e) {
            log.warn("fail to confirming with id:{}, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (IllegalStateException e) {
            log.warn("fail to confirming with id:{}, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to confirming with id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("settle.confirmed.fail");
            return result;
        }
    }

    /**
     * 根据订单号生成该订单下的所有结算记录 <br/>
     * 以下为产生订单结算记录的时机: <br/>
     *
     *
     * @param orderId   订单号订单号
     * @return 操作是否成功
     * @see  com.aixforce.rrs.settle.util.SettlementVerification#checkOrderStatus
     */
    public Response<Long> generateForPresale(Long orderId,Date paidAt) {
        Response<Long> result = new Response<Long>();
        try {

            checkArgument(notNull(orderId), "settlement.generate.order.id.null");
            Long id = newSettlementForPresale(orderId, Boolean.FALSE, paidAt);
            checkState(notNull(id));
            result.setResult(id);

        } catch (IllegalArgumentException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to generate with orderId:{}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("settlement.generate.fail");
        }

        return result;
    }



    /**
     * 根据订单号生成该订单下的所有结算记录 <br/>
     * 以下为产生订单结算记录的时机: <br/>
     *
     *
     * @param orderId   订单号订单号
     * @return 操作是否成功
     * @see  com.aixforce.rrs.settle.util.SettlementVerification#checkOrderStatus
     */
    public Response<Long> generate(Long orderId) {
        Response<Long> result = new Response<Long>();
        try {

            checkArgument(notNull(orderId), "settlement.generate.order.id.null");
            Long id = newSettlement(orderId, Boolean.FALSE);
            checkState(notNull(id));
            result.setResult(id);

        } catch (IllegalArgumentException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to generate with orderId:{}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("settlement.generate.fail");
        }

        return result;
    }


    public Response<Long> generateMulti(Long orderId) {
        Response<Long> result = new Response<Long>();
        try {

            checkArgument(notNull(orderId), "settlement.generate.order.id.null");
            Long id = newSettlement(orderId, Boolean.TRUE);
            checkState(notNull(id));
            result.setResult(id);

        } catch (IllegalArgumentException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to generate with orderId:{}, error:{}", orderId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to generate with orderId:{}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("settlement.generate.fail");
        }

        return result;
    }



    private Long newSettlement(Long orderId, Boolean multi) {
        Order order = getOrder(orderId);
        boolean valid = checkOrderStatus(order);   // 判断当前订单有无必要创建结算信息
        checkState(valid, "settlement.generate.status.incorrect");

        Settlement settlement = settlementDao.getByOrderId(orderId);
        if (notNull(settlement)) {      // 如果已经创建了settlement则直接返回结果
            return settlement.getId();
        }


        //创建订单结算信息

        // 填用户名
        settlement = new Settlement();
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

    private Long newSettlementForPresale(Long orderId, Boolean multi,Date paidAt) {
        Order order = getOrder(orderId);
        Settlement settlement = settlementDao.getByOrderId(orderId);
        if (notNull(settlement)) {      // 如果已经创建了settlement则直接返回结果
            return settlement.getId();
        }


        //创建订单结算信息

        // 填用户名
        settlement = new Settlement();
        User seller = getUser(order.getSellerId());
        settlement.setSellerId(seller.getId());
        settlement.setSellerName(seller.getName());

        User buyer = getUser(order.getBuyerId());
        settlement.setBuyerId(buyer.getId());
        settlement.setBuyerName(buyer.getName());
        settlement.setPaymentCode(order.getPaymentCode());

        // 订单相关
        settlement.setOrderId(orderId);
        settlement.setPaidAt(paidAt); //预售支付时 用预售的支付时间当尾款支付后再更新为尾款支付时间
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
        return settlementManager.createForPresale(settlement, rate);
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

    /**
     * 分页查找指定日期范围内的子订单结算记录
     *
     * @param orderId   订单号
     * @param pageNo    起始偏移, 可以为空
     * @param size      返回条数, 可以为空
     * @return 查询结果
     */
    @Override
    public Response<Paging<ItemSettlement>> findSubsBy(@ParamInfo("orderId") Long orderId,
                                                        @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                        @ParamInfo("size") @Nullable Integer size,
                                                        @ParamInfo("baseUser") BaseUser user) {
        Response<Paging<ItemSettlement>> result = new Response<Paging<ItemSettlement>>();

        try {
            if (orderId == null) {
                result.setResult(Paging.empty(ItemSettlement.class));
                return result;
            }


            ItemSettlement criteria = getAuthorizedItemCriteria(user);
            criteria.setOrderId(orderId);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
            params.put("criteria", criteria);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            Paging<ItemSettlement> paging = itemSettlementDao.findBy(params);
            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.warn("fail to find itemSettlement with orderId:{}, pageNo:{}, size:{}, user:{}, error:{} ",
                    orderId, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(ItemSettlement.class));
        } catch (Exception e) {
            log.error("fail to find itemSettlement with orderId:{}, pageNo:{}, size:{}, user:{}, cause:{} ",
                    orderId, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(ItemSettlement.class));
        }

        return result;
    }

    private ItemSettlement getAuthorizedItemCriteria(BaseUser user) {
        ItemSettlement criteria = new ItemSettlement();

        if (isSeller(user)) {
            criteria.setSellerId(user.getId());
            return criteria;
        }

        // admin 用户则不会根据商户号来查询，若是其他用户没有权限
        checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");
        return  criteria;
    }


    /**
     * 分页查找符合条件的有效订单结算记录，有效订单指商户收入不为0的结算记录
     *
     * @param sellerName        商户名称, 可以为空
     * @param settleStatus      结算状态, 可以为空
     * @param paidStartAt       确认起始日期,可以为空
     * @param paidEndAt         确认截止日期,可以为空
     * @param paidAt            确认时间(具体到天)，可以为空
     * @param cashed            有线上交易的订单（非普通货到付款订单)
     * @param business          类别，可以为空
     * @param size              返回条数, 可以为空
     * @return 查询结果
     */
    public Response<Paging<FatSettlement>> findValidBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                       @ParamInfo("orderId") @Nullable Long orderId,
                                                       @ParamInfo("status") @Nullable Integer settleStatus,
                                                       @ParamInfo("startAt") @Nullable String paidStartAt,
                                                       @ParamInfo("endAt") @Nullable String paidEndAt,
                                                       @ParamInfo("paidAt") @Nullable String paidAt,
                                                       @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                                       @ParamInfo("cashed") @Nullable Boolean cashed,
                                                       @ParamInfo("business") @Nullable Long business,
                                                       @ParamInfo("type") @Nullable Integer type,
                                                       @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                       @ParamInfo("size") @Nullable Integer size,
                                                       @ParamInfo("baseUser") BaseUser user) {

        Response<Paging<FatSettlement>> result = findBy(sellerName, orderId, settleStatus, paidStartAt,
                paidEndAt, paidAt, confirmedAt, cashed,
                business, type, pageNo, size, user);

        filterValidSettlements(result);
        return result;
    }

    private void filterValidSettlements(Response<Paging<FatSettlement>> result) {
        Paging<FatSettlement> paging = result.getResult();
        List<FatSettlement> settlements = paging.getData();

        for (FatSettlement settlement : settlements) {
            if (!equalWith(settlement.getSellerEarning(), 0L)) {
               settlement.setId(null);
            } else {
                log.info("filter settlement:(id:{}, sellerEarning:{}, totalExpenditure:{})",
                        settlement.getId(), settlement.getSellerEarning(), settlement.getTotalExpenditure());
            }

        }
        result.setResult(paging);
    }


    /**
     * 分页查找符合条件的订单结算记录
     *
     * @param sellerName        商户名称, 可以为空
     * @param settleStatus      结算状态, 可以为空
     * @param paidStartAt       确认起始日期,可以为空
     * @param paidEndAt         确认截止日期,可以为空
     * @param paidAt            确认时间(具体到天)，可以为空
     * @param cashed            有线上交易的订单（非普通货到付款订单)
     * @param business          类别
     * @param pageNo            起始偏移, 可以为空
     * @param size              返回条数, 可以为空
     * @return 查询结果
     */
    public Response<Paging<FatSettlement>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                  @ParamInfo("orderId") @Nullable Long orderId,
                                                  @ParamInfo("status") @Nullable Integer settleStatus,
                                                  @ParamInfo("startAt") @Nullable String paidStartAt,
                                                  @ParamInfo("endAt") @Nullable String paidEndAt,
                                                  @ParamInfo("paidAt") @Nullable String paidAt,
                                                  @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                                  @ParamInfo("cashed") @Nullable Boolean cashed,
                                                  @ParamInfo("business") @Nullable Long business,
                                                  @ParamInfo("type") @Nullable Integer type,
                                                  @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                  @ParamInfo("size") @Nullable Integer size,
                                                  @ParamInfo("baseUser") BaseUser user) {

        Response<Paging<FatSettlement>> result = new Response<Paging<FatSettlement>>();

        try {

            // 非管理员用户不允许查询指定商家
            nonAdminCanNotQueryBySellerName(sellerName, user);
            // 授权用户查询范围
            Settlement criteria = getAuthorizedCriteria(sellerName, user);
            criteria.setSettleStatus(settleStatus);
            criteria.setBusiness(business);
            criteria.setOrderId(orderId);
            criteria.setType(type);

            // 日期查询范围
            Map<String, Object> params = getDateRangedCriteriaParams(criteria, user, paidStartAt, paidEndAt, paidAt, confirmedAt);
            // 设定分页参数
            PageInfo pageInfo = new PageInfo(pageNo, size);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            log.debug("query params {}", params);
            params.put("cashed", cashed);

            if (notNull(orderId)) {
                params.put("orderId", orderId);
            }


            Paging<Settlement> paging = settlementDao.findBy(params);
            Paging<FatSettlement> fatPaging = getFatSettlementPaging(user, paging);

            result.setResult(fatPaging);
        } catch (IllegalStateException e) {
            log.warn("fail to find settlement with sellerName:{}, settleStatus:{}, paidStartAt:{}, " +
                    "paidEndAt:{}, pageNo:{}, size:{}, user:{}, error:{}", sellerName, settleStatus,
                    paidStartAt, paidEndAt, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(FatSettlement.class));
        } catch (Exception e) {
            log.error("fail to find settlement with sellerName:{}, settleStatus:{}, paidStartAt:{}, " +
                    "paidEndAt:{}, pageNo:{}, size:{}, user:{}, cause:{}", sellerName, settleStatus,
                    paidStartAt, paidEndAt, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(FatSettlement.class));
        }

        return result;
    }



    private Paging<FatSettlement> getFatSettlementPaging(BaseUser user, Paging<Settlement> paging) {
        Paging<FatSettlement> fatPaging;

        if (isSeller(user)) { // 若当前的用户是seller

            // 判断是否要禁用确认按钮
            Boolean needLock = depositManager.isAccountLocked(user.getId(), threshold);
            fatPaging = FatSettlement.transform(paging, needLock);

        } else {
            fatPaging = FatSettlement.transform(paging, Boolean.FALSE);
        }
        return fatPaging;
    }


    private void nonAdminCanNotQueryBySellerName(String sellerName, BaseUser user) {
        if (isNotAdmin(user) && isNotFinance(user)) {
            checkState(Strings.isNullOrEmpty(sellerName), "user.has.no.permission");
        }
    }

    private Settlement getAuthorizedCriteria(String sellerName, BaseUser user) {
        if (isAdmin(user) || isFinance(user)) {
            return adminAuthorizedCriteria(sellerName);
        }

        if (isSeller(user)) {
            return sellerAuthorizedCriteria(user);
        }

        throw new IllegalStateException("user.type.incorrect");
    }

    /**
     * 获取管理员的查询范围
     */
    private Settlement adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            Response<User> sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
            User seller = sellerQueryResult.getResult();
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        Settlement criteria = new Settlement();
        criteria.setSellerId(sellerId);
        return criteria;
    }

    /**
     * 获取商家的查询范围
     */
    private Settlement sellerAuthorizedCriteria(BaseUser user) {
        Settlement criteria = new Settlement();
        criteria.setSellerId(user.getId());
        return criteria;
    }

    private Map<String, Object> getDateRangedCriteriaParams(Settlement criteria, BaseUser user,
                                                            String startAt, String endAt, String paidAt, String confirmedAt) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
        params.put("criteria", criteria);

        if (!Strings.isNullOrEmpty(confirmedAt)) {
            criteria.setConfirmedAt(DFT.parseDateTime(confirmedAt).toDate());
            params.put("confirmedStartAt", startOfDay(criteria.getConfirmedAt()));
            params.put("confirmedEndAt", endOfDay(criteria.getConfirmedAt()));
            return params;
        }



        if (!Strings.isNullOrEmpty(paidAt)) {
            criteria.setPaidAt(DFT.parseDateTime(paidAt).toDate());
            params.put("paidStartAt", startOfDay(criteria.getPaidAt()));
            params.put("paidEndAt", endOfDay(criteria.getPaidAt()));
            return params;
        }

        if (isAdmin(user) || isFinance(user)) {
            adminAuthorizedDateRange(startAt, endAt, params);
            return params;
        }

        if (isSeller(user)) {
            sellerAuthorizedDateRange(startAt, endAt, params);
            return params;
        }

        log.error("user ({}) type not support", user);
        throw new IllegalStateException("user.type.correct");
    }

    private void sellerAuthorizedDateRange(String startAt, String endAt, Map<String, Object> params) {
        DateTime permit = DateTime.now().minusDays(permitDay); //商家只允许查7天前的订单
        DateTime startDateTime;
        DateTime endDateTime;

        if (Strings.isNullOrEmpty(startAt)) {
            startDateTime = DateTime.now().minusMonths(1);
        } else {
            startDateTime = DFT.parseDateTime(startAt);
        }

        if (Strings.isNullOrEmpty(endAt)) {
            endDateTime = permit.withTimeAtStartOfDay().plusDays(1);
        } else {
            endDateTime = DFT.parseDateTime(endAt);
            endDateTime = endDateTime.plusDays(1);
        }

        if (startDateTime.isAfter(permit)) {
            startDateTime = permit.withTimeAtStartOfDay();
        }

        if (endDateTime.isAfter(permit)) {
            endDateTime = permit.withTimeAtStartOfDay().plusDays(1);
        }

        if (endDateTime.isEqual(startDateTime)) {
            endDateTime = startDateTime.withTimeAtStartOfDay().plusDays(1);
        }

        checkState(startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime), "seller.settlement.query.start.after.end");
        params.put("paidStartAt", startDateTime.toDate());
        params.put("paidEndAt", endDateTime.toDate());
    }

    private void adminAuthorizedDateRange(String startAt, String endAt, Map<String, Object> params) {
        Date paidStartAt;
        Date paidEndAt;
        if (!Strings.isNullOrEmpty(startAt)) {   //若为空则默认开始时间一年前
            paidStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            paidStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (!Strings.isNullOrEmpty(endAt)) {   //若为空则默认开始时间为系统时间的次日
            DateTime paidDate = DFT.parseDateTime(endAt);

            if (Objects.equal(startAt, endAt)) {
                paidEndAt = paidDate.plusDays(1).toDate();
            } else {
                paidDate = paidDate.plusDays(1);
                paidEndAt = paidDate.toDate();
            }
        } else {
            paidEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("paidStartAt", paidStartAt);
        params.put("paidEndAt", paidEndAt);
    }

    /**
     * 更新和8码相关的所有表
     *
     * @param shop 店铺信息
     * @return 执行是否成功
     */
    @Override
    public Response<Boolean> batchUpdateOuterCodeOfShopRelated(String outerCode, Shop shop) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            settlementManager.batchUpdateOuterCodeOfSeller(outerCode, shop.getUserId());
            result.setResult(Boolean.TRUE);

        } catch (Exception e) {
            log.error("fail to batch update shop(id:{}, name:{}, userId:{}, userName:{}), cause:{}",
                    shop.getId(), shop.getName(), shop.getUserId(), shop.getUserName(), Throwables.getStackTraceAsString(e));
            result.setError("batch.update.outer.code.fail");
        }

        return result;
    }


    /**
     * 获取支付宝的帐务记录
     *
     * @param merchantNo 支付宝的外部商户号
     * @return 支付宝帐务记录
     */
    @Override
    public Response<List<AlipayTrans>> findAlipayTransByMerchantNo(String merchantNo) {
        Response<List<AlipayTrans>> result = new Response<List<AlipayTrans>>();
        try {

            checkArgument(notEmpty(merchantNo), "trans.no.can.not.be.empty");

            List<AlipayTrans> alipayTranses = getAlipayTranses(merchantNo);
                    checkState(!CollectionUtils.isEmpty(alipayTranses), "alipay.trans.not.found");
            result.setResult(alipayTranses);


        } catch (IllegalArgumentException e) {
            log.error("fail to get alipay trans by merchantNo:{}, error:{}", merchantNo, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to get alipay trans by merchantNo:{}, error:{}", merchantNo, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to get alipay trans by merchantNo:{}, cause:{}", merchantNo, Throwables.getStackTraceAsString(e));
            result.setError("alipay.trans.query.fail");
        }

        return result;
    }


    private List<AlipayTrans> getAlipayTranses(String merchantNo) {
        List<AlipayTrans> alipayTranses = Lists.newArrayList();

        AlipaySettlementResponse alipayResponse = PageQueryRequest.build(token)
                .start(DateTime.now().toDate()).end(DateTime.now().toDate())
                .merchantOutOrderNo(merchantNo)
                .pageNo(1).pageSize(200).query();
        Preconditions.checkState(alipayResponse.isSuccess(), "settlement.alipay.trans.download.fail");
        List<AlipaySettlementDto> dtos = alipayResponse.getResult().getPaging().getAccountLogList();
        for (AlipaySettlementDto dto : dtos) {
            AlipayTrans alipayTrans = new AlipayTrans();
            BeanMapper.copy(dto, alipayTrans);
            alipayTransDao.create(alipayTrans);
            alipayTranses.add(alipayTrans);
        }

        return alipayTranses;
    }

    /**
     * 根据订单id和type 查询唯一子订单结算
     * @param orderId 订单id
     * @param type 1:普通交易, 2:预售定金, 3:预售尾款
     * @return 子订单结算
     */
    @Override
    public Response<ItemSettlement> findByOrderIdAndType(Long orderId,Integer type) {
        Response<ItemSettlement> result = new Response<ItemSettlement>();
        try {

            checkArgument(!isNull(orderId), "illegal.param");
            checkArgument(!isNull(type), "illegal.param");

            ItemSettlement itemSettlement = itemSettlementDao.findByOrderIdAndType(orderId,type);
            checkState(notNull(itemSettlement), "item.settlement.not.found");
            result.setResult(itemSettlement);

        } catch (IllegalArgumentException e) {
            log.error("fail to get item settlement by order(id={}) type={}, cause:{}", orderId, type, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to get item settlement by order(id={}) type={}, cause:{}", orderId, type, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to get item settlement by order(id={}) type={}, cause:{}", orderId,type, Throwables.getStackTraceAsString(e));
            result.setError("item.settlement.query.fail");
        }

        return result;
    }

    @Override
    public Response<Boolean> updateItemSettlement(ItemSettlement itemSettlement) {

        Response<Boolean> result = new Response<Boolean>();
        try{
           checkArgument(!isNull(itemSettlement.getId()),"illegal.param");
           Boolean isUpdate = itemSettlementDao.update(itemSettlement);
           result.setResult(isUpdate);
           return result;

        }catch (IllegalArgumentException e){
            log.error("fail to update item settlement error:{}",e.getMessage());
            result.setError(e.getMessage());
        } catch(Exception e){
            log.error("fail to update item settlement");
            result.setError("fail.to.update.item.settlement");
        }
        return result;
    }


    @Override
    public Response<Boolean> updateSettlement(Settlement settlement) {

        Response<Boolean> result = new Response<Boolean>();
        try{
            checkArgument(!isNull(settlement.getId()),"illegal.param");
            Boolean isUpdate = settlementDao.update(settlement);
            result.setResult(isUpdate);
            return result;

        }catch (IllegalArgumentException e){
            log.error("fail to update settlement error:{}",e.getMessage());
            result.setError(e.getMessage());
        } catch(Exception e){
            log.error("fail to update settlement");
            result.setError("fail.to.update.settlement");
        }
        return result;
    }

    /**
     * 根据订单id查询唯一订单结算
     * @param orderId 订单id
     * @return 订单结算
     */
    @Override
    public Response<Settlement> findByOrderId(Long orderId) {
        Response<Settlement> result = new Response<Settlement>();
        try {

            checkArgument(!isNull(orderId), "illegal.param");

            Settlement settlement = settlementDao.getByOrderId(orderId);
            checkState(notNull(settlement), "settlement.not.found");
            result.setResult(settlement);

        } catch (IllegalArgumentException e) {
            log.error("fail to get settlement by order(id={}), cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to get settlement by order(id={}) , cause:{}", orderId,Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to get settlement by order(id={}), cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("settlement.query.fail");
        }

        return result;
    }

    /**
     * 更改某订单的结算状态
     * @author jiangpeng
     * @createAt 2015/1/6 15:10
     * @param orderNo 订单号
     * @param state 订单状态
     * @return 响应结果
     */
    public Response<Boolean> updateSettleStatus(String orderNo,String state) {
        //定义返回结果
        Response<Boolean> result = new Response<Boolean>();
        try {
            //执行更新操作
            boolean execResult = settlementDao.updateSettleStatus(orderNo, state);
            result.setResult(execResult);
            result.setSuccess(execResult);
        }catch (Exception e){
            result.setError("update settle status error");
            log.error("faild to update SettleStatus,cause by :{}",e);
        }
        //返回结果
        return result;
    }
}
