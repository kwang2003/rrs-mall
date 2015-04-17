package com.aixforce.user.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.dto.UserProfileDto;
import com.aixforce.user.model.UserProfile;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-07-17
 */
public interface UserProfileService {

    Response<Long> createUserProfile(UserProfile userProfile);

    Response<UserProfile> findUserProfileByUserId(Long userId);

    Response<UserProfileDto> findUserProfileByUser(@ParamInfo("baseUser") BaseUser baseUser);

    Response<Boolean> updateUserProfileByUserId(UserProfile userProfile);

    Response<Boolean> deleteUserProfileByUserId(Long userId);
}
