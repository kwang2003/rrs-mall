package com.aixforce.item.manager;

import com.aixforce.item.dao.mysql.ItemDao;
import com.aixforce.item.service.RichItems;
import com.aixforce.search.ESClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-15
 */
abstract class ItemBaseIndexer {

    @Autowired
    protected ESClient esClient;

    @Autowired
    protected RichItems richItems;

    @Autowired
    protected ItemDao itemDao;
}
