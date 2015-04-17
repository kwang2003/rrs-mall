package com.rrs.brand.model;


import com.aixforce.user.base.BaseUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by temp on 2014/7/10.
 */
@ToString
@EqualsAndHashCode
public class BrandUser extends BaseUser{
    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String flag;

    @Getter
    @Setter
    private String passWord;

    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private String resourcePassword;

    @Getter
    @Setter
    private String frozenStatus;

}
