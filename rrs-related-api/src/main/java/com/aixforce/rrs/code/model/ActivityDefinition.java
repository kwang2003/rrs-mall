package com.aixforce.rrs.code.model;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wanggen on 14-7-3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDefinition implements Serializable {

    private static final long serialVersionUID = 4938387347554688827L;

    private Long id;                    //自增主键

    private String activityName;        //优惠活动名称

    private String activityDesc;        //优惠活动描述

    private Long businessId;            //行业编码 1:家电 2:家具 3:家装 4:家饰 5:净水

    private Integer activityType;       //活动类别[公开码|渠道码]

    private Integer status;             //本次活动状态  0:新建 | 1:生效 | -1:失效 | -2:手工失效

    private Integer channelType;        //频道类型

    private Integer discount;           //优惠码折扣金额

    private Integer stock;              //优惠码发放数量

    private Integer useLimit;           //买家使用数量

    private Integer orderCount;         //订单数量

    private Date startAt;               //有效开始时间

    private Date endAt;                 //有效截止时间

    private Date createdAt;             //创建时间

    private Date updatedAt;             //修改时间

    public static enum Status {

        INIT(0, "新建未生效"),
        OK(1, "已生效"),
        INVALID(-1, "已失效"),
        STOP(-2, "手动失效");

        private int value;
        private String display;

        private Status(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int toNumber() {
            return value;
        }

        public static Status fromNumber(int number) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.value, number)) {
                    return status;
                }
            }
            return null;
        }
    }


    /**
     * 目前优惠码分‘公开码’、‘渠道码’两种
     */
    public static enum ActivityType {
        /**
         * 公开码
         */
        PUBLIC_CODE(1, "公开码"),
        /**
         * 渠道码
         */
        CHANNEL_CODE(2, "渠道码");

        private final int value;

        private final String description;

        private ActivityType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Created by songrenfei on 14-7-3.
     */
    public enum ChannelType {

        DEALER(1, "经销商"),
        SERVICE(2, "服务兵");

        private final int value;
        private final String display;

        private ChannelType(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int toNumber() {
            return value;
        }

        public static ChannelType fromNumber(int number) {
            for (ChannelType channelType : ChannelType.values()) {
                if (Objects.equal(channelType.value, number)) {
                    return channelType;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return display;
        }
    }


}
