package com.aixforce.user.model;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/*
* Author: jlchen
* Date: 2012-12-04
*/
public class UserQuota implements Serializable {
    private static final long serialVersionUID = 7994853609200081045L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Integer maxImageCount;

    @Getter
    @Setter
    private Long maxImageSize;

    @Getter
    @Setter
    private Integer maxWidgetCount;

    @Getter
    @Setter
    private Integer usedImageCount;

    @Getter
    @Setter
    private Long usedImageSize;

    @Getter
    @Setter
    private Integer usedWidgetCount;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof UserQuota)) {
            return false;
        }
        UserQuota that = (UserQuota) o;
        return Objects.equal(userId, that.userId) && Objects.equal(maxImageCount, that.maxImageCount)
                && Objects.equal(maxImageSize, that.maxImageSize) && Objects.equal(maxWidgetCount, maxWidgetCount)
                && Objects.equal(usedImageCount, that.usedImageCount) && Objects.equal(usedImageSize, that.usedImageSize)
                && Objects.equal(usedWidgetCount, that.usedWidgetCount);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("userId", userId).add("maxImageCount", maxImageCount)
                .add("maxImageSize", maxImageSize).add("maxWidgetCount", maxWidgetCount).add("usedImageCount", usedImageCount)
                .add("usedImageSize", usedImageSize).add("usedWidgetCount", usedWidgetCount).omitNullValues().toString();
    }
}
