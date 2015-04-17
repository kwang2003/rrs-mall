package com.aixforce.trade.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-01 9:39 AM  <br>
 * Author: xiao
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderDescription implements Serializable {

    private static final long serialVersionUID = -2341204723214819469L;

    @Getter
    @Setter
    private String title = "日日顺订单";           // 订单标题

    @Getter
    @Setter
    private String content = "日日顺商品信息";      // 订单内容（商品描述等)

}
