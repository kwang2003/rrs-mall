package com.aixforce.rrs.code.model;

import com.google.common.base.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:优惠码使用情况
 * Created by songrenfei
 * Date:2014-07-03.
 */
@ToString
@EqualsAndHashCode
public class CodeUsage implements Serializable {
    private static final long serialVersionUID = -3805721685372914949L;
    
    @Getter
    @Setter
    private Long id;                    // 自赠主键

    @Getter
    @Setter
    private Long activityId;            // 对应优惠活动id

    @Getter
    @Setter
    private String activityName;        // 优惠活动名称

    @Getter
    @Setter
    private Integer activityType;       // 优惠活动类型

    @Getter
    @Setter
    private Long orderId;               // 对应订单id


    @Getter
    @Setter
    private Long businessId;            // 行业编码 1:家电 2:家具 3:家装 4:家饰 5:净水

    @Getter
    @Setter
    private String code;                // 优惠码名称

    @Getter
    @Setter
    private Long buyerId;               // 买家id

    @Getter
    @Setter
    private String buyerName;           // 买家账户

    @Getter
    @Setter
    private Long sellerId;              // 卖家id

    @Getter
    @Setter
    private String sellerName;          // 卖家账户


    @Getter
    @Setter
    private Integer discount;           // 优惠金额

    @Getter
    @Setter
    private Integer originPrice;        // 优惠前金额

    @Getter
    @Setter
    private Integer price;              // 优惠后金额


    @Getter
    @Setter
    private Integer channelType;        // 渠道码类别(1.经销商; 2.服务兵),只有当 code_type=2 时该标识有效

    @Getter
    @Setter
    private Date usedAt;                // 创建时间

    @Getter
    @Setter
    private Integer usedCount;          // 使用次数

    @Getter
    @Setter
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    private Date updatedAt;             // 修改时间


    public static enum ChannelType {
        DEL(1, "经销商"),
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

        @SuppressWarnings("unused")
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
