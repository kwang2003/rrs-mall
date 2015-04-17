package com.aix.open.test.shop;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by yangjingang on 14-8-7.
 */
public class ShopInfoTest {
    public static void main(String... args) {

        String channel = "third_tm";
        String outerCode ="";
        String shopId = "";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        String loginId = "13840851784";//15165326589:店铺账号
        String password = "738148";
        String type = "2";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("shopId", shopId);
        map.put("outerCode", outerCode);
        map.put("loginId", loginId);
        map.put("password", password);
        map.put("type", type);
        String sign = Sign.buildParams(map, key);
        //String response = HttpRequest
        //        .post("http://console.goodaysh.com/api/shop/info").send("channel="+channel+"&shopId="+shopId+"&outerCode="+outerCode+"&sign=" + key).body();
        String response = HttpRequest
               .post("http://console.goodaysh.com/api/open/shop/info", true,
                       "channel", channel,
                       "shopId", shopId,
                       "loginId", loginId,
                       "password", password,
                       "type", type,
                       "outerCode", outerCode,
                       "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
