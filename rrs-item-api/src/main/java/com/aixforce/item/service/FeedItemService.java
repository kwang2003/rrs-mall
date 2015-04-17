package com.aixforce.item.service;

import com.aixforce.common.model.Response;

/**
 * @Description: <br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/12/22 <br/>
 */
public interface FeedItemService {

    /**
     * 记录库存发生更新的items
     * @return
     */
    Response<Boolean>  createChangeItem(Long itemId);
}
