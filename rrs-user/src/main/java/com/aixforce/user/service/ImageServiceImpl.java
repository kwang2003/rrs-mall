/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.model.UserImage;
import com.aixforce.user.mysql.UserImageDao;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-21
 */
@Service
public class ImageServiceImpl implements ImageService {
    private final static Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Autowired
    private UserImageDao userImageDao;


    @Override
    public Response<Boolean> addUserImage(UserImage userImage) {
        Response<Boolean> result = new Response<Boolean>();
        if (userImage.getUserId() == null) {
            log.error("userId can not be null");
            result.setError("userId.not.found");
            return result;
        }
        if (userImage.getFileName() == null) {
            log.error("image file name can not be null");
            result.setError("fileName.not.found");
            return result;
        }
        if (userImage.getFileSize() == null) {
            log.error("image file size can noe be null");
            result.setError("fileSize.not.found");
            return result;
        }
        try {
            userImageDao.create(userImage);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to create {},cause:{}", userImage, Throwables.getStackTraceAsString(e));
            result.setError("add.UserImage.fail");
            return result;
        }
    }

    @Override
    public Response<UserImage> findUserImageById(Long imageId) {
        Response<UserImage> result = new Response<UserImage>();
        if (imageId == null) {
            log.error("imageId can not be null");
            result.setError("imageId.not.found");
            return result;
        }
        UserImage userImage = userImageDao.findById(imageId);
        result.setResult(userImage);
        return result;
    }

    @Override
    public Response<Boolean> deleteUserImage(UserImage userImage) {
        Response<Boolean> result = new Response<Boolean>();
        if (userImage == null) {
            log.error("imageId can not be null");
            result.setError("userImage.not.found");
            return result;
        }
        try {
            userImageDao.delete(userImage.getId());
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete {},cause:{}", userImage, Throwables.getStackTraceAsString(e));
            result.setError("userImage.delete.fail");
            return result;
        }
    }

    @Override
    public Response<Paging<UserImage>> findUserImages(Long userId, String category, Integer offset, Integer limit) {
        Response<Paging<UserImage>> result = new Response<Paging<UserImage>>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("userId.not.found");
            return result;
        }
        offset = Objects.firstNonNull(offset, 0);
        limit = Objects.firstNonNull(limit, 20);
        try {
            Paging<UserImage> userImageP = userImageDao.findByUserIdAndCategory(userId, category, offset, limit);
            result.setResult(userImageP);
            return result;
        } catch (Exception e) {
            log.error("failed to find UserImages by user_id={} limit {},{},cause:{}",
                    userId, offset, limit, Throwables.getStackTraceAsString(e));
            result.setError("userImage.not.found");
            return result;
        }
    }


    /**
     * 删除用户对应的上传记录
     *
     * @param userId 用户id
     */
    @Override
    public Response<Boolean> deleteByUserId(Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("userId can not be null");
            result.setError("userId.not.found");
            return result;
        }
        try {
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete userImage of user(id={}),cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("image.delete.fail");
            return result;
        }
    }


}
