package com.aixforce.trade.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 14-9-3
 */
@Data
public class DeliveryMethod implements Serializable {
    private static final long serialVersionUID = -9212774105758048756L;

    private Long id;

    private String name; //配送方式名称

    private Integer status; //状态 1启用，-1停用，-2删除

    private Integer type;   //类型，1送达时段，2送达承诺

    private Date createdAt;

    private Date updatedAt;

    public static enum Status {
        OK(1,"启用"),
        STOP(-1,"停用"),
        DELETE(-2,"删除");

        private final int value;

        private final String display;

        private Status (int value, String display) {
            this.value = value;
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }

        public int value() {
            return value;
        }

        public static Status fromNumber(int val) {
            for(Status s : Status.values()) {
                if(s.value == val) {
                    return s;
                }
            }
            return null;
        }
    }

    public static enum Type {
        DELIVER_TIME(1, "送达时段"),
        DELIVER_PROMISE(2, "送达承诺");

        private final int value;

        private final String display;

        private Type(int value, String display) {
            this.value = value;
            this.display = display;
        }

        @Override
        public String toString() {
            return this.display;
        }

        public int value() {
            return this.value;
        }

        public static Type fromNumber(int val) {
            for(Type t : Type.values()) {
                if(t.value == val) {
                    return t;
                }
            }
            return null;
        }
    }

}
