package com.aix.open.test.promo;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-9-5.
 */
public class CouponsForestallTest {
    public static void main(String... args) {

        String channel = "third_tm";
        String uid ="1040";
        String couponid = "84";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        String session = "f528764dZ0d69b93dZ14844f8645eZd950";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("couponid", couponid);
        map.put("uid", uid);
        map.put("session", session);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/promo/coupons/forestall", true,
                        "channel", channel,
                        "couponid", couponid,
                        "session", session,
                        "uid", uid,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
