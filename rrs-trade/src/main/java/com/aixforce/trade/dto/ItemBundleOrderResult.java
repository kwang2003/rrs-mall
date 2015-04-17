package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-4-25
 */
@ToString
public class ItemBundleOrderResult implements Serializable {
    private static final long serialVersionUID = 3237997228316942781L;

    @Getter
    @Setter
    private Long orderId;

    @Getter
    @Setter
    private List<StockChange> stockChanges;
}
