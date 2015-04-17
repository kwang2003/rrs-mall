/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-17
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = -7547958430147682873L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String birthday;

    @Getter
    @Setter
    @Min(0)
    private Long userId;

    @Getter
    @Setter
    @Length(max = 10, message = "真实姓名不能超过10个字符")
    private String realName;

    @Getter
    @Setter
    @Length(max = 20, message = "身份证号不能超过20个字符")
    private String idCardNum;

    @Getter
    @Setter
    private Integer gender;  //1-male, 2-female

    @Getter
    @Setter
    private Integer provinceId;  //see address

    @Getter
    @Setter
    private Integer cityId;  //see address

    @Getter
    @Setter
    private Integer regionId; //see address

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    @Length(max = 20, message = "qq号码不能超过20个字符")
    private String qq;

    @Getter
    @Setter
    @Min(0)
    private Integer buyerCredit;

    @Getter
    @Setter
    @Min(0)
    private Integer sellerCredit;

    @Getter
    @Setter
    @Length(max = 100, message = "描述不能超过100个字符")
    private String description;

    @Getter
    @Setter
    private String extra;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    @Getter
    @Setter
    private String phone; //联系电话,在注册成功后添加

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UserProfile)) {
            return false;
        }
        UserProfile that = (UserProfile) o;
        return Objects.equal(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
