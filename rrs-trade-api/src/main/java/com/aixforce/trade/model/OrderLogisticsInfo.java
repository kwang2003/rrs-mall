package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 订单物流信息
 * Author: haolin
 * On: 9/23/14
 */
@Data
public class OrderLogisticsInfo implements Serializable{

    private static final long serialVersionUID = -8978708927839041774L;

    private Long id;

    private Long orderId;                    //订单id

    private String expressName;              //快递名称, 第三方快递时输入

    private String expressCode;              //快递代码, 冗余

    private String expressNo;                //快递单号, 第三方快递时输入

    private Integer type;                    //物流类型

    private String remark;                   //备注, 自有物流时输入

    private Date createdAt;

    private Date updatedAt;

    public static enum Type {
        THIRD(0, "第三方物流"),
        SELF(1, "自有物流"),
        NONE(2, "无需物流");

        private final int value;
        private final String desc;

        private Type(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int value() {
            return value;
        }

        public static Type from(Integer value) {
            for (Type type : Type.values()) {
                if (Objects.equal(type.value, value)) {
                    return type;
                }
            }
            return null;
        }
    }
}
