package com.aix.open.test.trade;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-8-7.
 */
public class TradeOrdersPageTest {
    public static void main(String... args) {

        String channel = "shop_ehaier_1";
        String begin ="20140101000000";
        String end = "";
        String pageNo = "1";
        String size = "2";
        String key = "fb5eef4ccab3ec3831f8de0c2e44226e";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("begin", begin);
        map.put("end", end);
        map.put("pageNo", pageNo);
        map.put("size", size);
        String sign = Sign.buildParams(map, key);
        //String response = HttpRequest
        //        .post("http://console.goodaysh.com/api/shop/info").send("channel="+channel+"&shopId="+shopId+"&outerCode="+outerCode+"&sign=" + key).body();
        String response = HttpRequest
               .post("http://console.goodaysh.com/api/open/trade/orders/page", true,
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
