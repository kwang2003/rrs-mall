package com.aixforce.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/*
 * Author: jlchen
 * Date: 2012-12-27
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 3614645326426215581L;

    public static enum BelongType {
        User(1), Item(2), Site(3);

        private final int value;

        private BelongType(int value) {
            this.value = value;
        }

        public static BelongType fromNumber(Integer value) {
            if (value == null) {
                return null;
            }
            for (BelongType belongType : BelongType.values()) {
                if (belongType.value == value) {
                    return belongType;
                }
            }
            return null;
        }

        public int toNumber() {
            return this.value;
        }
    }

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long belongId;

    @Getter
    @Setter
    private Integer belongType;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Long replyTo;

    @Getter
    @Setter
    private String content;

    @Override
    public int hashCode() {
        return Objects.hashCode(belongType, belongId, userId, replyTo, content);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Comment)) {
            return false;
        }
        Comment that = (Comment) o;
        return Objects.equal(userId, that.userId) && Objects.equal(replyTo, that.replyTo)
                && Objects.equal(belongId, that.belongId) && Objects.equal(content, that.content)
                && Objects.equal(belongType, that.belongType);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("userId", userId).add("belongType", BelongType.fromNumber(belongType))
                .add("belongId", belongId).add("replyTo", replyTo).add("content", content).omitNullValues().toString();

    }
}
