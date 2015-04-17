package com.aixforce.rrs.predeposit.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 14-2-12
 */
@ToString
@EqualsAndHashCode
public class PreDeposit implements Serializable {

    private static final long serialVersionUID = -6799834930371759076L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long spuId;                 // spu id

    @Getter
    @Setter
    private Long itemId;                // 对应的商品id

    @Getter
    @Setter
    private String shopIds;             // 该预售绑定的店铺id列表

    @Getter
    @Setter
    private Integer plainBuyLimit;      // 普通用户购买限制,最多购买数量

    @Getter
    @Setter
    private Boolean byStorage;          // 是否需要支持分仓

    @Getter
    @Setter
    private Integer earnest;            // 定金

    @Getter
    @Setter
    private Integer remainMoney;        // 尾款

    @Getter
    @Setter
    private String advertise;           // 广告语

    @Getter
    @Setter
    private Integer price;              // 原价

    @Getter
    @Setter
    private Integer fakeSoldQuantity;   // 虚拟销量

    @Getter
    @Setter
    private Integer status;             // 预售状态 0: 待发布, 1: 已发布待运行 2:运行中 3:已结束 -1:已终止


    @Getter
    @Setter
    private Integer earnestTimeLimit;   // 付定金时限,以小时为单位


    @Getter
    @Setter
    private Date releasedAt;             // 发布时间

    @Getter
    @Setter
    private Date preSaleStartAt;        // 预售开始时间

    @Getter
    @Setter
    private Date preSaleFinishAt;       // 预售结束时间

    @Getter
    @Setter
    private Date remainStartAt;         // 尾款起始时间

    @Getter
    @Setter
    private Date remainFinishAt;        // 尾款结束时间

    @Getter
    @Setter
    private Date createdAt;              // 创建时间

    @Getter
    @Setter
    private Date updatedAt;              // 更新时间

    public static enum Status {

        NOT_RELEASED(0, "待发布"),
        RELEASED(1, "已发布"),
        RUNNING(2, "运行中"),
        FINISHED(3, "已结束"),
        STOPPED(-1, "已终止");

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
