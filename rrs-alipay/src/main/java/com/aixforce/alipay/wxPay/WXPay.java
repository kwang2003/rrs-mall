package com.aixforce.alipay.wxPay;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by DJs on 15-3-5.
 */
@Slf4j
public class WXPay {

    protected Map<String, Object> params = Maps.newTreeMap();

    /**
     * 得到签名
     * @return
     */
    public String sign (String key) {
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        // 拼接key
        toVerify += "&key=" + key;
        // 得到签名
        String sign = Hashing.md5().newHasher().putString(toVerify, Charsets.UTF_8).hash().toString().toUpperCase();
        return sign;
    }

    /**
     * 验证签名
     * @param params
     * @param sign
     * @param key
     * @return
     */
    public static boolean verify (Map<String, String> params, String sign, String key) {
        // 1.拼接字符串
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        log.info("传入的参数 = " + toVerify);
        // 2.拼接key
        toVerify = toVerify + "&key=" + key;
        // 3.进行md5运算
        String expect = Hashing.md5().newHasher().putString(toVerify, Charsets.UTF_8).hash().toString().toLowerCase();
        sign = sign.toLowerCase();
        log.info("wx verify sign = {}. expect = {}.", sign, expect);
        Boolean isSignMatch = Objects.equal(expect, sign);
        if (!isSignMatch) {
            log.error("wx verify sign not match");
        }
        return isSignMatch;
    }

}
