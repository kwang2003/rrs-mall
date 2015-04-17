package com.aixforce.rrs.settle.model;

import com.google.common.base.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单结算
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@ToString
@EqualsAndHashCode
public class Settlement implements Serializable {
    private static final long serialVersionUID = 3285164731495245404L;

    @Getter
    @Setter
    private Long id;                            // 自增主键

    @Getter
    @Setter
    private Long orderId;                       // 订单id

    @Getter
    @Setter
    private Long sellerId;                      // 卖家id

    @Getter
    @Setter
    private String sellerName;                  // 卖家名称

    @Getter
    @Setter
    private Long buyerId;                       // 买家id

    @Getter
    @Setter
    private String buyerName;                   // 买家名称

    @Getter
    @Setter
    private Long business;                      // 订单所属行业id 根据不同行业抽取不同的佣金润点

    @Getter
    @Setter
    private Integer tradeStatus;                // 订单状态 (同order.status)

    @Getter
    @Setter
    private Integer type;                       // 参考订单交易类型, 1:普通交易, 2:预售定金, 3:预售尾款

    @Getter
    @Setter
    private Long fee;                           // 订单金额, 用以计算货到付款的平台佣金

    @Getter
    @Setter
    private Integer payType;                    // 参考订单支付方式, 1:在线支付, 2:货到付款, 3:积分

    @Getter
    @Setter
    private String paymentCode;                 // 交易流水

    @Getter
    @Setter
    private Integer multiPaid;                  // 是否合并支付的订单

    @Getter
    @Setter
    private Long totalEarning;                  // 交易总收入

    @Getter
    @Setter
    private Long totalExpenditure;              // 交易总支出

    @Getter
    @Setter
    private Long sellerEarning;                 // 商家收入

    @Getter
    @Setter
    private Long rrsCommission;                 // 平台佣金收入

    @Getter
    @Setter
    private Long scoreEarning;                  // 积分收入

    @Getter
    @Setter
    private Long presellDeposit;                // 预售定金收入

    @Getter
    @Setter
    private Long presellCommission;             // 营业外收入

    @Getter
    @Setter
    private Long thirdPartyCommission;          // 第三方佣金收入

    @Getter
    @Setter
    private Double commissionRate;              // 佣金扣点

    @Getter
    @Setter
    private String voucher;                     // 凭据号

    @Getter
    @Setter
    private String thirdPartyReceipt;           // 第三方(如支付宝)手续费发票号

    @Getter
    @Setter
    private Integer settleStatus;               // 结算状态 0:待结算,1:结算中, 2:待确认, 3:已确认, 4:已结算

    @Getter
    @Setter
    private Boolean fixed;                      // 是否补帐

    @Getter
    @Setter
    private Integer cashed;                     // 提现状态 0:未提现 1:已提现

    @Getter
    @Setter
    private Integer finished;                   // 完成/关闭状态 0:未完成 1:已完成

    @Getter
    @Setter
    private Integer settled;                    // 是否结算完成 0:未完成 1:已完成

    @Getter
    @Setter
    private Integer confirmed;                  // 商家确认 0:未确认 1:已确认

    @Getter
    @Setter
    private Integer synced;                     // 是否同步JDE完成 0:未完成 1:已完成

    @Getter
    @Setter
    private Integer vouched;                    // 是否更新凭据完成 0:未完成 1:已完成

    @Getter
    @Setter
    private Date orderedAt;                     // 下单时间

    @Getter
    @Setter
    private Date paidAt;                        // 付款时间

    @Getter
    @Setter
    private Date finishedAt;                    // 订单结束时间

    @Getter
    @Setter
    private Date settledAt;                     // 结算时间(商户确认的时间)

    @Getter
    @Setter
    private Date confirmedAt;                   // 确认时间

    @Getter
    @Setter
    private Date syncedAt;                      // 同步时间

    @Getter
    @Setter
    private Date vouchedAt;                     // 更新凭证时间

    @Getter
    @Setter
    private Date thirdPartyReceiptAt;           // 更新凭证时间

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 修改时间


    public static enum TradeStatus {        // 结算需要关注的订单状态
        PAID(1, "买家已付款,等待卖家发货"),
        DONE(3, "交易成功"),
        CANCELED_BY_BUYER(-1, "买家关闭交易"),
        CANCELED_BY_REFUND(-3, "卖家已退款,关闭交易"),
        CANCELED_BY_RETURN_GOODS(-4, "卖家确认收到退货,关闭交易"),
        CANCELED_BY_REMAIN_EXPIRE(-6, "付尾款超时,关闭交易"),
        CANCELED_PRESALE_DEPOSIT_BY_BUYER(-7, "买家关闭已付定金的预售订单");


        private final int value;

        private final String description;

        private TradeStatus(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static TradeStatus from(final int value) {
            for (TradeStatus status : TradeStatus.values()) {
                if (Objects.equal(status.value, value)) {
                    return status;
                }
            }
            return null;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public static enum Type {
        PLAIN(1, "普通订单"),
        PRE_SELL(2,"预售订单");


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

    public static enum PayType {
        ONLINE(1, "在线支付"),
        COD(2,"货到付款"),
        SCORE(3,"积分");


        private final int value;

        private final String description;

        private PayType(int value, String description) {
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


    public static enum SettleStatus {
        NOT(0, "待结算"),
        ING(1,"结算中"),
        CONFIRMING(2, "待确认"),
        CONFIRMED(3, "已确认"),
        FINISH(4,"已结算"),
        FAIL(-1,"结算失败");


        private final int value;

        private final String description;

        private SettleStatus(int value, String description) {
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

    public static enum Cashed {
        NOT(0, "未结算"),
        DONE(1, "已提现");

        private final int value;

        private final String description;

        private Cashed(int value, String description) {
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


    public static enum Finished {
        NOT(0, "未完成/未关闭"),
        DONE(1, "已完成/已关闭"),
        FAIL(-1, "失败");

        private final int value;

        private final String description;

        private Finished(int value, String description) {
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

    public static enum Settled {
        NOT(0, "未结算"),
        DONE(1,"已结算");


        private final int value;

        private final String description;

        private Settled(int value, String description) {
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


    public static enum Confirmed {
        NOT(0, "未确认"),
        DONE(1,"已确认");


        private final int value;

        private final String description;

        private Confirmed(int value, String description) {
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

    public static enum Synced {
        NOT(0, "未同步"),
        DONE(1,"已同步");


        private final int value;

        private final String description;

        private Synced(int value, String description) {
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

    public static enum Vouched {
        NOT(0, "未开票"),
        DONE(1,"已开票");


        private final int value;

        private final String description;

        private Vouched(int value, String description) {
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

    public static enum MultiPaid {
        NOT(0, "非合并支付订单"),
        YES(1,"合并支付订单");


        private final int value;

        private final String description;

        private MultiPaid(int value, String description) {
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
