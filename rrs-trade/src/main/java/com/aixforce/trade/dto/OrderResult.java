package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-05
 */
@ToString
public class OrderResult implements Serializable {
    private static final long serialVersionUID = 3190478587881041174L;

    @Getter
    @Setter
    private Map<Long,Long> sellerIdAndOrderId;

    @Getter
    @Setter
    private List<StockChange> stockChanges;
}
