package com.aix.open.test.item;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by yangjingang on 14-8-20.
 */
public class ItemDetailTest {

    private static final Pattern imgUrlPattern = Pattern.compile("(http).*?(\\.jpg|\\.png|\\.bmp|\\.gif|\\.JPG|\\.PNG|\\.BMP|\\.GIF)");

    public static void main(String... args) {



        String channel = "third_tm";
        String id ="227827";//373588:item,227827:spu
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/item/"+id+"/detail", true,
                        "channel", channel,
                        "sign", sign).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
