/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


public class Delivery implements Serializable {

    private static final long serialVersionUID = -602893271939032638L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long orderId;

    @Getter
    @Setter
    private Integer company;

    @Getter
    @Setter
    private String trackCode;

    @Getter
    @Setter
    private Integer type;

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId, company);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Delivery)) {
            return false;
        }
        Delivery that = (Delivery) o;
        return Objects.equal(orderId, that.orderId) && Objects.equal(company, that.company);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("orderId", orderId).add("company", company)
                .add("trackingCode", trackCode).add("type", type).omitNullValues().toString();
    }
}
