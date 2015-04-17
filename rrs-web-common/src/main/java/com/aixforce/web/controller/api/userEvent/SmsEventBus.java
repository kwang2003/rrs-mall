package com.aixforce.web.controller.api.userEvent;

import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.google.common.eventbus.AsyncEventBus;

@Component
public class SmsEventBus {
	private final AsyncEventBus eventBus;

	public SmsEventBus() {
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
