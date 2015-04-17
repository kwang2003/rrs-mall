package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.DepositFeeCashDao;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
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
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.aixforce.user.util.UserVerification.isFinance;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-20 12:53 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class DepositFeeCashServiceImpl implements DepositFeeCashService {

    @Autowired
    private AccountService<User> accountService;
    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");



    @Override
    public Response<Paging<DepositFeeCash>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                   @ParamInfo("id") @Nullable Long id,
                                                   @ParamInfo("status") @Nullable Integer status,
                                                   @ParamInfo("startAt") @Nullable String createdStartAt,
                                                   @ParamInfo("endAt") @Nullable String createdEndAt,
                                                   @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                   @ParamInfo("size") @Nullable Integer size,
                                                   @ParamInfo("baseUser")BaseUser user) {

        Response<Paging<DepositFeeCash>> result = new Response<Paging<DepositFeeCash>>();

        try {

            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");
            DepositFeeCash criteria = adminAuthorizedCriteria(sellerName);
            Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
            criteria.setId(id);
            criteria.setStatus(status);
            params.put("criteria", criteria);
            adminAuthorizedDateRange(createdStartAt, createdEndAt, params);


            PageInfo page = new PageInfo(pageNo, size);
            params.put("limit", page.getLimit());
            params.put("offset", page.getOffset());
            Paging<DepositFeeCash> paging = depositFeeCashDao.findBy(params);
            result.setResult(paging);

        } catch (IllegalStateException e) {
            log.warn("fail to query deposit fee cash with sellerName:{}, startAt:{}, endAt:{}, error:{}",
                    sellerName, createdStartAt, createdEndAt, e.getMessage());
            result.setResult(Paging.empty(DepositFeeCash.class));
        } catch (Exception e) {
            log.error("fail to query deposit fee cash with sellerName:{}, startAt:{}, endAt:{}, cause:{}",
                    sellerName, createdStartAt, createdEndAt, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DepositFeeCash.class));
        }

        return result;
    }

    private DepositFeeCash adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            Response sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
            BaseUser seller = (BaseUser)sellerQueryResult.getResult();
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        DepositFeeCash criteria = new DepositFeeCash();
        criteria.setSellerId(sellerId);
        return criteria;
    }


    private void adminAuthorizedDateRange(String startAt, String endAt, Map<String, Object> params) {
        Date createdStartAt;
        Date createdEndAt;
        if (!Strings.isNullOrEmpty(startAt)) {   //若为空则默认开始时间一年前
            createdStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            createdStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (!Strings.isNullOrEmpty(endAt)) {   //若为空则默认开始时间为系统时间的次日
            if (Objects.equal(startAt, endAt)) {
                createdEndAt = DFT.parseDateTime(endAt).plusDays(1).toDate();
            } else {
                createdEndAt = DFT.parseDateTime(endAt).toDate();
            }
        } else {
            createdEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("createdStartAt", createdStartAt);
        params.put("createdEndAt", createdEndAt);
    }


    @Override
    public Response<Boolean> cashing(Long id, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");
            checkArgument(notNull(id), "id.can.not.be.empty");

            DepositFeeCash updating = depositFeeCashDao.get(id);
            checkState(notNull(updating), "record.not.found");
            depositFeeCashDao.cashing(updating);
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to cashing with DepositFeeCash(id:{}), error:{}",
                    id, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to cashing with DepositFeeCash(id:{}), error:{}",
                    id, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to cashing with DepositFeeCash(id:{}), cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            result.setError("deposit.fee.cashing.fail");
        }

        return result;
    }


}
