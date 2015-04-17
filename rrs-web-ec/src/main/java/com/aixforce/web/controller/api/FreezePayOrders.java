package com.aixforce.web.controller.api;

import com.aixforce.alipay.Bank;
import com.aixforce.alipay.exception.BankNotFoundException;
import com.aixforce.alipay.request.CallBack;
import com.aixforce.alipay.request.FreezePayRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.alipay.request.UnFreezeRequest;
import com.google.common.base.Strings;
import com.aixforce.alipay.request.*;
import com.google.common.base.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Author:  dong-jie@neusoft.com
 * Date: 2014-12-01
 */
@Controller
@RequestMapping("/api")
@Slf4j
public class FreezePayOrders {

   @Autowired
   private Token token;

   @Value("#{app.alipayNotifySuffix}")
   private String notifyUrl;

   @Value("#{app.alipayReturnSuffix}")
   private String returnUrl;

   @RequestMapping(value = "/buyer/freezePay/{id}/pay" ,method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
   @ResponseBody
   public String freezePay (@PathVariable("id") long id,
                             @CookieValue(value = "bank", required = false) String bank,
                             @RequestParam(value = "amount") int amount,
                             @RequestParam(value = "productCode") String productCode,
                             @RequestParam(value = "sceneCode") String sceneCode) {
       String orderTitle = "testtest";
       String tradeNo = String.valueOf(id);
       String outRequestNo = null;
       // 当前毫秒 + 5位随机数
       outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
       log.info("支付宝资金授权流水号 outRequestNo = " + outRequestNo);
       return this.buildFreezePayUrl(bank, orderTitle, tradeNo, amount, outRequestNo, productCode, sceneCode);
   }

    @RequestMapping(value = "/buy/payNow", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String payNow (@RequestParam("out_trade_no") String out_trade_no,
                           @RequestParam("authNo") String authNo,
                           @RequestParam("total_fee") int total_fee,
                           @RequestParam("buyerId") String buyerId) {
        String productCode = "FUND_TRADE_FAST_PAY";
        String subject = "test"; // 此次交易的描述
        return this.buildCreateAndPayUrl(out_trade_no, total_fee, productCode, authNo, subject, buyerId);
    }

    @RequestMapping(value = "/buy/unFreezePay" , method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String unFreezePay (@RequestParam("authNo") String authNo,
                                @RequestParam(value = "amount") int amount) {
        String remark = "2014-12-2";
        // 当前毫秒 + 5位随机数
        String outRequestNo = String.valueOf(System.currentTimeMillis()) + String.valueOf((int)(Math.random() * 10000));
        log.info("支付宝资金授权流水号 outRequestNo = " + outRequestNo);
        return this.buildUnFreezePayUrl(authNo, outRequestNo, amount, remark);
    }

    /**
     * 构建支付宝资金授权 - 付款 请求url
     * @param bank                  银行（为空默认选择支付宝）
     * @param orderTitle           订单标题 （如产品描述 最长50汉字）
     * @param tradeNo               RRS商城订单号
     * @param amount                金额
     * @param outRequestNo         流水号（唯一 最长64位）
     * @param productCode          业务产品码（支付宝签约接口时分配）
     * @param sceneCode            业务场景码（支付宝签约接口时分配）
     * @return
     */
   public String buildFreezePayUrl(String bank,
                                     String orderTitle,
                                     String tradeNo,
                                     int amount,
                                     String outRequestNo,
                                     String productCode,
                                     String sceneCode) {
       CallBack notify = new CallBack(notifyUrl);
       CallBack forward = new CallBack(returnUrl);
       FreezePayRequest payRequest = FreezePayRequest.build(token)
               .orderTitle(orderTitle)
               .outOrderNo(tradeNo)
               .amount(amount)
               .outRequestNo(outRequestNo)
               .notify(notify)
               .forward(forward)
               .productCode(productCode)
               .sceneCode(sceneCode);
       if (!Strings.isNullOrEmpty(bank)) {
           try {
               payRequest.defaultBank(Bank.from(bank));
           } catch (BankNotFoundException e) {
               // ignore
           }
       }
       log.info(payRequest.url());
       return payRequest.url();
   }

    /**
     * 立即支付
     * @param outTradeNo     商城订单号
     * @param totalFee       支付金额
     * @param productCode    产品码
     * @return
     */
    public String buildCreateAndPayUrl (String outTradeNo,
                          int totalFee,
                          String productCode,
                          String authNo,
                          String subject,
                          String buyerId) {
        CallBack notify = new CallBack(notifyUrl);
        CreateAndPay payRequest = CreateAndPay.build(token)
                                              .notify(notify)
                                              .outTradeNo(outTradeNo)
                                              .totalFee(totalFee)
                                              .productCode(productCode)
                                              .subject(subject)
                                              .authNo(authNo)
                                              .buyerId(buyerId);
        System.out.println(payRequest.url());
        return payRequest.url();
    }

    /**
     * 构建支付宝资金授权 - 退款 请求url
     * @param authNo            RRS商城订单号
     * @param outRequestNo     流水号（唯一 最长64位）
     * @param amount            金额
     * @param remark            业务描述（最多50汉字）
     * @return
     */
    public String buildUnFreezePayUrl (String authNo,
                                        String outRequestNo,
                                        int amount,
                                        String remark) {
       CallBack notify = new CallBack(notifyUrl);
       CallBack forward = new CallBack(returnUrl);
       UnFreezeRequest payRequest = UnFreezeRequest.build(token)
               .authNo(authNo)
               .outRequestNo(outRequestNo)
               .amount(amount)
               .remark(remark)
               .notify(notify);
       log.info(payRequest.url());
       return payRequest.url();
   }
}
