package com.aixforce.alipay.event;

import com.aixforce.alipay.request.TradeCloseRequest;
import com.aixforce.common.model.Response;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-07 4:31 PM  <br>
 * Author: xiao
 */
@Slf4j
@SuppressWarnings("all")
public class TradeCloseEventListener {

    private final AlipayEventBus eventBus;

    public TradeCloseEventListener(AlipayEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void closeTrade(TradeCloseEvent event) {
        try {

            log.info("Alipay: try to close trade:{}", event.getOuterOrderNo());
            Response<Boolean> response = TradeCloseRequest.build(event.getToken())
                    .outOrderNo(event.getOuterOrderNo()).notifyToClose();
            checkState(response.isSuccess(), response.getError());
            log.info("Alipay: close trade:{} successfully", event.getOuterOrderNo());

        } catch (IllegalStateException e) {
            log.warn("Alipay: fail to close trade(no:{}), error:{}",
                    event.getOuterOrderNo(), e.getMessage());
        } catch (Exception e) {
            log.error("Alipay: fail to close trade(no:{}), cause:{}",
                    event.getOuterOrderNo(), Throwables.getStackTraceAsString(e));
        }
    }
}
