package com.aix.open.test.user;

import com.aixforce.common.utils.JsonMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neusoft on 14-8-15.
 */
public class LoginTest {


    private final static JsonMapper jsonMapper = JsonMapper.JSON_NON_DEFAULT_MAPPER;
    private final static JavaType mapType = jsonMapper.createCollectionType(HashMap.class, String.class, String.class);

    public static void main(String... args) {

        String channel = "brand_ygs_1";
        String key = "f26d238e87825b5a8f7e668cfcb08f51";
        String loginId = "13840851784";
        String password = "8889";
        String type = "99";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("loginId", loginId);
        map.put("password", password);
        map.put("type", type);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/user/login", true,
                        "channel", channel,
                        "loginId", loginId,
                        "password", password,
                        "type", type,
                        "sign", sign).body();

        System.out.println(response);
    }
}
