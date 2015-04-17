package com.aixforce.restful.event;

import com.aixforce.shop.model.Shop;
import lombok.*;

import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-07 1:49 PM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OuterCodeEvent {

    @Getter
    @Setter
    private List<Shop> shops;   // 受影响的店铺列表

    @Getter
    @Setter
    private String outerCode;   // 商户编码


}
