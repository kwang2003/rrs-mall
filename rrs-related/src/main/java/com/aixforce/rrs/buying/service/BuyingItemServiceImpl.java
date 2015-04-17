package com.aixforce.rrs.buying.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dao.BuyingItemDao;
import com.aixforce.rrs.buying.manager.BuyingActivityManger;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 抢购活动商品关联service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class BuyingItemServiceImpl implements BuyingItemService{

    @Autowired
    private BuyingItemDao buyingItemDao;

    @Autowired
    private BuyingActivityManger buyingActivityManger;

    @Override
    public Response<BuyingItem> create(BuyingItem buyingItem) {

        Response<BuyingItem> result = new Response<BuyingItem>();

        try {
            checkArgument(!isNull(buyingItem),"illegal.param");
            buyingItemDao.create(buyingItem);
            result.setResult(buyingItem);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to create buyingItem {}, cause:{}", buyingItem, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.create.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(BuyingItem buyingItem) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingItem),"illegal.param");
            checkArgument(!isNull(buyingItem.getId()),"illegal.param");
            result.setResult(buyingItemDao.update(buyingItem));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to update buyingItem {}, cause:{}", buyingItem, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            result.setResult(buyingItemDao.delete(id));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to delete buyingItem (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.delete.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingItem> findById(Long id) {
        Response<BuyingItem> result = new Response<BuyingItem>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            BuyingItem bd = buyingItemDao.findById(id);
            checkState(!isNull(bd), "buying.item.not.found");
            result.setResult(bd);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to find buyingItem(id = {}),error:{}",id,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to find buyingItem(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.query.failed");
            return result;
        }
    }

    @Override
    public Response<List<BuyingItem>> findByActivityId(Long id) {

        Response<List<BuyingItem>> result = new Response<List<BuyingItem>>();

        try {
            checkArgument(!isNull(id),"illegal.param");
            List<BuyingItem> buyingItemList = buyingItemDao.findByActivityId(id);
            checkState(!isNull(buyingItemList), "buying.item.not.found");
            result.setResult(buyingItemList);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("not to find buyingItem by activity(id = {}),error:{}",id,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to find buyingItem by activity(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.query.failed.by.activity");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateFakeSoldQuantity(List<BuyingItem> buyingItemList) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingItemList),"illegal.param");
            result.setResult(buyingActivityManger.updateFakeSoldQuantity(buyingItemList));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to batch update buyingItem list={}, cause:{}", buyingItemList, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.update.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingItem> findByActivityIdAndItemId(Long activityId, Long itemId) {
        Response<BuyingItem> result = new Response<BuyingItem>();
        try {
            checkArgument(!isNull(activityId),"illegal.param");
            checkArgument(!isNull(itemId),"illegal.param");
            BuyingItem buyingItem = buyingItemDao.findByActivityIdAnditemId(activityId,itemId);
            checkState(!isNull(buyingItem), "buying.item.not.found");
            result.setResult(buyingItem);
            return  result;
        }catch (IllegalStateException e){
            log.error("not to find buyingItem by activity(id = {}) and itemId(id={}),error:{}",activityId,itemId,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e){
            log.error("failed to find buyingItem by activity(id = {}) and itemId(id={}), cause:{}", activityId,itemId, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.query.failed.by.activity");
            return result;
        }
    }


    @Override
    public Response<BuyingItem> findLatestByItemId(Long itemId) {
        Response<BuyingItem> result = new Response<BuyingItem>();
        try {
            checkArgument(!isNull(itemId),"illegal.param");
            BuyingItem buyingItem = buyingItemDao.findLatestByItemId(itemId);
            checkState(!isNull(buyingItem),"buying.item.not.found");
            result.setResult(buyingItem);
        } catch (Exception e) {
            log.error("failed to find latest buyingItem by itemId {}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("buying.item.not.found");
        }
        return result;
    }
}
