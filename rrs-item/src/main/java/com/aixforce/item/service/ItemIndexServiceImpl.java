package com.aixforce.item.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.manager.ItemBatchIndexer;
import com.aixforce.item.manager.ItemRealTimeIndexer;
import com.aixforce.item.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-15
 */
@Service
public class ItemIndexServiceImpl implements ItemIndexService {

    private final static Logger log = LoggerFactory.getLogger(ItemIndexServiceImpl.class);

    private final ItemBatchIndexer itemBatchIndexer;

    private final ItemRealTimeIndexer itemRealTimeIndexer;

    @Autowired
    public ItemIndexServiceImpl(ItemBatchIndexer itemBatchIndexer,
                                ItemRealTimeIndexer itemRealTimeIndexer) {
        this.itemBatchIndexer = itemBatchIndexer;
        this.itemRealTimeIndexer = itemRealTimeIndexer;
    }

    /**
     * 全量dump搜索引擎
     */
    @Override
    public Response<Boolean> fullDump() {

        Response<Boolean> result = new Response<Boolean>();

        try {
            itemBatchIndexer.fullDump();
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to full dump item", e);
            result.setError("item.full.dump.fail");
            return result;
        }


    }

    /**
     * 增量dump搜索引擎
     *
     * @param interval 间隔时间,以分钟计算
     */
    @Override
    public Response<Boolean> deltaDump(Integer interval) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            itemBatchIndexer.deltaDump(interval);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delta dump item", e);
            result.setError("item.delta.dump.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> itemRealTimeIndex(List<Long> itemIds, Item.Status status) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            itemRealTimeIndexer.index(itemIds, status);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("failed to index on-shelf items when add or remove tags", e);
            result.setError("item.dump.fail");
            return result;
        }
    }
}
