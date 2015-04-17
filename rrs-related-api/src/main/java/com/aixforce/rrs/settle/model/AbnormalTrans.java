package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-19 1:02 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class AbnormalTrans implements Serializable {

    private static final long serialVersionUID = 3306319187320464341L;

    @Getter
    @Setter
    private Long id;                    // 主键

    @Getter
    @Setter
    private Long settlementId;          // 帐务id

    @Getter
    @Setter
    private Long orderId;               // 订单id

    @Getter
    @Setter
    private String reason;              // 原因

    @Getter
    @Setter
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    private Date updatedAt;             // 更新时间


}
