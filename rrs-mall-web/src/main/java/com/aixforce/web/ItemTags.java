package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemIndexService;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ItemTagService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-01
 */
@Controller
@RequestMapping("/api/seller/itemTags")
public class ItemTags {
    private final static Logger log = LoggerFactory.getLogger(ItemTags.class);

    public static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final ItemTagService itemTagService;

    private final LoadingCache<Long, Long> shopCache; //shop

    private final MessageSources messageSources;

    private final ItemService itemService;

    private final ItemIndexService itemIndexService;

    @Autowired
    public ItemTags(final ItemTagService itemTagService,
                    final ShopService shopService, MessageSources messageSources,
                    final ItemService itemService,
                    final ItemIndexService itemIndexService) {
        this.itemTagService = itemTagService;
        this.messageSources = messageSources;
        this.itemService = itemService;
        this.itemIndexService = itemIndexService;
        this.shopCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(50000).build(new CacheLoader<Long, Long>() {
                    @Override
                    public Long load(Long userId) throws Exception {
                        Response<Shop> result = shopService.findByUserId(userId);
                        if (result.isSuccess()) {
                            Shop shop = result.getResult();
                            if (shop != null) {
                                return shop.getId();
                            } else {
                                log.error("no shop found for user(id={})", userId);
                                throw new IllegalStateException("shop.not.found");
                            }
                        }
                        log.error("no shop found for user(id={})", userId);
                        throw new ServiceException("shop.query.fail");
                    }
                });
    }

    @RequestMapping(value = "/tree", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findTree() {
        Long userId = UserUtil.getUserId();
        Response<String> result = itemTagService.findTree(userId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw new JsonResponseException(500, result.getError());
        }
    }

    @RequestMapping(value = "/tree", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long saveTagTree(@RequestParam("tags") String tags, @RequestParam(value = "deleteTags", required = false) String deleteTags) {
        Long shopId = findShopId();
        List<String> tagsToBeRemoved = Collections.emptyList();
        if (!Strings.isNullOrEmpty(deleteTags)) {
            tagsToBeRemoved = JSON_MAPPER.fromJson(deleteTags,
                    JSON_MAPPER.createCollectionType(List.class, String.class));
        }
        Response<Long> result = itemTagService.saveTree(UserUtil.getMasterId(), shopId, tags, tagsToBeRemoved);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw new JsonResponseException(500, result.getError());
        }
    }


    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String addTagsForItems(@RequestParam("itemIds") String items, @RequestParam("tags") String tags) {
        Long masterId = findMasterId();
        List<Long> itemIds = JSON_MAPPER.fromJson(items, JSON_MAPPER.createCollectionType(List.class, Long.class));
        //tags is a json array ,should be convert
        List<String> tagList = JSON_MAPPER.fromJson(tags, JSON_MAPPER.createCollectionType(List.class, String.class));

        Response<Boolean> result = itemTagService.addTagsForItems(masterId, itemIds, tagList);
        if (result.isSuccess()) {
            //item real time index
            itemRealTimeIndex(itemIds);
            return "ok";
        } else {
            log.error("failed to add tag ({}) to items of user(id={}),cause:{}", tags, masterId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    @RequestMapping(value = "/item/{itemId}/removeTag", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String removeTagForItem(@PathVariable("itemId") Long itemId, @RequestParam("tag") String tag) {
        Long masterId = findMasterId();
        Response<Boolean> result = itemTagService.removeTagOfItem(masterId, itemId, tag);
        if (result.isSuccess()) {
            //item real time index
            itemRealTimeIndex(Lists.newArrayList(itemId));
            return "ok";
        } else {
            log.error("failed to remove tag ({}) for item (id={}),cause:{}", tag, itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/batchRemove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String removeTagsForItems(@RequestParam("itemIds") String items, @RequestParam("tags") String tags) {
        Long masterId = findMasterId();
        List<Long> itemIds = JSON_MAPPER.fromJson(items, JSON_MAPPER.createCollectionType(List.class, Long.class));
        //tags is a json array ,should be convert
        List<String> tagList = JSON_MAPPER.fromJson(tags, JSON_MAPPER.createCollectionType(List.class, String.class));
        Response<Boolean> result = itemTagService.removeTagsOfItems(masterId, itemIds, tagList);
        if (result.isSuccess()) {
            //item real time index
            itemRealTimeIndex(itemIds);
            return "ok";
        } else {
            log.error("failed to remove tags ({}) from items ({}),cause:{}", tags, itemIds, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    private Long findMasterId() {
        BaseUser user = UserUtil.getCurrentUser();
        return Objects.firstNonNull(user.getParentId(), user.getId());//优先取parentId,即主账号的id
    }

    private Long findShopId() {
        BaseUser user = UserUtil.getCurrentUser();
        Long userId = Objects.firstNonNull(user.getParentId(), user.getId());//优先取parentId,即主账号的id

        try {
            return shopCache.getUnchecked(userId);
        } catch (Exception e) {
            log.error("no shop found for user(id={})", userId);
            throw new JsonResponseException(500, messageSources.get("shop.not.found"));
        }
    }

    private void itemRealTimeIndex(List<Long> itemIds) {
        Iterables.removeIf(itemIds, new Predicate<Long>() {
            @Override
            public boolean apply(Long input) {
                Response<Item> itemR = itemService.findById(input);
                return !itemR.isSuccess() || itemR.getResult() == null || !Objects.equal(itemR.getResult().getStatus(), Item.Status.ON_SHELF.toNumber());
            }
        });
        if(itemIds.isEmpty()) {
            return;
        }
        itemIndexService.itemRealTimeIndex(itemIds, Item.Status.ON_SHELF);
    }

}
