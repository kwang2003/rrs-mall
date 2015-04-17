/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.Arguments;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.dto.DepositAccountDto;
import com.aixforce.rrs.settle.dto.DepositFeeDto;
import com.aixforce.rrs.settle.dto.DepositSummary;
import com.aixforce.rrs.settle.dto.TechFeeSummaryDto;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.service.DepositAccountService;
import com.aixforce.rrs.settle.service.DepositFeeCashService;
import com.aixforce.rrs.settle.service.DepositFeeService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopBaseInfo;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopExtraService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.util.UserVerification;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.aixforce.user.util.UserVerification.isFinance;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: 14-1-24
 * Time: PM6:27
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Controller
@Slf4j
@RequestMapping("/api/admin/deposits")
public class Deposits {
    @Autowired
    private DepositFeeService depositFeeService;

    @Autowired
    private DepositAccountService depositAccountService;

    @Autowired
    private DepositFeeCashService depositFeeCashService;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopExtraService shopExtraService;

    @Autowired
    private MessageSources messageSources;


    @RequestMapping(method = RequestMethod.GET, value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ShopBaseInfo getShopBaseInfo(@RequestParam("sellerName") String sellerName) {

        ShopBaseInfo shopBaseInfo = new ShopBaseInfo();

        try {

            BaseUser user  = UserUtil.getCurrentUser();
            checkState(notNull(user), "user.log.login.yet");
            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");

            Response<User> sellerQueryResult = accountService.findUserBy(sellerName, LoginType.NAME);
            User seller = sellerQueryResult.getResult();
            Response<Shop> shopQueryResult = shopService.findByUserId(seller.getId());
            checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());
            Shop shop = shopQueryResult.getResult();
            Response<ShopExtra> extraQueryResult = shopExtraService.findByShopId(shop.getId());
            ShopExtra extra = extraQueryResult.getResult();


            shopBaseInfo.setUserId(seller.getId());
            shopBaseInfo.setUserName(seller.getName());
            shopBaseInfo.setName(shop.getName());
            shopBaseInfo.setOuterCode(extra.getOuterCode());

        } catch (IllegalStateException e) {
            log.error("fail to get shopBaseInfo with sellerName:{}, error:{}", sellerName, e.getMessage());
        } catch (Exception e) {
            log.error("fail to get shopBaseInfo with sellerName:{}, cause:{}", sellerName, Throwables.getStackTraceAsString(e));
        }

        return shopBaseInfo;
    }



    @RequestMapping(method = RequestMethod.GET, value = "/{id}/sum", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DepositSummary summary(@PathVariable("id") Long id) {

        try {
            BaseUser seller = new BaseUser();
            seller.setType(User.TYPE.SELLER.toNumber());
            seller.setId(id);

            Response<DepositAccountDto> accountQueryResult = depositAccountService.getBy(seller);
            checkState(accountQueryResult.isSuccess(), accountQueryResult.getError());

            Response<TechFeeSummaryDto> techQueryResult = depositFeeService.summaryOfTechFee(seller);
            checkState(techQueryResult.isSuccess(), techQueryResult.getError());

            return new DepositSummary(accountQueryResult.getResult(), techQueryResult.getResult());
        } catch (IllegalStateException e) {
            log.warn("fail to query summary with seller(id:{}), error:{}", id, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to query summary with seller(id:{}), cause:{}", id, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("deposit.sum.query.fail"));
        }
    }



    /**
     *
     * 创建一笔保证金或技术服务费用<br/>
     *
     * @param sellerName        商家名称
     * @param type              费用类型
     * @param depositOfYuan     金额 (单位:元)
     * @param description       备注（可以为空）
     * @return                  创建是否成功
     */
    @RequestMapping(method = RequestMethod.POST, value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(@RequestParam("sellerName") String sellerName,
                       @RequestParam("type") Integer type,
                       @RequestParam("paymentType") Integer paymentType,
                       @RequestParam("depositOfYuan") String depositOfYuan,
                       @RequestParam("description") String description) {


        Long depositOfFen  = new BigDecimal(depositOfYuan).multiply(new BigDecimal(100)).longValue();
        DepositFee depositFee = new DepositFee();
        depositFee.setSellerName(sellerName);
        depositFee.setType(type);
        depositFee.setDeposit(depositOfFen);
        depositFee.setDescription(description);
        depositFee.setPaymentType(paymentType);

        Response<Long> result = depositFeeService.create(depositFee, UserUtil.getCurrentUser());

        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }


    /**
     * PATCH/PUT /deposits/1
     * @param id 目标 deposit id
     * @param fee 提交更新 deposit 实体数据
     * @return 成功返回 "ok"
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public String update(@PathVariable("id") Long id, DepositFeeDto fee) {
        fee.setId(id);
        Double deposit = fee.getDepositOfYuan() * 100;
        fee.setDeposit(deposit.longValue());
        Response<Long> result = depositFeeService.update(fee, UserUtil.getCurrentUser());
        if (result.isSuccess()) {
            return "ok";
        }

        log.error("fail to update {}, error code {}.", fee, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }


    /**
     * GET /deposits/1
     * @param id 将浏览的 deposit id
     * @return 返回实体
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DepositFee view(@PathVariable("id") Long id) {
        Response<DepositFee> result = depositFeeService.findDepositDetailByID(id);
        if (result.isSuccess()) {
            return result.getResult();
        }

        log.error("fail to find deposit detail by given id '{}', error code {}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     * GET /deposits/1/edit
     * @param id 基础费用id
     * @return 将编辑的实体
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DepositFee edit(@PathVariable("id") Long id) {
        Response<DepositFee> result = depositFeeService.findDepositDetailByID(id);
        if (result.isSuccess()) {
            return result.getResult();
        }

        log.error("fail while fetching object: id='{}', error code {}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     *
     * 标记保证金提现记录为已提现
     *
     * @param id   基础费用提现id
     * @return  标记提现的记录
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/cash", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String cashing(@PathVariable("id") Long id) {
        Response<Boolean> result = depositFeeCashService.cashing(id, UserUtil.getCurrentUser());
        if (result.isSuccess()) {
            return "ok";
        }

        log.error("fail to cash object: id='{}', error code {}", id, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

}
