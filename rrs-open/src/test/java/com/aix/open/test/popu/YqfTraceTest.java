package com.aix.open.test.popu;

import com.github.kevinsawicki.http.HttpRequest;

/**
 * Created by neusoft on 14-9-9.
 */
public class YqfTraceTest {
    public static void main(String... args) {

        String channel = "cps";
        String source = "test";
        String cid = "1234";
        String wi = "wiwiwiwi";
        String target = "http://beta.goodaysh.com/items/376427";


        String response = HttpRequest
                .post("http://beta.goodaysh.com/api/open/popu/yqf/trace", true,
                        "channel", channel,
                        "cid", cid,
                        "wi", wi,
                        "target", target,
                        "source", source).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
