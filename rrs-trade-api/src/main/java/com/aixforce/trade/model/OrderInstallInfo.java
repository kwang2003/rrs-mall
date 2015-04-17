package com.aixforce.trade.model;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 订单安装信息
 * Author: haolin
 * On: 9/23/14
 */
@Data
public class OrderInstallInfo implements Serializable {

    private static final long serialVersionUID = -968802572060017563L;

    private Long id;

    private Long orderId;           //订单id

    private String time;            //时间点

    private String context;         //信息

    private Date createdAt;

    private Date updatedAt;
}
