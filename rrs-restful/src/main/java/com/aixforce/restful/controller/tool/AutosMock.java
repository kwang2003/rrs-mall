package com.aixforce.restful.controller.tool;

import com.aixforce.common.model.Paging;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.util.Signatures;
import com.aixforce.trade.dto.HaierOrder;
import com.aixforce.trade.dto.HaierOrderItem;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 9:51 AM  <br>
 * Author: xiao
 */
//@Controller
@Slf4j
@RequestMapping("/api/auto")
public class AutosMock {
    private final static DateTimeFormatter dft = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Value("#{app.restkey}")
    private String key;

    /**
     * 提供库存同步,自动发布商品,以及商品自动上下架的功能
     *
     * @param data  请求数据
     * @param sign  消息摘要
     * @return      返回的具体信息, 如果isSuccess的值为true表示同步成功
     */
    @RequestMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> items(@RequestParam(value = "data") String data,
                                   @RequestParam(value = "sign") String sign) {

        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        if (Strings.isNullOrEmpty(data)) {
            log.error("Rest '/items' args 'data' cannot be empty");
            result.setError("rest.data.null");
            return result;
        }

        if (Strings.isNullOrEmpty(sign)) {
            log.error("Rest '/items' args 'sign' cannot be empty");
            result.setError("rest.sign.null");
            return result;
        }

        TreeMap<String, String> params = Maps.newTreeMap();
        params.put("data", data);

        if (!check(sign, params)) {
            log.error("Rest '/items' sign incorrect sign={}, params={}", sign, params);
            result.setError("rest.illegal.sign");
            return result;
        }

        log.debug("data accepted data={}", data);

        result.setSuccess(Boolean.TRUE);
        return result;
    }


    /**
     * 订单状态同步接口
     *
     * @param orderId   RRS的订单id
     * @param status    订单要更新的状态
     * @param sign      消息摘要
     * @return          返回的具体信息, 如果isSuccess的值为true表示同步成功
     */
    @RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> orderSync(@RequestParam(value = "orderId") String orderId,
                                    @RequestParam(value = "status") String status,
                                    @RequestParam(value = "sign") String sign) {

        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        if (Strings.isNullOrEmpty(orderId)) {
            log.error("Rest '/orders' args 'orderId' cannot be empty");
            result.setError("rest.order.id.null");
            return result;
        }

        if (Strings.isNullOrEmpty(status)) {
            log.error("Rest '/orders' args 'status' cannot be empty");
            result.setError("rest.status.null");
            return result;
        }

        if (Strings.isNullOrEmpty(sign)) {
            log.error("Rest '/orders' args 'sign' cannot be empty");
            result.setError("rest.sign.null");
            return result;
        }

        TreeMap<String, String> params = Maps.newTreeMap();
        params.put("orderId", orderId);
        params.put("status", status);

        if (!check(sign, params)) {
            log.error("Rest '/orders' sign incorrect sign={}, params={}", sign, params);
            result.setError("rest.illegal.sign");
            return result;
        }

        log.debug("data accepted orderId={}, status={}", orderId, status);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    /**
     * 订单查询接口,返回订单的分页信息（根据updateAt字段查询起止范围)
     *
     * @param begin     开始时间(yyyyMMdd)
     * @param end       截止时间
     * @param pageNo    页号
     * @param size      页大小
     * @param sign      消息摘要
     * @return          分页信息
     */
    @RequestMapping(value = "/orders/page", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Paging<HaierOrder>> ordersPage(@RequestParam(value = "begin", required = false) String begin,
                                        @RequestParam(value = "end", required = false) String end,
                                        @RequestParam(value = "pageNo", required = false) String pageNo,
                                        @RequestParam(value = "size", required = false) String size,
                                        @RequestParam(value = "sign", required = true) String sign) {
        HaierResponse<Paging<HaierOrder>> result = new HaierResponse<Paging<HaierOrder>>();


        if (Strings.isNullOrEmpty(sign)) {
            log.error("Rest '/orders/page' args 'sign' cannot be empty");
            result.setError("rest.sign.null");
            return result;
        }

        TreeMap<String, String> params = Maps.newTreeMap();

        if (begin != null)
            params.put("begin", begin);
        if (end != null)
            params.put("end", end);
        if (pageNo != null)
            params.put("pageNo", pageNo);
        if (size != null)
            params.put("size", size);

        if (!check(sign, params)) {
            log.error("Rest '/orders' sign incorrect sign={}, params={}", sign, params);
            result.setError("rest.illegal.sign");
            return result;
        }

        log.debug("data accepted begin={}, end={}, pageNo={}, size={}, sign={}", begin, end, pageNo, size, sign);

        Paging<HaierOrder> paging = mock();
        result.setResult(paging);

        result.sign(key);   //使用密钥签名
        return result;
    }

    /**
     * 获取服务器当前的时钟
     *
     * @return  日期(yyyyMMddHHmmss)
     */
    @RequestMapping(value = "/now", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<String> now() {
        HaierResponse<String> result = new HaierResponse<String>();
        String date = dft.print(DateTime.now());
        result.setResult(date);
        return result;
    }

    @RequestMapping(value = "/order/get", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<HaierOrder> order(@RequestParam(value = "orderId") String orderId,
                                      @RequestParam(value = "sign") String sign) {

        HaierResponse<HaierOrder> result = new HaierResponse<HaierOrder>();

        if (Strings.isNullOrEmpty(orderId)) {
            log.error("Rest '/get' args 'orderId' cannot be empty");
            result.setError("rest.order.id.null");
            return result;
        }

        if (Strings.isNullOrEmpty(sign)) {
            log.error("Rest '/get' args 'sign' cannot be empty");
            result.setError("rest.sign.null");
            return result;
        }

        TreeMap<String, String> params = Maps.newTreeMap();
        params.put("orderId", orderId);

        if (!check(sign, params)) {
            log.error("Rest '/orders' sign incorrect sign={}, params={}", sign, params);
            result.setError("rest.illegal.sign");
            return result;
        }


        HaierOrder order = orderMock();
        result.setResult(order);
        log.debug("data accepted orderId={} ", orderId);
        return result;
    }


    private HaierOrder orderMock() {
        HaierOrder dto = new HaierOrder();
        String fmtDate = dft.print(DateTime.now());
        dto.setFee(500000);
        dto.setId(1L);
        dto.setCanceledDate(fmtDate);
        dto.setCreatedDate(fmtDate);
        dto.setDeliveredDate(fmtDate);
        dto.setDoneDate(fmtDate);
        dto.setPaidDate(fmtDate);
        dto.setPaymentType(1);
        dto.setStatus(1);
        dto.setOuterCode("88888888");
        dto.setType(1);
        dto.setUpdatedDate(fmtDate);


        HaierOrderItem item1 = new HaierOrderItem();
        item1.setQuantity(1);
        item1.setFee(300000);
        item1.setItemName("海尔彩电");

        HaierOrderItem item2 = new HaierOrderItem();
        item1.setQuantity(1);
        item1.setFee(200000);
        item1.setItemName("海尔冰箱");

        List<HaierOrderItem> items = Lists.newArrayList(item1, item2);
        dto.setItems(items);
        return dto;
    }



    private Paging<HaierOrder> mock(){
        Paging<HaierOrder> paging = new Paging<HaierOrder>();
        HaierOrder dto = new HaierOrder();
        DateTime now = DateTime.now();
        String fmtDate = dft.print(now);

        dto.setFee(500000);
        dto.setId(1L);
        dto.setCanceledDate(fmtDate);
        dto.setCreatedDate(fmtDate);
        dto.setDeliveredDate(fmtDate);
        dto.setDoneDate(fmtDate);
        dto.setPaidDate(fmtDate);
        dto.setPaymentType(1);
        dto.setStatus(1);
        dto.setType(1);
        dto.setOuterCode("8888888");
        dto.setUpdatedDate(fmtDate);


        HaierOrderItem item1 = new HaierOrderItem();
        item1.setQuantity(1);
        item1.setFee(300000);
        item1.setItemName("海尔彩电");

        HaierOrderItem item2 = new HaierOrderItem();
        item1.setQuantity(1);
        item1.setFee(200000);
        item1.setItemName("海尔冰箱");

        List<HaierOrderItem> items = Lists.newArrayList(item1, item2);
        dto.setItems(items);
        List<HaierOrder> dtos = Lists.newArrayList(dto, dto);


        paging.setTotal(2L);
        paging.setData(dtos);
        return paging;
    }

    /**
     * 校验签名，防止内容被篡改
     *
     * @param sign      消息摘要
     * @param params    参数
     * @return  是否通过校验 true | false
     */
    public boolean check(String sign, TreeMap<String,String> params) {

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


    public static void main(String[] args) {
        TreeMap<String, String> params = Maps.newTreeMap();
//        params.put("begin","20131201010000");
//        params.put("end","20131202010000");
        String data = "[{\"shopId\":\"8800003441\",\"skus\":[{\"skuId\":665552936,\"stock\":6},{\"skuId\":4696147,\"stock\":6},{\"skuId\":5156546,\"stock\":6},{\"skuId\":355540490,\"stock\":4},{\"skuId\":518505,\"stock\":9},{\"skuId\":382527,\"stock\":1},{\"skuId\":371467,\"stock\":2},{\"skuId\":3829280,\"stock\":14},{\"skuId\":244843,\"stock\":8},{\"skuId\":446811,\"stock\":6},{\"skuId\":462866,\"stock\":2},{\"skuId\":362363,\"stock\":64},{\"skuId\":449447,\"stock\":2},{\"skuId\":513446,\"stock\":5},{\"skuId\":6649045,\"stock\":4},{\"skuId\":364142,\"stock\":2},{\"skuId\":6315832,\"stock\":5},{\"skuId\":504575,\"stock\":5},{\"skuId\":5806811,\"stock\":6},{\"skuId\":511780,\"stock\":1},{\"skuId\":6863497,\"stock\":1},{\"skuId\":12077667,\"stock\":22},{\"skuId\":509240354,\"stock\":9},{\"skuId\":6798280,\"stock\":5},{\"skuId\":4886350,\"stock\":10},{\"skuId\":361326,\"stock\":2},{\"skuId\":444108,\"stock\":25},{\"skuId\":521375,\"stock\":6},{\"skuId\":3826856,\"stock\":7},{\"skuId\":506203,\"stock\":9},{\"skuId\":347841,\"stock\":6},{\"skuId\":458181,\"stock\":6},{\"skuId\":12077029,\"stock\":6},{\"skuId\":5947522,\"stock\":6},{\"skuId\":106758,\"stock\":12},{\"skuId\":523264,\"stock\":9},{\"skuId\":461769,\"stock\":9},{\"skuId\":443331,\"stock\":22},{\"skuId\":404040,\"stock\":1},{\"skuId\":357280155,\"stock\":10},{\"skuId\":458632,\"stock\":22},{\"skuId\":6236199,\"stock\":12},{\"skuId\":3829284,\"stock\":12},{\"skuId\":5416825,\"stock\":81},{\"skuId\":505256,\"stock\":9},{\"skuId\":4442,\"stock\":3},{\"skuId\":439950,\"stock\":2},{\"skuId\":4886879,\"stock\":11},{\"skuId\":355431924,\"stock\":1},{\"skuId\":665552938,\"stock\":9}]}]";
        params.put("data", data);
        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        String sign = Signatures.sign(toVerify + "123456", 1);
        System.out.print(sign);

    }

}
