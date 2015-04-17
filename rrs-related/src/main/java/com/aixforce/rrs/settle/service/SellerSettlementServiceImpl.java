package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.dao.SellerSettlementDao;
import com.aixforce.rrs.settle.dto.PrintableSettlementDto;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
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

import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.aixforce.user.util.UserVerification.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 5:09 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class SellerSettlementServiceImpl implements SellerSettlementService {

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private AccountService accountService;

    @Autowired
    private ShopService shopService;

    @Value("#{app.permitDay}")
    private Integer permitDay;


    /**
     * 根据商户确认的起止日期来查询该商户日结算汇总分页信息 <br/>
     * 若为空则查询所有商户信息（此项操作仅运营人能可以执行）
     *
     * @param sellerName            商户名称,可为空
     * @param confirmedStartAt      起始时间(基于创建时间),可为空
     * @param confirmedEndAt        截止时间(基于创建时间),可为空
     * @param confirmedAt           创建时间(基于创建时间),可为空，如填写则此项则startAt,endAt失效
     * @param pageNo                页码，从1开始
     * @param size                  每页显示条目数
     * @return  满足条件的日结算信息列表，若查不到则返回空列表
     */
    @Override
    public Response<Paging<SellerSettlement>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                     @ParamInfo("startAt") @Nullable String confirmedStartAt,
                                                     @ParamInfo("endAt") @Nullable String confirmedEndAt,
                                                     @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                                     @ParamInfo("filter") @Nullable Boolean filter,
                                                     @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                     @ParamInfo("size") @Nullable Integer size,
                                                     @ParamInfo("baseUser") BaseUser user) {
        Response<Paging<SellerSettlement>> result = new Response<Paging<SellerSettlement>>();

        try {
            // 非管理员用户不允许查询指定商家
            nonAdminCanNotQueryBySellerName(sellerName, user);

            // 根据用户获取不同的查询条件
            SellerSettlement criteria = getAuthorizedCriteria(sellerName, user);

            // 根据输入时间获取用户允许查询的时间范围
            Map<String, Object> params = getDateRangedCriteriaParams(criteria, user, confirmedStartAt, confirmedEndAt, confirmedAt);
            // 设置分页
            setPagingParams(pageNo, size, params);
            log.debug("query params {}", params);
            params.put("filter", filter);

            // 查询并返回结果集
            Paging<SellerSettlement> paging = sellerSettlementDao.findBy(params);
            result.setResult(paging);

        } catch (IllegalStateException e) {
            log.warn("fail to query with start:{}, end:{}, confirmedAt:{}, sellerName:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    confirmedStartAt, confirmedEndAt, confirmedAt, sellerName, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(SellerSettlement.class));
        } catch (Exception e) {
            log.error("fail to query with start:{}, end:{}, confirmedAt:{}, sellerName:{}, pageNo:{}, size:{}, user:{}, cause:{}",
                    confirmedStartAt, confirmedEndAt, confirmedAt, sellerName, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(SellerSettlement.class));
        }

        return result;
    }



    private void nonAdminCanNotQueryBySellerName(String sellerName, BaseUser user) {
        if (isNotAdmin(user) && isNotFinance(user)) {
            checkState(Strings.isNullOrEmpty(sellerName), "user.has.no.permission");
        }
    }


    private SellerSettlement getAuthorizedCriteria(String sellerName, BaseUser user) {
        if (isAdmin(user) || isFinance(user)) {
            return adminAuthorizedCriteria(sellerName);
        }

        if (isSeller(user)) {
            return sellerAuthorizedCriteria(user);
        }

        throw new IllegalStateException("user.type.incorrect");
    }

    private SellerSettlement sellerAuthorizedCriteria(BaseUser user) {
        SellerSettlement criteria = new SellerSettlement();
        criteria.setSellerId(user.getId());
        return criteria;
    }

    private SellerSettlement adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            Response sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
            BaseUser seller = (BaseUser)sellerQueryResult.getResult();
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        SellerSettlement criteria = new SellerSettlement();
        criteria.setSellerId(sellerId);
        return criteria;
    }

    private Map<String, Object> getDateRangedCriteriaParams(SellerSettlement criteria, BaseUser user,
                                                            String startAt, String endAt, String confirmedAt) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
        params.put("criteria", criteria);

        if (!Strings.isNullOrEmpty(confirmedAt)) {
            criteria.setConfirmedAt(DFT.parseDateTime(confirmedAt).toDate());
            params.put("confirmedStartAt", startOfDay(criteria.getConfirmedAt()));
            params.put("confirmedEndAt", endOfDay(criteria.getConfirmedAt()));
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
        //DateTime permit = DateTime.now().minusDays(permitDay); //商家只允许查7天前的订单
        DateTime startDateTime;
        DateTime endDateTime;

        if (Strings.isNullOrEmpty(startAt)) {
            startDateTime = DateTime.now().minusMonths(1);
        } else {
            startDateTime = DFT.parseDateTime(startAt);
        }

        if (Strings.isNullOrEmpty(endAt)) {
            endDateTime = DateTime.now().withTimeAtStartOfDay().plusDays(1);
        } else {
            endDateTime = DFT.parseDateTime(endAt);
            endDateTime = endDateTime.plusDays(1);
        }
        if (endDateTime.isEqual(startDateTime)) {
            endDateTime = startDateTime.withTimeAtStartOfDay().plusDays(1);
        }

        checkState(startDateTime.isBefore(endDateTime) || startDateTime.isEqual(endDateTime), "seller.settlement.query.start.after.end");
        params.put("confirmedStartAt", startDateTime.toDate());
        params.put("confirmedEndAt", endDateTime.toDate());
    }

    private void adminAuthorizedDateRange(String startAt, String endAt, Map<String, Object> params) {
        Date confirmedStartAt;
        Date confirmedEndAt;
        if (!Strings.isNullOrEmpty(startAt)) {   //若为空则默认开始时间一年前
            confirmedStartAt = DFT.parseDateTime(startAt).toDate();
        } else {
            confirmedStartAt = DateTime.now().withTimeAtStartOfDay().minusYears(1).toDate();
        }

        if (!Strings.isNullOrEmpty(endAt)) {   //若为空则默认开始时间为系统时间的次日
            DateTime endDate = DFT.parseDateTime(endAt);
            endDate = endDate.plusDays(1);
            confirmedEndAt = endDate.toDate();

        } else {
            confirmedEndAt =  DateTime.now().plusDays(1).toDate();
        }

        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
    }

    private void setPagingParams(Integer pageNo, Integer size, Map<String, Object> params) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        params.put("offset", pageInfo.offset);
        params.put("limit", pageInfo.limit);
    }


    @Override
    public Response<PrintableSettlementDto> get(@ParamInfo("id") Long id, @ParamInfo("baseUser") BaseUser user) {
        Response<PrintableSettlementDto> result = new Response<PrintableSettlementDto>();
        try {

            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");

            SellerSettlement expected = sellerSettlementDao.get(id);
            checkState(notNull(expected), "seller.settlement.not.found");
            checkState(notNull(expected.getSellerId()), "seller.id.empty");

            Response<Shop> shopResult = shopService.findByUserId(expected.getSellerId());
            checkState(shopResult.isSuccess(), shopResult.getError());
            Shop shop = shopResult.getResult();

            PrintableSettlementDto printed = new PrintableSettlementDto();
            BeanMapper.copy(expected, printed);
            printed.setShopName(shop.getName());

            result.setResult(printed);

        } catch (IllegalStateException e) {
            log.warn("fail to query seller settlements with id:{}, user:{}, error:{}",
                    id, user, e.getMessage());
        } catch (Exception e) {
            log.error("fail to query seller settlements with id:{}, user:{}, cause:{}",
                    id, user, Throwables.getStackTraceAsString(e));
        }
        return result;
    }

    /**
     * 标记指定的商户日汇总记录为"已打印"
     *
     * @param id   日汇总记录id
     * @param user 当前用户
     * @return 是否操作成功
     */
    @Override
    public Response<Boolean> printing(Long id, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notNull(id), "id.can.not.be.empty");
            checkArgument(notNull(user), "user.can.not.be.empty");
            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");


            SellerSettlement sellerSettlement = sellerSettlementDao.get(id);
            checkState(notNull(sellerSettlement), "seller.settlement.not.found");

            // 打印结算单
            Boolean success = sellerSettlementDao.printing(sellerSettlement);
            checkState(success, "seller.settlement.update.fail");
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.warn("fail to printing sellerSettlement with id:{}, user:{}, error:{}",
                    id, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to printing sellerSettlement with id:{}, user:{}, error:{}",
                    id, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to printing sellerSettlement with id:{}, user:{}, cause:{}",
                    id, user, Throwables.getStackTraceAsString(e));
            result.setError("seller.settlement.print.fail");
        }

        return result;
    }


}
