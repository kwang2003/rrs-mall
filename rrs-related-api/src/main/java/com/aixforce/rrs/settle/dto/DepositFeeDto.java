package com.aixforce.rrs.settle.dto;

import com.aixforce.rrs.settle.model.DepositFee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-14 5:50 PM  <br>
 * Author: xiao
 */
@ToString
public class DepositFeeDto extends DepositFee {

    private static final long serialVersionUID = -6226300330025196172L;
    @Getter
    @Setter
    private Double depositOfYuan;

}
