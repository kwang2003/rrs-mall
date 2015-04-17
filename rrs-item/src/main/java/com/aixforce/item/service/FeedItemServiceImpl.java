package com.aixforce.item.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.dao.redis.FeedItemsDao;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: <br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE: 2014/12/22 <br/>
 */
@Service
public class FeedItemServiceImpl implements FeedItemService {

    private final static Logger log = LoggerFactory.getLogger(FeedItemServiceImpl.class);

    @Autowired
    private FeedItemsDao feedItemsDao;

    @Override
    public Response<Boolean> createChangeItem(Long itemId){

        Response<Boolean> result = new Response<Boolean>();
        try {

            feedItemsDao.create(itemId);
            result.setSuccess(true);
        }catch (Exception e){
            result.setResult(false);
            log.error("failed to add items for itemId {},cause:{}", itemId, Throwables.getStackTraceAsString(e));
        }

        return result;
    }
}
