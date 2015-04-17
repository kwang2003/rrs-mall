package com.aixforce.admin.event;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.service.DepositAccountService;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-12 1:26 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class AdminEventListener {

    private final AdminEventBus eventBus;

    private final DepositAccountService depositAccountService;


    @Autowired
    public AdminEventListener(AdminEventBus eventBus, DepositAccountService depositAccountService) {
        this.eventBus = eventBus;
        this.depositAccountService = depositAccountService;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void createDepositAccount(OuterCodeSetEvent outerCodeSetEvent) {

        try {
            Long shopId = outerCodeSetEvent.getShopId();
            String outerCode = outerCodeSetEvent.getOuterCode();

            checkArgument(notNull(shopId), "shop.id.empty");
            checkArgument(notEmpty(outerCode), "outer.code.empty");

            Response<Long> createResult = depositAccountService.create(shopId, outerCode);
            checkState(createResult.isSuccess(), createResult.getError());

        } catch (IllegalArgumentException e) {
            log.error("fail to create deposit account with outerCodeSetEvent:{}, error:{}",
                    outerCodeSetEvent, e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to create deposit account with outerCodeSetEvent:{}, error:{}",
                    outerCodeSetEvent, e.getMessage());
        } catch (Exception e) {
            log.error("fail to create deposit account with outerCodeSetEvent:{}, cause:{}",
                    outerCodeSetEvent, Throwables.getStackTraceAsString(e));
        }
    }

}
