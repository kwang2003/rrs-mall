package com.aixforce.alipay.event;

import com.aixforce.alipay.request.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-07 4:30 PM  <br>
 * Author: xiao
 */
@ToString
@AllArgsConstructor
public class TradeCloseEvent {

    @Getter
    @Setter
    private Token token;            // 令牌

    @Getter
    @Setter
    private String outerOrderNo;    // 外部商户号

}
