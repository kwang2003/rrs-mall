package com.aixforce.trade.dto;

import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
public class SkuAndItem implements Serializable {
    private static final long serialVersionUID = 4299217764375423591L;

    @Getter
    @Setter
    private Sku sku;

    @Getter
    @Setter
    private Item item;

}
