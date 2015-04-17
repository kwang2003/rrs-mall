package com.aixforce.trade.dto;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用于记录将要变化的库存
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
public class StockChange implements Serializable {
    private static final long serialVersionUID = -5416721427612964149L;

    @Getter
    @Setter
    private Long skuId;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private Integer quantity;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("skuId", skuId)
                .add("itemId", itemId)
                .add("quantity", quantity)
                .toString();
    }
}
