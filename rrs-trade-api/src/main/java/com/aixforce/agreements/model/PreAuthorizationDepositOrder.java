package com.aixforce.agreements.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 预授权押金状态表
 * Created by neusoft on 14-11-21.
 */
@ToString
@Data
public class PreAuthorizationDepositOrder implements Serializable {
    private static final long serialVersionUID = -3295476126494319121L;

    @Getter
    @Setter
    private Long id;// 自增主键

    @Getter
    @Setter
    private Long orderId;// 订单id

    @Getter
    @Setter
    private String tradeNo;//付宝支交易流水号

    @Getter
    @Setter
    private Integer status;//预授权押金状态：0未冻结资金、1资金冻结、2资金扣除、-1资金解冻（退款）

    @Getter
    @Setter
    private Integer type;//类型：1预授权，2押金

    @Getter
    @Setter
    private String payerLogonId;// 付款方支付宝账号

    @Getter
    @Setter
    private String payerUserId;//付款方支付宝用户号

    @Getter
    @Setter
    private Integer deliverStatus;//发货状态 0 未发货 1 已发货


    public static enum DepositPayType {

        NOPAY(0, "未支付"),
        PAYED(1, "冻结"),
        PAYFINNSH(3, "交易完成"),
        DELIVER(-1, "申请退货"),
        DELIVERED(-2, "退货申请通过"),
        DELIVERFINNSH(-3, "退货完成（押金已退）"),
        LOST(-4, "失联");

        private final int value;

        private final String description;

        private DepositPayType(int value, String description) {
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
