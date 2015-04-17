package com.aixforce.restful.controller.ec;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.dto.OuterIdDto;
import com.aixforce.restful.service.HaierService;
import com.aixforce.restful.util.Signatures;
import com.aixforce.trade.dto.HaierOrder;
import com.aixforce.trade.model.InstallInfo;
import com.aixforce.trade.model.OrderInstallInfo;
import com.aixforce.trade.service.OrderInstallInfoService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Nullable;
import java.util.*;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 9:51 AM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/auto")
public class Autos {
    private final static DateTimeFormatter dft = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    private final static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private final static JavaType OUTER_ID_DTO_TYPE = jsonMapper.createCollectionType(ArrayList.class, OuterIdDto.class);

    private final static int DEFAULT_SIZE = 50;

    @Autowired
    private HaierService haierService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private OrderInstallInfoService orderInstallInfoService;

    @Value("#{app.restkey}")
    private String key;


    @Autowired
    private MessageSources messageSources;

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
        log.debug("/items accepted data={} ", data);
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();


        try {
            checkArgument(notEmpty(data), "data.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            TreeMap<String, String> params = Maps.newTreeMap();
            params.put("data", data);
            checkState(check(sign, params), "sign.verify.fail");
            List<OuterIdDto> outerIdDtos = jsonMapper.fromJson(data, OUTER_ID_DTO_TYPE);
            result = haierService.autoReleaseOrUpdateItem(outerIdDtos);
            return result;

        } catch (IllegalArgumentException e) {
            log.error("fail to invoke method 'items' with data={}, sign={}, error:{}", data, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("fail to invoke method 'items' with data={}, sign={}, error:{}", data, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));

        } catch (Exception e) {
            log.error("fail to invoke method 'items' with data={}, sign={}, cause:{}",
                    data, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("items.sync.fail"));
        }

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

        try {
            String date = dft.print(DateTime.now());
            result.setResult(date);

        } catch (Exception e) {
            log.error("fail to invoke method 'now'", e);
            result.setError(messageSources.get("server.time.get.fail"));
        }

        return result;
    }


    /**
     * 获取单个订单信息
     *
     * @param orderId   订单编号
     * @param sign      签名
     * @return 订单信息
     */
    @RequestMapping(value = "/order/get", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<HaierOrder> order(@RequestParam(value = "orderId") Long orderId,
                                           @RequestParam(value = "sign") String sign) {


        log.debug("/order/get accepted orderId={}, sign={}", orderId, sign);
        HaierResponse<HaierOrder> result = new HaierResponse<HaierOrder>();

        try {
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            TreeMap<String, String> params = Maps.newTreeMap();
            params.put("orderId", orderId.toString());
            checkState(check(sign, params), "sign.verify.fail");

            Response<HaierOrder> orderGetResult = orderQueryService.findHaierOrderById(orderId);
            checkState(orderGetResult.isSuccess(), orderGetResult.getError());

            HaierOrder haierOrder = orderGetResult.getResult();
            result.setResult(haierOrder);

        } catch (IllegalArgumentException e) {
            log.error("fail to invoke method 'order' with orderId={}, sign={}, error:{}", orderId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to invoke method 'order' with orderId={}, sign={}, error:{}", orderId, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to invoke method 'order' with orderId={}, sign={}, cause:{}",
                    orderId, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }



    /**
      * 订单查询接口,返回订单的分页信息（根据updateAt字段查询起止范围)
      *
      * @param begin     开始时间,未输入则表示无开始时间
      * @param end       截止时间,未输入则表示无截止时间
      * @param pageNo    页号,缺省 1
      * @param size      页大小,缺省 50
      * @param sign      消息摘要,必输
      * @return          分页信息
      */
    @RequestMapping(value = "/orders/page", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Paging<HaierOrder>> ordersPage(@RequestParam(value = "begin", required = false) String begin,
                                                        @RequestParam(value = "end", required = false) String end,
                                                        @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                        @RequestParam(value = "size", required = false) Integer size,
                                                        @RequestParam(value = "sign", required = true) String sign) {

        log.debug("/orders/page accepted begin={}, end={}, pageNo={}, size={}, sign={}",
                begin, end, pageNo, size, sign);

        HaierResponse<Paging<HaierOrder>> result = new HaierResponse<Paging<HaierOrder>>();

        try {
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");
            TreeMap<String, String> params = Maps.newTreeMap();   //参数按名称升序
            if (notNull(begin)) {
                params.put("begin", begin);
            }
            if (notNull(end)) {
                params.put("end", end);
            }
            if (notNull(pageNo)) {
                params.put("pageNo", pageNo.toString());
            } else {
                pageNo = 1;
            }

            if (notNull(size)) {
                params.put("size", size.toString());
            } else {
                size = DEFAULT_SIZE;
            }

            checkState(check(sign, params), "sign.verify.fail");

            Date beginAt, endAt;

            if (notEmpty(begin)) {
                beginAt = dft.parseDateTime(begin).toDate();
            } else {
                beginAt = DateTime.now().minusDays(7).toDate();
            }

            if (notEmpty(end)) {
                endAt = dft.parseDateTime(end).toDate();
            } else {
                endAt = DateTime.now().toDate();
            }

            //修改接口,直接返回haier的DTO
            Response<Paging<HaierOrder>> ordersQuery = orderQueryService
                    .findHaierOrderByUpdatedAt(beginAt, endAt, Lists.newArrayList(1L, 5L), pageNo, size);
            checkState(ordersQuery.isSuccess(), ordersQuery.getError());



            Paging<HaierOrder> paging = ordersQuery.getResult();
            result.setResult(paging);
            result.sign(key);   //使用密钥签名


        } catch (IllegalArgumentException e) {
            log.error("fail to invoke 'orderPage' with begin={}, end={}, pageNo={}, size={}, sign={}, error:{}"
                    , begin, end, pageNo, size, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to invoke 'orderPage' with begin={}, end={}, pageNo={}, size={}, sign={}, error:{}"
                    , begin, end, pageNo, size, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to invoke 'orderPage' with begin={}, end={}, pageNo={}, size={}, sign={}, cause:{}"
                    , begin, end, pageNo, size, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.query.fail"));
        }

        return result;
    }


    /**
      * 订单状态同步接口  <br/>
      *
      * 特殊场景:  订单包含 A, B 2个子订单，如果A有货而B缺货， <br/>
      * 则卖家需要在RRS中对B发起退款，同时在ESTORE中标记A为已发货  <br/>
      * 在上述两个业务动作完成后，EC会将ESTORE中的总订单状态标记为“已发货”并通知RRS
      *
      *
      * @param orderId   RRS的订单id
      * @param status    订单要更新的状态
      * @param sign      消息摘要
      * @return          返回的具体信息, 如果isSuccess的值为true表示同步成功
      */
    @RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> updateStatus(@RequestParam(value = "orderId") String orderId,
                                               @RequestParam(value = "status") String status,
                                               @RequestParam(value = "sign") String sign) {

        log.debug("/orders accepted orderId={}, status={}, sign={}", orderId, status, sign);
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();

        try {
            checkArgument(notEmpty(orderId), "order.id.can.not.be.empty");
            checkArgument(notEmpty(status), "status.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            TreeMap<String, String> params = Maps.newTreeMap();   //参数按名称升序
            params.put("orderId", orderId);
            params.put("status", status);
            checkState(check(sign, params), "sign.verify.fail");

            Response<Boolean> updateResult = orderWriteService
                    .updateStatus(Long.valueOf(orderId), Integer.valueOf(status));
            checkState(updateResult.isSuccess(), updateResult.getError());

            result.setResult(Boolean.TRUE);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("fail to updateStatus with orderId={}, status={}, sign={}, error:{}", orderId, status, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to updateStatus with orderId={}, status={}, sign={}, error:{}", orderId, status, sign, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to updateStatus with orderId={}, status={}, sign={}, cause:{}",
                    orderId, status, sign, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("order.update.status.fail"));
        }

        return result;
    }


    @RequestMapping(value = "/invoice/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HaierResponse<Boolean> updateInvoice(@RequestParam("orderId") Long orderId,
                                                @RequestParam("invoiceNo") String invoiceNo,
                                                @RequestParam("url") String url,
                                                @RequestParam("sign") String sign) {


        log.debug("/invoice/update accepted orderId:{}, invoiceNo:{}, url:{}", orderId, invoiceNo, url);
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();
        try {
            checkArgument(notNull(orderId), "order.id.can.not.be.empty");
            checkArgument(notEmpty(invoiceNo), "status.can.not.be.empty");
            checkArgument(notEmpty(url), "url.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            TreeMap<String, String> params = Maps.newTreeMap();   //参数按名称升序
            params.put("orderId", orderId.toString());
            params.put("invoiceNo", invoiceNo);
            params.put("url", url);

            checkState(check(sign, params), "sign.verify.fail");

            Response<Boolean> updateResult = orderWriteService.updateElectInvoice(orderId, invoiceNo, url);
            checkState(updateResult.isSuccess(), updateResult.getError());
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to updateElectInvoice with orderId:{}, invoiceNo:{}, url:{}, error:{}",
                    orderId, invoiceNo, url, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to updateElectInvoice with orderId:{}, invoiceNo:{}, url:{}, error:{}",
                    orderId, invoiceNo, url, e.getMessage());
            result.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to updateElectInvoice with orderId:{}, invoiceNo:{}, url:{}, cause:{}",
                    orderId, invoiceNo, url, Throwables.getStackTraceAsString(e));
            result.setError(messageSources.get("elect.invoice.update.fail"));
        }
        return  result;
    }

    /**
     * 提供订单安装信息同步
     * @param data  请求数据, 以json数组格式: 如{"orderId": "2010231232", "time": "2014-09-12 10:12:25", "context": "分配网点"}
     * @param sign  消息摘要
     * @return      isSuccess为true, 同步成功; isSuccess为false, 同步失败, error为错误信息
     */
    @RequestMapping(value = "/order_install_infos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Boolean> syncOrderInstallInfo(@RequestParam(value = "data") String data,
                                        @RequestParam(value = "sign") String sign) {
        log.debug("/order_install_infos accepted data={} ", data);
        Response<Boolean> resp = new Response<Boolean>();
        try {
            checkArgument(notEmpty(data), "data.can.not.be.empty");
            checkArgument(notEmpty(sign), "sign.can.not.be.empty");

            TreeMap<String, String> params = Maps.newTreeMap();
            params.put("data", data);
            checkState(check(sign, params), "sign.verify.fail");
            OrderInstallInfo orderInstallInfo = jsonMapper.fromJson(data, OrderInstallInfo.class);
            Response<Long> createResp = orderInstallInfoService.create(orderInstallInfo);
            if (!createResp.isSuccess()){
                throw new Exception(createResp.getError());
            }
            resp.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to invoke method 'syncOrderInstallInfo' with data={}, sign={}, error:{}", data, sign, e.getMessage());
            resp.setError(messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to invoke method 'syncOrderInstallInfo' with data={}, sign={}, error:{}", data, sign, e.getMessage());
            resp.setError(messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to invoke method 'syncOrderInstallInfo' with data={}, sign={}, cause:{}",
                    data, sign, Throwables.getStackTraceAsString(e));
            resp.setError(messageSources.get("order.install.info.sync.fail"));
        }
        return resp;
    }


    /**
     * 校验签名，防止内容被篡改
     *
     * @param sign      消息摘要
     * @param params    参数
     * @return  是否通过校验 true | false
     */
    private boolean check(String sign, TreeMap<String,String> params) {

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
