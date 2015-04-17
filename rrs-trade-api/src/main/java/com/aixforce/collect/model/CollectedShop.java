package com.aixforce.collect.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 3:08 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class CollectedShop implements Serializable {

    private static final long serialVersionUID = 9156547367716294815L;

    @Getter
    @Setter
    private Long id;                            // 主键

    @Getter
    @Setter
    private Long buyerId;                       // 买家id

    @Getter
    @Setter
    private Long sellerId;                      // 商家id


    @Getter
    @Setter
    private Long shopId;                        // 商品id

    @Getter
    @Setter
    private String shopNameSnapshot;            // 店铺名称（快照）

    @Getter
    @Setter
    private String shopLogoSnapshot;            // 店铺logo（快照）

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 修改时间

}
