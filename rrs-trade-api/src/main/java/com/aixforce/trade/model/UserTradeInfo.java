/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-09
 */
public class UserTradeInfo implements Serializable {

    private static final long serialVersionUID = -4244823077635817308L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer status;//1代表有效, -1代表删除

    @Getter
    @Setter
    private String phone;

    @Getter
    @Setter
    private String province;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String district;

    @Getter
    @Setter
    private String street;

    @Getter
    @Setter
    private Integer provinceCode;

    @Getter
    @Setter
    private Integer cityCode;

    @Getter
    @Setter
    private Integer districtCode;

    @Getter
    @Setter
    private String zip;

    @Getter
    @Setter
    private Integer isDefault; //是否默认地址 0-非默认,1-默认

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, name, district, phone, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserTradeInfo)) {
            return false;
        }
        UserTradeInfo that = (UserTradeInfo) obj;
        return Objects.equal(userId, that.userId) && Objects.equal(name, that.name) && Objects.equal(district, that.district)
                && Objects.equal(phone, that.phone) && Objects.equal(status, that.status);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("name", name)
                .add("province", province)
                .add("city", city)
                .add("district", district)
                .add("provinceCode", provinceCode)
                .add("cityCode", cityCode)
                .add("districtCode", districtCode)
                .add("zip", zip)
                .add("isDefault", isDefault)
                .add("phone", phone)
                .add("status", status)
                .omitNullValues().toString();
    }
}
