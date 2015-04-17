package com.aixforce.alipay.event;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executors;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-07 4:26 PM  <br>
 * Author: xiao
 */
public class AlipayEventBus {

    private final AsyncEventBus eventBus;

    public AlipayEventBus(final int size) {
        this.eventBus = new AsyncEventBus(Executors.newFixedThreadPool(size));
    }

    public void register(Object object) {
        eventBus.register(object);
    }

    public void post(Object event) {
        eventBus.post(event);
    }

    @SuppressWarnings("unused")
    public void unRegister(Object object) {
        eventBus.unregister(object);
    }

}
