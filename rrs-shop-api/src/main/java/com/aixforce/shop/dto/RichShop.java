package com.aixforce.shop.dto;

import com.aixforce.common.model.Indexable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 13-12-29
 */
public class RichShop implements Serializable, Indexable{
    private static final long serialVersionUID = -6668563515797562110L;

    @Getter
    @Setter
    private Long id;   //店铺id

    @Getter
    @Setter
    private Long userId;  //卖家id

    @Getter
    @Setter
    private String userName;  //卖家nick

    @Getter
    @Setter
    private String name;  //店铺名称,要求唯一


    @Getter
    @Setter
    private Integer status; //店铺状态


    @Getter
    @Setter
    private String phone;    //电话

    @Getter
    @Setter
    private String email;    //电子邮件

    @Getter
    @Setter
    private Long businessId;  //店铺类目

    @Getter
    @Setter
    private String imageUrl;    //店铺主图

    @Getter
    @Setter
    private String provinceName;   //店铺所在省份

    @Getter
    @Setter
    private String cityName;      //店铺所在市

    @Getter
    @Setter
    private String regionName;    //店铺所在区域

    @Getter
    @Setter
    private String street;     //店铺所在街道

    @Getter
    @Setter
    private Integer itemCount; //店铺宝贝数

    @Getter
    @Setter
    private Integer soldQuantity; //出售商品数量

    @Getter
    @Setter
    private Long sale; //总销售额

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    @Getter
    @Setter
    private Boolean deliveryTime;       //是否支持配送时段

    @Getter
    @Setter
    private Boolean deliveryPromise;    //是否支持配送承诺
}
