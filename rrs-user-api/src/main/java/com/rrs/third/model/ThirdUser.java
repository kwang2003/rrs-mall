package com.rrs.third.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhaop01 on 2014/9/2.
 */
@ToString
@EqualsAndHashCode
public class ThirdUser implements Serializable {

    // 系统主键
    @Getter
    @Setter
    private int id;

    // 原始用户名
    @Getter
    @Setter
    private String sourceName;

    // RRS系统生成的用户名
    @Getter
    @Setter
    private String rrsUserName;

    // 创建时间
    @Getter
    @Setter
    private Date createTime;

}
