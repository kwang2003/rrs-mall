/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-30
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = -7161289916347440184L;


    public static enum CommentType {
        GOOD(1, "好评"),
        PLAIN(0, "中评"),
        BAD(-1, "差评");

        private final int value;

        private final String display;

        private CommentType(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public static CommentType from(int value) {
            for (CommentType commentType : CommentType.values()) {
                if (Objects.equal(commentType.value, value)) {
                    return commentType;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public static enum CommentStatus {
        NORMAL(1, "正常"),
        DELETE(-1, "删除");

        private final int value;
        private final String display;

        private CommentStatus(int value, String display) {
            this.value = value;
            this.display = display;
        }

        public int getValue() {
            return value;
        }

        public static CommentStatus from(int value) {
            for (CommentStatus status : CommentStatus.values()) {
                if (Objects.equal(status.value, value)) {
                    return status;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    @Getter
    @Setter
    private Long id;  //主键

    @Getter
    @Setter
    private Long userId; //评价者id

    @Getter
    @Setter
    private Long targetId;//评价对象id

    @Getter
    @Setter
    private Integer targetType;//评价对象类型

    @Getter
    @Setter
    private Integer type; //好评,中评,差评

    @Getter
    @Setter
    private Integer status;//状态

    @Getter
    @Setter
    private String content; //评价内容

    @Getter
    @Setter
    private Date createdAt; //创建时间

    @Getter
    @Setter
    private Date updatedAt; //修改时间


    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Comment)) {
            return false;
        }
        Comment that = (Comment) o;
        return Objects.equal(userId, that.userId) && Objects.equal(targetType, that.targetType)
                && Objects.equal(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, targetType, targetId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("targetType", targetType)
                .add("targetId", targetId).add("type", type).add("status", status).add("content", content).toString();
    }
}
