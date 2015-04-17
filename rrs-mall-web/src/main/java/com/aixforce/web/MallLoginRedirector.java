package com.aixforce.web;

import com.aixforce.user.base.BaseUser;
import com.aixforce.web.controller.api.LoginRedirector;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-02
 */
@Component
public class MallLoginRedirector implements LoginRedirector {

    @Value("#{app.mainSite}")
    private  String mainSite;
    /**
     * 生成登陆后待跳转的url地址
     *
     * @param target   如果指定了target,则直接返回target
     * @param user 用户
     * @return 待跳转的url地址
     */
    @Override
    public String redirectTarget(String target, BaseUser user) {
        if (!Strings.isNullOrEmpty(target)) {
            return target;
        } else {
            switch (BaseUser.TYPE.fromNumber(user.getType())) {
                case BUYER:
                    return"http://" + mainSite + "/buyer/index";
                case SELLER:
                    return "http://" + mainSite + "/seller/index";
                default:
                    return "http://" + mainSite;
            }
        }
    }
}
