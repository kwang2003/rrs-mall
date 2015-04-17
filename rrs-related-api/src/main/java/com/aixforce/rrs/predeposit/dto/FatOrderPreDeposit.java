package com.aixforce.rrs.predeposit.dto;

import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.trade.dto.FatOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by yangzefeng on 14-2-14
 */
@ToString
public class FatOrderPreDeposit extends FatOrder{

    private static final long serialVersionUID = -185193156698887331L;
    @Getter
    @Setter
    private PreDeposit preDeposit;
}
