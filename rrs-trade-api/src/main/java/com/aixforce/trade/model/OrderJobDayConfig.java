/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
public class OrderJobDayConfig implements Serializable {

    @Getter
    @Setter
    private Long id;                // id

    @Getter
    @Setter
    private Long skuId;             // 商品SKUID

    @Getter
    @Setter
    private Integer expireDay;       // 自动收货完成时间

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新时间

}
