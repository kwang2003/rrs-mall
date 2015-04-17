package com.aixforce.trade.model;

import com.aixforce.item.model.Sku;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = -6698597450849302906L;

    @Getter
    @Setter
    private Sku sku;

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    private String itemImage;

    @Getter
    @Setter
    private String itemName;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private Integer count;

    @Getter
    @Setter
    private String region;

    @Override
    public int hashCode() {
        return Objects.hashCode(sku, count);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof CartItem)) {
            return false;
        }
        CartItem that = (CartItem) o;
        return Objects.equal(sku, that.sku) && Objects.equal(count, that.count);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("sku", sku).add("count", count).omitNullValues().toString();
    }
}
