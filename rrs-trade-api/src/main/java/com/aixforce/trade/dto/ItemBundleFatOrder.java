package com.aixforce.trade.dto;

import com.aixforce.item.model.ItemBundle;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yangzefeng on 14-4-25
 */
@ToString
public class ItemBundleFatOrder extends FatOrder {

    private static final long serialVersionUID = -8569484176468098056L;

    @Getter
    @Setter
    private ItemBundle itemBundle;
}
