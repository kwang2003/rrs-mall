/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.model;

import com.aixforce.common.model.Indexable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

public class Item implements Indexable {

    public static enum TradeType {
        BUY_OUT(1, "一口价"),
        BIT(2, "拍卖");

        private final int value;
        private final String display;


        private TradeType(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int toNumber() {
            return value;
        }

        public static TradeType fromNumber(int number) {
            for (TradeType tradeType : TradeType.values()) {
                if (Objects.equal(tradeType.value, number)) {
                    return tradeType;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public static enum Status {
        INIT(0, "未上架"),
        ON_SHELF(1, "上架"),
        OFF_SHELF(-1, "下架"),
        FROZEN(-2, "冻结"),
        DELETED(-3, "删除");

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


        @Override
        public String toString() {
            return display;
        }
    }

    private static final long serialVersionUID = -3938589927710387727L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long spuId;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private Long brandId; //品牌id

    @Getter
    @Setter
    @NotBlank
    private String name;

    @Getter
    @Setter
    private String shopName; //冗余店铺名称，需要dump到search中去

    @Getter
    @Setter
    //@NotBlank
    private String mainImage;

    @Getter
    @Setter
    private Integer tradeType;

    @Getter
    @Setter
    private Date onShelfAt;

    @Getter
    @Setter
    private Date offShelfAt;

    @Getter
    @Setter
    private Integer price;

    @Getter
    @Setter
    private Integer originPrice;

    @Getter
    @Setter
    private Integer quantity;

    @Getter
    @Setter
    private Integer soldQuantity;

    @Getter
    @Setter
    private Integer status; //商品状态 0：未上架，1：上架，，-1：下架，-2：冻结

    @Getter
    @Setter
    private String region; //所在地区

    //物流使用的数据字段
    @Getter
    @Setter
    private Long freightSize;       //尺寸大小

    @Getter
    @Setter
    private Long freightWeight;     //重量

    @Getter
    @Setter
    private Long freightModelId;    //模板编号

    @Getter
    @Setter
    private String freightModelName;//模板名称

    @Getter
    @Setter
    private Long deliveryMethodId;  //配送方式id，对应DeliveryMethod的id

    @Getter
    @Setter
    private String templateId;//模板名称

    @Getter
    @Setter
    private String templateName;//模板名称

    @Getter
    @Setter
    private Date createdAt;

    @JsonIgnore
    @Getter
    @Setter
    private Date updatedAt;


    @Override
    public int hashCode() {
        return Objects.hashCode(spuId, userId, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Item)) {
            return false;
        }
        Item that = (Item) obj;
        return Objects.equal(spuId, that.spuId) && Objects.equal(userId, that.userId) && Objects.equal(name, that.name);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("spuId", spuId).add("userId", userId)
                .add("shopId", shopId).add("name", name).add("tradeType", tradeType).add("status", status)
                .add("price", price).add("soldQuantity", soldQuantity).omitNullValues().toString();
    }
}
