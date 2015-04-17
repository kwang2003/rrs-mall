package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 行业费率
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-14 10:15 AM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class BusinessRate  implements Serializable {
    private static final long serialVersionUID = 3792272980467340944L;

    @Getter
    @Setter
    private Long id;            // 主键

    @Getter
    @Setter
    private Long business;      // 行业代码

    @Getter
    @Setter
    private Double rate;        // 费率

    @Getter
    @Setter
    private Date createdAt;     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;     // 更新时间

}
