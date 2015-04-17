package com.aix.open.test.promo;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-9-9.
 */
public class DtdOrdersByShopIdTest {
    public static void main(String... args) {

        String begin ="20140101000000";
        String end = "";
        String pageNo = "1";
        String size = "2";
        String channel = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("begin", begin);
        map.put("end", end);
        map.put("pageNo", pageNo);
        map.put("size", size);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/promo/dtd/shop/5678/orders", true,
                        "channel", channel,
                        "begin", begin,
                        "end", end,
                        "pageNo", pageNo,
                        "size", size,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
