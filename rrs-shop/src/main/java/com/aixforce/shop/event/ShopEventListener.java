package com.aixforce.shop.event;

import com.aixforce.shop.manager.ShopManager;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-06-07 5:26 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class ShopEventListener {

    private final ShopEventBus eventBus;

    private final ShopManager shopManager;



    @Autowired
    public ShopEventListener(ShopEventBus eventBus, ShopManager shopManager) {
        this.eventBus = eventBus;
        this.shopManager = shopManager;
    }

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void createExtra(ApprovePassEvent approvePassEvent) {
        List<Long> ids = approvePassEvent.getIds();
        try {
            checkArgument(notNull(ids), "ids.can.not.be.empty");
            shopManager.bulkCreateExtraOfShops(ids);

        } catch (IllegalArgumentException e) {
            log.error("fail to create shopExtra with ids:{}, error:{}", ids, e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to create shopExtra with ids:{}, error:{}", ids, e.getMessage());
        } catch (Exception e) {
            log.error("fail to create shopExtra with ids:{}, cause:{}", ids, Throwables.getStackTraceAsString(e));
        }
    }

}
