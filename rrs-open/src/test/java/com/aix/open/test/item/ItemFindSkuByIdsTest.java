package com.aix.open.test.item;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-8-7.
 */
public class ItemFindSkuByIdsTest {
    public static void main(String... args) {

        String channel = "brand_ygs_1";
        String skuIds ="291860";
        String key = "f26d238e87825b5a8f7e668cfcb08f51";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("skuIds", skuIds);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/item/sku/lists", true,
                        "channel", channel,
                        "skuIds", skuIds,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
