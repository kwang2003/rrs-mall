package com.aixforce.rrs.code.model;

import com.google.common.base.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:优惠活动绑定情况
 * Created by songrenfei
 * Date:2014-07-03.
 */
@ToString
@EqualsAndHashCode
public class ActivityBind implements Serializable {
    private static final long serialVersionUID = -3805721685372914949L;
    
    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long activityId;            //对应活动id


    @Getter
    @Setter
    private Long targetId;      //优惠码绑定id

    @Getter
    @Setter
    private Integer targetType;   //优惠码绑定的类型1（sku）2(spu) 3(品类)',

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间


    public static enum TargetType {
        ITEM(1, "ITEM"),
        SPU(2, "SPU"),
        BUSSINESS(3, "品类");

        private final int value;
        private final String display;


        private TargetType(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int toNumber() {
            return value;
        }

        public static TargetType fromNumber(int number) {
            for (TargetType targetType : TargetType.values()) {
                if (Objects.equal(targetType.value, number)) {
                    return targetType;
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
