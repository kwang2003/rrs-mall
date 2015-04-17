package com.aix.open.test.user;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import util.Sign;

import java.util.Map;

/**
 * Created by neusoft on 14-8-15.
 */
public class SmsTest {


    public static void main(String... argss) {

        String channel = "brand_ygs_1";
        String key = "f26d238e87825b5a8f7e668cfcb08f51";
        String id = "";
        String mobile = "13964204871";//13061261110  13840851784
        String args = "9997";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("id", id);
        map.put("mobile", mobile);
        map.put("args", args);

        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .post("http://console.goodaysh.com/api/open/user/sms", true,
                        "id", id,
                        "mobile", mobile,
                        "args", args,
                        "channel", channel,
                        "sign", sign).body();
        System.out.println(response);
    }
}
