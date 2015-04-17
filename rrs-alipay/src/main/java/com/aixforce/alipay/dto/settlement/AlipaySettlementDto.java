package com.aixforce.alipay.dto.settlement;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 10:50 AM  <br>
 * Author: xiao
 */
@ToString
@XStreamAlias("AccountQueryAccountLogVO")
public class AlipaySettlementDto {

    @Getter
    @Setter
    @XStreamAlias("balance")
    private String balance;

    @Getter
    @Setter
    @XStreamAlias("bank_account_name")
    private String bankAccountName;

    @Getter
    @Setter
    @XStreamAlias("bank_account_no")
    private String bankAccountNo;

    @Getter
    @Setter
    @XStreamAlias("bank_name")
    private String bankName;

    @Getter
    @Setter
    @XStreamAlias("buyer_name")
    private String buyerName;

    @Getter
    @Setter
    @XStreamAlias("buyer_account")
    private String buyerAccount;

    @Getter
    @Setter
    @XStreamAlias("currency")
    private String currency;

    @Getter
    @Setter
    @XStreamAlias("deposit_bank_no")
    private String depositBankNo;

    @Getter
    @Setter
    @XStreamAlias("goods_title")
    private String goodsTitle;

    @Getter
    @Setter
    @XStreamAlias("income")
    private String income;

    @Getter
    @Setter
    @XStreamAlias("iw_account_log_id")
    private String iwAccountLogId;

    @Getter
    @Setter
    @XStreamAlias("memo")
    private String memo;

    @Getter
    @Setter
    @XStreamAlias("merchant_out_order_no")
    private String merchantOutOrderNo;

    @Getter
    @Setter
    @XStreamAlias("other_account_email")
    private String otherAccountEmail;

    @Getter
    @Setter
    @XStreamAlias("other_account_fullname")
    private String otherAccountFullname;

    @Getter
    @Setter
    @XStreamAlias("other_user_id")
    private String otherUserId;

    @Getter
    @Setter
    @XStreamAlias("outcome")
    private String outcome;

    @Getter
    @Setter
    @XStreamAlias("partner_id")
    private String partnerId;

    @Getter
    @Setter
    @XStreamAlias("seller_account")
    private String sellerAccount;

    @Getter
    @Setter
    @XStreamAlias("seller_fullname")
    private String sellerFullname;

    @Getter
    @Setter
    @XStreamAlias("service_fee")
    private String serviceFee;

    @Getter
    @Setter
    @XStreamAlias("service_fee_ratio")
    private String serviceFeeRatio;

    @Getter
    @Setter
    @XStreamAlias("total_fee")
    private String totalFee;

    @Getter
    @Setter
    @XStreamAlias("trade_no")
    private String tradeNo;

    @Getter
    @Setter
    @XStreamAlias("trade_refund_amount")
    private String tradeRefundAmount;

    @Getter
    @Setter
    @XStreamAlias("trans_account")
    private String transAccount;

    @Getter
    @Setter
    @XStreamAlias("trans_code_msg")
    private String transCodeMsg;

    @Getter
    @Setter
    @XStreamAlias("trans_date")
    private String transDate;

    @Getter
    @Setter
    @XStreamAlias("trans_out_order_no")
    private String transOutOrderNo;

    @Getter
    @Setter
    @XStreamAlias("sub_trans_code_msg")
    private String subTransCodeMsg;

    @Getter
    @Setter
    @XStreamAlias("sign_product_name")
    private String signProductName;

    @Getter
    @Setter
    @XStreamAlias("rate")
    private String rate;
}
