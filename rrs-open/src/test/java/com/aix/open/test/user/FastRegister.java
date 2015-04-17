package com.aix.open.test.user;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-8-7.
 */
public class FastRegister {
    public static void main(String... args) {

        String channel = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        String phone = "13840851784";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("phone", phone);
        String sign = Sign.buildParams(map, key);
        //String response = HttpRequest
        //        .post("http://console.goodaysh.com/api/shop/info").send("channel="+channel+"&shopId="+shopId+"&outerCode="+outerCode+"&sign=" + key).body();
        String response = HttpRequest
               .post("http://console.goodaysh.com/api/extend/user/fast-register", true,
                       "channel", channel,
                       "phone", phone,
                       "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
