package com.aixforce.rrs.predeposit.dto;

import com.aixforce.item.model.Item;
import com.aixforce.rrs.predeposit.model.PreDeposit;
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
    private PreDeposit preDeposit;

    @Getter
    @Setter
    private Item item;
}
