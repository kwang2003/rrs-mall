package com.aixforce.item.event;

import com.aixforce.item.service.FeedItemService;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Description: <br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/12/22 <br/>
 */
@Slf4j
@Component
public class DspEventListener {

    private final ItemEventBus eventBus;

    private final FeedItemService feedItemService;

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
    }

    @Autowired
    public DspEventListener(ItemEventBus eventBus, FeedItemService feedItemsDao){
        this.eventBus = eventBus;
        this.feedItemService = feedItemsDao;
    }

    /**
     * 记录变化itemId
     * @param dspEvent
     */
    @Subscribe
    public void recordRedis(DspEvent dspEvent){
        try{

            Long itemId = dspEvent.getItemId();
            feedItemService.createChangeItem(itemId);

        }catch (Exception e){
            log.error("fail to add items data by Dsp with registerEvent:{}, error:{}",
                    dspEvent, Throwables.getStackTraceAsString(e));
            e.printStackTrace();
        }

    }
}
