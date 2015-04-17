package com.aixforce.alipay.request;

import com.aixforce.alipay.dto.AlipayFreezeSyncResponse;
import com.aixforce.alipay.dto.AlipaySyncResponse;
import com.aixforce.common.model.Response;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-25 9:37 AM  <br>
 * Author: xiao
 */
public class Request {
    private final static Logger log = LoggerFactory.getLogger(Request.class);
    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    protected Map<String, Object> params = Maps.newTreeMap();
    protected Token token;

    private static XStream xStream;

    static {
        xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.processAnnotations(AlipaySyncResponse.class);
    }


    protected Request(Token token) {
        params.put("partner", token.getPid());
        if (!Strings.isNullOrEmpty(token.getAccount())) {
            params.put("seller_email", token.getAccount());
        }
        params.put("_input_charset", "utf-8");
        this.token = token;
    }

    public Map<String, Object> param() {
        return params;
    }

    public String url() {
        sign();
        String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
        return token.getGateway() + "?" + suffix;
    }

    protected Response<Boolean> convertToResponse(String body) {
        Response<Boolean> result = new Response<Boolean>();
        checkState(!Strings.isNullOrEmpty(body), "alipay.refund.fail");

        AlipaySyncResponse refundResponse = (AlipaySyncResponse)xStream.fromXML(body);
        if (refundResponse.isSuccess()) {
            result.setResult(Boolean.TRUE);
        } else {
            log.error("refund raise fail: {}", refundResponse.getError());
            result.setError(refundResponse.getError());
        }
        return result;
    }


    /**
     * 对参数列表进行签名
     */
    public void sign() {
        try {
            String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

            String sign = Hashing.md5().newHasher()
                    .putString(toVerify, Charsets.UTF_8)
                    .putString(token.getKey(), Charsets.UTF_8).hash().toString();

            params.put("sign", sign);
            params.put("sign_type", "MD5");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证签名
     * @param params    参数
     * @param sign      签名
     * @return  校验通过
     */
    public static boolean verify(Map<String, String> params, String sign, Token token) {
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

        String expect = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(token.getKey(), Charsets.UTF_8).hash().toString();
        final boolean isSignMatch = Objects.equal(expect, sign);
        if(!isSignMatch){
            log.error("alipay sign mismatch, expected ({}), actual({}), toVerify is:{}", expect, sign, toVerify);
        } else {
            log.info("alipay sign matched, expected ({}), actual({})", expect, sign, toVerify);
        }
        return isSignMatch;
    }

}
