package com.aixforce.web;

import com.aixforce.collect.dto.CollectedBar;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.service.CollectedItemService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.trade.service.CartService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 商品收藏
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-13 11:19 AM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping(value = "/api/collect-item")
public class CollectedItems {

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CollectedItemService collectedItemService;

    private Splitter splitter = Splitter.on(",");




    /**
     * 用户添加商品收藏
     * @param itemId    商品id
     */
    @RequestMapping(value = "/{itemId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CollectedBar collected(@PathVariable Long itemId,@RequestParam(value="activityId",required = false)Long activityId) {
        try {
            Response<CollectedBar> res = collectedItemService.collected(itemId, UserUtil.getUserId(),activityId);
            checkState(res.isSuccess(), res.getError());
            return res.getResult();

        } catch (Exception e) {
            log.error("fail to check if item has been collected with itemId:{} user(id:{}), cause:{}",
                    itemId, UserUtil.getUserId(), Throwables.getStackTraceAsString(e));
            return new CollectedBar();
        }
    }

    /**
     * 用户添加商品收藏
     * @param itemId    商品id
     */
    @RequestMapping(value = "/{itemId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CollectedSummary create(@PathVariable Long itemId,@RequestParam(value="activityId",required = false) Long activityId) {

        try {
            checkArgument(notNull(itemId), "item.id.can.not.be.empty");
            Response<CollectedSummary> res = collectedItemService.create(UserUtil.getUserId(), itemId,activityId);
            checkState(res.isSuccess(), res.getError());
            return res.getResult();

        } catch (IllegalArgumentException e) {
            log.error("fail to add collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), itemId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to add collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), itemId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add collected items to the cart with userId:{}, itemId:{}, cause:{}",
                    UserUtil.getUserId(), itemId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.create.fail"));
        }
    }

    /**
     * 用户取消商品收藏
     * @param itemId    商品id
     */
    @RequestMapping(value = "/{itemId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long itemId,@RequestParam(value="activityId",required = false)Long activityId) {

        try {
            checkArgument(notNull(itemId), "item.id.can.not.be.empty");
            Response<Boolean> res = collectedItemService.delete(UserUtil.getUserId(), itemId,activityId);
            checkState(res.isSuccess(), res.getError());
            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), itemId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), itemId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete collected items to the cart with userId:{}, itemId:{}, cause:{}",
                    UserUtil.getUserId(), itemId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.delete.fail"));
        }
    }

    /**
     * 用户取消商品收藏
     * @param id    id
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long id) {

        try {
            checkArgument(notNull(id), "item.id.can.not.be.empty");
            Response<Boolean> res = collectedItemService.delete(UserUtil.getUserId(), id);
            checkState(res.isSuccess(), res.getError());
            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), id, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemId:{}, error:{}",
                    UserUtil.getUserId(), id, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete collected items to the cart with userId:{}, itemId:{}, cause:{}",
                    UserUtil.getUserId(), id, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.delete.fail"));
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bulkDelete(@RequestParam("itemIds") String itemIds) {
        try {
            checkArgument(notEmpty(itemIds), "item.id.can.not.be.empty");
            List<String> ids = splitter.splitToList(itemIds);
            checkArgument(notEmpty(ids), "ids.can.not.be.empty");

            Response<Boolean> res = collectedItemService.bulkDelete(UserUtil.getUserId(), convertToLong(ids));
            checkState(res.isSuccess(), res.getError());

            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemIds:{}, error:{}",
                    UserUtil.getUserId(), itemIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items to the cart with userId:{} itemIds:{}, error:{}",
                    UserUtil.getUserId(), itemIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete collected items to the cart with userId:{}, itemIds:{}, cause:{}",
                    UserUtil.getUserId(), itemIds, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.delete.fail"));
        }
    }

    private List<Long> convertToLong(List<String> entries) {
        List<Long> res = Lists.newArrayListWithCapacity(entries.size());
        for (String entry : entries) {
            res.add(Long.parseLong(entry));
        }

        return res;
    }


    /**
     * 批量添加商品至购物车，取商品第一个sku库存
     * @param itemIds   商品id列表
     */
    @RequestMapping(value = "/add-cart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bulkAddToCarts(@RequestParam("itemIds") String itemIds) {
        try {
            checkArgument(notEmpty(itemIds), "item.ids.can.not.be.empty");
            List<String> ids = splitter.splitToList(itemIds);
            checkArgument(notNull(ids),"ids.can.not.be.empty");
            checkArgument(!isEmpty(ids), "ids.can.not.be.empty");
            checkArgument(ids.size() <= 10, "item.ids.limit.overflow");

            for (String id : ids) {
                try {
                    Long itemId = Long.parseLong(id);
                    addSingleItemToCart(itemId);
                } catch (IllegalStateException e) {
                    log.error("fail to add collected items to the cart with itemId:{}, error:{}", id, e.getMessage());
                } catch (Exception e) {
                    log.error("fail to add collected items to the cart with itemId:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
                }
            }

            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to add collected items to the cart with itemIds:{}, error:{}", itemIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to add collected items to the cart with itemIds:{}, error:{}", itemIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add collected items to the cart with itemIds:{}, cause:{}", itemIds, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.add.cart.fail"));
        }
    }



    /**
     * 添加商品至购物车，取商品第一个sku库存
     * @param itemId   商品id
     */
    @RequestMapping(value = "/{itemId}/add-cart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String addToCart(@PathVariable Long itemId) {
        try {
            addSingleItemToCart(itemId);
            return "ok";

        } catch (IllegalStateException e) {
            log.error("fail to add collected items to the cart with itemId:{}, error:{}", itemId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add collected items to the cart with itemId:{}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.item.add.cart.fail"));
        }
    }

    /**
     * 添加单个商品至购物车
     * @param itemId    商品id
     */
    private void addSingleItemToCart(Long itemId) {
        Response<Item> itemRes = itemService.findById(itemId);
        Item item = itemRes.getResult();
        checkState(equalWith(item.getStatus(), Item.Status.ON_SHELF.toNumber()), "item.status.not.on.shelf");


        Response<List<Sku>> skuRes = itemService.findSkusByItemId(itemId);
        checkState(skuRes.isSuccess(), skuRes.getError());
        List<Sku> skus = skuRes.getResult();
        Sku sku = getFirstAvailableSku(skus);
        checkState(notNull(sku), "collected.item.sku.empty");

        Response<Integer> addRes = cartService.changePermanentCart(UserUtil.getUserId(), sku.getId(), 1);
        checkState(addRes.isSuccess(), addRes.getError());
    }

    /**
     * 获取商品可用的库存列表中的第一个sku
     * @param skus  sku列表
     */
    private Sku getFirstAvailableSku(List<Sku> skus) {
        checkState(notNull(skus) && notEmpty(skus), "collected.item.has.no.sku");

        Sku result = null;
        for (Sku sku : skus) {
            if (sku.getStock() > 0) {
                result = sku;
                break;
            }
        }

        return result;
    }
}
