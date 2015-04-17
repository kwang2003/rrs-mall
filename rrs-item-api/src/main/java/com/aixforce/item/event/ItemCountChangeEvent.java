package com.aixforce.item.event;

import com.aixforce.common.model.Response;
import com.aixforce.item.service.ItemService;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by yangzefeng on 14-5-13
 */
@Component
@Slf4j
public class ItemCountChangeEvent {

    @Autowired
    private ItemEventBus eventBus;

    @Autowired
    private ItemService itemService;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void itemCountChange(ItemCountEvent event) {
        List<Long> shopIds = event.getShopIds();
        for(Long shopId : shopIds) {
            Response<Long> itemNumR = itemService.countOnShelfByShopId(shopId);
            if(!itemNumR.isSuccess()) {
                log.error("fail to find on shelf item by shopId={},error code:{}", shopId, itemNumR.getError());
            }
            Long itemNum = itemNumR.getResult();
            Response<Boolean> setR = itemService.setItemCountByShopId(shopId, itemNum);
            if(!setR.isSuccess()) {
                log.error("fail to set item count by shopId={},itemNum={}, error code:{}",
                        shopId, itemNum, setR.getError());
            }
        }
    }
}
