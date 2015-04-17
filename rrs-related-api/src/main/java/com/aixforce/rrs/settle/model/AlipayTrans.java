package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 10:58 AM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class AlipayTrans implements Serializable {

    private static final long serialVersionUID = 2559576020799809499L;

    private static final BigDecimal ratio = new BigDecimal("100");  // 元转分时的倍率

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String balance;                 // 账户余额

    @Getter
    @Setter
    private String bankAccountName;         // 银行账户名称

    @Getter
    @Setter
    private String bankAccountNo;           // 银行账户

    @Getter
    @Setter
    private String bankName;                // 银行名

    @Getter
    @Setter
    private String buyerName;               // 买家姓名

    @Getter
    @Setter
    private String buyerAccount;            // 买家账户

    @Getter
    @Setter
    private String currency;                // 货币代码

    @Getter
    @Setter
    private String depositBankNo;           // 充值网银流水

    @Getter
    @Setter
    private String goodsTitle;

    @Getter
    @Setter
    private String income;                  // 收入金额

    @Getter
    @Setter
    private String iwAccountLogId;          // 帐务流水

    @Getter
    @Setter
    private String memo;                    // 备注

    @Getter
    @Setter
    private String merchantOutOrderNo;      // 订单id

    @Getter
    @Setter
    private String otherAccountEmail;       // 帐务对方邮箱

    @Getter
    @Setter
    private String otherAccountFullname;    // 帐务对方全称

    @Getter
    @Setter
    private String otherUserId;             // 帐务对方支付宝用户号

    @Getter
    @Setter
    private String outcome;                 // 支出金额

    @Getter
    @Setter
    private String partnerId;               // 合作者身份id

    @Getter
    @Setter
    private String sellerAccount;           // 买家支付宝人民币支付帐号(user_id+0156)

    @Getter
    @Setter
    private String sellerFullname;          // 卖家姓名

    @Getter
    @Setter
    private String serviceFee;              // 交易服务费

    @Getter
    @Setter
    private String serviceFeeRatio;         // 交易服务费率

    @Getter
    @Setter
    private String totalFee;                // 交易总金额

    @Getter
    @Setter
    private String tradeNo;                 // 支付宝交易流水

    @Getter
    @Setter
    private String tradeRefundAmount;       // 累计退款金额

    @Getter
    @Setter
    private String transAccount;            // 帐务本方支付宝人民币资金帐号

    @Getter
    @Setter
    private String transCodeMsg;            // 业务类型

    @Getter
    @Setter
    private String transDate;               // 交易发生日期

    @Getter
    @Setter
    private String transOutOrderNo;         // 商户订单号

    @Getter
    @Setter
    private String subTransCodeMsg;         // 子业务类型代码

    @Getter
    @Setter
    private String signProductName;         // 签约产品

    @Getter
    @Setter
    private String rate;                    // 费率

    @Getter
    @Setter
    private Date createdAt;                 // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                 // 更新时间


    /**
     * 将支付宝的金额转换成内部的单位计算（单位：分）
     */
    public Long getOutcomeOfFen() {
        if (outcome == null) {
            return 0L;
        }

        BigDecimal money = new BigDecimal(outcome);
        return money.multiply(ratio).longValue();
    }

    /**
     * 将支付宝的付款款金额转换成内部的单位计算（单位：分）
     */
    public Long getTotalFeeOfFen() {
        if (totalFee == null) {
            return 0L;
        }

        BigDecimal money = new BigDecimal(totalFee);
        return money.multiply(ratio).longValue();
    }
}
