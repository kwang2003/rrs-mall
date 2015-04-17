package com.aixforce.alipay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 支付宝退款单数据集
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-12 11:18 AM  <br>
 * Author: xiao
 */
@ToString
public class AlipayRefundData {

    @Getter
    @Setter
    private String tradeNo;         // 支付宝交易号
    @Getter
    @Setter
    private Integer refundAmount;   // 退款金额
    @Getter
    @Setter
    private String reason;          // 退款理由


    public AlipayRefundData(String tradeNo, Integer refundAmount, String reason) {
        this.tradeNo = tradeNo;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }

}
