package com.aixforce.trade.dto;

import com.aixforce.trade.model.OrderComment;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Date: 5/27/14
 * Time: 23:56
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class OrderCommentSumUp implements Serializable {

    private static final long serialVersionUID = 1644978657488276101L;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private Long rExpress=0L;

    @Getter
    @Setter
    private Long rService=0L;

    @Getter
    @Setter
    private Long rQuality=0L;

    @Getter
    @Setter
    private Long rDescribe=0L;

    @Getter
    @Setter
    private Long tradeQuantity=0L;

    public void addRQuality(Long rQuality) {
        this.rQuality = firstNonNull(this.rQuality, 0l) + firstNonNull(rQuality, 0l);
    }

    public void addRDescribe(Long rDescribe) {
        this.rDescribe = firstNonNull(this.rDescribe, 0l) + firstNonNull(rDescribe, 0l);
    }

    public void addRService(Long rService) {
        this.rService = firstNonNull(this.rService, 0l) + firstNonNull(rService, 0l);
    }

    public void addRExpress(Long rExpress) {
        this.rExpress = firstNonNull(this.rExpress, 0l) + firstNonNull(rExpress, 0l);
    }

    public void addTradeQuantity(Long tradeQuality) {
        this.tradeQuantity = firstNonNull(this.tradeQuantity, 0l) + firstNonNull(tradeQuality, 0l);
    }

    public void sumFromComment(OrderComment c) {
        addRDescribe((long)c.getRDescribe());
        addRExpress((long)c.getRExpress());
        addRQuality((long)c.getRQuality());
        addRService((long)c.getRService());
        addTradeQuantity(1l);
    }
}
