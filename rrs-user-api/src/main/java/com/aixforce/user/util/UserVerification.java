package com.aixforce.user.util;

import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-04-08 12:01 PM  <br>
 * Author: xiao
 */
public class UserVerification {

    public static  <T extends BaseUser> boolean isAdmin(T user) {
        return user!=null && Objects.equal(user.getTypeEnum(), BaseUser.TYPE.ADMIN);
    }

    public static  <T extends BaseUser> boolean isFinance(T user) {
        return user!=null && Objects.equal(user.getTypeEnum(), BaseUser.TYPE.FINANCE);
    }

    public static <T extends BaseUser> boolean isSiteOwner(T user) {
        return user!=null && Objects.equal(user.getTypeEnum(), BaseUser.TYPE.SITE_OWNER);
    }

    public static <T extends BaseUser> boolean isNotAdmin(T user) {
        return !isAdmin(user);
    }

    public static <T extends BaseUser> boolean isNotFinance(T user) {
        return !isFinance(user);
    }

    public static <T extends BaseUser> boolean isSeller(T user) {
        return Objects.equal(user.getTypeEnum(), BaseUser.TYPE.SELLER);
    }

    public static <T extends BaseUser> void nonAdminDenied(T user) {
        checkState(isAdmin(user), "user.has.no.permission");
    }

    public static <T extends User> boolean active(T user) {
        return !frozen(user) && !locked(user);
    }

    public static <T extends User> boolean inactive(T user) {
        return !active(user);
    }

    public static <T extends User> boolean locked(T user) {
        return Objects.equal(User.STATUS.LOCKED.toNumber(), user.getStatus());
    }

    public static <T extends User> boolean frozen(T user) {
        return Objects.equal(User.STATUS.FROZEN.toNumber(), user.getStatus());
    }

}
