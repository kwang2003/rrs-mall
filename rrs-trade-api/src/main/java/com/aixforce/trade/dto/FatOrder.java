package com.aixforce.trade.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * 买家提交订单时提供的信息,此时尚未创建订单
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
@ToString @EqualsAndHashCode
public class FatOrder implements Serializable {
    private static final long serialVersionUID = 3471037279746051441L;

    @Getter
    @Setter
    private Long sellerId;   //卖家id

    @Getter
    @Setter
    private String buyerNotes;  //买家留言

    @Getter
    @Setter
    private Integer invoiceType; //发票类型，1为普通发票，2为增值税发票
    @Getter
    @Setter
    private String invoice;    //发票信息（json） 只有在类型为普通税发票的时候才有值

    @Getter
    @Setter
    private String deliverTime;     //送达时段

    @Getter
    @Setter
    private Integer paymentType; //付款类型

    @Getter
    @Setter
    private String codeName;    //优惠码名称

    @Getter
    @Setter
    private Long activityId;    //活动id

    @Getter
    @Setter
    private Map<Long, Integer> skuIdAndQuantity; // sku的id和数量

    @Getter
    @Setter
    private String deliverType;     //配送方式：0 物流配送 1 到店自提

    @Getter
    @Setter
    private Long couponIds;//选择的商家优惠券信息
}
