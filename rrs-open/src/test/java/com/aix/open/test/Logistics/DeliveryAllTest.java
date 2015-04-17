package com.aix.open.test.Logistics;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-9-9.
 */
public class DeliveryAllTest {
    public static void main(String... args) {

        String channel = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/logistics/delivery/all", true,
                        "channel", channel,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
