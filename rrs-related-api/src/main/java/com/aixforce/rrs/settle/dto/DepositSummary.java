package com.aixforce.rrs.settle.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-18 9:25 AM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DepositSummary implements Serializable {

    private static final long serialVersionUID = -625559094242592153L;

    @Getter
    @Setter
    private DepositAccountDto depositAccountDto;    // 保证金余额

    @Getter
    @Setter
    private TechFeeSummaryDto techFeeSummaryDto;    // 技术服务费汇总

}
