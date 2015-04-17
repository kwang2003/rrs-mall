package com.aixforce.rrs.settle.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 技术服务费实收和应收
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-10 11:31 AM  <br>
 * Author: xiao
 */
@ToString
public class TechFeeSummaryDto implements Serializable {

    private static final long serialVersionUID = -650087561745733381L;
    @Getter
    @Setter
    private Long techFeeActual;      // 实收金额


    @Getter
    @Setter
    private Long techFeeNeed;        // 应收金额


}
