package com.aixforce.rrs.grid.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 14-1-16
 */
public class ShopAuthorizeInfo implements Serializable{
    private static final long serialVersionUID = -4329015026075215089L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private String jsonAuthorize; //店铺认证信息，已json存储

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;


    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof ShopAuthorizeInfo)) {
            return false;
        }
        ShopAuthorizeInfo that = (ShopAuthorizeInfo) o;
        return Objects.equal(that.id, id) && Objects.equal(that.shopId, shopId)
                && Objects.equal(that.jsonAuthorize, jsonAuthorize);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, shopId, jsonAuthorize);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("shopId", shopId).add("jsonAuthorize", jsonAuthorize).toString();
    }
}
