package com.aixforce.admin.dto;

import com.aixforce.rrs.settle.model.AlipayTrans;
import com.aixforce.shop.model.Shop;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.user.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-22 10:06 AM  <br>
 * Author: xiao
 */
@ToString
public class RefundOrderDto implements Serializable {

    private static final long serialVersionUID = -4425237663941448446L;

    @Getter
    @Setter
    private Order order;                        // 订单信息

    @Getter
    @Setter
    private List<OrderItem> orderItems;         // 子订单信息

    @Getter
    @Setter
    private User buyer;                         // 买家信息

    @Getter
    @Setter
    private User seller;                        // 卖家信息

    @Getter
    @Setter
    private Shop shop;                          // 店铺信息

}
