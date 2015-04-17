package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Date: 14-2-21
 * Time: PM3:30
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class RichOrderCommentForBuyer extends RichOrderComment implements Serializable {
    private static final long serialVersionUID = 501536345274279414L;

    @Getter
    @Setter
    private Long sellerId;      // seller's user id

    @Getter
    @Setter
    private String SellerName;  // seller's user name

    @Getter
    @Setter
    private Long shopId;        // shop's id

    @Getter
    @Setter
    private String shopName;    // shop's short name
}
