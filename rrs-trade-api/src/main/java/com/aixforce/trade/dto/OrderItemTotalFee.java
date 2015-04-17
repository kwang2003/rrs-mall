package com.aixforce.trade.dto;

import com.aixforce.trade.model.OrderItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-3-13
 */
@ToString
public class OrderItemTotalFee extends OrderItem implements Serializable{
    private static final long serialVersionUID = 7255622775306316896L;

    @Getter
    @Setter
    private Integer totalFee; //订单总价，预售订单才用到

    @Getter
    @Setter
    private Integer earnestFee;

    @Getter
    @Setter
    private Integer totalRefundAmount; //订单退款总额，预售订单才用到

    @Getter
    @Setter
    private Integer preState; //押金预授权订单表状态
}
