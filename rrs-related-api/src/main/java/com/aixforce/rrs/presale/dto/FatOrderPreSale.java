package com.aixforce.rrs.presale.dto;

import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.trade.dto.FatOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yangzefeng on 14-2-14
 */
@ToString
public class FatOrderPreSale extends FatOrder{

    private static final long serialVersionUID = -185193156698887331L;
    @Getter
    @Setter
    private PreSale preSale;
}
