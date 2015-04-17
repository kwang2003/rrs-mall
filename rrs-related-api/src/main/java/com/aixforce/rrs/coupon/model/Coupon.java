package com.aixforce.rrs.coupon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Effet on 4/21/14.
 */
@ToString
@EqualsAndHashCode
public class Coupon implements Serializable {

    private static final long serialVersionUID = 8507029544459407914L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private Integer amount;          // 优惠券面额

    @Getter
    @Setter
    private Integer useLimit;        // 使用条件：满多少才可使用

    @Getter
    @Setter
    private Integer type;            // 优惠券种类：@see Type

    @Getter
    @Setter
    private Integer status;          // 优惠券状态：@see Status

    @Getter
    @Setter
    private Integer taken;           // 领用人数

    @Getter
    @Setter
    private Integer used;            // 使用人数（指使用了优惠券的订单数）

    @Getter
    @Setter
    private Integer clicked;         // 点击人数（进店人数）

    @Getter
    @Setter
    private Date startAt;            // 优惠券开始使用时间（精确到日）

    @Getter
    @Setter
    private Date endAt;              // 使用结束时间

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;

    public enum Status {

        INIT(0, "初始"),
        RELEASE(1, "发布"),
        VALID(2, "生效"),
        SUSPEND(-1, "挂起"),
        EXPIRE(3, "过期");

        private final int value;

        private final String display;

        private Status(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public enum Type {

        OBTAINED(1, "前台领取"),
        RECEIVED(2, "活动赠送");

        private final int value;
        private final String display;

        private Type(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
