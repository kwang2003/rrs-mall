/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.model;

import com.aixforce.user.base.BaseUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Min;
import java.util.Date;

import static com.google.common.base.Objects.equal;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-07-31
 */
@ToString(callSuper = true)
public class User extends BaseUser {

    public static enum STATUS {
        FROZEN(-2, "已冻结"),
        LOCKED(-1, "已锁定"),
        NOT_ACTIVATE(0, "未激活"),
        NORMAL(1, "正常");

        private final int value;

        private final String display;

        private STATUS(int number, String display) {
            this.value = number;
            this.display = display;
        }

        public static STATUS fromNumber(int number) {
            for (STATUS status : STATUS.values()) {
                if (Objects.equal(status.value, number)) {
                    return status;
                }
            }
            return null;
        }

        public int toNumber() {
            return value;
        }


        @Override
        public String toString() {
            return display;
        }
    }

    public static enum ThirdPartType {
        PLAIN(1),
        WEIBO(2),
        WEIXIN(3);

        private final int value;

        private ThirdPartType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static ThirdPartType from(Integer value) {
            for (ThirdPartType type : ThirdPartType.values()) {
                if (Objects.equal(type.value, value)) {
                    return type;
                }
            }
            return null;
        }
    }

    private static final long serialVersionUID = 5061383195453133821L;

    @Email
    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    @URL
    private String avatar;

    @Getter
    @Setter
    @JsonIgnore
    private String encryptedPassword;

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    @Min(0)
    private Long parent;

    @Getter
    @Setter
    private String thirdPartId;

    @Getter
    @Setter
    private Integer thirdPartType;

    @JsonIgnore
    @Getter
    @Setter
    private Date createdAt;

    @JsonIgnore
    @Getter
    @Setter
    private Date updatedAt;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof User)) {
            return false;
        }
        User that = (User) obj;
        return equal(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }


}
