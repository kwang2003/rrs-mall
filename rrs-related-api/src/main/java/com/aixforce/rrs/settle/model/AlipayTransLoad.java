package com.aixforce.rrs.settle.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 11:00 AM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@RequiredArgsConstructor
public class AlipayTransLoad implements Serializable {

    private static final long serialVersionUID = 1631267751412144190L;

    @Getter
    @Setter
    private Long id;                // 主键

    @Getter
    @Setter
    @NonNull
    private Date queryStart;        // 查询起始日期

    @Getter
    @Setter
    @NonNull
    private Date queryEnd;          // 查询截止日期


    @Getter
    @Setter
    @NonNull
    private Integer pageNo;         // 页码

    @Getter
    @Setter
    @NonNull
    private Integer pageSize;       // 分页大小

    @Getter
    @Setter
    private Integer status;         // 状态 1:成功 -1:失败

    @Getter
    @Setter
    private Boolean next;           // 是否存在下一批

    @Getter
    @Setter
    private Date createdAt;         // 创建时间

    @Getter
    @Setter
    private Date updatedAt;         // 更新时间

    public static enum Status {
        DONE(1, "普通订单"),
        FAIL(-1,"失败");


        private final int value;

        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
