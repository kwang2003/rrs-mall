/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.model.UserImage;

import javax.annotation.Nullable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-21
 */
public interface ImageService {

    Response<Boolean> addUserImage(UserImage userImage);

    /**
     * 获取某个用户某分类的图片，如果不传入分类则获取所有未分类的图片
     *
     * @param userId 用户ID
     * @param category 分类
     * @param offset offset
     * @param limit limit
     * @return 分页查询的图片信息
     */
    Response<Paging<UserImage>> findUserImages(Long userId, @Nullable String category, Integer offset, Integer limit);

    /**
     * 删除用户对应的上传记录
     *
     * @param userId 用户id
     */
    Response<Boolean> deleteByUserId(Long userId);

    /**
     * 删除一个用户图片
     *
     * @param userImage 用户图片
     */
    Response<Boolean> deleteUserImage(UserImage userImage);

    /**
     * 通过id获取一个用户图片
     *
     * @param imageId 用户图片id
     * @return 用户图片
     */
    Response<UserImage> findUserImageById(Long imageId);
}
