/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@ToString
public class OrderJobOverDay implements Serializable {

    @Getter
    @Setter
    private Long id;                // id

    @Getter
    @Setter
    private Long orderId;           // 订单id

    @Getter
    @Setter
    private Integer status;         // 状态

    @Getter
    @Setter
    private Long skuId;             // 商品SKUID

    @Getter
    @Setter
    private Date overDay;       // 自动收货完成时间

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新时间

    @Getter
    @Setter
    private Date overDayStart;       // 自动收货完成时间

    @Getter
    @Setter
    private Date overDayEnd;       // 自动收货完成时间

    public static enum Status {
        WAIT_FOR_PAY(0, "未处理"),
        PAID(1, "JOB已处理");

        private final int value;

        @SuppressWarnings("unused")
        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public static Status from(int value) {
            for(Status ta: Status.values()) {
                if(ta.value==value) {
                    return ta;
                }
            }

            return null;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }


}
