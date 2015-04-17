package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 物流快递信息
 * Author: haolin
 * On: 9/22/14
 */
@Data @EqualsAndHashCode(of = {"name"})
public class ExpressInfo implements Serializable {

    private static final long serialVersionUID = 2705491981168122928L;

    private Long id;

    private String name;                    //快递名称

    private String code;                    //快递代码

    private String interfaceName;           //接口名称

    private Integer status;                 //状态, 1启用, 0停用, -1逻辑删除

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

    /**
     * expresses:{userId}:usual
     * @param userId
     * @return
     */
    public static String keyOfUsual(Long userId){
        return "expresses:" + userId + ":usual";
    }
}
