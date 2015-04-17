package com.aixforce.trade.dto;

import com.aixforce.item.model.Sku;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
public class RichOrderItem implements Serializable {

    private static final long serialVersionUID = -285764361165319218L;
    @Getter
    @Setter
    private Long orderItemId;

    @Getter
    @Setter
    private Sku sku;

    @Getter
    @Setter
    private String itemName;

    @Getter
    @Setter
    private String itemImage;

    @Getter
    @Setter
    private int count;

    @Getter
    @Setter
    private Integer discount;

    @Getter
    @Setter
    private int unitFee; // 单价，运行时由 fee / count 算出来的

    @Getter
    @Setter
    private int fee;

    @Getter
    @Setter
    private Integer deliverFee;     //用于保存运费信息（这个阶段还未保存到数据库）

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    private String reason;

    @Getter
    @Setter
    private Integer orderItemType; //子订单类型

    @Getter
    @Setter
    private Integer refundAmount;

    @Getter
    @Setter
    private String deliveryPromise;     //送达承诺

    @Getter
    @Setter
    private Date requestRefundAt;

    @Getter
    @Setter
    private Integer paymentType;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private BigDecimal couponAmount;

    @Getter
    @Setter
    private Boolean isEhaier;               //是否是ehaier商家

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("sku", sku)
                .add("itemName", itemName)
                .add("count", count)
                .add("discount", discount)
                .add("fee", fee)
                .add("status", status)
                .add("reason", reason)
                .add("refundAmount", refundAmount)
                .add("requestRefundAt", requestRefundAt)
                .add("paymentType", paymentType)
                .omitNullValues().toString();
    }

}
