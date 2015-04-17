/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


public class ItemDetail implements Serializable {

    private static final long serialVersionUID = -983402074905798577L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long itemId;

    @Getter
    @Setter
    private String image1;

    @Getter
    @Setter
    private String image2;

    @Getter
    @Setter
    private String image3;

    @Getter
    @Setter
    private String image4;

    //物流使用的数据字段
    @Getter
    @Setter
    private Integer freightSize;       //尺寸大小,允许一位小数，所以乘10后保存

    @Getter
    @Setter
    private Integer freightWeight;     //重量，允许一位小数，所以乘10后保存

    @Getter
    @Setter
    private String packingList;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ItemDetail)) {
            return false;
        }
        ItemDetail that = (ItemDetail) obj;
        return Objects.equal(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("itemId", itemId).add("image1", image1).add("image2", image2)
                .add("image3", image3).add("image4", image4).add("packingList", packingList).omitNullValues().toString();
    }
}
