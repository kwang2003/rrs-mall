package com.aixforce.trade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-9-26
 */
@Data
public class BuyingFatOrder implements Serializable {
    private static final long serialVersionUID = -4751712438771103820L;

    private Long sellerId;

    private Long buyingTempOrderId; //抢购虚拟订单id

    private String buyerNotes;  //买家留言

    private Integer invoiceType; //发票类型，1为普通发票，2为增值税发票private String

    private String invoice;    //发票信息（json） 只有在类型为普通税发票的时候才有值

    private String deliverTime;     //送达时段

    private Integer payType; //付款类型

    private Long buyingActivityId;  //抢购活动id

    private Long skuId;

    private Integer quantity;

    private Long activityId;    //优惠码活动id

    private String codeName;    //优惠码名称
}
