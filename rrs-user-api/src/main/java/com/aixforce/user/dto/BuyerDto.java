package com.aixforce.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * User: yangzefeng
 * Date: 13-11-20
 * Time: 上午10:24
 */
@SuppressWarnings("unused")
public class BuyerDto implements Serializable{

    private static final long serialVersionUID = -2854799947688326125L;
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mobile; //注册时的验证电话

    @Getter
    @Setter
    private String realName;

    @Getter
    @Setter
    private Integer gender;  //1-male, 2-female

    @Getter
    @Setter
    private String birthday;

    @Getter
    @Setter
    private String province;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    private String phone; //联系电话,在注册成功后添加

    @Getter
    @Setter
    private Integer type; // 用户类型，见 BaseUser.TYPE

    public BuyerDto() {

    }
}
