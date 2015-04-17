package com.aixforce.user.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.dto.UserProfileDto;
import com.aixforce.user.model.User;
import com.aixforce.user.model.UserProfile;
import com.aixforce.user.mysql.UserDao;
import com.aixforce.user.mysql.UserProfileDao;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-07-17
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {
    private final static Logger log = LoggerFactory.getLogger(UserProfileServiceImpl.class);

    @Autowired
    private UserProfileDao userProfileDao;

    @Autowired
    private UserDao userDao;

    public Response<Long> createUserProfile(UserProfile userProfile) {
        Response<Long> result = new Response<Long>();
        if (userProfile.getUserId() == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null.fail");
            return result;
        }
        try {
            userProfileDao.create(userProfile);
            result.setResult(userProfile.getId());
            return result;
        } catch (Exception e) {
            log.error("failed to create {},cause:{}", userProfile, Throwables.getStackTraceAsString(e));
            result.setError("user.profile.create.fail");
            return result;
        }
    }

    @Override
    public Response<UserProfile> findUserProfileByUserId(Long userId) {
        Response<UserProfile> result = new Response<UserProfile>();
        try {
            UserProfile userProfile = userProfileDao.findByUserId(userId);
            if (userProfile == null) {
                result.setError("result.not.found");
                return result;
            }
            result.setResult(userProfile);
            return result;
        } catch (Exception e) {
            log.error("failed to find userProfile by userId={},cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("query.fail");
            return result;

        }
    }

    @Override
    public Response<UserProfileDto> findUserProfileByUser(BaseUser baseUser) {
        Response<UserProfileDto> result = new Response<UserProfileDto>();
        UserProfileDto userProfileDto = new UserProfileDto();
        try {
            User user = userDao.findById(baseUser.getId());
            userProfileDto.setEmail(user.getEmail());
            userProfileDto.setName(user.getName());
            userProfileDto.setMobile(user.getMobile());
            userProfileDto.setAvatar(user.getAvatar());
            Response<UserProfile> profile = findUserProfileByUserId(baseUser.getId());
            if (profile.isSuccess()) {
                userProfileDto.setUserProfile(profile.getResult());
            }else{
                log.error("failed to find user profile (userId={}),cause:{}",baseUser.getId(), profile.getError());
            }
            result.setResult(userProfileDto);
        } catch (Exception e) {
            log.error("failed to find profile by user, userId={}, cause:{}", baseUser.getId(), Throwables.getStackTraceAsString(e));
            result.setError("query.fail");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateUserProfileByUserId(UserProfile userProfile) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            UserProfile up = userProfileDao.findByUserId(userProfile.getUserId());
            if (up == null) {
                createUserProfile(userProfile);
            } else {
                userProfileDao.updateByUserId(userProfile);
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to update {},cause:{}", userProfile, Throwables.getStackTraceAsString(e));
            result.setError("user.profile.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> deleteUserProfileByUserId(Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null.fail");
            return result;
        }
        try {
            userProfileDao.deleteByUserId(userId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete userProfile by userId={},cause:{}", userId, Throwables.getStackTraceAsString(e));
            result.setError("user.profile.delete.fail");
            return result;
        }
    }
}
