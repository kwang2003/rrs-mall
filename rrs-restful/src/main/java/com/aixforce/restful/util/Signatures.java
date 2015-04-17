package com.aixforce.restful.util;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 5:09 PM  <br>
 * Author: xiao
 */
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


}
