package com.aixforce.trade.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-25-下午3:04
 */
@ToString
public class LogisticsOrder implements Serializable {

    private static final long serialVersionUID = -1179348846513499603L;

    @Getter
    @Setter
    private Long orderId;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private String shopName;
}
