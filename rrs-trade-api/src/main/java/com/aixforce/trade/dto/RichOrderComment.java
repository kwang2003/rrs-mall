package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Date: 14-2-20
 * Time: PM5:32
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class RichOrderComment implements Serializable {
    private static final long serialVersionUID = -557880979967571402L;

    @Getter
    @Setter
    private Long orderCommentId;    // id if order item

    @Getter
    @Setter
    private String orderComment;    // comment of order item

    @Getter
    @Setter
    private String orderCommentReply;    // comment reply of order item

    @Getter
    @Setter
    private Integer fee;            // fee of order item

    @Getter
    @Setter
    private Integer rQuality;       // rate of order item's quality, 489 means 4.89

    @Getter
    @Setter
    private Integer rDescribe;     // ditto to item's decryption

    @Getter
    @Setter
    private Integer rService;       // ditto to seller's custom service

    @Getter
    @Setter
    private Integer rExpress;       // ditto to express service

    @Getter
    @Setter
    private Long itemId;            // id of sold item

    @Getter
    @Setter
    private String itemImg;         // url of order item's img

    @Getter
    @Setter
    private String itemName;        // name of order item

    @Getter
    @Setter
    private Integer orderType;      // type of order item

    @Getter
    @Setter
    private Date updatedAt;   // the last update time of the comment's
}
