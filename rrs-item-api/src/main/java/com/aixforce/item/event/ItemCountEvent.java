package com.aixforce.item.event;

import lombok.Getter;

import java.util.List;

/**
 * Created by yangzefeng on 14-5-13
 */
public class ItemCountEvent {

    @Getter
    private final List<Long> shopIds;

    public ItemCountEvent(List<Long> shopIds) {
        this.shopIds = shopIds;
    }
}
