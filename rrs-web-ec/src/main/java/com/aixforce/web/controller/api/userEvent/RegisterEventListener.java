package com.aixforce.web.controller.api.userEvent;

import com.aixforce.common.model.Response;
import com.aixforce.user.model.UserAccountSummary;
import com.aixforce.user.service.UserAccountSummaryService;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-09 5:42 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class RegisterEventListener {

    private final UserEventBus eventBus;

    private final UserAccountSummaryService userSummaryService;

    @Autowired
    public RegisterEventListener(UserEventBus eventBus, UserAccountSummaryService userSummaryService) {
        this.eventBus = eventBus;
        this.userSummaryService = userSummaryService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void createUserAccountSummary(RegisterEvent registerEvent) {
        try {

            HttpServletRequest request = registerEvent.getRequest();
            Map<String, String> mappedCookie = getMappedCookie(request);

            checkState(notNull(registerEvent.getUserId()), "user.id.empty");
            checkState(notEmpty(registerEvent.getUserName()), "user.name.empty");

            UserAccountSummary creating = new UserAccountSummary();
            creating.setUserId(registerEvent.getUserId());
            creating.setUserName(registerEvent.getUserName());

            String channel = mappedCookie.get("channel");
            channel = Objects.firstNonNull(channel, "web");

            String activity = mappedCookie.get("active");
            activity = Objects.firstNonNull(activity, "");

            String from = mappedCookie.get("source");
            from = Objects.firstNonNull(from, "");

            creating.setChannel(channel);
            creating.setActivity(activity);
            creating.setFrom(from);
            creating.setLoginType(1L);

            Response<Long> summaryResult = userSummaryService.create(creating);
            checkState(summaryResult.isSuccess(), summaryResult.getError());

        } catch (IllegalStateException e) {
            log.error("fail to created userAccountSummary with registerEvent:{}, error:{}",
                    registerEvent, e.getMessage());
        } catch (Exception e) {
            log.error("fail to created userAccountSummary with registerEvent:{}, error:{}",
                    registerEvent, Throwables.getStackTraceAsString(e));
        }
    }


    private Map<String, String> getMappedCookie(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for(Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        return cookieKVs;
    }


}
