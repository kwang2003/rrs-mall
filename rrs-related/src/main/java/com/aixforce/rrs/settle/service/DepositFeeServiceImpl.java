package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.DepositAccountDao;
import com.aixforce.rrs.settle.dao.DepositFeeCashDao;
import com.aixforce.rrs.settle.dao.DepositFeeDao;
import com.aixforce.rrs.settle.dto.TechFeeSummaryDto;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.DepositAccount;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.common.utils.Arguments.positive;
import static com.aixforce.rrs.settle.model.DepositFee.isNotSynced;
import static com.aixforce.user.util.UserVerification.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@Slf4j
@Service
public class DepositFeeServiceImpl implements DepositFeeService {

    @Autowired
    private DepositManager depositManager;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private DepositAccountDao depositAccountDao;

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    /**
     * 缴纳或者扣除保证金
     *
     *
     * @param fee    保证金对象
     * @param user   当前用户
     * @return 如果创建成功则返回id
     */
    @Override
    public Response<Long> create(DepositFee fee, BaseUser user) {
        Response<Long> result = new Response<Long>();

        try {

            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");
            checkCreateArguments(fee);

            User seller = getSellerByName(fee.getSellerName());
            createAccountIfNotExist(seller);

            fee.setSellerId(seller.getId());
            Long id = depositManager.createDepositFee(fee);

            result.setResult(id);

        } catch (IllegalArgumentException e) {
            log.warn("fail to create fee with fee:{}, error:{}", fee, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to create fee with fee:{}, error:{}", fee, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create fee with fee:{}, cause:{}", fee, Throwables.getStackTraceAsString(e));
            result.setError("deposit.create.fail");
        }

        return result;
    }

    private void checkCreateArguments(DepositFee fee) {
        checkArgument(notNull(fee.getDeposit()), "deposit.create.fee.null");
        checkArgument(notNull(fee.getSellerName()), "deposit.create.seller.name.null");
        checkArgument(notNull(fee.getType()), "deposit.create.type.null");
        checkArgument(notNull(fee.getPaymentType()), "deposit.create.payment.type.null");
        checkArgument(positive(fee.getDeposit()), "deposit.create.lt0");

        checkArgument(DepositFee.Type.values.contains(fee.getType()), "deposit.fee.type.incorrect");
    }

    private User getSellerByName(String name) {
        Response<User> sellerQueryResult = accountService.findUserBy(name, LoginType.NAME);
        // 没有找到会返回 user.not.found
        checkState(sellerQueryResult.isSuccess(), sellerQueryResult.getError());
        return sellerQueryResult.getResult();
    }

    private ShopExtra getShopExtraOfSeller(User seller) {
        Response<ShopExtra> extraQueryResult = shopService.getExtra(seller.getId());
        checkState(extraQueryResult.isSuccess(), extraQueryResult.getError());
        checkState(notNull(extraQueryResult.getResult()), "deposit.create.shop.extra.empty");
        return extraQueryResult.getResult();
    }

    private Shop getShopOfSeller(User seller) {
        Response<Shop> shopGet = shopService.findByUserId(seller.getId());
        checkState(shopGet.isSuccess(), shopGet.getError());
        return shopGet.getResult();
    }

    private void createAccountIfNotExist(User seller) {
        DepositAccount account = depositAccountDao.findBySellerId(seller.getId());
        if (account == null) { //创建保证金账户
            Shop shop = getShopOfSeller(seller);
            ShopExtra extra = getShopExtraOfSeller(seller);

            DepositAccount newAccount = new DepositAccount();
            newAccount.setBalance(0L);  //初始保证金余额为0L
            newAccount.setSellerId(seller.getId());
            newAccount.setSellerName(seller.getName());
            newAccount.setBusiness(shop.getBusinessId());
            newAccount.setOuterCode(extra.getOuterCode());
            newAccount.setShopId(shop.getId());
            newAccount.setShopName(shop.getName());

            depositAccountDao.create(newAccount);
            checkState(notNull(newAccount.getId()), "deposit.account.create.fail");
        }
    }


    /**
     * 更新一行保证金明细的记录
     *
     * @param updating   待更新的记录
     * @param user       当前操作用户
     * @return   更新后的记录编号
     */
    @Override
    public Response<Long> update(DepositFee updating, BaseUser user) {
        Response<Long> result = new Response<Long>();

        try {
            checkState(isAdmin(user) || isFinance(user), "user.has.no.permission");
            checkUpdateArguments(updating);
            // 如果有凭证，不能再修改保证金
            DepositFee origin = depositFeeDao.get(updating.getId());
            checkState(notNull(origin), "deposit.not.found");
            // 若已经同步则部允许修改
            checkState(isNotSynced(origin), "deposit.can.not.modify");

            // 若存在保证金提现明细并已标记提现, 则提示不允许修改
            DepositFeeCash cash = depositFeeCashDao.getByDepositId(updating.getId());
            if (notNull(cash)) {
                checkState(equalWith(cash.getStatus(), DepositFeeCash.Status.NOT.value()), "deposit.has.been.cashed");
            }

            // 金额发生变化时候要更新对应的账户余额
            updating.setPaymentType(origin.getPaymentType());
            updating.setType(origin.getType());
            Long id = depositManager.updateDeposit(origin, updating);
            result.setResult(id);
        } catch (IllegalArgumentException e) {
            log.warn("fail to update with fee:{}, error {}", updating, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to update with fee:{}, error {}", updating, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to update with fee:{}, cause {}", updating, Throwables.getStackTraceAsString(e));
            result.setError("deposit.update.fail");
        }

        return result;
    }

    private void checkUpdateArguments(DepositFee fee) {
        checkArgument(notNull(fee.getId()), "deposit.update.id.null");
        checkArgument(notNull(fee.getDeposit()), "deposit.update.fee.null");
        checkArgument(positive(fee.getDeposit()), "deposit.create.negative");
    }


    /**
     * 根据商户名字分页查询各种费用
     * 由组件调用
     * @param name      商家 name, 可为空
     * @param bid       类目 id, 可为空
     * @param pageNo    页码, 可为空
     * @param size      每页记录数, 可为空
     * @param user      当前操作的用户
     *
     * @return  符合条件的查询列表
     */
    @Override
    public Response<Paging<DepositFee>> findDepositDetailByName(@ParamInfo("sellerName") @Nullable String name,
                                                                @ParamInfo("businessId") @Nullable Long bid,
                                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                @ParamInfo("size") @Nullable Integer size,
                                                                @ParamInfo("baseUser") BaseUser user) {

        Response<Paging<DepositFee>> result = new Response<Paging<DepositFee>>();

        try {
            // 非管理员不能查询指定商户
            nonAdminCanNotQueryBySellerName(name, user);
            DepositFee criteria = getAuthorizedCriteria(name, user);
            criteria.setBusiness(bid);
            PageInfo pageInfo = new PageInfo(pageNo, size);

            Paging<DepositFee> paging = depositFeeDao.findDepositFeeBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (IllegalStateException e) {
            log.warn("fail to find deposit fee with name:{}, bid:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    name, bid, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(DepositFee.class));
        } catch (Exception e) {
            log.error("fail to find deposit fee with name:{}, bid:{}, pageNo:{}, size:{}, user:{}, cause:{}",
                    name, bid, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DepositFee.class));
        }

        return result;
    }

    /**
     * 根据商家名字分页查询各种费用
     * 由组件调用
     * @param name      商家标识
     * @param bid       类目ID
     * @param pageNo    页码
     * @param size      每页显示条数
     * @param user      用户
     * @return  符合查询条件的分页对象
     */
    @Override
    public Response<Paging<DepositFee>> findTechFeeDetailByName(@ParamInfo("sellerName") @Nullable String name,
                                                                @ParamInfo("businessId") @Nullable Long bid,
                                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                @ParamInfo("size") @Nullable Integer size,
                                                                @ParamInfo("baseUser") BaseUser user) {
        Response<Paging<DepositFee>> result = new Response<Paging<DepositFee>>();

        try {
            // 非管理员不能查询指定商户
            nonAdminCanNotQueryBySellerName(name, user);
            DepositFee criteria = getAuthorizedCriteria(name, user);
            criteria.setBusiness(bid);
            PageInfo pageInfo = new PageInfo(pageNo, size);

            Paging<DepositFee> paging = depositFeeDao.findTechFeeBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (IllegalStateException e) {
            log.warn("fail to find deposit fee with name:{}, bid:{}, pageNo:{}, size:{}, user:{}, error:{}",
                    name, bid, pageNo, size, user, e.getMessage());
            result.setResult(Paging.empty(DepositFee.class));
        } catch (Exception e) {
            log.error("fail to find deposit fee with name:{}, bid:{}, pageNo:{}, size:{}, user:{}, cause:{}",
                    name, bid, pageNo, size, user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DepositFee.class));
        }

        return result;
    }


    private void nonAdminCanNotQueryBySellerName(String sellerName, BaseUser user) {
        if (isNotAdmin(user) && isNotFinance(user)) {
            checkState(Strings.isNullOrEmpty(sellerName), "user.has.no.permission");
        }
    }

    private DepositFee getAuthorizedCriteria(String sellerName, BaseUser user) {
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
    private DepositFee adminAuthorizedCriteria(String sellerName) {
        Long sellerId;

        if (!Strings.isNullOrEmpty(sellerName)) { //若输入了商户名，则查询此商户信息
            User seller = getSellerByName(sellerName);
            sellerId = seller.getId();
        } else {
            sellerId = null;
        }

        DepositFee criteria = new DepositFee();
        criteria.setSellerId(sellerId);
        return criteria;
    }

    /**
     * 查询商家的保证金列表
     * 由组件调用
     *
     * @param pageNo    页码
     * @param size      每页显示
     * @param user      用户
     * @return 基础费用单
     */
    @Override
    public Response<Paging<DepositFee>> findBaseDetailByName(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                             @ParamInfo("size") @Nullable Integer size,
                                                             @ParamInfo("baseUser") BaseUser user) {
        Response<Paging<DepositFee>> result = new Response<Paging<DepositFee>>();
        try {
            checkState(isSeller(user), "user.type.incorrect");
            DepositFee criteria = sellerAuthorizedCriteria(user);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            Paging<DepositFee> paging = depositFeeDao.findDepositFeeBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (IllegalStateException e) {
            log.warn("fail to query deposit fee with user: {}, error:{} ", user, e.getMessage());
            result.setResult(Paging.empty(DepositFee.class));
        } catch (Exception e) {
            log.error("fail to query deposit fee with user: {}, cause:{} ", user, Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(DepositFee.class));
        }
        return result;
    }

    /**
     * 获取商家的查询范围
     */
    private DepositFee sellerAuthorizedCriteria(BaseUser user) {
        DepositFee criteria = new DepositFee();
        criteria.setSellerId(user.getId());
        return criteria;
    }

    /**
     *
     * @param id  保证金id
     * @return 保证金信息
     */
    @Override
    public Response<DepositFee> findDepositDetailByID(Long id) {
        Response<DepositFee> result = new Response<DepositFee>();
        try {
            checkArgument(notNull(id), "deposit.fee.id.null");
            DepositFee fee = depositFeeDao.get(id);
            checkState(notNull(fee), "deposit.fee.not.found");

            result.setResult(fee);

        } catch (IllegalArgumentException e) {
            log.warn("fail to find deposit detail with id:{}, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("fail to find deposit detail with id:{}, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to find deposit detail with id:{}, error:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("deposit.find.detail.fail");
        }

        return result;
    }

    /**
     * 统计卖家已缴纳的技术服务费
     *
     * @param user 用户信息
     * @return 技术服务费汇总
     */
    @Override
    public Response<TechFeeSummaryDto> summaryOfTechFee(@ParamInfo("baseUser") BaseUser user) {
        Response<TechFeeSummaryDto> result = new Response<TechFeeSummaryDto>();

        try {
            checkState(isSeller(user), "user.type.incorrect");
            Long summary = depositFeeDao.summaryTechFeeOfSeller(user.getId());

            TechFeeSummaryDto dto = new TechFeeSummaryDto();
            dto.setTechFeeActual(summary);
            result.setResult(dto);

        } catch (IllegalStateException e) {
            log.warn("fail to summary techFee with user:{}, error:{}", user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to summary techFee with user:{}, cause:{}", user, Throwables.getStackTraceAsString(e));
            result.setError("tech.fee.summary.fail");
        }

        return result;
    }

}
