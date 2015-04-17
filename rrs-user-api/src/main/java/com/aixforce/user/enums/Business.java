package com.aixforce.user.enums;

import lombok.Getter;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-01 3:00 PM  <br>
 * Author: xiao
 */
public enum Business {

    APPLIANCE(1L, "家电", "家电商城"),
    FURNITURE(2L, "家具", "家具商城"),
    DECORATION(3L, "家装", "建材商城"),
    EMBELLISHMENT(4L, "家饰", "家饰商城"),
    WATER(5L, "净水", "净水商城"),
    RRSPLAZA(6L, "家具建材等(RRS广场)", "RRS广场");


    private final long value;

    private final String description;

    @Getter
    private final String mall;

    private Business(long value, String description, String mall) {
        this.value = value;
        this.description = description;
        this.mall = mall;
    }

    public static Business from(int value) {
        for (Business b : Business.values()) {
            if (b.value == value) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }

    public Long value() {
        return value;
    }

}
