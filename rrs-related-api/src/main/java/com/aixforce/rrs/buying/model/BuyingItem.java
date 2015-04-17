package com.aixforce.rrs.buying.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢购活动和商品关联
 * Created by songrenfei on 14-9-22.
 */
@ToString
@EqualsAndHashCode
public class BuyingItem implements Serializable {


    private static final long serialVersionUID = -4124794261378724996L;

    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long itemId;            //商品id

    @Getter
    @Setter
    private Long buyingActivityId;            //抢购活动定义id

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
    private Integer buyLimit;   //每个用户限购数

    @Getter
    @Setter
    private Integer fakeSoldQuantity;      //虚拟销量

    @Getter
    @Setter
    private Boolean isStorage;      //是否支持分仓

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间


}
