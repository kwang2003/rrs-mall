package com.aixforce.restful.event;

import com.aixforce.common.model.Response;
import com.aixforce.user.model.UserAccountSummary;
import com.aixforce.user.service.UserAccountSummaryService;
import com.aixforce.web.controller.api.userEvent.UserEventBus;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * 第三方注册用户事件
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-10 10:11 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class ThirdRegisterEventListener {


    private final UserEventBus eventBus;

    private final UserAccountSummaryService userSummaryService;

    @Autowired
    public ThirdRegisterEventListener(UserEventBus eventBus, UserAccountSummaryService userSummaryService) {
        this.eventBus = eventBus;
        this.userSummaryService = userSummaryService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void createUserAccountSummary(ThirdRegisterEvent registerEvent) {
        try {

            String channel = registerEvent.getChannel();
            // 若渠道为空，则不作处理
            if (isEmpty(channel)) return;

            checkState(notNull(registerEvent.getUserId()), "user.id.empty");
            checkState(notEmpty(registerEvent.getUserName()), "user.name.empty");

            UserAccountSummary creating = new UserAccountSummary();
            creating.setUserId(registerEvent.getUserId());
            creating.setUserName(registerEvent.getUserName());


            String activity = registerEvent.getActivity();
            activity = Objects.firstNonNull(activity, "");

            String from = registerEvent.getFrom();
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

}
