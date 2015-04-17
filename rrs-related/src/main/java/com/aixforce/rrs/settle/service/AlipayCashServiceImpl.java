package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.AlipayCashDao;
import com.aixforce.rrs.settle.dao.SellerAlipayCashDao;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.aixforce.user.util.UserVerification.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 3:02 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class AlipayCashServiceImpl implements AlipayCashService {
    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private AlipayCashDao alipayCashDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Value("#{app.permitDay}")
    private Integer permitDay;
    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");


    @Override
    public Response<Paging<SellerAlipayCash>> findSellerAlipayCashesBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                                       @ParamInfo("startAt") @Nullable String startAt,
                                                                       @ParamInfo("endAt") @Nullable String endAt,
                                                                       @ParamInfo("summedAt") @Nullable String summedAt,
                                                                       @ParamInfo("filter") @Nullable Boolean filter,
                                                                       @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                       @ParamInfo("size") @Nullable Integer size,
                                                                       @ParamInfo("baseUser") BaseUser user) {

        Response<Paging<SellerAlipayCash>> result = new Response<Paging<SellerAlipayCash>>();
        try {
            // 非管理员用户不允许查询指定商家
            nonAdminCanNotQueryBySellerName(sellerName, user);
            // 授权用户查询范围
            SellerAlipayCash criteria = getAuthorizedCriteria(sellerName, user);
            // 日期查询范围
            Map<String, Object> params = getDateRangedCriteriaParams(criteria, user, startAt, endAt, summedAt);
            // 设定分页参数
            PageInfo pageInfo = new PageInfo(pageNo, size);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);
            log.debug("query params {}", params);
            params.put("filter", filter);


            Paging<SellerAlipayCash> paging = sellerAlipayCashDao.findBy(params);
            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.warn("fail to find sellerAlipayCashes by sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    sellerName, startAt, endAt, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(SellerAlipayCash.class));
        } catch (IllegalStateException e) {
            log.warn("fail to find sellerAlipayCashes by sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    sellerName, startAt, endAt, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(SellerAlipayCash.class));
        } catch (Exception e) {
            log.error("fail to find sellerAlipayCashes by sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, user:{}, cause:{}",
                    sellerName, startAt, endAt, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(SellerAlipayCash.class));
        }
        return result;
    }

    /**
     * 非管理员用户不能查询指定具体的商户
     */
    private void nonAdminCanNotQueryBySellerName(String sellerName, BaseUser user) {
        if (isNotAdmin(user) && isNotFinance(user)) {
            checkState(Strings.isNullOrEmpty(sellerName), "user.has.no.permission");
        }
    }

    private SellerAlipayCash getAuthorizedCriteria(String sellerName, BaseUser user) {
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
    private SellerAlipayCash adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            Response sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
            BaseUser seller = (BaseUser)sellerQueryResult.getResult();
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSellerId(sellerId);
        return criteria;
    }

    /**
     * 获取商家的查询范围
     */
    private SellerAlipayCash sellerAuthorizedCriteria(BaseUser user) {
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSellerId(user.getId());
        return criteria;
    }

    /**
     * 获取日期查询的范围
     */
    private Map<String, Object> getDateRangedCriteriaParams(SellerAlipayCash criteria, BaseUser user,
                                                            String startAt, String endAt, String summedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
        params.put("criteria", criteria);

        if (!Strings.isNullOrEmpty(summedAt)) {
            criteria.setSummedAt(DFT.parseDateTime(summedAt).toDate());
            params.put("summedStartAt", startOfDay(criteria.getSummedAt()));
            params.put("summedEndAt", endOfDay(criteria.getSummedAt()));
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

        log.warn("user ({}) type not support", user);
        throw new IllegalStateException("user.type.correct");
    }


    /**
     * 商家可查询的日期范围
     */
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

        checkState(startDateTime.isBefore(endDateTime), "seller.alipay.cash.query.start.after.end");
        params.put("summedStartAt", startDateTime.toDate());
        params.put("summedEndAt", endDateTime.toDate());
    }


    /**
     * 根据起止日期分页查询所有的支付宝提现记录
     *
     * @param startAt  开始日期
     * @param endAt    截止日期
     * @param pageNo   页码，从1开始
     * @param size     每页显示条目数
     * @return 符合条件的查询记录
     */
    @Override
    public Response<Paging<AlipayCash>> findBy( @ParamInfo("startAt") @Nullable String startAt,
                                                @ParamInfo("endAt") @Nullable String endAt,
                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                @ParamInfo("size") @Nullable Integer size,
                                                @ParamInfo("baseUser") BaseUser user) {

        Response<Paging<AlipayCash>> result = new Response<Paging<AlipayCash>>();

        try {
            checkArgument(isAdmin(user) || isFinance(user), "user.has.no.permission");
            AlipayCash criteria = new AlipayCash();

            Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
            params.put("criteria", criteria);
            adminAuthorizedDateRange(startAt, endAt, params);

            PageInfo page = new PageInfo(pageNo, size);
            params.put("limit", page.getLimit());
            params.put("offset", page.getOffset());
            Paging<AlipayCash> paging = alipayCashDao.findBy(params);
            result.setResult(paging);


        } catch (IllegalArgumentException e) {
            log.warn("fail to query alipayCash with startAt:{}, endAt:{}, pageNo:{}, size:{}, error:{}",
                    startAt, endAt, pageNo, size, e.getMessage());
            result.setResult(Paging.empty(AlipayCash.class));
        } catch (IllegalStateException e) {
            log.warn("fail to query alipayCash with startAt:{}, endAt:{}, pageNo:{}, size:{}, error:{}",
                    startAt, endAt, pageNo, size, e.getMessage());
            result.setResult(Paging.empty(AlipayCash.class));
        } catch (Exception e) {
            log.error("fail to query alipayCash with startAt:{}, endAt:{}, pageNo:{}, size:{}, cause:{}",
                    startAt, endAt, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(AlipayCash.class));
        }

        return result;
    }

    /**
     * 管理员可查询的日期范围
     */
    private void adminAuthorizedDateRange(String startAt, String endAt, Map<String, Object> params) {
        Date summedStartAt;
        Date summedEndAt;
        if (!Strings.isNullOrEmpty(startAt)) {   //若为空则默认开始时间一年前
            summedStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            summedStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (!Strings.isNullOrEmpty(endAt)) {   //若为空则默认开始时间为系统时间的次日
            DateTime summedEndDate = DFT.parseDateTime(endAt);
            summedEndDate = summedEndDate.plusDays(1);
            summedEndAt = summedEndDate.toDate();
        } else {
            summedEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("summedStartAt", summedStartAt);
        params.put("summedEndAt", summedEndAt);
    }
}
