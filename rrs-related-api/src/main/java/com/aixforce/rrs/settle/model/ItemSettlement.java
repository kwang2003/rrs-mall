package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 子订单结算
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@ToString
@EqualsAndHashCode
public class ItemSettlement implements Serializable {

    private static final long serialVersionUID = 3285164731495245404L;

    @Getter
    @Setter
    private Long id;                        // 自增主键

    @Getter
    @Setter
    private Long orderId;                   // 订单id

    @Getter
    @Setter
    private Long orderItemId;               // 子订单id

    @Getter
    @Setter
    private Long sellerId;                  // 卖家id

    @Getter
    @Setter
    private String sellerName;                  // 卖家名称

    @Getter
    @Setter
    private Long buyerId;                   // 买家id

    @Getter
    @Setter
    private String buyerName;               // 买家名称

    @Getter
    @Setter
    private Long business;                  // 订单所属行业id 根据不同行业抽取不同的佣金润点

    @Getter
    @Setter
    private Integer tradeStatus;            // 订单状态 (同order.status)

    @Getter
    @Setter
    private String itemName;                // 商品名称

    @Getter
    @Setter
    private Integer itemQuantity;           // 商品名称

    @Getter
    @Setter
    private Integer type;                   // 参考订单交易类型, 1:普通交易, 2:预售定金, 3:预售尾款

    @Getter
    @Setter
    private Integer payType;                // 参考订单支付方式, 1:在线支付, 2:货到付款, 3:积分

    @Getter
    @Setter
    private String paymentCode;             // 交易流水

    @Getter
    @Setter
    private Long fee;                       // 订单金额, 用以计算货到付款的平台佣金

    @Getter
    @Setter
    private String reason;                  // 退款理由

    @Getter
    @Setter
    private Long refundAmount;              // 退款金额

    @Getter
    @Setter
    private Long totalEarning;              // 交易总收入

    @Getter
    @Setter
    private Long totalExpenditure;          // 交易总支出

    @Getter
    @Setter
    private Long sellerEarning;             // 商家收入

    @Getter
    @Setter
    private Long rrsCommission;             // 平台佣金收入

    @Getter
    @Setter
    private Long scoreEarning;              // 积分收入

    @Getter
    @Setter
    private Long presellDeposit;            // 预售定金收入

    @Getter
    @Setter
    private Long presellCommission;         // 营收成本

    @Getter
    @Setter
    private Long thirdPartyCommission;      // 第三方佣金收入

    @Getter
    @Setter
    private Double commissionRate;          // 佣金扣点

    @Getter
    @Setter
    private Integer settleStatus;           // 结算状态 1:未结算,2:结算中,3:已结算'

    @Getter
    @Setter
    private Boolean fixed;                  // 是否补帐

    @Getter
    @Setter
    private String voucher;                 // 凭据号，冗余字段

    @Getter
    @Setter
    private String thirdPartyReceipt;       // 第三方(如支付宝)手续费发票号

    @Getter
    @Setter
    private Date paidAt;                    // 付款时间

    @Getter
    @Setter
    private Date settledAt;                 // 结算时间

    @Getter
    @Setter
    private Date confirmedAt;               // 确认时间

    @Getter
    @Setter
    private Date createdAt;                 // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                 // 修改时间

    public static enum Type {
        PLAIN(1, "普通交易"),
        PRESELL_DEPOSIT(2,"预售定金"),
        PRESELL_REST(3,"预售尾款");

        private final int value;

        private final String description;

        private Type(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

}
