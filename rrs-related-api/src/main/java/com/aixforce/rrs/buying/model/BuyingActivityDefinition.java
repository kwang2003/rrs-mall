package com.aixforce.rrs.buying.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢购活动定义
 * Created by songrenfei on 14-9-22.
 */
@ToString
@EqualsAndHashCode
public class BuyingActivityDefinition implements Serializable {

    private static final long serialVersionUID = -41150088745536744L;

    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private String activityName;            //对应活动标题


    @Getter
    @Setter
    private Date activityStartAt;      //活动开始时间

    @Getter
    @Setter
    private Date activityEndAt;   //活动结束时间

    @Getter
    @Setter
    private Date orderStartAt;   //订单开始时间

    @Getter
    @Setter
    private Date orderEndAt;   //订单结束时间

    @Getter
    @Setter
    private Integer payLimit;      //购买时限

    @Getter
    @Setter
    private Integer status;      //状态(1->待发布;2->已发布,代运行;3->正在运行;4->已结束;5->已中止)

    @Getter
    @Setter
    private Long sellerId;                    //商家id

    @Getter
    @Setter
    private String sellerName;                    //商家名称

    @Getter
    @Setter
    private Long shopId;                    //店铺id

    @Getter
    @Setter
    private String shopName;                    //店铺id

    @Getter
    @Setter
    private Long businessId;            // 频道id


    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间


    public static enum Status {

        NOT_RELEASED(1, "待发布"),
        RELEASED(2, "已发布"),
        RUNNING(3, "运行中"),
        FINISHED(4, "已结束"),
        STOPPED(5, "已终止");

        private final int value;

        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return description;
        }
    }


}
