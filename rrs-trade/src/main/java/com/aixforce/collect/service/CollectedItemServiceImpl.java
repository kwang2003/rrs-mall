package com.aixforce.collect.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.collect.dao.CollectedItemDao;
import com.aixforce.collect.dto.CollectedBar;
import com.aixforce.collect.dto.CollectedItemInfo;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.manager.CollectedManager;
import com.aixforce.collect.model.CollectedItem;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.service.BuyingActivityDefinitionService;
import com.aixforce.rrs.buying.service.BuyingItemService;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 6:26 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class CollectedItemServiceImpl implements CollectedItemService {

    @Autowired
    private CollectedManager collectedManager;

    @Autowired
    private CollectedItemDao collectedItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BuyingItemService buyingItemService;

    @Autowired
    private BuyingActivityDefinitionService buyingActivityDefinitionService;


    /**
     * 添加商品收藏记录
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @return 操作是否成功
     */
    @Override
    public Response<CollectedSummary> create(Long userId, Long itemId,Long activityId) {
        Response<CollectedSummary> result = new Response<CollectedSummary>();

        try {
            CollectedItem collectedItem = new CollectedItem();
            collectedItem.setBuyerId(userId);
            collectedItem.setItemId(itemId);

            Response<Item> itemResponse = itemService.findById(itemId);
            checkState(itemResponse.isSuccess(), itemResponse.getError());

            Item item = itemResponse.getResult();
            collectedItem.setItemNameSnapshot(item.getName());
            collectedItem.setBuyingActivityId(activityId);

            collectedManager.createCollectedItem(collectedItem);

            Long total = collectedItemDao.countOf(userId);
            result.setResult(new CollectedSummary(collectedItem.getId(), total));

        } catch (IllegalArgumentException e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, error:{}",
                    userId, itemId, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, error:{}",
                    userId, itemId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create collect item whit userId:{}, itemId:{}, cause:{}",
                    userId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.create.fail");
        }

        return result;
    }

    /**
     * 删除商品收藏记录
     *
     * @param userId 用户id
     * @param itemId 店铺id
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> delete(Long userId, Long itemId,Long activityId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            CollectedItem deleting = collectedItemDao.getByUserIdAndItemIdAndActivityId(userId,itemId,activityId);
            if (notNull(deleting)) {
               collectedItemDao.delete(deleting.getId());
            }
            result.setResult(Boolean.TRUE);

        } catch (Exception e) {
            log.error("fail to delete collected item with userId:{}, itemId:{}, cause:{}",
                    userId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }
        return result;
    }

    /**
     * 删除商品收藏记录
     *
     * @param userId 用户id
     * @param itemId 店铺id
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> delete(Long userId, Long itemId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            //CollectedItem deleting = collectedItemDao.getByUserIdAndItemIdAndActivityId(userId,itemId,activityId);
            //if (notNull(deleting)) {
                collectedItemDao.delete(itemId);
            //}
            result.setResult(Boolean.TRUE);

        } catch (Exception e) {
            log.error("fail to delete collected item with userId:{}, itemId:{}, cause:{}",
                    userId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }
        return result;
    }

    /**
     * 查询用户收藏的商品
     *
     * @param itemName 商品名称，选填，模糊匹配
     * @param baseUser 查询用户
     * @return 收藏商品分页
     */
    @Override
    public Response<Paging<CollectedItemInfo>> findBy(@ParamInfo("itemName") @Nullable String itemName,
                                                  @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                  @ParamInfo("size") @Nullable Integer size,
                                                  @ParamInfo("baseUser") BaseUser baseUser) {

        Response<Paging<CollectedItemInfo>> result = new Response<Paging<CollectedItemInfo>>();

        try {

            PageInfo pageInfo = new PageInfo(pageNo, size);

            CollectedItem criteria = new CollectedItem();
            criteria.setItemNameSnapshot(itemName);
            criteria.setBuyerId(baseUser.getId());
            Paging<CollectedItem> paging = collectedItemDao.findBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(appendItemDetail(paging));


        } catch (Exception e) {
            log.error("fail to query collect items by itemName:{}, pageNo:{}, size:{}, uid:{} cause:{}",
                    itemName, pageNo, size, baseUser.getId(), Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(CollectedItemInfo.class));
        }

        return result;
    }


    private Paging<CollectedItemInfo> appendItemDetail(Paging<CollectedItem> paging) {
        List<CollectedItem> collectedItems = paging.getData();
        List<CollectedItemInfo> dtos = Lists.newArrayListWithCapacity(collectedItems.size());
        List<Long> ids = Lists.newArrayListWithCapacity(collectedItems.size());

        for (CollectedItem item : collectedItems) {
            ids.add(item.getItemId());
        }

        Response<List<Item>> itemRes = itemService.findByIds(ids);
        checkState(itemRes.isSuccess(), itemRes.getError());

        Map<Long, Item> mappedItems = convertToMappedItems(itemRes.getResult());

        for (CollectedItem collectedItem : collectedItems) {
            Long activityId = collectedItem.getBuyingActivityId();
            // 判断活动id不为空的时候，为抢购商品收藏
            if (activityId != null) {
                // 根据活动id 查询抢购商品信息
                Response<BuyingItem> result = buyingItemService.findByActivityIdAndItemId(activityId,collectedItem.getItemId());
                if (result.isSuccess() && result.getResult()!=null) {
                    BuyingItem buyingItem = result.getResult();
                    collectedItem.setIsBuying(Boolean.TRUE);
                    collectedItem.setBuyingItemId(buyingItem.getId());
                    collectedItem.setItemBuyingPrice(buyingItem.getItemBuyingPrice());
                    // 根据活动id查询该次活动信息
                    Response<BuyingActivityDefinition> buyingActivityDefinitionResponse =buyingActivityDefinitionService.findById(activityId);
                    if(buyingActivityDefinitionResponse.isSuccess()){
                        BuyingActivityDefinition buyingActivityDefinition = buyingActivityDefinitionResponse.getResult();
                        // 设置活动状态，供前台显示
                        collectedItem.setActivityStatus(buyingActivityDefinition.getStatus());
                    }
                }
            }
            collectedItem.setId(collectedItem.getId());
            collectedItem.setPriId(collectedItem.getId());
            dtos.add(CollectedItemInfo.transform(collectedItem, mappedItems.get(collectedItem.getItemId())));
            ids.add(collectedItem.getId());

        }
        //
        return new Paging<CollectedItemInfo>(paging.getTotal(), dtos);
    }

    private Map<Long, Item> convertToMappedItems(List<Item> items) {
        Map<Long, Item> mappedItems = Maps.newHashMap();
        for (Item item : items) {
            mappedItems.put(item.getId(), item);
        }

        return mappedItems;
    }


    /**
     * 批量删除商品收藏记录
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> bulkDelete(Long userId, List<Long> itemIds) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            collectedManager.bulkDeleteCollectedItems(userId, itemIds);
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, itemIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, itemIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, cause:{}",
                    userId, itemIds, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }

        return result;
    }

    /**
     * 商品收藏组件(显示在商品详情页)
     *
     * @param itemId    商品id
     * @return 商品收藏组件所需信息
     */
    @Override
    public Response<Map<String,Long>> getBarOfItem(@ParamInfo("itemId") Long itemId,@ParamInfo("activityId") Long activityId) {
        //Response<Long> result = new Response<Long>();
        Response<Map<String,Long>> result = new Response<Map<String,Long>>();
        try {
            Map<String,Long> map = Maps.newHashMap();
            map.put("itemId",itemId);
            map.put("activityId",activityId);
            result.setResult(map);
        } catch (Exception e) {
            log.error("fail to get bar with itemId:{},  cause:{}", itemId, Throwables.getStackTraceAsString(e));
        }
        return result;
    }


    @Override
    public Response<CollectedBar> collected(@ParamInfo("itemId") Long itemId,
                                            @ParamInfo("buyerId") Long buyerId,@ParamInfo("activityId") Long activityId) {
        Response<CollectedBar> result = new Response<CollectedBar>();

        try {
            if (isNull(buyerId)) {
                result.setResult(new CollectedBar());
                return result;
            }

            //Long buyingActivityId = null;
           // Response<BuyingItem> buyingItemResponse = buyingItemService.findLatestByItemId(itemId);
           // i/f (buyingItemResponse.isSuccess() && buyingItemResponse.getResult()!=null) {
            //    BuyingItem buyingItem = buyingItemResponse.getResult();
            //    buyingActivityId = buyingItem.getBuyingActivityId();
            //}

            //CollectedItem collectedItem = collectedItemDao.getByUserIdAndItemId(buyerId, itemId);
            CollectedItem collectedItem = collectedItemDao.getByUserIdAndItemIdAndActivityId(buyerId,itemId,activityId);
            Boolean hasCollected = notNull(collectedItem) ? Boolean.TRUE : Boolean.FALSE;
            result.setResult(new CollectedBar(itemId, hasCollected));

        } catch (Exception e) {
            log.error("fail to get bar with itemId:{}, buyer:{}, cause:{}",
                    itemId, buyerId, Throwables.getStackTraceAsString(e));
            // 加载失败不影响整个详情页面的渲染
            result.setResult(new CollectedBar());
        }

        return result;
    }
}
