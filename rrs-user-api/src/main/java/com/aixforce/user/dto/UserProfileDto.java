package com.aixforce.user.dto;

import com.aixforce.user.model.UserProfile;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * User: yangzefeng
 * Date: 13-12-5
 * Time: 上午11:30
 */
public class UserProfileDto implements Serializable{

    private static final long serialVersionUID = 9168574133153336154L;
    @Getter
    @Setter
    private UserProfile userProfile;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String avatar;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String mobile;
}
