package com.aixforce.alipay.request;

import org.junit.Test;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-25 12:11 PM  <br>
 * Author: xiao
 */
public class AlipayPayRequestTest {

    @Test
    public void testPayRequestOk() {
        Token token = new Token("todo", "todo", "todo", "https://mapi.alipay.com/gateway.do");
        CallBack notify = new CallBack("http://beta.rrs.com/api/alipay/notify");
        CallBack forward = new CallBack("http://beta.rrs.com/buyer/trade-success");

        notify.append("zzz", "zzz");
        notify.append("aaa", "aaa");
        forward.append("bbb", "bbb");


        String url = PayRequest.build(token).title("酱油").outerTradeNo("XSSSS001102").total(1)
                .notify(notify).forward(forward).pay();
        System.out.print(url);
    }

}
