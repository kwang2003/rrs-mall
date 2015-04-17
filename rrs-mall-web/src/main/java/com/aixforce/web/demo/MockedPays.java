package com.aixforce.web.demo;

import com.aixforce.alipay.AlipayConfig;
import com.aixforce.rrs.settle.model.AlipayTrans;
import com.aixforce.rrs.settle.service.SettlementService;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * 支付宝交易的模拟器
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-04-21 9:59 AM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/demo/")
public class MockedPays {


    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    SettlementService settlementService;

    @RequestMapping("/gateway")
    public String gateway(HttpServletRequest request, HttpServletResponse response) {

        String service = request.getParameter("service");
        if (Objects.equal(service, "create_direct_pay_by_user")) {
            return "forward:/demo/pay";
        }
        if (Objects.equal(service, "account.page.query")) {
            return "forward:/demo/query";
        }

        return refund(request, response);
    }

    @RequestMapping(value = "/alipay", method = RequestMethod.POST)
    public String alipay(HttpServletRequest request) {

        String notifyUrl = request.getParameter("notify");
        String returnUrl = request.getParameter("return");
        String id = request.getParameter("orderId");
        String totalFee = request.getParameter("fee");

        Map<String, Object> params = Maps.newTreeMap();
        params.put("is_success", "T");
        params.put("out_trade_no", id);
        params.put("trade_no", id);  // Just use the out_trade_no as trade_no
        params.put("trade_status", "TRADE_SUCCESS");
        params.put("notify_id", id);  // Just use the out_trade_no as notify_id

        String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
        String sign = Hashing.md5().newHasher()
                .putString(toVerify, Charsets.UTF_8)
                .putString(AlipayConfig.ALIPAY_KEY, Charsets.UTF_8).hash().toString();

        params.put("sign", sign);
        params.put("sign_type", "MD5");

        AlipayTrans alipayTrans = new AlipayTrans();
        alipayTrans.setMerchantOutOrderNo(id);
        alipayTrans.setTradeNo(id);
        alipayTrans.setTransOutOrderNo(id);
        alipayTrans.setIncome(totalFee);
        alipayTrans.setTotalFee(totalFee);
        alipayTrans.setRate("0.03");

        try {
//            settlementService.createAlipayTrans(alipayTrans);  // Trans for income
            alipayTrans.setIncome("0");
            BigDecimal total = BigDecimal.valueOf(Double.parseDouble(totalFee));
            alipayTrans.setOutcome("" + total.multiply(BigDecimal.valueOf(0.03)));
//            settlementService.createAlipayTrans(alipayTrans); // Trans for outcome
        } catch (Throwable t) {
            // Ignore possible error.
        }
        String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
        HttpRequest.get(notifyUrl + "?" + suffix).connectTimeout(1000000).readTimeout(1000000).body();
        return "redirect:/demo/success?url=" + returnUrl;
    }

    @RequestMapping(value = "/refund", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String refund(HttpServletRequest request, HttpServletResponse response) {
        log.info("invoke refund");
        String xml = "<alipay>\n" +
                "   <is_success>T</is_success>\n" +
                "</alipay>";

        try {
            String batchNo = request.getParameter("batch_no");
            String num = request.getParameter("batch_num");
            String detail = request.getParameter("detail_data");

            List<String> trades = Splitter.on("#").splitToList(detail);
            List<String> details = Lists.newArrayListWithCapacity(trades.size());
            for (String trade : trades) {
                List<String> infos = Splitter.on("^").splitToList(trade);
                String tradeNo = infos.get(0);
                String fee = infos.get(1);
                String success = "SUCCESS";
                details.add(Joiner.on("^").join(tradeNo, fee, success));
            }

            String dd = Joiner.on("#").join(details);
            Map<String, Object> params = Maps.newTreeMap();
            params.put("notify_time", DFT.print(DateTime.now()));
            params.put("notify_type", "trade_status_sync");
            params.put("notify_id", "70fec0c2730b27528665af4517c27b95");


            params.put("batch_no", batchNo);
            params.put("success_num", num);
            params.put("result_details", dd);

            String toVerify = Joiner.on('&').withKeyValueSeparator("=").join(params);
            String sign = Hashing.md5().newHasher()
                    .putString(toVerify, Charsets.UTF_8)
                    .putString(AlipayConfig.ALIPAY_KEY, Charsets.UTF_8).hash().toString();

            params.put("result_details", URLEncoder.encode(dd, "utf-8"));
            params.put("sign", sign);
            params.put("sign_type", "MD5");



            String url = request.getParameter("notify_url");
            String suffix = Joiner.on('&').withKeyValueSeparator("=").join(params);
            HttpRequest.get(url + "?" + suffix).connectTimeout(10000).readTimeout(10000).body();

            response.getWriter().write(xml);
        } catch (Exception e) {
            log.error("raise error", e);
        }

        return null;
    }


    @RequestMapping(value = "/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String query(HttpServletRequest request, HttpServletResponse response) {
        String startTime = request.getParameter("gmt_start_time");
        String stopTime = request.getParameter("gmt_end_time");
        String pageNo = request.getParameter("page_no");
        String pageSize = request.getParameter("page_size");
        String tradeNo = request.getParameter("trade_no");
        String orderNo = request.getParameter("merchant_out_order_no");

        return null;
    }

}
