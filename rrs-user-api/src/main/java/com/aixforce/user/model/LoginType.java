package com.aixforce.user.model;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-28
 */
public enum LoginType {
    EMAIL(1),
    MOBILE(2),
    NAME(3);

    private final int value;

    private LoginType(int value) {
        this.value = value;
    }

    public static LoginType from(int value) {
        for (LoginType loginType : LoginType.values()) {
            if (loginType.value == value) {
                return loginType;
            }
        }
        return null;
    }
}
