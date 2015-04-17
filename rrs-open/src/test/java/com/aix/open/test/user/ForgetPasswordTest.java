package com.aix.open.test.user;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-8-29.
 */
public class ForgetPasswordTest {

    public static void main(String... argss) {

        String channel = "mobile-site";
        String key = "949d4b77c359db11638d9260f7ae5d9b";
        String mobile = "13840851784";//13061261110  13840851784
        String password = "111111";
        String captcha = "bdc6";
        String session = "f528764dZ4f2cc91cZ1482114f2d2Z84eb";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("mobile", mobile);
        map.put("password", password);
        map.put("captcha", captcha);
        map.put("session", session);

        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/user/forget-password", true,
                        "mobile", mobile,
                        "password", password,
                        "captcha", captcha,
                        "session", session,
                        "channel", channel,
                        "sign", sign).body();
        System.out.println(response);
    }
}
