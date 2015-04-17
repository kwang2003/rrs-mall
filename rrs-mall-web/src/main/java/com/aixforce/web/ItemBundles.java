package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemBundle;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemBundleService;
import com.aixforce.item.service.ItemService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by yangzefeng on 14-4-23
 */
@Controller
@Slf4j
@RequestMapping("/api/seller/itemBundle")
public class ItemBundles {

    @Autowired
    private ItemBundleService itemBundleService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(ItemBundle itemBundle) {
        Long userId = UserUtil.getUserId();
        itemBundle.setSellerId(userId);
        itemBundle.setStatus(ItemBundle.Status.OnShelf.toNumber()); //默认有效
        Response<Long> result = itemBundleService.create(itemBundle);
        if(!result.isSuccess()) {
            log.error("fail to create item bundle{}, error code:{}", itemBundle, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@PathVariable("id")Long id,
                       ItemBundle itemBundle) {
        itemBundle.setId(id);
        Long userId = UserUtil.getUserId();
        Response<Boolean> result = itemBundleService.update(itemBundle, userId);
        if(!result.isSuccess()) {
            log.error("fail to update item bundle{}, error code:{}", itemBundle, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}/updateStatus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateStatus(@PathVariable("id")Long id,
                             @RequestParam("status")ItemBundle.Status status) {
        ItemBundle updated = new ItemBundle();
        updated.setId(id);
        updated.setStatus(status.toNumber());
        Long userId = UserUtil.getUserId();
        Response<Boolean> result = itemBundleService.update(updated, userId);
        if(!result.isSuccess()) {
            log.error("fail to update item bundle={} status={}, error code:{}",
                    id, status, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/items", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Item> findItemsBySellerId() {
        Long userId = UserUtil.getUserId();
        Response<List<Item>> result =  itemService.findBySellerId(userId);
        if(!result.isSuccess()) {
            log.error("fail to find items by sellerId={},error code:{}",userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/item/{itemId}/skus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Sku> findSkusByItemId(@PathVariable("itemId") Long itemId) {
        Response<List<Sku>> result = itemService.findSkusByItemId(itemId);
        if(!result.isSuccess()) {
            log.error("fail to find skus by itemId={},error code:{}",itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/spu/{spuId}/items", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Item> findItemsBySpuId(@PathVariable("spuId") Long spuId) {
        Response<List<Item>> result = itemService.findOnShelfBySpuId(spuId);
        if(!result.isSuccess()) {
            log.error("fail to find items by spuId={}. error code:{}", spuId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
}
