package com.aixforce.rrs.settle.dto;

import com.aixforce.rrs.settle.model.SellerSettlement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-10 10:00 AM  <br>
 * Author: xiao
 */
@ToString
public class PrintableSettlementDto extends SellerSettlement {

    private static final long serialVersionUID = -5221021777501694567L;

    @Getter
    @Setter
    private String shopName;

}
