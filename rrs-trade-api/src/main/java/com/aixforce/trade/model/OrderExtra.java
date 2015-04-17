package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
@ToString
public class OrderExtra implements Serializable {
    private static final long serialVersionUID = 6929251808713895387L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long orderId;

    @Getter
    @Setter
    private String buyerNotes;

    @Getter
    @Setter
    private String invoice;

    @Getter
    @Setter
    private Boolean hasLogistics;     //订单是否有物流快递信息

    @Getter
    @Setter
    private String logisticsInfo;    //订单物流信息JSON字符串,

    @Getter
    @Setter
    private Boolean hasInstall;      //订单是否有物流安装信息

    @Getter
    @Setter
    private Integer installType;     //安装类别, 冗余

    @Getter
    @Setter
    private String installName;     //安装公司名称, 冗余

    @Getter
    @Setter
    private String deliverTime;     //用户要求的送达时间

    @Getter
    private Integer source; // 订单来源

    @Getter
    private Boolean isFromDtd;

    @Getter
    @Setter
    private String updatedAt;

    @Setter
    @Getter
    private Integer deliverType;     //配送方式：0 物流配送 1 到店自提

    public void setSource(Integer source) {
        this.source = source;
        if (Objects.equal(source, SourceType.DTD_B.value())) {
            this.isFromDtd = true;
        }
        if (Objects.equal(source, SourceType.WATER.value())) {
            this.isFromDtd = true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orderId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof OrderExtra)) {
            return false;
        }
        OrderExtra that = (OrderExtra) o;
        return Objects.equal(this.orderId, that.orderId);
    }


    public static enum Type {
        PLAIN("1", "普通发票"),
        VAT("2","增值税发票"),
        ELECT("3","电子发票");

        private final String value;

        private final String description;

        private Type(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * 配送方式
     */
    public static enum DeliverTypeEnum {
        DELIVER(0, "物流配送"),
        STORE(1,"到店自提");

        private final Integer value;

        private final String description;

        private DeliverTypeEnum(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        public Integer value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

    public static enum SourceType {
        PHONE(1, "手机端"),
        WATER(10,"净水商城"),
        DTD_B(2,"DTD B端");

        private final Integer value;

        private final String description;

        private SourceType(Integer value, String description) {
            this.value = value;
            this.description = description;
        }

        public Integer value() {
            return this.value;
        }


        @Override
        public String toString() {
            return description;
        }
    }

}
