package com.aixforce.item.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>gi
 * Date: 2013-02-01
 */
@ToString
public class Sku extends BaseSku {
    private static final long serialVersionUID = -2189348280769719400L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private Integer stock;

    @Getter
    @Setter
    private String image;

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId, attributeName1, attributeValue1, attributeName2, attributeValue2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Sku)) {
            return false;
        }
        Sku that = (Sku) o;

        return Objects.equal(itemId, that.itemId) && Objects.equal(attributeName1, that.attributeName1)
                && Objects.equal(attributeName2, that.attributeName2)
                && Objects.equal(attributeValue1, that.attributeValue1) && Objects.equal(attributeValue2, that.attributeValue2);
    }
}
