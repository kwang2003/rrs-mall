package com.aixforce.alipay.request;

import com.aixforce.alipay.dto.AlipayRefundData;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-25 12:12 PM  <br>
 * Author: xiao
 */
public class AlipayRefundRequestTest {

    @Test
    public void testAlipayRefundRequestOk() {
        AlipayRefundData refund = new AlipayRefundData("12345678", 1000, "上海");
        Token token = new Token("todo", "todo", "todo", "https://mapi.alipay.com/gateway.do");
        String batchNo = RefundRequest.toBatchNo(DateTime.now().toDate(), 1L);
        CallBack notify = new CallBack("http://101.71.249.198:9080/wak");

        String ss = RefundRequest.build(token).batch(batchNo).detail(Lists.newArrayList(refund)).notify(notify).url();
        System.out.print(ss);

    }
}
