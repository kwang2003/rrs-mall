package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.OrderAlipayCashDao;
import com.aixforce.rrs.settle.manager.AlipayCashManager;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
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
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.aixforce.user.util.UserVerification.isFinance;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 9:59 AM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class OrderAlipayCashServiceImpl implements OrderAlipayCashService {

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private AlipayCashManager alipayCashManager;

    @Autowired
    private AccountService<User> accountService;


    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");


    /**
     * 根据起止日期来查询该订单提现明细分页信息 <br/>
     * 若为空则查询所有商户信息（此项操作仅运营人能可以执行）
     *
     * @param sellerName            商家名称
     * @param startAt               查询起始日期（基于交易日期）
     * @param endAt                 查询截止日期（基于交易日期）
     * @param cashedAt              查询指定某一天的日期（基于提现日期）
     * @param tradedAt              查询指定某一天的日期（基于交易日期）
     * @param pageNo                页码
     * @param size                  每页大小
     * @param user                  用户
     * @return  订单提现分页明细
     */
    public Response<Paging<OrderAlipayCash>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                    @ParamInfo("orderId") @Nullable Long orderId,
                                                    @ParamInfo("type") @Nullable Integer type,
                                                    @ParamInfo("status") @Nullable Integer status,
                                                    @ParamInfo("startAt") @Nullable String startAt,
                                                    @ParamInfo("endAt") @Nullable String endAt,
                                                    @ParamInfo("cashedAt") @Nullable String cashedAt,
                                                    @ParamInfo("tradedAt") @Nullable String tradedAt,
                                                    @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                    @ParamInfo("size") @Nullable Integer size,
                                                    @ParamInfo("baseUser") BaseUser user) {


        Response<Paging<OrderAlipayCash>> result = new Response<Paging<OrderAlipayCash>>();

        try {
            checkArgument(isAdmin(user) || isFinance(user), "user.has.no.permission");
            OrderAlipayCash criteria = adminAuthorizedCriteria(sellerName);

            Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
            criteria.setOrderId(orderId);
            criteria.setType(type);
            criteria.setStatus(status);


            params.put("criteria", criteria);

            if (notEmpty(cashedAt)) {
                Date cashedDate = DFT.parseDateTime(cashedAt).toDate();

                params.put("cashedStartAt", startOfDay(cashedDate));
                params.put("cashedEndAt", endOfDay(cashedDate));
            } else {
                adminAuthorizedDateRange(tradedAt, startAt, endAt, params);
            }


            PageInfo page = new PageInfo(pageNo, size);
            params.put("limit", page.getLimit());
            params.put("offset", page.getOffset());
            Paging<OrderAlipayCash> paging = orderAlipayCashDao.findBy(params);
            result.setResult(paging);

        } catch (IllegalArgumentException e) {
            log.error("fail to query alipayCash with sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, error:{}",
                    sellerName, startAt, endAt, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(OrderAlipayCash.class));
        } catch (IllegalStateException e) {
            log.error("fail to query alipayCash with sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, error:{}",
                    sellerName, startAt, endAt, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(OrderAlipayCash.class));
        } catch (Exception e) {
            log.error("fail to query alipayCash with sellerName:{}, startAt:{}, endAt:{}, pageNo:{}, size:{}, cause:{}",
                    sellerName, startAt, endAt, pageNo, size, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(OrderAlipayCash.class));
        }

        return result;
    }

    private OrderAlipayCash adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            Response sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
            BaseUser seller = (BaseUser)sellerQueryResult.getResult();
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        OrderAlipayCash criteria = new OrderAlipayCash();
        criteria.setSellerId(sellerId);
        return criteria;
    }


    /**
     * 管理员可查询的日期范围
     */
    private void adminAuthorizedDateRange(String tradedAt, String startAt, String endAt, Map<String, Object> params) {
        Date tradedStartAt;
        Date tradedEndAt;

        if (notEmpty(tradedAt)) {
            Date dateAt = DFT.parseDateTime(tradedAt).toDate();
            params.put("tradedStartAt", startOfDay(dateAt));
            params.put("tradedEndAt", endOfDay(dateAt));
            return;
        }

        if (notEmpty(startAt)) {   // 若为空则默认开始时间一年前
            tradedStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            tradedStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (notEmpty(endAt)) {    // 若为空则默认开始时间为系统时间的次日
            DateTime summedEndDate = DFT.parseDateTime(endAt);
            summedEndDate = summedEndDate.plusDays(1);
            tradedEndAt = summedEndDate.toDate();
        } else {
            tradedEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("tradedStartAt", tradedStartAt);
        params.put("tradedEndAt", tradedEndAt);
    }

    /**
     * 确认提现
     *
     * @param id   提现明细的id
     * @param user 用户
     * @return 是否操作成功
     */
    @Override
    public Response<Boolean> cashing(Long id, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notNull(id), "id.can.not.be.empty");
            checkArgument(notNull(user), "user.can.not.be.empty");
            checkArgument(isAdmin(user) || isFinance(user), "user.has.no.permission");


            OrderAlipayCash cash = orderAlipayCashDao.get(id);
            checkState(notNull(cash), "order.cash.not.found");
            alipayCashManager.cashing(cash, user.getName());

            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to confirm orderAlipayCash with id:{}, user:{}, error:{}", id, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to confirm orderAlipayCash with id:{}, user:{}, error:{}", id, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to confirm orderAlipayCash with id:{}, user:{}, cause:{}", id, user, Throwables.getStackTraceAsString(e));
            result.setError("order.alipay.cash.confirm.fail");
        }

        return result;
    }

    /**
     * 批量确认提现
     *
     * @param ids  提现明细的id列表
     * @param user 用户
     * @return 是否操作成功
     */
    @Override
    public Response<Boolean> batchCashing(List<Long> ids, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notNull(ids), "id.can.not.be.empty");
            checkArgument(notNull(user), "user.can.not.be.empty");
            checkArgument(isAdmin(user) || isFinance(user), "user.has.no.permission");

            List<OrderAlipayCash> cashes = orderAlipayCashDao.findByIds(ids);
            alipayCashManager.batchCashing(cashes, user.getName());

            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to confirm orderAlipayCash with ids:{}, user:{}, error:{}", ids, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to confirm orderAlipayCash with ids:{}, user:{}, error:{}", ids, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to confirm orderAlipayCash with ids:{}, user:{}, cause:{}",
                    ids, user, Throwables.getStackTraceAsString(e));
            result.setError("order.alipay.cash.confirm.fail");
        }

        return result;
    }

}
