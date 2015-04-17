/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

public class UserImage implements Serializable {

    private static final long serialVersionUID = 729768790032970538L;
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String category;

    @Getter
    @Setter
    private String fileName;

    @Getter
    @Setter
    private Integer fileSize;

    @Getter
    @Setter
    private Date createdAt;

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, fileName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UserImage)) {
            return false;
        }
        UserImage that = (UserImage) obj;
        return Objects.equal(userId, that.userId) && Objects.equal(fileName, that.fileName) && Objects.equal(category, that.category);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("userId", userId).add("category", category)
                .add("fileName", fileName).add("fileSize", fileSize).toString();
    }
}
