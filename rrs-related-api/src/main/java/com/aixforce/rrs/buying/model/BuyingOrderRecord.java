package com.aixforce.rrs.buying.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢购活动和订单关联
 * Created by songrenfei on 14-9-22.
 */
@ToString
@EqualsAndHashCode
public class BuyingOrderRecord implements Serializable {


    private static final long serialVersionUID = -2389168808898650598L;

    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long orderId;            //订单id

    @Getter
    @Setter
    private Long buyingActivityId;            //抢购活动定义id

    @Getter
    @Setter
    private Long itemId;            //商品id

    @Getter
    @Setter
    private Long buyerId;            //买家id

    @Getter
    @Setter
    private Long sellerId;            //卖家id

    @Getter
    @Setter
    private Integer quantity;      //购买数量

    @Getter
    @Setter
    private Integer itemOriginPrice;      //商品原价

    @Getter
    @Setter
    private Integer itemBuyingPrice;   //商品抢购价

    @Getter
    @Setter
    private Integer discount;   //减免金额

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间


}
