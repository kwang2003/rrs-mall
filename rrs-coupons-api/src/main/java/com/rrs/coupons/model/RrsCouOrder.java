package com.rrs.coupons.model;

import lombok.Getter;
import lombok.Setter;

public class RrsCouOrder extends RrsCou {

	private static final long serialVersionUID = 1L;

    @Getter
    @Setter
	private Long orderId;

    @Getter
    @Setter
	private Long userId;
}
