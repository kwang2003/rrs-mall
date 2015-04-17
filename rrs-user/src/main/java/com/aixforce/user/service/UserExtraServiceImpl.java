package com.aixforce.user.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.manager.UserExtraManager;
import com.aixforce.user.model.UserExtra;
import com.aixforce.user.mysql.UserExtraDao;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yangzefeng on 14-3-4
 */
@Service
@Slf4j
public class UserExtraServiceImpl implements UserExtraService{
    @Autowired
    private UserExtraDao userExtraDao;
    @Autowired
    private UserExtraManager userExtraManager;

    @Override
    public Response<UserExtra> findById(Long id) {
        Response<UserExtra> result = new Response<UserExtra>();
        if(id == null) {
            log.error("id can not be null when find userExtra by id");
            result.setError("illegal.param");
            return result;
        }
        try {
            UserExtra userExtra = userExtraDao.findById(id);
            result.setResult(userExtra);
            return result;
        }catch (Exception e) {
            log.error("failed to find userExtra by id={}, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            result.setError("userExtra.query.fail");
            return result;
        }
    }

    @Override
    public Response<UserExtra> findByUserId(Long userId) {
        Response<UserExtra> result = new Response<UserExtra>();
        if(userId == null) {
            log.error("userId can not be null when find userExtra by userId");
            result.setError("illegal.param");
            return result;
        }
        try {
            UserExtra userExtra = userExtraDao.findByUserId(userId);

            if (userExtra==null) {
                userExtra = new UserExtra();
                userExtra.setUserId(userId);
                userExtra.setTradeSum(0l);
                userExtra.setTradeQuantity(0);
                userExtraDao.create(userExtra);
            }

            result.setResult(userExtra);
            return result;
        }catch (Exception e) {
            log.error("failed to find userExtra by userId={}, cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            result.setError("userExtra.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> create(UserExtra userExtra) {
        Response<Long> result = new Response<Long>();
        if(userExtra == null) {
            log.error("userExtra can not be null when create");
            result.setError("illegal.param");
            return result;
        }
        try {
            userExtraDao.create(userExtra);
            result.setResult(userExtra.getId());
            return result;
        }catch (Exception e) {
            log.error("failed to create userExtra{}, cause:{}",
                    userExtra, Throwables.getStackTraceAsString(e));
            result.setError("userExtra.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateByUserId(UserExtra userExtra) {
        Response<Boolean> result = new Response<Boolean>();
        if(userExtra.getUserId() == null) {
            log.error("userId can not be null when update userExtra");
            result.setError("illegal.param");
            return result;
        }
        try {
            userExtraDao.update(userExtra);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("failed to update userExtra{} by userId, cause:{}",
                    userExtra, Throwables.getStackTraceAsString(e));
            result.setError("userExtra.update.fail");
            return result;
        }
    }

    @Override
    public Response<Long> increaseUserTradeInfo(Long buyerId, Long fee) {
        Response<Long> result = new Response<Long>();

        try {
            UserExtra extra = userExtraDao.findByUserId(buyerId);
            if (extra == null) {
                extra = new UserExtra();
                extra.setUserId(buyerId);
                extra.setTradeQuantity(1);
                extra.setTradeSum(fee);
                userExtraDao.create(extra);
                result.setResult(extra.getId());
                return result;
            }
            extra.increaseTradeSum(fee);
            userExtraDao.update(extra);
            result.setResult(extra.getId());
            return result;
        } catch (Exception e) {
            log.error("method 'increaseUserTradeInfo' create or update record fail", e);
            result.setError("userExtra.increase.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> bulkInsertOrReplace(List<UserExtra> userExtras) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            userExtraManager.bulkInsertOrReplace(userExtras);
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`bulkInsertOrReplace` invoke fail. e:{}", e);
            result.setError("userExtra.bulk.insert.or.replace.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> bulkInsertOrUpdate(List<UserExtra> userExtras) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            userExtraManager.bulkInsertOrUpdate(userExtras);
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`bulkInsertOrUpdate` invoke fail. e:{}", e);
            result.setError("userExtra.bulk.insert.or.update.fail");
            return result;
        }
    }
}
