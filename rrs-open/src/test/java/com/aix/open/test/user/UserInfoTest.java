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
public class UserInfoTest {


    private final static JsonMapper jsonMapper = JsonMapper.JSON_NON_DEFAULT_MAPPER;
    private final static JavaType mapType = jsonMapper.createCollectionType(HashMap.class, String.class, String.class);

    public static void main(String... args) {

        String sessionId = "f528764dZ4104193fZ148109fcb9bZ4f22";
        String channel = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        String uid = "6982108";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("sessionId", sessionId);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/user/"+uid, true,
                        "channel", channel,
                        "sessionId", sessionId,
                        "sign", sign).body();
        System.out.println(response);
    }
}
