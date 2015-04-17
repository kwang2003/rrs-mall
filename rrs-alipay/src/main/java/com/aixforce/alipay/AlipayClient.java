package com.aixforce.alipay;

import com.aixforce.alipay.request.PayRequest;
import com.aixforce.alipay.request.RefundRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.Response;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 *
 * 支付宝的客户端调用
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-04 9:25 AM  <br>
 * Author: xiao
 */
@ToString
public class AlipayClient {

    @Getter
    @Setter
    private Token token;                        // 登录凭证


    public AlipayClient(Token token) {
        this.token = token;
    }


    public String pay(PayRequest request) {
        return request.pay();
    }

    public Response<Boolean> refund(RefundRequest request) {
        return request.refund();
    }

    /**
     * 验证签名
     * @param params    参数
     * @param sign      签名
     * @return  校验通过
     */
    public boolean verify(Map<String, String> params, String sign) {
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

        String expect = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(token.getKey(), Charsets.UTF_8).hash().toString();
        return Objects.equal(expect, sign);
    }


}
