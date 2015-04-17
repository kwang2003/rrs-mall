package com.aixforce.item.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 带tag的商品基本信息,用作dto
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-02
 */
public class ItemWithTags implements Serializable {

    private static final long serialVersionUID = 1475501216750152089L;
    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private String itemName;

    @Getter
    @Setter
    private String imageUrl;

    @Getter
    @Setter
    private List<String> tags;

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    private Integer price;
    @Getter
    @Setter
    private Integer quantity;

    @Getter
    @Setter
    private Integer soldQuantity;

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ItemWithTags)) {
            return false;
        }
        ItemWithTags that = (ItemWithTags) o;
        return Objects.equal(this.itemId, that.itemId) && Objects.equal(this.tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.itemId, this.tags);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("itemId", itemId)
                .add("itemName", itemName)
                .add("imageUrl", imageUrl)
                .add("tags", tags)
                .add("status", status)
                .add("price", price)
                .add("quantity", quantity)
                .add("soldQuantity", soldQuantity)
                .omitNullValues()
                .toString();
    }
}
