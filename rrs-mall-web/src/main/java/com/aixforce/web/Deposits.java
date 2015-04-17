package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.dto.DepositAccountDto;
import com.aixforce.rrs.settle.dto.TechFeeSummaryDto;
import com.aixforce.rrs.settle.service.DepositAccountService;
import com.aixforce.rrs.settle.service.DepositFeeService;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-26 4:24 PM  <br>
 * Author: xiao
 */
@Controller
@Slf4j
@RequestMapping("/api/deposits")
public class Deposits {

    @Autowired
    private DepositAccountService depositAccountService;

    @Autowired
    private DepositFeeService depositFeeService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private MessageSources messageSources;


    /**
     * 获取用户的保证金账户信息, 包括应交金额，实交金额
     */
    @RequestMapping(value = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DepositAccountDto getAccount() {
        BaseUser user = UserUtil.getCurrentUser();

        try {
            checkState(notNull(user), "user.not.login.yet");

            // 获取用户账户及账户锁定信息
            Response<DepositAccountDto> accountQueryResult = depositAccountService.getBy(UserUtil.getCurrentUser());
            checkState(accountQueryResult.isSuccess(), accountQueryResult.getError());
            DepositAccountDto dto = accountQueryResult.getResult();

            // 获取用户应缴纳金额
            Response<ShopExtra> shopExtraQueryResult =  shopService.getExtra(user.getId());
            checkState(shopExtraQueryResult.isSuccess(), shopExtraQueryResult.getError());
            ShopExtra shopExtra = shopExtraQueryResult.getResult();
            dto.setDepositNeed(Objects.firstNonNull(shopExtra.getDepositNeed(), 0L));
            return accountQueryResult.getResult();

        } catch (IllegalStateException e) {
            log.error("fail to get depositAccount with user:{}, error:{}", user, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get depositAccount with user:{}, error:{}", user, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("deposit.account.query.fail"));
        }
    }


    @RequestMapping(value = "/tech", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TechFeeSummaryDto getTechFeeSummary() {

        BaseUser user = UserUtil.getCurrentUser();

        try {
            checkState(notNull(user), "user.not.login.yet");

            // 获取用户账户及账户锁定信息
            Response<TechFeeSummaryDto> summaryResult = depositFeeService.summaryOfTechFee(user);
            checkState(summaryResult.isSuccess(), summaryResult.getError());
            TechFeeSummaryDto dto = summaryResult.getResult();

            // 获取用户应缴纳金额
            Response<ShopExtra> shopExtraQueryResult =  shopService.getExtra(user.getId());
            checkState(shopExtraQueryResult.isSuccess(), shopExtraQueryResult.getError());
            ShopExtra shopExtra = shopExtraQueryResult.getResult();
            dto.setTechFeeNeed(Objects.firstNonNull(shopExtra.getTechFeeNeed(), 0L));
            return summaryResult.getResult();

        } catch (IllegalStateException e) {
            log.error("fail to get depositAccount with user:{}, error:{}", user, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get depositAccount with user:{}, error:{}", user, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("deposit.account.query.fail"));
        }

    }

}
