package com.aixforce.user.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-09 6:22 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class UserAccountSummary implements Serializable {

    private static final long serialVersionUID = -4436672103279221095L;

    @Getter
    @Setter
    private Long id;                    // 用户id

    @Getter
    @Setter
    private String activity;            // 活动

    @Getter
    @Setter
    private String channel;             // 渠道

    @Getter
    @Setter
    private String from;                // 来源

    @Getter
    @Setter
    private Long userId;                // 用户id

    @Getter
    @Setter
    private String userName;            // 登录名

    @Getter
    @Setter
    private Long loginType;             // 登录类型

    @Getter
    @Setter
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    private Date updatedAt;             // 更新事件
}
