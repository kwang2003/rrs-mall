package com.aix.open.test.Logistics;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-9-9.
 */
public class DeliveryUpdateTest {
    public static void main(String... args) {

        String channel = "third_tm";
        String orderId = "third_tm";
        String session = "third_tm";
        String userId = "third_tm";
        String deliverTime = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("orderId", orderId);
        map.put("session", session);
        map.put("userId", userId);
        map.put("deliverTime", deliverTime);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/logistics/delivery/update", true,
                        "channel", channel,
                        "orderId", orderId,
                        "session", session,
                        "userId", userId,
                        "channel", channel,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
