package com.aixforce.shop.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 店铺基本信息
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-21 10:04 AM  <br>
 * Author: xiao
 */
@ToString
public class ShopBaseInfo implements Serializable {

    private static final long serialVersionUID = -3646637334092056443L;

    @Getter
    @Setter
    private Long userId;            // 商家id

    @Getter
    @Setter
    private String userName;        // 商家名称


    @Getter
    @Setter
    private String name;            // 店铺名称

    @Getter
    @Setter
    private String outerCode;       // 商家编码


}
