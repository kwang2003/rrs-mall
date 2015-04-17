package com.aixforce.web.controller.api.validator;

import com.aixforce.redis.utils.JedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import static com.aixforce.common.utils.Arguments.isEmpty;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-01 4:17 PM  <br>
 * Author: xiao
 */
@Component
public class SmsCountValidator {

    @Autowired
    private JedisTemplate template;

    private final static int LIMIT = 3;


    /**
     * 判断某手机号有没有发过短信
     *
     * @param mobile   手机号
     * @return 发送未超过三次
     */
    public boolean check(final String mobile) {

        return template.execute(new JedisTemplate.JedisAction<Boolean>() {
            @Override
            public Boolean action(Jedis jedis) {
                String result =  jedis.get("mobile:" + mobile);
                if (isEmpty(result)) {  // 未找到手机则默认没发送
                    jedis.set("mobile:" + mobile, "1", "NX", "EX", 60 * 60 * 24);
                    return Boolean.TRUE;
                } else {
                    Integer count = Integer.parseInt(result);
                    count ++;
                    if (count > LIMIT) {
                        return Boolean.FALSE;
                    }

                    jedis.set("mobile:" + mobile, count.toString(), "XX", "EX", 60 * 60 * 24);
                    return Boolean.TRUE;
                }
            }
        });
    }





}
