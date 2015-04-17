package com.aixforce.item.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;

import java.util.List;

/**
 * 索引商品服务,将商品信息dump至搜索引擎
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-15
 */
public interface ItemIndexService {

    /**
     * 全量dump搜索引擎
     */
    Response<Boolean> fullDump();

    /**
     * 增量dump搜索引擎
     *
     * @param interval 间隔时间,以分钟计算
     */
    Response<Boolean> deltaDump(Integer interval);

    /**
     * 准实时dump搜索引擎
     * @param itemIds 商品id列表
     * @param status  商品状态
     * @return  操作是否成功
     */
    Response<Boolean> itemRealTimeIndex(List<Long> itemIds, Item.Status status);
}
