package com.aixforce.trade.dto;

import com.google.common.base.Objects;
import com.rrs.coupons.model.RrsCouOrderItem;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-11
 */
public class RichOrderBuyerView extends RichOrder implements Serializable {

    private static final long serialVersionUID = -6508586529830576828L;

    @Getter
    @Setter
    private String siteName;

    @Getter
    @Setter
    private Long siteId;

    @Getter
    @Setter
    private String shopImage;

    @Getter
    @Setter
    private Boolean canConfirm; //是否能进行确认收货操作

    @Getter
    @Setter
    private Boolean canComment; // 是否能评论订单，不持久化



    @Getter
    @Setter
    private List<RrsCouOrderItem> couponList; // 优惠券使用情况

    @Getter
    @Setter
    private String systemDate; // 当前系统时间

    @Getter
    @Setter
    private Boolean buyingCanPay;   //如果是抢购订单是否可以付款
    @Getter
    @Setter
    private Integer deliverType;     //配送方式：0 物流配送 1 到店自提


    @Override
    public int hashCode() {
        return Objects.hashCode(siteId, orderItems);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RichOrderBuyerView)) {
            return false;
        }
        RichOrderBuyerView that = (RichOrderBuyerView) o;
        return Objects.equal(this.siteId, that.siteId) && Objects.equal(this.orderItems, that.orderItems);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("sellerId", siteId).add("sellerName", siteName)
                .add("orderItems", orderItems).omitNullValues().toString();
    }

}
