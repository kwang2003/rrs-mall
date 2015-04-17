package com.aixforce.restful;

import com.aixforce.restful.util.Signatures;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * Author: haolin
 * On: 10/13/14
 */
public class SignaturesTest {

    @Test
    public void generateSign(){
        String key = "123456";
        String data = "{\"orderId\":\"2014101012\",\"time\":\"2014-10-12 12:10:20\",\"context\":\"服务送转开始\"}";
        Map<String, String> params = Maps.newHashMap();
        params.put("data", data);

        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        String signed = Signatures.sign(toVerify + key, 1);

        System.out.println(signed);
    }
}
