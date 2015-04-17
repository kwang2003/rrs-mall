package com.aixforce.admin.web.controller;

import com.aixforce.admin.web.jobs.OrderJobs;
import com.aixforce.category.dto.AttributeDto;
import com.aixforce.category.model.Spu;
import com.aixforce.common.utils.JsonMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-02
 */
public class OrderJobOverDayTest {

    // /api/orders/jobs/orderNotConfirmDeliverExpire

    public static void main(String... args) {

//        String url = "127.0.0.1:8081/admin";
//        String url = "console.goodaysh.com";
        String url = "console-admin.rrs.cn";
        String itemName ="";
        String tOrderId ="1";
        String buyerId ="6982108";
        String begin ="20140924222622";
        String end = "20140927222622";
        String pageNo = "1";
        String size = "";
        String channel = "third_tm";
        String key = "2291651ed37cb463aa3f4d94d3383b8d";
        Map map = Maps.newConcurrentMap();
        map.put("channel", channel);
        map.put("itemName", itemName);
        map.put("tOrderId", tOrderId);
        map.put("buyerId", buyerId);
        map.put("begin", begin);
        map.put("end", end);
        map.put("pageNo", pageNo);
        map.put("size", size);
//        String sign = Sign.buildParams(map, key);
        String response = HttpRequest
                .get("http://" + url + "/api/orders/jobs/orderNotConfirmDeliverExpire", true).body();
        //String response = HttpRequest
        //        .get("http://console.goodaysh.com/api/shop/info", true, "channel", channel, "shopId", shopId, "outerCode", outerCode, "sign", sign).body();
        System.out.println(response);
    }
}
