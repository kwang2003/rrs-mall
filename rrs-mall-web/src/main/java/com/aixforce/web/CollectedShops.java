package com.aixforce.web;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.service.CollectedShopService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemSearchService;
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

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 店铺收藏
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-15 5:37 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping(value = "/api/collect-shop")
public class CollectedShops {

    @Autowired
    private CollectedShopService collectedShopService;

    @Autowired
    private ItemSearchService itemSearchService;

    @Autowired
    private MessageSources messageSources;

    private Splitter splitter = Splitter.on(",");



    /**
     * 用户添加店铺收藏
     * @param shopId    店铺id
     */
    @RequestMapping(value = "/{shopId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CollectedSummary create(@PathVariable Long shopId) {

        try {
            checkArgument(notNull(shopId), "shop.id.can.not.be.empty");
            Response<CollectedSummary> res = collectedShopService.create(UserUtil.getUserId(), shopId);
            checkState(res.isSuccess(), res.getError());
            return res.getResult();

        } catch (IllegalArgumentException e) {
            log.error("fail to add collected shop to the cart with userId:{} shopId:{}, error:{}",
                    UserUtil.getUserId(), shopId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to add collected shop to the cart with userId:{} shopId:{}, error:{}",
                    UserUtil.getUserId(), shopId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to add collected shop to the cart with userId:{}, shopId:{}, cause:{}",
                    UserUtil.getUserId(), shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.shop.create.fail"));
        }
    }


    /**
     * 用户取消店铺收藏
     * @param shopId    商品id
     */
    @RequestMapping(value = "/{shopId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long shopId) {

        try {
            checkArgument(notNull(shopId), "shop.id.can.not.be.empty");
            Response<Boolean> res = collectedShopService.delete(UserUtil.getUserId(), shopId);
            checkState(res.isSuccess(), res.getError());
            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected shop to the cart with userId:{} shopId:{}, error:{}",
                    UserUtil.getUserId(), shopId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete collected shop to the cart with userId:{} shopId:{}, error:{}",
                    UserUtil.getUserId(), shopId, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete collected shop to the cart with userId:{}, shopId:{}, cause:{}",
                    UserUtil.getUserId(), shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.shop.delete.fail"));
        }
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bulkDelete(@RequestParam("shopIds") String shopIds) {
        try {
            checkArgument(notEmpty(shopIds), "shop.id.can.not.be.empty");
            List<String> ids = splitter.splitToList(shopIds);
            checkArgument(notEmpty(ids), "ids.can.not.be.empty");

            Response<Boolean> res = collectedShopService.bulkDelete(UserUtil.getUserId(), convertToLong(ids));
            checkState(res.isSuccess(), res.getError());

            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected shops to the cart with userId:{} shopIds:{}, error:{}",
                    UserUtil.getUserId(), shopIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("fail to delete collected shops to the cart with userId:{} shopIds:{}, error:{}",
                    UserUtil.getUserId(), shopIds, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to delete collected shops to the cart with userId:{}, shopIds:{}, cause:{}",
                    UserUtil.getUserId(), shopIds, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("collected.shop.delete.fail"));
        }
    }

    private List<Long> convertToLong(List<String> entries) {
        List<Long> res = Lists.newArrayListWithCapacity(entries.size());
        for (String entry : entries) {
            res.add(Long.parseLong(entry));
        }

        return res;
    }


    @RequestMapping(value = "/{shopId}/hot-items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Item> searchItems(@PathVariable String shopId,@ParamInfo("sellerId") Long sellerId,
                                 @ParamInfo("dataSource") String dataSource,
                                 @ParamInfo("size") Integer size,
                                 @ParamInfo("order") String order,
                                 @ParamInfo("rid")Integer region) {
        try {
            checkArgument(size > 0 && size < 5, "item.search.size.overflow");
            Response<List<Item>> res = itemSearchService.recommendItemInShop(sellerId, dataSource, null, size, order, region);
            checkState(res.isSuccess(), res.getError());
            return res.getResult();

        } catch (IllegalArgumentException e) {
            log.error("fail to search hot items of shop(id:{}) cause:{}", shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("hot.item.search.fail"));
        } catch (IllegalStateException e) {
            log.error("fail to search hot items of shop(id:{}) cause:{}", shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("hot.item.search.fail"));
        } catch (Exception e) {
            log.error("fail to search hot items of shop(id:{}) cause:{}", shopId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("hot.item.search.fail"));
        }
    }



}
