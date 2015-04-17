package com.aixforce.shop.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺所属类目
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
@Repository
public class ShopCategory implements Serializable {
    private static final long serialVersionUID = -7007141978843607770L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Long parentId; //对于一级类目,parentId=0

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ShopCategory)) {
            return false;
        }
        ShopCategory that = (ShopCategory) o;
        return Objects.equal(this.name, that.name) && Objects.equal(this.parentId, that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, parentId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("parentId", parentId)
                .omitNullValues()
                .toString();
    }
}
