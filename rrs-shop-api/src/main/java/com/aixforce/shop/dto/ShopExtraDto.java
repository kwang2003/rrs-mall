package com.aixforce.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Date: 14-2-26
 * Time: PM2:15
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class ShopExtraDto implements Serializable {

    private static final long serialVersionUID = -7273541575195521239L;


    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private Integer rQuality=0;

    @Getter
    @Setter
    private Integer rDescribe=0;

    @Getter
    @Setter
    private Integer rService=0;

    @Getter
    @Setter
    private Integer rExpress=0;

    @Getter
    @Setter
    private Integer tradeCount=0;

    public void addRQuality(Integer rQuality) {
        this.rQuality = firstNonNull(this.rQuality, 0) + firstNonNull(rQuality, 5);
    }

    public void addRDescribe(Integer rDescribe) {
        this.rDescribe = firstNonNull(this.rDescribe, 0) + firstNonNull(rDescribe, 5);
    }

    public void addRService(Integer rService) {
        this.rService = firstNonNull(this.rService, 0) + firstNonNull(rService, 5);
    }

    public void addRExpress(Integer rExpress) {
        this.rExpress = firstNonNull(this.rExpress, 0) + firstNonNull(rExpress, 5);
    }

    public void addTradeCount(Integer tradeQuality) {
        this.tradeCount = firstNonNull(this.tradeCount, 0) + firstNonNull(tradeQuality, 1);
    }
}
