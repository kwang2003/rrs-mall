package com.aixforce.admin.event;


import lombok.*;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-12 1:21 PM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OuterCodeSetEvent {

    @Getter
    @Setter
    private Long shopId;            // 店铺id

    @Getter
    @Setter
    private String outerCode;       // 商户的外部编码

}
