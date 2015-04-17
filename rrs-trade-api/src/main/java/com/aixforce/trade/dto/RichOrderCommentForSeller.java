package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Date: 14-2-20
 * Time: PM5:59
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class RichOrderCommentForSeller extends RichOrderComment implements Serializable {
    private static final long serialVersionUID = -850760718097431621L;

    @Getter
    @Setter
    private String buyerName;       // nick name of buyer

    @Getter
    @Setter
    private Long buyerId;         // id of buyer
}
