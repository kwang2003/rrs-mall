package com.aixforce.rrs.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author songrenfei on 14-7-7.
 * @Desc:
 */
public class CodeOrderDto implements Serializable {


    private static final long serialVersionUID = 7083149911326233619L;

    @Getter
    @Setter
    private Long orderId; //订单编号

    @Getter
    @Setter
    private Long buyerId; //买家id


    @Getter
    @Setter
    private Integer status; //订单状态

    @Getter
    @Setter
    private Date createdAt; //订单创建时间

    @Getter
    @Setter
    private Integer discount;   //优惠金额

    @Getter
    @Setter
    private Integer originPrice;   //优惠前金额

    @Getter
    @Setter
    private Integer price;   //优惠后金额

    @Getter
    @Setter
    private String code;   //优惠码


}
