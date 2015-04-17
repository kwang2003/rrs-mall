package com.aixforce.web.controller.api.userEvent;

import lombok.Data;

@Data
public class SmsEvent {
	protected long orderId;
	protected long orderItemId;
	protected String type;

	public SmsEvent(long orderId, long orderItemId,String type) {
		this.orderId = orderId;
		this.orderItemId=orderItemId;
		this.type = type;
	}

}
