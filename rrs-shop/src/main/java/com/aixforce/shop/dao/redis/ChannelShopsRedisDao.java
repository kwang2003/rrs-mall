package com.aixforce.shop.dao.redis;

import com.aixforce.common.model.Response;
import com.aixforce.redis.utils.JedisTemplate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jack.yang on 14-8-11.
 */
@Slf4j
@Repository
public class ChannelShopsRedisDao {

    private static final String OPEN_API_CHANNEL_SECRET = "open:api:channel:";

    private static final String OPEN_API_SMS = "open:api:sms:";

    private final JedisTemplate template;

    private static final Splitter splitter = Splitter.on(",");

    private static final Pattern mobilePattern = Pattern.compile("^((13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");

    @Autowired
    public ChannelShopsRedisDao(JedisTemplate template) {
        this.template = template;
    }

    public List<Long> getShops(final String channel) {
        List<Long> longList = Lists.newArrayList();
        String shopIds = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+channel).get("shopIds");
            }
        });
        Iterator<String> iterator = splitter.split(shopIds).iterator();
        while (iterator.hasNext()) {
            longList.add(Long.parseLong(iterator.next()));
        }
        return longList;
    }

    public String getKey(final String channel) {
        String result =  template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+channel).get("key");
            }
        });
        return result;
    }

    public Long getRole1(final String channel) {
        String result =  template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+channel).get("role1");
            }
        });
        return Long.parseLong(result);
    }

    public void setPermanentKey(final HashMap<String, String> hashMap, final String channel) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(OPEN_API_CHANNEL_SECRET+channel, hashMap);
            }
        });
    }

    public List<Long> getUserIds(final String channel) {
        List<Long> longList = Lists.newArrayList();
        String userIds = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+channel).get("userIds");
            }
        });
        Iterator<String> iterator = splitter.split(userIds).iterator();
        while (iterator.hasNext()) {
            longList.add(Long.parseLong(iterator.next()));
        }
        return longList;
    }

    public List<Long> getAuthRoles(final String method) {
        List<Long> longList = Lists.newArrayList();
        String authRoles = template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+method).get("roles");
            }
        });
        if (!Strings.isNullOrEmpty(authRoles)) {
            Iterator<String> iterator = splitter.split(authRoles).iterator();
            while (iterator.hasNext()) {
                longList.add(Long.parseLong(iterator.next()));
            }
        }
        return longList;
    }

    public void setAuthRoles(final HashMap<String, String> hashMap, final String method) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(OPEN_API_CHANNEL_SECRET+method, hashMap);
            }
        });
    }

    public Response<Boolean> checkMobileSendable(final String mobile) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (Strings.isNullOrEmpty(mobile) || !mobilePattern.matcher(mobile).matches()) {
                result.setResult(false);
                return result;
            }

            Long alive = template.execute(new JedisTemplate.JedisAction<Long>() {
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
    public Response<Boolean> setMobileSent(final String mobile, final String code, final Long id) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (Strings.isNullOrEmpty(mobile) || !mobilePattern.matcher(mobile).matches()) {
                result.setResult(false);
                return result;
            }

            template.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    jedis.del("sms:code:"+mobile);

                    jedis.set("sms:code:"+mobile, code, "NX", "EX", 60);
                }
            });

            // 保留半个小时，用于做code的验证（一部分业务需要，例如：通过手机号验证码来登录系统）
            template.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    jedis.del("sms:code:validate:"+mobile+":"+code+":"+id);

                    jedis.set("sms:code:validate:"+mobile+":"+code+":"+id, code, "NX", "EX", 1800);
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

    public String getSms(final Long id) {
        return  template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_SMS+id).get("content");
            }
        });
    }

    public void setSms(final HashMap<String, String> hashMap, final Long id) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.hmset(OPEN_API_SMS+id, hashMap);
            }
        });
    }

    public String getBusinessIds(final String channel) {
        return template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.hgetAll(OPEN_API_CHANNEL_SECRET+channel).get("business_id");
            }
        });

    }

    public Response<Boolean> validateMobileCode(final String mobile, final String code) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            if (Strings.isNullOrEmpty(mobile) || !mobilePattern.matcher(mobile).matches()) {
                result.setResult(false);
                return result;
            }

            Long alive = template.execute(new JedisTemplate.JedisAction<Long>() {
                @Override
                public Long action(Jedis jedis) {
                    return jedis.ttl("sms:code:validate:"+mobile+":"+code+":0");
                }
            });

            if (alive<=0) {
                result.setError("sms.code.not.match");
                return result;
            }

            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("`checkMobileSendable` invoke fail. can't access mobile ttl, mobile:{}", mobile, e);
            result.setError("sms.validate.code.fail");
            return result;
        }

    }
}
