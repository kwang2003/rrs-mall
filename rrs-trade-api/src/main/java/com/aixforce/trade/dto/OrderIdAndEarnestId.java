package com.aixforce.trade.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-9-16
 */
@Data
public class OrderIdAndEarnestId implements Serializable {
    private static final long serialVersionUID = 7969717775615522635L;

    private Long orderId;   //预售总订单id

    private Long earnestId; //预售定金id
}
