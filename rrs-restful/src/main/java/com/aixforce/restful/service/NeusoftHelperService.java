package com.aixforce.restful.service;

import com.aixforce.common.model.Response;
import com.aixforce.redis.utils.JedisTemplate;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.regex.Pattern;

/**
 * Date: 4/21/14
 * Time: 16:37
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Service
@Slf4j
public class NeusoftHelperService {

    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");

    @Autowired
    JedisTemplate jedisTemplate;

    public Response<Boolean> checkMobileSendable(final String mobile) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (Strings.isNullOrEmpty(mobile) || !mobilePattern.matcher(mobile).matches()) {
                result.setResult(false);
                return result;
            }

            Long alive = jedisTemplate.execute(new JedisTemplate.JedisAction<Long>() {
                @Override
                public Long action(Jedis jedis) {
                    return jedis.ttl("sms:code:"+mobile);
                }
            });

            if (alive>=0) {
                result.setError("sms.mobile.too.frequently");
                return result;
            }

            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`checkMobileSendable` invoke fail. can't access mobile ttl, mobile:{}", mobile, e);
            result.setError("sms.check.mobile.available.fail");
            return result;
        }

    }

    /**
     *
     * @param mobile
     * @return
     */
    public Response<Boolean> setMobileSent(final String mobile, final String code) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (Strings.isNullOrEmpty(mobile) || !mobilePattern.matcher(mobile).matches()) {
                result.setResult(false);
                return result;
            }

            jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    jedis.del("sms:code:"+mobile);

                    jedis.set("sms:code:"+mobile, code, "NX", "EX", 60);
                }
            });

            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`setMobileSent` invoke fail. can't set mobile expired, mobile:{}, e:{}", mobile, e);
            result.setError("sms.set.mobile.sent");
            return result;
        }
    }
}
