package com.aixforce.collect.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 2:14 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class CollectedItem implements Serializable {


    private static final long serialVersionUID = 7595152878069322170L;
    @Getter
    @Setter
    private Long id;                            // 主键

    @Getter
    @Setter
    private Long buyerId;                       // 买家id

    @Getter
    @Setter
    private Long itemId;                        // 商品id

    @Getter
    @Setter
    private String itemNameSnapshot;            // 商品名称（快照）

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 修改时间

    @Getter
    @Setter
    private Boolean isBuying;                  // 是否是抢购单

    @Getter
    @Setter
    private Long buyingActivityId;            //抢购活动定义id

    @Getter
    @Setter
    private Integer activityStatus; //活动状态

    @Getter
    @Setter
    private  Integer itemBuyingPrice;// 活动价格

    @Getter
    @Setter
    private Long buyingItemId;

    @Getter
    @Setter
    private Long priId;// 主键id,值存放
}
