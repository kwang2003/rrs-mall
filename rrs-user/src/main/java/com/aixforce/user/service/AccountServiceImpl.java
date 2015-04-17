/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.NameValidator;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.dto.BuyerDto;
import com.aixforce.user.endpoint.HaierUserClient;
import com.aixforce.user.manager.AccountManager;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.mysql.UserDao;
import com.aixforce.user.mysql.UserProfileDao;
import com.aixforce.user.util.RSAToBCDCoder;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.google.common.base.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.user.util.UserVerification.active;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 安全相关实体的管理类,包括用户和权限组.
 *
 * @author jlchen
 */
@Service
public class AccountServiceImpl implements AccountService<User> {

    private final static Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final static HashFunction sha512 = Hashing.sha512();

    private final static Splitter splitter = Splitter.on('@').trimResults();

    private final static Joiner joiner = Joiner.on('@').skipNulls();

    private final static HashFunction md5 = Hashing.md5();

    private final LoadingCache<Long, User> userCache;

    @Autowired
    private Validator validator;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserProfileDao userProfileDao;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private HaierUserClient haierUserClient;


    private List<User> EMPTY_USER_LIST = Collections.emptyList();

    public AccountServiceImpl() {
        userCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Long, User>() {
            @Override
            public User load(Long id) throws Exception {
                return userDao.findById(id);
            }
        });
    }

    /**
     * 从数据库中load用户,按照id逆序排列
     *
     * @param status 用户状态
     * @param pageNo 页码
     * @param count  每页返回条数
     * @return 分页结果
     */
    @Override
    public Response<Paging<User>> list(Integer status, Integer pageNo, Integer count) {
        Response<Paging<User>> result = new Response<Paging<User>>();
        if (status == null) {
            log.error("status can not be null when list user");
            result.setError("status.null");
            return result;
        }
        pageNo = firstNonNull(pageNo, 1);
        count = firstNonNull(count, 20);
        pageNo = pageNo > 0 ? pageNo : 1;
        count = count > 0 ? count : 20;
        int offset = (pageNo - 1) * count;

        try {
            Paging<User> users = userDao.findUsers(status, offset, count);
            result.setResult(users);
            return result;
        } catch (Exception e) {
            log.error("failed to list user where status={},pageNo={},count={},cause:{} ",
                    status, pageNo, count, Throwables.getStackTraceAsString(e));
            result.setError("user.query.error");
            return result;
        }
    }

    /**
     * 根据id寻找user
     *
     * @param id 用户id
     * @return 用户对象
     */
    @Override
    public Response<User> findUserById(Long id) {
        Response<User> result = new Response<User>();
        if (id == null) {
            log.error("user id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            User user = userCache.getUnchecked(id);
            if (user == null) {
                log.error("failed to find user where id={}", id);
                result.setError("user.not.found");
                return result;
            }
            result.setResult(user);

            return result;
        } catch (Exception e) {
            log.error("failed to find user where id={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("user.query.fail");
            return result;
        }
    }


    /**
     * 创建User对象
     *
     * @param user 用户
     * @return 新创建的id
     */
    @Override
    public Response<Long> createUser(User user) {
        Response<Long> result = new Response<Long>();
        if (Strings.isNullOrEmpty(user.getName())) {
            log.error("user name can not be null");
            result.setError("user.name.empty");
            return result;
        }
        if (!NameValidator.validate(user.getName())) {
            log.error("用户名称只能由字母,数字和下划线组成,but got {}", user.getName());
            result.setError("user.illegal.name");
            return result;
        }
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            log.error("failed to create user {},cause:{}", user, sb.toString());
            result.setError("illegal.param");
            return result;

        }
        try {
            User existed = userDao.findByName(user.getName());
            if (existed != null) {
                log.error("failed to create user {},cause: duplicated name ", user.getName());
                result.setError("user.name.duplicated");
                return result;
            }
            if (!Strings.isNullOrEmpty(user.getEmail())) {
                existed = userDao.findByEmail(user.getEmail());
                if (existed != null) {
                    log.error("failed to create user {},cause: duplicated email ", user.getEmail());
                    result.setError("user.email.duplicated");
                    return result;
                }
            }
            if (!Strings.isNullOrEmpty(user.getMobile())) {
                existed = userDao.findByMobile(user.getMobile());
                if (existed != null) {
                    log.error("failed to create user {},cause: duplicated mobile ", user.getMobile());
                    result.setError("user.mobile.duplicated");
                    return result;
                }
            }

            user.setEncryptedPassword(encryptPassword(user.getEncryptedPassword()));//encrypted password
            userDao.create(user);
            result.setResult(user.getId());
            return result;
        } catch (Exception e) {
            log.error("failed to create new user {}, cause:{} ", user, Throwables.getStackTraceAsString(e));
            result.setError("user.create.fail");
            return result;
        }
    }

    /**
     * 更新用户对象
     *
     * @param user 用户
     */
    @Override
    public Response<Boolean> updateUser(User user) {
        Response<Boolean> result = new Response<Boolean>();

        if (user.getId() == null) {
            log.error("user id can not be null when updated");
            result.setError("user.null.fail");
            return result;
        }

        if (!Strings.isNullOrEmpty(user.getEncryptedPassword())) {
            user.setEncryptedPassword(encryptPassword(user.getEncryptedPassword()));//encrypted password
        }

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                sb.append(violation.getMessage()).append("\n");
            }
            log.error("failed to update user {},cause:{}", user, sb.toString());
            result.setError("illegal.param");
            return result;

        }
        try {
            userDao.update(user);
            userCache.invalidate(user.getId());
            result.setResult(Boolean.TRUE);
            return result;
        }catch (DuplicateKeyException e){
            log.error("failed to updated user {} for user {},cause:{}", user, e.getMessage());
            result.setError("user.login.id.duplicated");
            return result;
        } catch (Exception e) {
            log.error("failed to updated user {},cause:{}", user, Throwables.getStackTraceAsString(e));
            result.setError("user.update.fail");
            return result;
        }
    }

    public Response<User> userLogin(String id, LoginType type, String password) {
        Response<User> result = new Response<User>();

        try {
            checkArgument(notNull(id), "login.id.empty");
            checkArgument(notNull(type), "login.type.can.not.be.empty");
            checkArgument(notNull(password), "login.password.can.not.be.empty");

            User user = queryUserBy(id, type);
            checkState(notNull(user), "user.not.found");
            checkState(active(user), "user.account.locked");

            String storedPassword = user.getEncryptedPassword();

            if (isEmpty(storedPassword)) {   // 密码为空则需要校验海尔旧有系统的用户权限
                Response<Boolean> validateResult = checkUserTokenOfElder(id, password);
                checkState(validateResult.isSuccess(), validateResult.getError());
                updateUserPassword(user, password);
            } else {   // 密码不为空则走正常的登录流程
                checkState(passwordMatch(password, storedPassword), "user.password.incorrect");
            }
            result.setResult(user);

        } catch (IllegalArgumentException e) {
            log.error("fail to login with id:{} type:{}, error:{}", id, type, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to login with id:{} type:{}, error:{}", id, type, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to login with id:{} type:{}, cause:{}", id, type, Throwables.getStackTraceAsString(e));
            result.setError("user.login.fail");
        }

        return result;
    }

    /**
     * 校验原有系统的令牌
     */
    private Response<Boolean> checkUserTokenOfElder(String id, String password) {
        return haierUserClient.check(id, password);
    }

    /**
     * 更新用户密码,并使用户缓存失效
     */
    private void updateUserPassword(User user, String password) {
        String newPassword = encryptPassword(password); // 生成一个新的pass
        User updated = new User();
        updated.setId(user.getId());
        updated.setEncryptedPassword(newPassword);
        userDao.update(updated);
        userCache.invalidate(user.getId());
    }


    /**
     * 用户登陆
     *
     * @param id       id
     * @param type     登陆类型
     * @param password 密码
     * @return 用户
     */
    @Override
    public Response<User> login(String id, LoginType type, String password) {

        Response<User> result = new Response<User>();
        if (Strings.isNullOrEmpty(id)) {
            log.error("id can not be empty");
            result.setError("login.id.empty");
            return result;
        }
        User user;
        try {
            user = queryUserBy(id, type);
        } catch (Exception e) {
            log.error("failed to find user where login id ={} and login type={},cause:{}",
                    id, type, Throwables.getStackTraceAsString(e));
            result.setError("user.query.fail");
            return result;
        }


        if (user == null) {
            log.error("user not found  where login id ={} and login type={}", id, type);
            result.setError("user.not.found");
            return result;
        }
        if (Objects.equal(User.STATUS.LOCKED.toNumber(), user.getStatus())
                || Objects.equal(User.STATUS.FROZEN.toNumber(), user.getStatus())) {
            log.error("{} is locked", user);
            result.setError("user.account.locked");
            return result;
        }
        String storedPassword = user.getEncryptedPassword();
        if (passwordMatch(password, storedPassword)) {
            result.setResult(user);
            return result;
        } else {
            log.error("failed to login user  where login id ={} and login type={},cause:password mismatch ", id, type);
            result.setError("user.password.incorrect");
            return result;
        }
    }

    private boolean passwordMatch(String password, String encryptedPassword) {
        Iterable<String> parts = splitter.split(encryptedPassword);
        String salt = Iterables.get(parts, 0);
        String realPassword = Iterables.get(parts, 1);
        return Objects.equal(sha512.hashUnencodedChars(password + salt).toString().substring(0, 20), realPassword);
    }

    private String encryptPassword(String password) {
        String salt = md5.newHasher().putUnencodedChars(UUID.randomUUID().toString()).putLong(System.currentTimeMillis()).hash()
                .toString().substring(0, 4);
        String realPassword = sha512.hashUnencodedChars(password + salt).toString().substring(0, 20);
        return joiner.join(salt, realPassword);
    }


    /**
     * 修改手机号码
     *
     * @param userId   用户Id
     * @param mobile   新的手机号码
     * @param password 用户密码
     */
    @Override
    public Response<Boolean> changeMobile(Long userId, String mobile, String password) {

        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("id can not be null");
            result.setError("id.not.null");
            return result;
        }
        if (Strings.isNullOrEmpty(mobile)) {
            log.error("mobile can not be empty");
            result.setError("user.mobile.empty");
            return result;
        }


        try {
            User user = userDao.findById(userId);
            if (user == null) {
                log.error("can not find user whose id={}", userId);
                result.setError("user.not.found");
                return result;
            }
            if (!passwordMatch(password, user.getEncryptedPassword())) {
                log.error("password not match for user(id={})", userId);
                result.setError("user.password.incorrect");
                return result;
            }
            User updated = new User();
            updated.setId(userId);
            updated.setMobile(mobile);
            userDao.update(updated);
            userCache.invalidate(userId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (DuplicateKeyException e){
            log.error("failed to change mobile for user(id={}),cause:{}", userId, e.getMessage());
            result.setError("user.mobile.duplicated");
            return result;
        }catch (Exception e) {
            log.error("failed to change mobile for user(id={}),cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("user.change.mobile.fail");
            return result;
        }
    }

    @Override
    public Response<Paging<BuyerDto>> listMembers(@ParamInfo("params") Map<String, String> params,
                                                  @ParamInfo("pageNo") Integer pageNo,
                                                  @ParamInfo("size") Integer size) {
        Response<Paging<BuyerDto>> result = new Response<Paging<BuyerDto>>();
        Response<Paging<User>> userR = findUser(params, pageNo, size);
        if(!userR.isSuccess()) {
            log.error("failed to find user, error code:{}", userR.getError());
            result.setError(userR.getError());
            return result;
        }
        Paging<User> userP = userR.getResult();
        try {
            List<BuyerDto> buyerDtos = transUserToBuyerDto(userP.getData());
            result.setResult(new Paging<BuyerDto>(userP.getTotal(), buyerDtos));
            return result;
        } catch (Exception e) {
            log.error("user find fail", e);
            result.setError("user.not.found");
            return result;
        }
    }

    @Override
    public Response<Paging<BuyerDto>> listAllMembers(@ParamInfo("params") Map<String, String> params,
                                                     @ParamInfo("pageNo") Integer pageNo,
                                                     @ParamInfo("size") Integer size) {
        Response<Paging<BuyerDto>> result = new Response<Paging<BuyerDto>>();

        Paging<User> userP;
        String userName = params.get("userName");
        if (!Strings.isNullOrEmpty(userName)) {
            User user = userDao.findByName(userName);
            userP = new Paging<User>(1l, Lists.newArrayList(user));
        } else {
            PageInfo pageInfo = new PageInfo(pageNo, size);
            userP = userDao.paginationAll(pageInfo.getOffset(), pageInfo.getLimit());
        }

        try {
            List<BuyerDto> buyerDtos = transUserToBuyerDto(userP.getData());
            result.setResult(new Paging<BuyerDto>(userP.getTotal(), buyerDtos));
            return result;
        } catch (Exception e) {
            log.error("user find fail", e);
            result.setError("user.not.found");
            return result;
        }
    }

    @Override
    public Response<Paging<User>> findUser(Map<String, String> params, Integer pageNo, Integer size) {
        Response<Paging<User>> result = new Response<Paging<User>>();
        PageInfo pageInfo = new PageInfo(pageNo,size);
        try {
            String userName = params.get("userName");
            if (!Strings.isNullOrEmpty(userName)) {
                User user = userDao.findByName(userName);
                result.setResult(new Paging<User>(1l, Lists.newArrayList(user)));
                return result;
            }
            //分页查找所有users表中的数据
            Paging<User> userP = userDao.paginationAll(pageInfo.getOffset(), pageInfo.getLimit());
            result.setResult(userP);
            return result;
        }catch (Exception e) {
            log.error("user find fail", e);
            result.setError("user.not.found");
            return result;
        }
    }


    /**
     * 分页获取所有商户
     *
     * @param pageNo 页数
     * @param size   每页数据数
     * @return 分页数据
     */
    @Override
    public Response<Paging<User>> listSellers(Integer pageNo, Integer size) {
        Response<Paging<User>> result = new Response<Paging<User>>();
        pageNo = firstNonNull(pageNo, 1);
        size = firstNonNull(size, 20);
        pageNo = pageNo > 0 ? pageNo : 1;
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;

        try {
            Paging<User> paging = userDao
                    .findByTypes(offset, size, Lists.newArrayList(User.TYPE.SELLER.toNumber()));
            result.setResult(paging);
            return result;
        } catch (Exception e) {
            log.error("fail to invoke method 'listSellers'", e);
            result.setError("user.seller.query.fail");
            return result;
        }
    }

    /**
     * 分页获取所有代理商
     *
     * @param name      账户名称，选填
     * @param pageNo    页数
     * @param size      每页大小
     * @return  满足条件的分页数据
     */
    @Override
    public Response<Paging<User>> listAgents(@ParamInfo("name") @Nullable String name,
                                   @ParamInfo("pageNo")Integer pageNo,
                                   @ParamInfo("size")Integer size) {
        Response<Paging<User>> result = new Response<Paging<User>>();


        try {
            Paging<User> paging;

            if (!Strings.isNullOrEmpty(name)) {
                paging = getSingleUserPaging(name);
            } else {
                paging = getAgentPaging(pageNo, size);
            }
            result.setResult(paging);
        } catch (Exception e) {
            log.error("fail to query agents", e);
            Paging<User> paging = emptyUserPaging();
            result.setResult(paging);
        }

        return result;
    }

    private Paging<User> emptyUserPaging() {
        return new Paging<User>(0L, EMPTY_USER_LIST);
    }

    private Paging<User> getSingleUserPaging(String name) {
        User user = userDao.findByName(name);
        Paging<User> paging;

        if (user != null) {
            List<User> users = Lists.newArrayList(user);
            paging = new Paging<User>(1L, users);
        } else {
            paging = emptyUserPaging();
        }
        return paging;
    }

    private Paging<User> getAgentPaging(Integer pageNo, Integer size) {
        pageNo = firstNonNull(pageNo, 1);
        size = firstNonNull(size, 20);
        pageNo = pageNo > 0 ? pageNo : 1;
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;

        return userDao
                .findByTypes(offset, size, Lists.newArrayList(User.TYPE.AGENT.toNumber()));
    }


    private List<BuyerDto> transUserToBuyerDto(List<User> users) {
        List<BuyerDto> buyerDtos = Lists.newArrayListWithCapacity(users.size());
        List<UserProfile> userProfiles = userProfileDao.findByUserIds(Lists.transform(users, new Function<User, Long>() {
            @Override
            public Long apply(User user) {
                return user.getId();
            }
        }));
        for(User user : users) {
            BuyerDto buyerDto = new BuyerDto();
            for(UserProfile userProfile : userProfiles) {
                if(Objects.equal(user.getId(), userProfile.getUserId())) {
                    BeanMapper.copy(userProfile, buyerDto);
                    break;
                }
            }
            BeanMapper.copy(user, buyerDto);
            buyerDtos.add(buyerDto);
        }
        return buyerDtos;
    }

    @Override
    public Response<Boolean> updateStatusByIds(List<Long> ids, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        if (ids == null || ids.size() == 0) {
            log.error("ids can not be null or empty");
            result.setError("ids.not.found");
            return result;
        }
        if (status == null) {
            log.error("status can not be null");
            result.setError("status.not.found");
            return result;
        }
        try {
            userDao.batchUpdateStatus(ids, status);
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("update seller status fail sellerIds:{} cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setError("user.update.fail");
            return result;
        }
    }

    /**
     * 更改密码
     *
     * @param userId      user id
     * @param oldPassword 老密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    @Override
    public Response<Boolean> changePassword(Long userId, String oldPassword, String newPassword) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        if (Strings.isNullOrEmpty(oldPassword) || Strings.isNullOrEmpty(newPassword)) {
            log.error("password should be provided");
            result.setError("user.password.empty");
            return result;
        }


        User updated = new User();
        updated.setId(userId);
        updated.setEncryptedPassword(encryptPassword(newPassword));
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                log.error("can not find user whose id={}", userId);
                result.setError("user.not.found");
                return result;
            }
            if (!passwordMatch(oldPassword, user.getEncryptedPassword())) {
                log.error("password not match for user(id={})", userId);
                result.setError("user.password.incorrect");
                return result;
            }
            userDao.update(updated);
            userCache.invalidate(userId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to change password for user (id={}),cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("user.change.password.fail");
            return result;
        }
    }

    /**
     * 重置密码
     *
     * @param userId   用户id
     * @param password 加密前的密码
     * @return 是否重置成功
     */
    @Override
    public Response<Boolean> resetPassword(Long userId, String password) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        if (Strings.isNullOrEmpty(password)) {
            log.error("password can not be null");
            result.setError("user.password.empty");
            return result;
        }

        try {
            User user = userDao.findById(userId);
            if (user == null) {
                log.error("can not find user where id={}", userId);
                result.setError("user.not.found");
                return result;
            }

            User updated = new User();
            updated.setId(user.getId());
            updated.setEncryptedPassword(encryptPassword(password));
            userDao.update(updated);
            userCache.invalidate(userId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to change password for user (id={}),cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            result.setError("user.reset.password.fail");
            return result;
        }
    }


    /**
     * 删除用户,如果尝试删除超级管理员将抛出异常.
     *
     * @param userId 用户id
     * @return 是否删除成功
     */
    @Override
    public Response<Boolean> deleteUser(Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        if (isSupervisor(userId)) {
            log.error("admin can not be deleted");
            result.setError("user.delete.fail");
            return result;
        }
        try {
            userDao.delete(userId);
            userCache.invalidate(userId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete user(id={}),cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("user.delete.fail");
            return result;
        }
    }

    /**
     * 判断是否超级管理员.
     */
    private boolean isSupervisor(Long id) {
        User user = userDao.findById(id);
        return user != null && user.getTypeEnum() == BaseUser.TYPE.ADMIN;
    }

    /**
     * 根据loginId查找用户,如果用户不存在也会报错
     *
     * @param loginId 登陆id
     * @param type    id类型,如电子邮件,手机号码,昵称等
     * @return 用户
     */
    @Override
    public Response<User> findUserBy(String loginId, LoginType type) {
        Response<User> result = new Response<User>();
        if (Strings.isNullOrEmpty(loginId)) {
            log.error("loginId can not be null");
            result.setError("login.id.empty");
            return result;
        }

        try {
            User user = queryUserBy(loginId, type);

            if (user == null) {
                log.error("user not found  where login id ={} and login type={}", loginId, type);
                result.setError("user.not.found");
                return result;
            }
            result.setResult(user);
            return result;

        } catch (Exception e) {
            log.error("failed to find user where login id ={} and login type={},cause:{}",
                    loginId, type, Throwables.getStackTraceAsString(e));
            result.setError("user.query.fail");
            return result;
        }
    }

    /**
     * 检查用户是否存在
     *
     * @param loginId   登陆id
     * @param loginType id类型,如电子邮件,手机号码,昵称等
     * @return 是否存在
     */
    @Override
    public Response<Boolean> userExists(String loginId, LoginType loginType) {
        Response<Boolean> result = new Response<Boolean>();
        if (Strings.isNullOrEmpty(loginId)) {
            log.error("loginId can not be null");
            result.setError("login.id.empty");
            return result;
        }

        try {
            User user = queryUserBy(loginId, loginType);


            result.setResult(user != null);
            return result;

        } catch (Exception e) {
            log.error("failed to check user exists where login id ={} and login type={},cause:{}",
                    loginId, loginType, Throwables.getStackTraceAsString(e));
            result.setError("user.query.fail");
            return result;
        }
    }

    private User queryUserBy(String loginId, LoginType type) {
        switch (type) {
            case EMAIL:
                return userDao.findByEmail(loginId);
            case MOBILE:
                return userDao.findByMobile(loginId);
            case NAME:
                return userDao.findByName(loginId);
            default:
                log.error("unknown login type:{}", type);
                throw new IllegalArgumentException("unknown login type");
        }
    }


    /**
     * 根据手机号获取用户信息
     *
     * @param mobile  手机号
     * @return  用户信息
     */
    public Response<User> findUserByMobile(String mobile) {
        Response<User> result = new Response<User>();

        if (Strings.isNullOrEmpty(mobile)) {
            log.error("method 'findUserByMobile' args 'mobile' cannot be null");
            result.setError("user.query.mobile.null");
            return result;
        }

        User user = userDao.findByMobile(mobile);
        result.setResult(user);
        return result;
    }

    @Override
    public Response<List<User>> findByIds(List<Long> ids) {
        Response<List<User>> result = new Response<List<User>>();
        if(ids == null) {
            log.error("method 'findByIds' args 'ids' can not be null");
            result.setError("illegal.params");
            return result;
        }
        try {
            List<User> users = userDao.findByIds(ids);
            result.setResult(users);
            return result;
        }catch (Exception e){
            log.error("failed to find users by ids{},cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setError("user.query.fail");
            return result;
        }
    }

    /**
     * 更新用户的状态
     *
     * @param id     用户id
     * @param status 更新后的状态
     */
    @Override
    public Response<Boolean> updateStatus(Long id, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            checkIfUserExist(id);
            doUpdateUserStatus(id, status);
            userCache.invalidate(id);
            result.setResult(Boolean.TRUE);
            return result;

        } catch (Exception e) {
            log.error("update user status fail with (id:{}, status:{})");
            result.setError("user.status.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> bulkUpdateUserType(List<Long> _ids, Integer type, Integer businessId) {
        Response<Boolean> result = new Response<Boolean>();
        ArrayList<Long> ids = Lists.newArrayList();
        ids.addAll(_ids);
        if (ids==null || ids.size()==0) {
            result.setResult(true);
            return result;
        }

        if (type==null || BaseUser.TYPE.fromNumber(type)==null) {
            log.error("can't update user, not such type:{}", type);
            result.setError("user.type.update.invalid.type");
            return result;
        }

        List<Long> illegel = Lists.newArrayList();
        Response<List<User>> usersGet  = findByIds(ids);
        if (!usersGet.isSuccess()) {
            log.error("`bulkUpdateUserType` invoke fail. can't find any users in id list:{}", ids);
            result.setError(usersGet.getError());
            return result;
        }

        List<User> userR = usersGet.getResult();
        for (Iterator<User> iterator = userR.iterator(); iterator.hasNext();) {
            User user = iterator.next();
            if (businessId!=null && businessId>=0) {
                if(!Objects.equal(user.getTypeEnum(), BaseUser.TYPE.SITE_OWNER)
                   && !Objects.equal(BaseUser.TYPE.fromNumber(type), BaseUser.TYPE.SITE_OWNER)) {
                    // only site owner can change business id
                    iterator.remove();
                } else {
                    // success and remove from list
                    ids.remove(user.getId());
                }
                continue;
            } else {
                if (!userTypeCanCastTo(user.getTypeEnum(), BaseUser.TYPE.fromNumber(type))) {
                    // skip bad record
                    iterator.remove();
                    continue;
                }
                // success and remove from list
                ids.remove(user.getId());
            }
        }
        illegel.addAll(ids);

        if (userR.isEmpty()) {
            result.setResult(true);
        } else {
            illegel.addAll(accountManager.bulkUpdateUserType(userR, type, businessId));
        }

        if (illegel.size() > 0) {
            log.error("id list length and update count mismatch, some id is illegal:{}", illegel);
        }
        result.setResult(true);
        return result;
    }

    private Boolean userTypeCanCastTo(BaseUser.TYPE source, BaseUser.TYPE target) {

        switch (source) {
            case SELLER:
                // 角色为卖家， 只能提升为品牌商，或冻结
                if (!Objects.equal(target, BaseUser.TYPE.WHOLESALER)
                        || Objects.equal(target, BaseUser.TYPE.BUYER)) {
                    return false;
                }
                return true;
            case BUYER:
                // 角色为买家，本鞥提升为卖家和品牌商
                if (Objects.equal(source, BaseUser.TYPE.SELLER)
                        || Objects.equal(source, BaseUser.TYPE.WHOLESALER)) {
                    return false;
                }
                return true;
            case WHOLESALER:
                // 角色为品牌商，不能提权，可以降为卖家
                if (Objects.equal(target, BaseUser.TYPE.SELLER)) {
                    return true;
                }
                return false;
            case SITE_OWNER:
                // 角色为频道运营，可以提升为 ADMIN，或者冻结
                if (Objects.equal(target, BaseUser.TYPE.ADMIN)) {
                    return true;
                }
                return false;
            case FINANCE:
                // 角色为财务，可以提升为 ADMIN，降为买家，或者冻结
                if (Objects.equal(target, BaseUser.TYPE.ADMIN)
                        || Objects.equal(target, BaseUser.TYPE.BUYER)) {
                    return true;
                }
                return false;
            case ADMIN:
                // 角色为admin，只能冻结
                if (Objects.equal(target, BaseUser.TYPE.ADMIN)) {
                    return true;
                }
                return false;
            default:return false;
        }
    }

    private void doUpdateUserStatus(Long id, Integer status) {
        User updated = new User();
        updated.setId(id);
        updated.setStatus(status);
        boolean success = userDao.update(updated);
        checkState(success, "user.update.fail");
    }


    private void checkIfUserExist(Long id) {
        User user = userDao.findById(id);
        checkState(user != null, "user.not.exist");
    }

    @Override
	public Response<Boolean> changeUserInfo(Long userId, String mobile,
			String newPassword) {
		Response<Boolean> result = new Response<Boolean>();
		if (userId == null) {
			log.error("userId can not be null");
			result.setError("userId.not.null.fail");
			return result;
		}

		if (Strings.isNullOrEmpty(mobile)) {
			log.error("mobile should be provided");
			result.setError("user.mobile.empty");
			return result;
		}

		if (Strings.isNullOrEmpty(newPassword)) {
			log.error("password should be provided");
			result.setError("user.password.empty");
			return result;
		}

		User updated = new User();
		updated.setId(userId);
		updated.setMobile(mobile);
		updated.setEncryptedPassword(encryptPassword(newPassword));
		try {
			User user = userDao.findById(userId);
			if (user == null) {
				log.error("can not find user whose id={}", userId);
				result.setError("user.not.found");
				return result;
			}
			userDao.update(updated);
			userCache.invalidate(userId);
			result.setResult(Boolean.TRUE);
			return result;
		} catch (Exception e) {
			log.error("failed to change userInfo for user (id={}),cause:{}",
					userId, Throwables.getStackTraceAsString(e));
			result.setError("user.change.userInfo.fail");
			return result;
		}
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@Override
	public Response<Map> searchUserInfo(String userId_md5str, String targetUrl) {
		Response<Map> result = new Response<Map>();
		  Map<String, String> params = Maps.newHashMapWithExpectedSize(3);
		try {
			String userId = RSAToBCDCoder.decryptByPrivateKey(userId_md5str,
					RSAToBCDCoder.getPrivateKey(RSAToBCDCoder.modulus,
							RSAToBCDCoder.private_exponent));
			if (StringUtils.isEmpty(userId)) {
				result.setResult(null);
				result.setError("userId can not be null");

				return result;
			}

			if (StringUtils.isEmpty(targetUrl)) {
				result.setResult(null);
				result.setError("targetUrl can not be null");

				return result;
			}

			User user = userDao.findById(Long.valueOf(userId));
			if(notNull(user)){
				params.put("userId", userId);
				params.put("name", user.getName());
				params.put("targetUrl", targetUrl);
			}
			result.setResult(params);
			return result;
		} catch (Exception e) {
			result.setResult(null);
			result.setError("searchUserInfo by userId error");
		}
		return result;
	}

}
