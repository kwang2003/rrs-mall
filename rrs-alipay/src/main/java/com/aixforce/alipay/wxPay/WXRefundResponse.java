package com.aixforce.alipay.wxPay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by DJs on 15-3-13.
 */
@XStreamAlias("xml")
public class WXRefundResponse {

    @Setter
    @Getter
    @XStreamAlias("return_code")
    private String returnCode;      //  SUCCESS/FAIL

    @Setter
    @Getter
    @XStreamAlias("return_msg")
    private String returnMsg;

    @Setter
    @Getter
    @XStreamAlias("appid")
    private String appId;

    @Setter
    @Getter
    @XStreamAlias("mch_id")
    private String mchId;

    @Setter
    @Getter
    @XStreamAlias("nonce_str")
    private String nonceStr;

    @Setter
    @Getter
    @XStreamAlias("sign")
    private String sign;

    @Setter
    @Getter
    @XStreamAlias("transaction_id")
    private String transactionId;

    @Setter
    @Getter
    @XStreamAlias("result_code")
    private String resultCode;

    @Setter
    @Getter
    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @Setter
    @Getter
    @XStreamAlias("out_refund_no")
    private String outRefundNo;

    @Setter
    @Getter
    @XStreamAlias("refund_id")
    private String refundId;

    @Setter
    @Getter
    @XStreamAlias("refund_channel")
    private String refundChannel;

    @Setter
    @Getter
    @XStreamAlias("refund_fee")
    private String refundFee;

    @Setter
    @Getter
    @XStreamAlias("coupon_refund_fee")
    private String couponRefundFee;

    @Setter
    @Getter
    @XStreamAlias("total_fee")
    private String totalFee;

    @Setter
    @Getter
    @XStreamAlias("cash_fee")
    private String cashFee;

    @Setter
    @Getter
    @XStreamAlias("coupon_refund_count")
    private String couponRefundCount;

    @Setter
    @Getter
    @XStreamAlias("cash_refund_fee")
    private String cashRefundFee;

    @Setter
    @Getter
    @XStreamAlias("err_code")
    private String errCode;

    @Setter
    @Getter
    @XStreamAlias("err_code_des")
    private String errCodeDes;

}
