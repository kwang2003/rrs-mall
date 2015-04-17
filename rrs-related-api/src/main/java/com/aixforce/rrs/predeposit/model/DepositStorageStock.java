package com.aixforce.rrs.predeposit.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 每个仓库里某个商品的库存及销量
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-07-09
 */
public class DepositStorageStock implements Serializable {
    private static final long serialVersionUID = 4998507119059937330L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long itemId;  //商品id

    @Getter
    @Setter
    private Long storageId;// 仓库id

    @Getter
    @Setter
    private Integer initStock;  //初始库存

    @Getter
    @Setter
    private Integer soldCount;  //当前销量

    @Getter
    @Setter
    private Date createdAt;     //创建时间

    @Getter
    @Setter
    private Date updatedAt;     //更新时间

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId, storageId, initStock, soldCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepositStorageStock that = (DepositStorageStock) o;

        return Objects.equal(itemId, that.itemId)
                && Objects.equal(storageId, that.storageId)
                && Objects.equal(initStock, that.initStock)
                && Objects.equal(soldCount, that.soldCount);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("itemId", itemId)
                .add("storageId", storageId)
                .add("initStock", initStock)
                .add("soldCount", soldCount)
                .add("createdAt", createdAt)
                .add("updatedAt", updatedAt)
                .omitNullValues()
                .toString();
    }
}
