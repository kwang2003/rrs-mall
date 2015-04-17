package com.aixforce.item.event;

import com.google.common.eventbus.AsyncEventBus;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

/**
 * Created by yangzefeng on 14-5-13
 */
@Component
public class ItemEventBus {

    private final AsyncEventBus eventBus;

    public ItemEventBus() {
        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
    }


    public void register(Object object) {
        eventBus.register(object);
    }


    public void post(Object event) {
        eventBus.post(event);
    }


    public void unregister(Object object) {
        eventBus.unregister(object);
    }
}
