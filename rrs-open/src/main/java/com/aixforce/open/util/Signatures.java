package com.aixforce.open.util;

import com.google.common.base.*;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 5:09 PM  <br>
 * Author: xiao
 */
@Slf4j
public class Signatures {


    public static String sign(String toVerify, int deep) {
        if (deep == 0) {
            return toVerify;
        }
        String expect = Hashing.md5().newHasher().putString(toVerify, Charsets.UTF_8).hash().toString();
        return sign(expect, deep - 1);
    }



    /**
     * 验证签名
     * @param request   请求
     * @param restKey   密钥
     * @return  校验通过
     */
    public static boolean verify(HttpServletRequest request,  String restKey) {

        checkNotNull(restKey, "channel doesn't exists!");

        String sign = request.getParameter("sign");
        Map<String, String> params = Maps.newTreeMap();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (isValueEmptyOrSignRelatedKey(key, value)) {
                continue;
            }
            params.put(key, value);
        }


        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);

        String expect = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(restKey, Charsets.UTF_8).hash().toString();
        return Objects.equal(expect, sign);
    }

    private static boolean isValueEmptyOrSignRelatedKey(String key, String value) {
        return Strings.isNullOrEmpty(value) || StringUtils.equalsIgnoreCase(key, "sign")
                || StringUtils.equalsIgnoreCase(key, "sign_type");
    }

    /**
     * 校验签名，防止内容被篡改
     *
     * @param sign      消息摘要
     * @param params    参数
     * @return  是否通过校验 true | false
     */
    public static boolean check(String sign, TreeMap<String,String> params, String key) {

        Map<String, String> filterMap = Maps.filterValues(params, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null;
            }
        });

        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(filterMap);
        String stub = Signatures.sign(toVerify + key , 1);
        log.debug("stub={}, sign={}, toVerify={}, checked={}", stub, sign, toVerify, Objects.equal(stub, sign));
        return Objects.equal(stub, sign);
    }

}
