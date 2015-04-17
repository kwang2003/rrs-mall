package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 技术服务费
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@ToString
@EqualsAndHashCode
public class DepositAccount implements Serializable {
    private static final long serialVersionUID = 3792272980467340942L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long sellerId;          // 商家id

    @Getter
    @Setter
    private String sellerName;      // 商家名称

    @Getter
    @Setter
    private Long shopId;            // 店铺id

    @Getter
    @Setter
    private String shopName;        // 店铺名称

    @Getter
    @Setter
    private Long business;          // 行业编号

    @Getter
    @Setter
    private String outerCode;       // 商家8码

    @Getter
    @Setter
    private Long balance;           // 商家保证金余额

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新时间

    public DepositAccount() {}

    public DepositAccount(Long sellerId, String sellerName, Long balance) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.balance = balance;
    }

    public DepositAccount(Long sellerId, Long balance) {
        this.sellerId = sellerId;
        this.balance = balance;
    }
}
