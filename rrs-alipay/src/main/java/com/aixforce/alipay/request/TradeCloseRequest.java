package com.aixforce.alipay.request;

import com.aixforce.common.model.Response;
import com.github.kevinsawicki.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 向支付宝网关发送请求，关闭交易(同步接口)
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-27 9:55 PM  <br>
 * Author: xiao
 */
@Slf4j
public class TradeCloseRequest extends Request {

    private TradeCloseRequest(Token token) {
        super(token);
        params.put("service", "close_trade");
    }

    public static TradeCloseRequest build(Token token) {
        return new TradeCloseRequest(token);
    }

    public TradeCloseRequest tradeNo(String tradeNo) {
        params.put("trade_no", tradeNo);
        return this;
    }


    public TradeCloseRequest outOrderNo(String orderNo) {
        params.put("out_order_no", orderNo);
        return this;
    }

    public TradeCloseRequest role(Role role) {
        params.put("trade_role", role.code);
        return this;
    }



    public Response<Boolean> notifyToClose() {
        String url = super.url();
        log.debug("close trade url: {}", url);
        String body = HttpRequest.get(url).connectTimeout(10000).readTimeout(10000).body();
        log.debug("close trade result: {}", body);
        return convertToResponse(body);
    }

    public static enum Role {
        BUYER(1, "买家", "B"),
        SELLER(2,"卖家", "S");


        private final int value;
        private final String description;
        private final String code;


        private Role(int value, String description, String code) {
            this.value = value;
            this.description = description;
            this.code = code;
        }

        public String code() {
            return code;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
