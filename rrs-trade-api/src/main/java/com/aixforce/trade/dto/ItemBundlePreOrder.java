package com.aixforce.trade.dto;

import com.aixforce.item.model.ItemBundle;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangzefeng on 14-4-23
 */
public class ItemBundlePreOrder implements Serializable {
    private static final long serialVersionUID = 7097313289035205108L;

    @Getter
    @Setter
    private ItemBundle itemBundle;

    @Getter
    @Setter
    private List<RichOrderItem> richOrderItems;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Boolean isCod;
}
