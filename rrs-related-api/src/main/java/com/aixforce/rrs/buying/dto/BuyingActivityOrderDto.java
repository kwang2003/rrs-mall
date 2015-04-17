package com.aixforce.rrs.buying.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author songrenfei on 14-9-23
 * @Desc:抢购活动信息和订单信息
 */
public class BuyingActivityOrderDto implements Serializable {


    private static final long serialVersionUID = 3267009625298163425L;

    @Getter
    @Setter
    private Long orderId; //订单编号

    @Getter
    @Setter
    private Long buyerId; //买家id

    @Getter
    @Setter
    private String name; //买家用户名

    @Getter
    @Setter
    private Long itemId; //商品id


    @Getter
    @Setter
    private Integer status; //订单状态

    @Getter
    @Setter
    private Date createdAt; //订单创建时间

    @Getter
    @Setter
    private Integer discount;   //减免金额

    @Getter
    @Setter
    private Integer originPrice;   //原价

    @Getter
    @Setter
    private Integer price;   //抢购价



}
