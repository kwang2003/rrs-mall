package com.aixforce.shop.dto;

import com.aixforce.shop.model.ShopExtra;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * User: yangzefeng
 * Date: 13-11-22
 * Time: 下午1:27
 */
@SuppressWarnings("unused")
public class ShopDto implements Serializable{

    private static final long serialVersionUID = -7273541575195520829L;
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
    private String taxRegisterNo;       // 税务登记号

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    @Getter
    @Setter
    private String businessLicense; //营业执照

    @Getter
    @Setter
    private ShopExtra extra;
}
