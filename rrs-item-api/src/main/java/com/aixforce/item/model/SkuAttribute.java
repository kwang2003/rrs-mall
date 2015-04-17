package com.aixforce.item.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-01-31
 */
public class SkuAttribute implements Serializable {
    private final static long serialVersionUID = -2199874156059488025L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long skuId;

    @Getter
    @Setter
    private Long attributeKeyId;

    @Getter
    @Setter
    private Long attributeValueId;

    @Override
    public int hashCode() {
        return Objects.hashCode(skuId, attributeKeyId, attributeValueId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SkuAttribute)) {
            return false;
        }
        SkuAttribute that = (SkuAttribute) o;
        return Objects.equal(skuId, that.skuId) && Objects.equal(attributeValueId, that.attributeValueId)
                && Objects.equal(attributeKeyId, that.attributeKeyId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("skuId", skuId).add("attributeKeyId", attributeKeyId)
                .add("attributeValueId", attributeValueId).omitNullValues().toString();
    }
}
