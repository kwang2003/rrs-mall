/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api.userEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-10
 */
@Component
public class LoginTokenRefresher {

    private final UserEventBus eventBus;

    @Autowired
    public LoginTokenRefresher(UserEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    /*@Subscribe
    public void deleteSessionId(LogoutEvent logoutEvent){
        AFSessionManager.instance().removeSessionIdCookie(logoutEvent.getResponse());
    }*/
}
