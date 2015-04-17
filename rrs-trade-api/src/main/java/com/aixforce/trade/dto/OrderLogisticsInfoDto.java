package com.aixforce.trade.dto;

import com.aixforce.trade.model.OrderLogisticsInfo;
import lombok.Data;
import java.io.Serializable;

/**
 * Author: haolin
 * On: 9/25/14
 */
@Data
public class OrderLogisticsInfoDto implements Serializable {

    private static final long serialVersionUID = 4102286304947273775L;

    private OrderLogisticsInfo orderLogisticsInfo;      //物流信息

    private Boolean hasInstall;                         //是否有安装信息

    private Integer installType;                        //安装类型

    private String installName;                         //安装名称
}
