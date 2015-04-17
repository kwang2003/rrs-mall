package com.aixforce.rrs.predeposit.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 区级别的地址到仓库码的映射关系
 *
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-07-09
 */
public class DepositAddressStorage implements Serializable {


    private static final long serialVersionUID = 4544116064690839903L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long itemId;   //商品id

    @Getter
    @Setter
    private Integer addressId;  //区级别的地址id

    @Getter
    @Setter
    private Long storageId;   //仓库id

    @Getter
    @Setter
    private Date createdAt;    //创建时间

    @Getter
    @Setter
    private Date updatedAt;    //修改时间

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DepositAddressStorage that = (DepositAddressStorage) o;

        return Objects.equal(itemId, that.itemId)
                && Objects.equal(addressId, that.addressId)
                && Objects.equal(storageId, that.storageId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(itemId, addressId, storageId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("itemId", itemId)
                .add("addressId", addressId)
                .add("storageId", storageId)
                .add("createdAt", createdAt)
                .add("updatedAt", updatedAt)
                .omitNullValues()
                .toString();
    }
}
