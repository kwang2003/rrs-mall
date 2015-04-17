package com.aixforce.trade.model;

import com.google.common.base.Objects;
import com.rrs.coupons.model.RrsCouponsItemList;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-16
 */
public class UserCart implements Serializable {
    private static final long serialVersionUID = -9195110058432963531L;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private String shopImage;

    @Getter
    @Setter
    private int countCou;

    @Getter
    @Setter
    private List<RrsCouponsItemList> shopCoupons;

    @Getter
    @Setter
    private List<CartItem> cartItems;

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("shopId", shopId)
                .add("sellerId", sellerId).add("shopName", shopName)
                .add("cartItems", cartItems).omitNullValues().toString();
    }
}
