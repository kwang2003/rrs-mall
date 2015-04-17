package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.Arguments;
import com.aixforce.shop.dao.ChannelShopsDao;
import com.aixforce.shop.dao.redis.ChannelShopsRedisDao;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by jack.yang on 14-8-11.
 */
@Slf4j
@Service
public class ChannelShopsServiceImpl implements ChannelShopsService{

    @Autowired
    ChannelShopsRedisDao channelShopsRedisDao;

    @Autowired
    ChannelShopsDao channelShopsDao;

    private static final Splitter splitter = Splitter.on(",");

    @Override
    public Response<List<Long>> findShops(String channel) {

        Response<List<Long>> result = new Response<List<Long>>();

        try {
            List<Long> mapList = channelShopsRedisDao.getShops(channel);
            checkArgument(!mapList.isEmpty(), "fail.to.get.channel.shops!");
            result.setResult(mapList);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }
        return result;
    }

    @Override
    public Response<String> findKey(String channel) {

        Response<String> result = new Response<String>();

        try {
            String key = channelShopsRedisDao.getKey(channel);
            if (Arguments.isNull(key)) {
                List<HashMap> mapList = channelShopsDao.findChannelShops(channel);
                checkArgument(!mapList.isEmpty(), "fail.to.get.channel.info!");
                channelShopsRedisDao.setPermanentKey(mapList.get(0), channel);
                key = channelShopsRedisDao.getKey(channel);
            }
            result.setResult(key);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }

        return result;
    }

    @Override
    public Response<List<Long>> findUserIds(String channel) {

        Response<List<Long>> result = new Response<List<Long>>();

        try {
            List<Long> mapList = channelShopsRedisDao.getUserIds(channel);
            checkArgument(!mapList.isEmpty(), "fail.to.get.channel.userIds!");
            result.setResult(mapList);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }
        return result;
    }

    @Override
    public Response<Boolean> isAuthRole(String method, String channel) {

        Response<Boolean> result = new Response<Boolean>();

        try {
            Boolean isAuthNeed = false;
            List<Long> longList = channelShopsRedisDao.getAuthRoles(method);

            if (longList.isEmpty()) {

                List<HashMap> hashMapList = channelShopsDao.findAuthRoles(method);
                checkArgument(!hashMapList.isEmpty(), "fail.to.get.auth.info!");

                channelShopsRedisDao.setAuthRoles(hashMapList.get(0), method);

                longList = channelShopsRedisDao.getAuthRoles(method);

            }

            Long channelRole = channelShopsRedisDao.getRole1(channel);
            for(Long role:longList) {
                if (Objects.equal(role,channelRole)) {
                    isAuthNeed = true;
                    break;
                }
            }

            result.setResult(isAuthNeed);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }
        return result;
    }

    @Override
    public Response<Boolean> checkMobileSendable(String mobile) {
        return channelShopsRedisDao.checkMobileSendable(mobile);
    }

    @Override
    public Response<Boolean> validateMobileCode(String mobile, String code) {
        return channelShopsRedisDao.validateMobileCode(mobile, code);
    }

    @Override
    public Response<Boolean> setMobileSent(String mobile, String code, Long id) {
        return channelShopsRedisDao.setMobileSent(mobile, code, id);
    }

    @Override
    public Response<String> findSmsMessage(Long id) {
        Response<String> result = new Response<String>();

        try {
            String key = channelShopsRedisDao.getSms(id);
            if (Arguments.isNull(key)) {
                HashMap map = channelShopsDao.findSmsMessage(id);
                checkArgument(!Arguments.isNull(map), "sms.not.exists!");
                channelShopsRedisDao.setSms(map, id);
                key = channelShopsRedisDao.getSms(id);
            }
            result.setResult(key);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }

        return result;
    }

    @Override
    public Response<List<Long>> findBusinessIds(String channel) {
        Response<List<Long>> result = new Response<List<Long>>();

        try {
            List<Long> longList = Lists.newArrayList();
            List<String> stringList = Lists.newArrayList();

            String businessIds = channelShopsRedisDao.getBusinessIds(channel);
            checkArgument(!Arguments.isNull(businessIds), "fail.to.get.business.info!");

            stringList = splitter.splitToList(businessIds);
            for(String str:stringList) {
                longList.add(Long.valueOf(str));
            }

            result.setResult(longList);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }
        return result;
    }

    @Override
    public Response<Long> findRole1(String channel) {
        Response<Long> result = new Response<Long>();

        try {
            Long role = channelShopsRedisDao.getRole1(channel);
            checkArgument(!Arguments.isNull(role), "fail.get.channel.role");
            result.setResult(role);
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setError(e.getMessage());
        }
        return result;
    }


}
