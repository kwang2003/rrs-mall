/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api;

import com.aixforce.category.model.RichAttribute;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.*;
import com.aixforce.item.service.DefaultItemService;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ItemTagService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dto.FreightModelDto;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.trade.service.FreightModelService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.html.HtmlEscapers;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-13
 */
@Controller
@RequestMapping("/api")
public class Items {
    private final static Logger log = LoggerFactory.getLogger(Items.class);
    private final Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();

    private final Splitter whiteSpaceSplitter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).trimResults().omitEmptyStrings();
    private final Joiner joiner = Joiner.on(",").skipNulls();

    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ItemTagService itemTagService;

    @Autowired
    private GridService gridService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private FreightModelService freightModelService;

    @Autowired
    private PreDepositService preDepositService;


    @RequestMapping(value = "/items/{itemId}/attributes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichAttribute> attributesOf(@PathVariable("itemId") Long itemId) {
        Response<List<RichAttribute>> result = itemService.attributesOf(itemId);
        if (result.isSuccess()) {
            return result.getResult();

        } else {
            log.error("failed to load attributes for item id = {},error code:{}",
                    itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/items/{itemId}/packingList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getPackingListById(@PathVariable("itemId") Long itemId) {
        Response<ItemDetail> result = itemService.findDetailBy(itemId);
        if (result.isSuccess()) {
            return result.getResult().getPackingList();
        } else {
            log.error("failed to find packingList by itemId={}, error code:{}", itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/seller/items", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String create(@RequestBody ItemDto itemDto) {
        BaseUser user = UserUtil.getCurrentUser();
        if (!Objects.equal(user.getType(), BaseUser.TYPE.SELLER.toNumber()) && !Objects.equal(user.getType(), BaseUser.TYPE.WHOLESALER.toNumber())) {
            log.error("only seller can create items , but current user  is {}", user);
            throw new JsonResponseException(500, "请不要同时登录多个账号");
        }

        Long userId = UserUtil.getUserId();

        Item item = itemDto.getItem();

        //验证运费模板是否合法
        if(item.getFreightModelId() != null && !Objects.equal(item.getFreightModelId(), -1L)) {
            Response<FreightModelDto> freightModelR = freightModelService.findById(item.getFreightModelId());
            if(!freightModelR.isSuccess()) {
                log.error("fail to find freightModel by id={}, error code:{}", item.getFreightModelId(),
                        freightModelR.getError());
                throw new JsonResponseException(500, messageSources.get(freightModelR.getError()));
            }
            FreightModelDto freightModelDto = freightModelR.getResult();
            //如果计费方式是按体积，但是商品体积字段为空，或者计费方式是按体重，但是商品重量字段为空，报错
            if((Objects.equal(freightModelDto.getCountWay(),FreightModel.CountWay.SIZE.toNumber()) && itemDto.getItemDetail().getFreightSize() == null)
                    || (Objects.equal(freightModelDto.getCountWay(), FreightModel.CountWay.WEIGHT.toNumber()) && itemDto.getItemDetail().getFreightWeight() == null)) {
                log.error("freight count way is {}, item freight size or weight is null", FreightModel.CountWay.from(freightModelDto.getCountWay()));
                throw new JsonResponseException(500, messageSources.get("item.freight.count.way.illegal"));
            }
        }

        if (item.getName().length() > 200) {
            throw new JsonResponseException(500, messageSources.get("item.name.too.long"));
        }
        item.setUserId(userId);
        item.setTradeType(Item.TradeType.BUY_OUT.toNumber());

        Response<Shop> result = shopService.findByUserId(userId);
        if (!result.isSuccess()) {
            log.error("fail to find shop by userId={}, error code:{}", userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Shop shop = result.getResult();
        checkShopStatus(shop.getId(), shop.getStatus());

        item.setShopId(shop.getId());
        item.setShopName(shop.getName());
        //shop authorize
        Response<List<Long>> shopAuthR = gridService.authorize(item, shop.getId());
        if (!shopAuthR.isSuccess()) {
            log.error("shop authorize fail, seller id={} don't have the right", userId);
            throw new JsonResponseException(401, "您没有被授权");
        }
        List<Long> resultIds = shopAuthR.getResult();
        long brandId = resultIds.get(0);
        List<Long> regionIds = resultIds.subList(1, resultIds.size());
        item.setBrandId(brandId);
        item.setRegion(joiner.join(regionIds));
        //item.setPrice(Objects.firstNonNull(item.getPrice(),0)*100);
        //escape the name
        item.setName(HtmlEscapers.htmlEscaper().escape(item.getName()));
        ItemDetail itemDetail = itemDto.getItemDetail();
        List<Sku> skus = itemDto.getSkus();
        int quantity = 0;

        for (Sku sku : skus) {
            //sku.setPrice(Objects.firstNonNull(sku.getPrice(),0)*100);
            quantity += sku.getStock();
        }

        item.setQuantity(quantity);
        item.setStatus(Item.Status.INIT.toNumber());
        item.setSoldQuantity(0);
        //
        Response<Long> itemIdR = itemService.create(item, itemDetail, skus, Boolean.TRUE);
        if (itemIdR.isSuccess()) {
            //默认打上未分类标签
            Response<Boolean> isSuccess = itemTagService.addUnclassifiedItem(item.getUserId(), itemIdR.getResult());
            if (isSuccess.isSuccess()) {
                return "ok";
            } else {
                log.error("failed to add UnclassifiedItem, itemId={}", item.getId());
                throw new JsonResponseException(500, messageSources.get(isSuccess.getError()));
            }
        } else {
            log.error("failed to create item from {},user(id={}), error code:{}",
                    itemDto, userId, itemIdR.getError());
            throw new JsonResponseException(500, messageSources.get(itemIdR.getError()));
        }
    }

    @RequestMapping(value = "/seller/items/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@PathVariable Long id, @RequestBody ItemDto itemDto) {
        Long userId = UserUtil.getUserId();

        Item item = itemDto.getItem();
        if (item.getName().length() > 200) {
            throw new JsonResponseException(500, messageSources.get("item.name.too.long"));
        }

        //验证运费模板是否合法
        if(item.getFreightModelId() != null && !Objects.equal(item.getFreightModelId(), -1L)) {
            Response<FreightModelDto> freightModelR = freightModelService.findById(item.getFreightModelId());
            if(!freightModelR.isSuccess()) {
                log.error("fail to find freightModel by id={}, error code:{}", item.getFreightModelId(),
                        freightModelR.getError());
                throw new JsonResponseException(500, messageSources.get(freightModelR.getError()));
            }
            FreightModelDto freightModelDto = freightModelR.getResult();
            //如果计费方式是按体积，但是商品体积字段为空，或者计费方式是按体重，但是商品重量字段为空，报错
            if((Objects.equal(freightModelDto.getCountWay(),FreightModel.CountWay.SIZE.toNumber()) && itemDto.getItemDetail().getFreightSize() == null)
                    || (Objects.equal(freightModelDto.getCountWay(), FreightModel.CountWay.WEIGHT.toNumber()) && itemDto.getItemDetail().getFreightWeight() == null)) {
                log.error("freight count way is {}, item freight size or weight is null", FreightModel.CountWay.from(freightModelDto.getCountWay()));
                throw new JsonResponseException(500, messageSources.get("item.freight.count.way.illegal"));
            }
        }

        Response<Shop> shopR = shopService.findByUserId(userId);
        if (!shopR.isSuccess()) {
            log.error("failed to find shop by userId={}, cause:{}", userId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Long shopId = shopR.getResult().getId();

        checkShopStatus(shopId, shopR.getResult().getStatus());

        Response<Item> result = itemService.findById(id);
        if (!result.isSuccess()) {
            log.error("failed to find item id = {},error code:{}",
                    id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Item existed = result.getResult();

        if (existed == null) {
            throw new JsonResponseException(500, messageSources.get("item.not.exist"));
        }
        if (!Objects.equal(existed.getUserId(), userId)) {
            throw new JsonResponseException(500, messageSources.get("item.not.owner"));
        }


        item.setUserId(null);
        item.setShopId(null);
        item.setStatus(null);

        item.setSoldQuantity(null);
        item.setName(HtmlEscapers.htmlEscaper().escape(item.getName()));
        item.setId(id);
        ItemDetail itemDetail = itemDto.getItemDetail();
        itemDetail.setItemId(item.getId());
        List<Sku> skus = itemDto.getSkus();
        int quantity = 0;
        for (Sku sku : skus) {
            quantity += sku.getStock();
        }
        item.setQuantity(quantity);
        if (quantity == 0 && Objects.equal(existed.getStatus(), Item.Status.ON_SHELF.toNumber())) { //对于已上架且库存为0的商品, 设置商品的状态为下架
            item.setStatus(Item.Status.OFF_SHELF.toNumber());
        }
        //商家价格在service修改
        Response<Boolean> iResult = itemService.update(item, itemDetail, skus);
        if (iResult.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to update item(id={}) from {},error code :{}",
                    id, itemDto, iResult.getError());
            throw new JsonResponseException(500, messageSources.get(iResult.getError()));
        }

    }

    @RequestMapping(value = "/seller/items/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long id) {
        Long userId = UserUtil.getUserId();

        Response<Shop> shopR = shopService.findByUserId(userId);
        if (!shopR.isSuccess()) {
            log.error("failed to find shop by userId={}, cause:{}", userId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Long shopId = shopR.getResult().getId();

        checkShopStatus(shopId, shopR.getResult().getStatus());

        Response<Boolean> result = itemService.delete(userId, id);
        if (result.isSuccess()) {
            Response<Boolean> removeTag = itemTagService.removeTagsOfItems(userId, Lists.newArrayList(id));
            if (!removeTag.isSuccess()) {
                log.error("failed to remove tag of item itemId={}, cause:{}", id, removeTag.getError());
                throw new JsonResponseException(500, messageSources.get(removeTag.getError()));
            }
            return messageSources.get("item.delete.success");
        } else {
            log.error("failed to delete item whose id={},error code:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }


    @RequestMapping(value = "/seller/items/bulkDelete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bulkDelete(@RequestParam(value = "ids") String ids) {
        Long userId = UserUtil.getUserId();
        List<String> parts = splitter.splitToList(ids);
        if (parts.isEmpty()) {
            log.warn("no item need to delete, return directly");
            return messageSources.get("item.ids.empty");
        }
        List<Long> parseIds = Lists.newArrayListWithCapacity(parts.size());
        for (String id : parts) {
            parseIds.add(Long.parseLong(id));
        }
        Response<Boolean> itemR = itemService.bulkDelete(userId, parseIds);
        if (!itemR.isSuccess()) {
            log.error("failed to bulk delete items(ids={}),error code:{}", ids, itemR.getError());
            throw new JsonResponseException(500, messageSources.get(itemR.getError()));
        }
        //remove all tags
        Response<Boolean> tagR = itemTagService.removeTagsOfItems(userId, parseIds);
        if (!tagR.isSuccess()) {
            log.error("failed to bulk delete items(ids={}) tags, error code:{}", parseIds, tagR.getError());
            throw new JsonResponseException(500, messageSources.get(tagR.getError()));
        }
        Response<Shop> shopR = shopService.findByUserId(userId);
        if (!shopR.isSuccess()) {
            log.error("failed to find shop by userId={}, cause:{}", userId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Long shopId = shopR.getResult().getId();

        checkShopStatus(shopId, shopR.getResult().getStatus());
        return "ok";
    }

    @RequestMapping(value = "/seller/items", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bulkUpdateStatus(@RequestParam("status") Integer status, @RequestParam("ids") String ids) {
        Long userId = UserUtil.getMasterId();
        if (UserUtil.getCurrentUser().getType() != BaseUser.TYPE.ADMIN.toNumber()) {//如果不是管理员账号, 得检查店铺状态
            Response<Shop> shopR = shopService.findByUserId(userId);
            if (!shopR.isSuccess()) {
                log.error("can not find shop with userId={}", userId);
                throw new JsonResponseException(500, "用户店铺未找到");
            }

            Shop shop = shopR.getResult();

            checkShopStatus(shop.getId(), shop.getStatus());
        }

        List<String> parts = splitter.splitToList(ids);
        if (Iterables.isEmpty(parts)) {
            log.warn("no items need to update,return directly");
            return messageSources.get("item.ids.empty");
        }
        List<Long> parsedIds = Lists.newArrayListWithCapacity(parts.size());
        for (String id : parts) {
            parsedIds.add(Long.parseLong(id));
        }
        Response<Boolean> result = itemService.bulkUpdateStatus(userId, status, parsedIds);
        if (!result.isSuccess()) {
            log.error("fail to bulk update items(ids={}) status to {},error code:{} ", ids, status, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return messageSources.get("item.update.success");

    }


    @RequestMapping(value = "/tag", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<ItemWithTags> findItemsOfTag(@RequestParam(value = "tag", required = false) String tag,
                                               @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam Map<String, String> params) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Paging<ItemWithTags>> result = itemService.findItemsOfTag(user, tag, pageNo, 20, params);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("find items of tag failed,tag:{} cause:{}", tag, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/couponTag", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<ItemWithTags> findItemsOfTagCoupon(@RequestParam(value = "tag", required = false) String tag,
                                               @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam Map<String, String> params) {
        BaseUser user = UserUtil.getCurrentUser();
        Response<Paging<ItemWithTags>> result = itemService.findItemsOfTagCoupons(user, tag, pageNo, 20, params);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("find items of tag failed,tag:{} cause:{}", tag, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/items/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Item> findByIds(@RequestParam(value = "ids") String idStr) {


        List<String> ids = whiteSpaceSplitter.splitToList(idStr);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> realIds = convertToLong(ids);
        Response<List<Item>> itemsR = itemService.findByIds(realIds);
        if (!itemsR.isSuccess()) {
            log.error("failed to find items by ids :{}, error code:{}", idStr, itemsR.getError());
        }

        //返回结果需要和传入的参数一致, 如果商品没有找到, 则返回一个空的item对象
        List<Item> items = itemsR.getResult();
        List<Item> result = Lists.newArrayListWithCapacity(items.size());

        for (Long realId : realIds) {
            boolean found = false;
            for (Item item : items) {
                if(Objects.equal(item.getId(), realId)){
                    result.add(item);
                    found=true;
                    break;
                }
            }
            if(!found){
                log.error("failed to find item where id={}", realId);
                result.add(new Item());
            }
        }
        return result;
    }

    @RequestMapping(value = "/items/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Item findById(@PathVariable("id") Long id) {
        Response<Item> result = itemService.findById(id);
        if (!result.isSuccess()) {
            log.error("fail to find item by id={}, error code:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Item item = result.getResult();
        Long userId = UserUtil.getUserId();
        if (!Objects.equal(userId, item.getUserId())) {
            log.error("authorize fail, currentUser={}, itemUser={}", userId, item.getUserId());
            throw new JsonResponseException(401, messageSources.get("authorize.fail"));
        }
        return item;
    }

    /**
     * 预售商品判断是否有符合授权的商家
     */
    @RequestMapping(value = "/items/{itemId}/authorize", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean authorize(@PathVariable("itemId") Long itemId,
                             @RequestParam(value="count", defaultValue = "1") Integer count,
                             HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionR.isSuccess()) {
            log.warn("region can not be null in cookies");
            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
        }
        Integer regionId = regionR.getResult();
        Response<List<Long>> result = preSaleService.findShopForPreOrder(itemId, regionId, count);
        if (!result.isSuccess()) {
            log.error("fail to find authorize shop for item id={},error code:{}", itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return true;
    }

    /**
     * 发商品前网格判断
     */
    @RequestMapping(value = "/items/release/authorize", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean itemReleaseAuthorize(@RequestParam("spuId") Long spuId) {
        Long userId = UserUtil.getUserId();
        Response<Shop> shopR = shopService.findByUserId(userId);
        if(!shopR.isSuccess() || shopR.getResult() == null) {
            log.error("fail to find shop by userId={}, error code={}", userId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Long shopId = shopR.getResult().getId();

        Response<List<Long>> authorizeR = gridService.authorizeBySpuIdAndShopId(spuId, shopId);
        if(!authorizeR.isSuccess()) {
            log.error("fail to authorize by spuId={}, shopId={}, error code={}", spuId, shopId, authorizeR.getError());
            throw new JsonResponseException(500, messageSources.get(authorizeR.getError()));
        }
        return Boolean.TRUE;
    }

    private List<Long> convertToLong(List<String> ids) {
        List<Long> result = Lists.newArrayListWithCapacity(ids.size());
        for (String id : ids) {
            result.add(Long.parseLong(id));
        }
        return result;
    }

    private void checkShopStatus(Long shopId, Integer status) throws JsonResponseException {
        if (!Objects.equal(status, Shop.Status.OK.value())) {
            log.error("shop(id={}) status(={}) is abnormal ", shopId, status);
            throw new JsonResponseException(500, messageSources.get("shop.status.abnormal"));
        }
    }


    @ToString
    public static class ItemDto {
        @Getter
        @Setter
        private Item item;

        @Getter
        @Setter
        private List<Sku> skus;

        @Getter
        @Setter
        private ItemDetail itemDetail;
    }

    /**
     * 预售商品判断是否有符合授权的商家
     */
    @RequestMapping(value = "/items/{itemId}/authorizeDeposit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean authorizeDeposit(@PathVariable("itemId") Long itemId,
                             @RequestParam(value="count", defaultValue = "1") Integer count,
                             HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for (Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
        if (!regionR.isSuccess()) {
            log.warn("region can not be null in cookies");
            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
        }
        Integer regionId = regionR.getResult();
        Response<List<Long>> result = preDepositService.findShopForPreOrder(itemId, regionId, count);
        if (!result.isSuccess()) {
            log.error("fail to find authorize shop for item id={},error code:{}", itemId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return true;
    }
}
