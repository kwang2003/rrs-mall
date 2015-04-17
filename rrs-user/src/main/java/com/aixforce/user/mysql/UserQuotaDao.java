package com.aixforce.user.mysql;

import com.aixforce.user.model.UserQuota;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-13
 */
@Repository
public class UserQuotaDao extends SqlSessionDaoSupport {

    @Autowired
    private UserImageDao userImageDao;

    public void create(UserQuota userQuota) {
        checkArgument(userQuota.getUserId() != null, "userId can not be null");
        getSqlSession().insert("UserQuota.create", userQuota);
    }

    public UserQuota findByUserId(Long userId) {
        return getSqlSession().selectOne("UserQuota.findByUserId", userId);
    }

    /**
     * 增加或者减少用户使用的图片消耗信息
     *
     * @param userId          用户id
     * @param imageCountDelta 图片数目变化,可以为负
     * @param imageSizeDelta  图片大小变化,可以为负
     */
    public void updateUsedImageInfo(final Long userId, final Integer imageCountDelta, final Integer imageSizeDelta) {
        checkArgument(userId != null, "userId can not be null");
        checkArgument(imageCountDelta != null, "imageCount delta can not be null");
        checkArgument(imageSizeDelta != null, "imageSize delta can not be null");
        getSqlSession().update("UserQuota.delta",
                ImmutableMap.of("userId", userId, "usedImageCountDelta", imageCountDelta, "usedImageSizeDelta", imageSizeDelta));
    }

    /**
     * 更新用户所使用的widget个数
     *
     * @param userId               用户id
     * @param usedWidgetCountDelta 变化数目个数,可正可负
     */
    public void updateUsedWidgetCount(Long userId, Integer usedWidgetCountDelta) {
        checkArgument(userId != null, "userId can not be null");
        checkArgument(usedWidgetCountDelta != null, "usedWidgetCountDelta delta can not be null");
        getSqlSession().update("UserQuota.delta", ImmutableMap.of("userId", userId, "usedWidgetCountDelta", usedWidgetCountDelta));
    }


    /**
     * 计算用户消耗的图片数目和大小
     *
     * @param userId 用户id
     */
    public void calculateUsedImageInfo(Long userId) {
        long total = userImageDao.totalSizeByUserId(userId);
        int count = userImageDao.totalCountOf(userId);

        UserQuota userQuota = new UserQuota();
        userQuota.setUserId(userId);
        userQuota.setUsedImageCount(count);
        userQuota.setUsedImageSize(total);
        update(userQuota);
    }

    public void update(UserQuota userQuota) {
        getSqlSession().update("UserQuota.update", userQuota);
    }

    public void deleteByUserId(final Long userId) {
        getSqlSession().delete("UserQuota.deleteByUserId", userId);
    }
}
