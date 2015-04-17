package com.aixforce.shop.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺内商品类目
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-07-11
 */
@Repository
public class ShopInteriorCategory implements Serializable {
    private static final long serialVersionUID = 7112786102686250010L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private String categories; //以json形式表示的类目树

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    @Override
    public int hashCode() {
        return Objects.hashCode(shopId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ShopInteriorCategory)) {
            return false;
        }
        ShopInteriorCategory that = (ShopInteriorCategory) obj;

        return Objects.equal(this.shopId, that.shopId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("shopId", shopId)
                .add("categories", categories).omitNullValues().toString();
    }
}
