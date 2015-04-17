package com.aixforce.admin.web.interceptors;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * Author: jlchen
 * Date: 2013-01-22
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private final static Logger log = LoggerFactory.getLogger(LoginInterceptor.class);

    private final AccountService<User> accountService;

    @Autowired
    public LoginInterceptor(AccountService<User> accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute(CommonConstants.SESSION_USER_ID);
            if (userId != null) {

                Response<User> result = accountService.findUserById(Long.parseLong(userId.toString()));
                if (!result.isSuccess()) {
                    log.error("failed to find user where id={},error code:{}", userId, result.getError());
                    return false;
                }
                User user = result.getResult();
                BaseUser baseUser = new BaseUser(user.getId(), user.getName(), user.getType());
                baseUser.setParentId(user.getParentId());
                UserUtil.putCurrentUser(baseUser);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserUtil.removeUser();
    }
}
