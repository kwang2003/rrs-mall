package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.DepositAccountDao;
import com.aixforce.rrs.settle.dto.DepositAccountDto;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.DepositAccount;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@Slf4j
@Service
public class DepositAccountServiceImpl implements DepositAccountService {

    @Autowired
    private DepositManager depositManager;

    @Autowired
    private DepositAccountDao depositAccountDao;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ShopService shopService;

    @Value("#{app.threshold}")
    private Integer threshold;

    /**
     * 查询商家的保证金账户
     * @param name           商家名称,选填
     * @param business       行业类目,选填
     * @param lower          金额下限,选填
     * @param upper          金额上限,选填
     * @param pageNo         页码,从1开始,选填
     * @param size           每页显示条数,选填
     * @return  保证金账户列表
     */
    @Override
    public Response<Paging<DepositAccount>> findBy(@ParamInfo("name") @Nullable String name,
                                            @ParamInfo("business") @Nullable Long business,
                                            @ParamInfo("lower") @Nullable Float lower,
                                            @ParamInfo("upper") @Nullable Float upper,
                                            @ParamInfo("pageNo") @Nullable Integer pageNo,
                                            @ParamInfo("size") @Nullable Integer size,
                                            @ParamInfo("baseUser")BaseUser user) {

        Response<Paging<DepositAccount>> result = new Response<Paging<DepositAccount>>();

        try {
            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");

            DepositAccount criteria;
            if (Strings.isNullOrEmpty(name)) {
                criteria = new DepositAccount();
            } else {
                criteria = getCriteriaOfQueryBySeller(name);
            }
            criteria.setBusiness(business);
            criteria.setSellerName(name);

            lower = lower != null ? lower * 100 : null;
            upper = upper != null ? upper * 100 : null;

            Integer lowerInt = lower == null ? null : lower.intValue();
            Integer upperInt = upper == null ? null : upper.intValue();

            PageInfo pageInfo = new PageInfo(pageNo, size);
            Paging<DepositAccount> paging = depositAccountDao.findBy(criteria, lowerInt, upperInt, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);
        } catch (IllegalStateException e) {
            log.warn("fail to query account with name={}, business={}, upper={}, lower={}, error:{}",
                    name, business, upper, lower, e.getMessage());
            result.setResult(Paging.empty(DepositAccount.class));
        } catch (Exception e) {
            log.error("fail to query account with name={}, business={}, upper={}, lower={}, cause:{}",
                    name, business, upper, lower, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DepositAccount.class));
        }
        return result;
    }


    private DepositAccount getCriteriaOfQueryBySeller(String name) {
        Response<User> sellerGetResult = accountService.findUserBy(name, LoginType.NAME);
        checkState(sellerGetResult.isSuccess(), sellerGetResult.getError());
        checkState(notNull(sellerGetResult.getResult()), "seller.not.found");
        User seller = sellerGetResult.getResult();
        DepositAccount criteria = new DepositAccount();
        criteria.setSellerId(seller.getId());
        return criteria;
    }


    /**
     * 查询商家的保证金账户
     *
     * @param user  用户信息
     * @return 保证金账户
     */
    @Override
    public Response<DepositAccountDto> getBy(@ParamInfo("baseUser")BaseUser user) {
        Response<DepositAccountDto> result = new Response<DepositAccountDto>();
        try {
            checkArgument(isSeller(user), "user.has.no.permission");
            DepositAccount account = depositAccountDao.findBySellerId(user.getId());

            checkState(notNull(account), "deposit.account.not.found");
            DepositAccountDto dto =  DepositAccountDto.transform(account,
                    depositManager.isAccountLocked(account, threshold));
            result.setResult(dto);

        } catch (IllegalArgumentException e) {
            log.warn("fail to query depositAccount with user:{}, error:{}", user, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to query depositAccount with user:{}, error:{}", user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to query depositAccount with user:{}, cause:{}", user, Throwables.getStackTraceAsString(e));
            result.setError("deposit.account.query.fail");
        }

        return result;
    }

    /**
     * 创建商家的保证金账户
     *
     * @param shopId    店铺id
     * @param outerCode 商家外部编码
     */
    @Override
    public Response<Long> create(Long shopId, String outerCode) {
        Response<Long> result = new Response<Long>();
        try {
            checkArgument(notNull(shopId), "shop.id.can.not.be.empty");
            checkArgument(notEmpty(outerCode), "outer.code.can.not.be.empty");


            Response<Shop> shopQueryResult = shopService.findById(shopId);
            checkState(shopQueryResult.isSuccess(), shopQueryResult.getError());
            Shop shop = shopQueryResult.getResult();


            DepositAccount existed = depositAccountDao.findBySellerId(shop.getUserId());
            if (notNull(existed)) {
                result.setResult(existed.getId());
                return result;
            }

            DepositAccount newAccount = new DepositAccount();
            newAccount.setBalance(0L);  //初始保证金余额为0L
            newAccount.setSellerId(shop.getUserId());
            newAccount.setSellerName(shop.getUserName());
            newAccount.setBusiness(shop.getBusinessId());
            newAccount.setOuterCode(outerCode);
            newAccount.setShopId(shop.getId());
            newAccount.setShopName(shop.getName());

            depositAccountDao.create(newAccount);
            checkState(notNull(newAccount.getId()), "deposit.account.create.fail");
            result.setResult(newAccount.getId());

        } catch (IllegalArgumentException e) {
            log.warn("fail to create account with shopId:{}, outerCode:{}, error:{}",
                    shopId, outerCode, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to create account with shopId:{}, outerCode:{}, error:{}",
                    shopId, outerCode, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create account with shopId:{}, outerCode:{}, cause:{}",
                    shopId, outerCode, Throwables.getStackTraceAsString(e));
            result.setError("deposit.account.create.fail");
        }

        return result;
    }
}
