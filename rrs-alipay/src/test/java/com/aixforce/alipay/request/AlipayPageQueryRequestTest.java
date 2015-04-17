package com.aixforce.alipay.request;

import com.aixforce.alipay.dto.AlipaySettlementResponse;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-14 6:55 PM  <br>
 * Author: xiao
 */
public class AlipayPageQueryRequestTest {
    @Test
    public void testQuery() throws Exception {
        Token token = new Token();
        token.setKey("todo");
        token.setPid("todo");
        token.setAccount("todo");
        token.setGateway("https://mapi.alipay.com/gateway.do");

        DateTime now = DateTime.parse("2014-05-09");

        boolean next = true;
        int pageNo = 1;

        while (next) {
            AlipaySettlementResponse result = PageQueryRequest.build(token).start(now.minusDays(1)
                .toDate()).end(now.toDate()).pageNo(pageNo).pageSize(5000).query();
            assertThat(result.isSuccess(), is(Boolean.TRUE));
            next = result.hasNextPage();
            pageNo ++;

            System.out.println("process pageNo:  " + pageNo);
        }

    }


}
