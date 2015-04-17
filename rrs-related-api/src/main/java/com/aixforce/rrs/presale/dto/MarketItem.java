package com.aixforce.rrs.presale.dto;

import com.aixforce.item.model.Item;
import com.aixforce.rrs.presale.model.PreSale;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-2-13
 */
@ToString
public class MarketItem implements Serializable{

    private static final long serialVersionUID = 8499144076958774869L;
    @Getter
    @Setter
    private PreSale preSale;

    @Getter
    @Setter
    private Item item;
}
