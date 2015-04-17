package com.aixforce.admin;

import com.aixforce.common.utils.JsonMapper;
import com.aixforce.trade.model.OrderInstallInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: haolin
 * On: 9/28/14
 */
public class JsonTest {

    private final static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private final static JavaType ORDER_INSTALL_INFO_TYPE = jsonMapper.createCollectionType(ArrayList.class, OrderInstallInfo.class);

    public static void main(String[] args){
        List<OrderInstallInfo> orderInstallInfos = Lists.newArrayList();
        OrderInstallInfo orderInstallInfo1 = new OrderInstallInfo();
//        orderInstallInfo1.setOrderId(123L);
//        orderInstallInfo1.setData("[{\"time\":\"2014-09-28 14:20:35\",\"status\":\"服务网点分配\"}]");
        orderInstallInfos.add(orderInstallInfo1);

        OrderInstallInfo orderInstallInfo2 = new OrderInstallInfo();
//        orderInstallInfo2.setOrderId(124L);
//        orderInstallInfo2.setData("[{\"time\":\"2014-09-28 14:20:35\",\"status\":\"服务网点分配\"}]");
        orderInstallInfos.add(orderInstallInfo2);

        System.out.println(jsonMapper.toJson(orderInstallInfos));

        jsonMapper.fromJson("[{\"orderId\":123,\"data\":\"[{\\\"time\\\":\\\"2014-09-28 14:20:35\\\",\\\"status\\\":\\\"服务网点分配\\\"}]\"},{\"orderId\":124,\"data\":\"[{\\\"time\\\":\\\"2014-09-28 14:20:35\\\",\\\"status\\\":\\\"服务网点分配\\\"}]\"}]", ORDER_INSTALL_INFO_TYPE);
    }
}
