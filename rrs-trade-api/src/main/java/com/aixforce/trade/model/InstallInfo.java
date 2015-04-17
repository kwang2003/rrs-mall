package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;

/**
 * 物流安装信息
 * Author: haolin
 * On: 9/22/14
 */
@Data @EqualsAndHashCode(of = {"name"})
public class InstallInfo implements Serializable {

    private static final long serialVersionUID = 7200375559330988888L;

    private Long id;

    private String name;            //安装公司名称

    private String code;            //安装公司代码

    private String interfaceName;   //接口名称

    private Integer status;         //状态

    private Integer type;           //类型

    private Date createdAt;

    private Date updatedAt;

    public static enum Status {
        ENABLED(1),         //启用
        DISABLED(0),        //停用
        DELETED(-1);        //逻辑删除

        private final int value;

        private Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Status from(Integer value) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.value, value)) {
                    return status;
                }
            }
            return null;
        }
    }

    public static enum Type {
        JINGSHUI(0, "净水"),
        JIADIAN(1, "家电"),
        JIAJU(2, "家具"),
        JIANCAI(3, "建材");

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
