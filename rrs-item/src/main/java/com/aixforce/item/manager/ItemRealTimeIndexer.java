package com.aixforce.item.manager;

import com.aixforce.item.dto.RichItem;
import com.aixforce.item.model.Item;
import com.aixforce.search.endpoint.SearchExecutor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * 准实时的dump item
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-15
 */
@Component
public class ItemRealTimeIndexer extends ItemBaseIndexer {

    private final static Logger log = LoggerFactory.getLogger(ItemRealTimeIndexer.class);

    public static final String ITEM_INDEX_NAME = "items";
    public static final String ITEM_INDEX_TYPE = "item";

    private final SearchExecutor searchExecutor;

    private final ExecutorService executorService;

    @Autowired
    ItemRealTimeIndexer(SearchExecutor searchExecutor) {
        this.searchExecutor = searchExecutor;
        this.executorService = new ThreadPoolExecutor(2, 4, 60L, TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(1000),
                new ThreadFactoryBuilder().setNameFormat("item-indexer-%d").build(),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                        IndexTask indexTask = (IndexTask) runnable;
                        log.error("item(id={}) index request is rejected", indexTask.getItemId());
                    }
                });
    }

    public void index(List<Long> itemIds, Item.Status status) {

        for (Long itemId : itemIds) {
            switch (status) {
                case DELETED:
                case OFF_SHELF:
                case FROZEN:
                    Item item = new Item();
                    item.setId(itemId);
                    searchExecutor.submit(ITEM_INDEX_NAME, ITEM_INDEX_TYPE, item,SearchExecutor.OP_TYPE.DELETE);
                    break;
                case ON_SHELF:
                    IndexTask task = new IndexTask(itemId);
                    this.executorService.submit(task);
                    break;
            }
        }
    }

    public void index(Long itemId) {
        IndexTask task = new IndexTask(itemId);
        this.executorService.submit(task);
    }

    public void delete(List<Long> itemIds) {
        for (Long itemId : itemIds) {
            IndexTask task = new IndexTask(itemId, true);
            this.executorService.submit(task);
        }
    }

    public void delete(Long itemId) {
        IndexTask task = new IndexTask(itemId, true);
        this.executorService.submit(task);
    }

    private class IndexTask implements Runnable {

        private final Long itemId;

        private final boolean delete;

        private IndexTask(Long itemId) {
            this(itemId, false);
        }

        private IndexTask(Long itemId, boolean delete) {
            this.itemId = itemId;
            this.delete = delete;
        }

        @Override
        public void run() {
            if (delete) {
                Item item = new Item();
                item.setId(itemId);
                searchExecutor.submit(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, item, SearchExecutor.OP_TYPE.DELETE);
                return;
            }
            Item item = itemDao.findById(itemId);
            Item.Status status = Item.Status.fromNumber(item.getStatus());
            switch (status) {
                case DELETED:
                case OFF_SHELF:
                case FROZEN:
                    searchExecutor.submit(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, item, SearchExecutor.OP_TYPE.DELETE);
                    break;
                case ON_SHELF:
                    RichItem richItem = richItems.make(item);
                    searchExecutor.submit(ITEM_INDEX_NAME,ITEM_INDEX_TYPE, richItem, SearchExecutor.OP_TYPE.INDEX);
                    break;
            }

        }

        private long getItemId() {
            return itemId;
        }
    }
}
