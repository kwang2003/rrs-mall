package com.aixforce.restful.service;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.*;
import com.aixforce.item.service.DefaultItemService;
import com.aixforce.item.service.ItemService;
import com.aixforce.restful.dto.HaierResponse;
import com.aixforce.restful.dto.OuterIdDto;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangzefeng on 14-1-18
 */
@Slf4j
@Service
public class HaierServiceImpl implements HaierService{

    private static final JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private static final JavaType javaType = jsonMapper.createCollectionType(ArrayList.class, BaseSku.class);

    private final Joiner joiner = Joiner.on(",").skipNulls();

    @Autowired
    private ItemService itemService;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private ShopService shopService;

    @Autowired (required = false)
    private GridService gridService;

    @Override
    public HaierResponse<Boolean> autoReleaseOrUpdateItem(List<OuterIdDto> outerIdDtos) {
        HaierResponse<Boolean> result = new HaierResponse<Boolean>();
        if(outerIdDtos == null) {
            log.error("outerIdDtos can not be null when releaseOrUpdateItem");
            result.setResult(Boolean.FALSE);
            return result;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        int handled = 0;
        for(OuterIdDto oid : outerIdDtos) {
            String outShopId = oid.getShopId();
            //一个outCode可能对应多个shops
            Response<List<Shop>> shopR = shopService.findByOuterCode(outShopId);
            if(!shopR.isSuccess()) {
                log.error("failed to find shop by outShopId{}, error error:{}", outShopId, shopR.getError());
                continue;
            }
            List<Shop> shops = shopR.getResult();
            for(Shop shop : shops) {
                long shopId = shop.getId();
                Long sellerId = shop.getUserId();
                for (OuterIdDto.SkuAndQuantity skuAndQuantity : oid.getSkus()) {
                    Long spuId = null;
                    try {
                        handled++;
                        String outerId = skuAndQuantity.getSkuId();
                        Integer stock = Integer.valueOf(skuAndQuantity.getStock());
                        stock = (stock != null) ? stock : 0;
                        //根据outer查询模版商品
                        Response<DefaultItem> defaultItemR = defaultItemService.findByOuterId(outerId);
                        if (!defaultItemR.isSuccess() || defaultItemR.getResult() == null) {
                            log.error("failed to find defaultItem with outerId={},skip it, error error:{}", outerId, defaultItemR.getError());
                            continue;
                        }
                        DefaultItem defaultItem = defaultItemR.getResult();

                        spuId = defaultItem.getSpuId();

                        //根据店铺id，spuId查找商品，这些商品为代运营商家发的商品
                        Response<Item> itemR = itemService.findBySellerIdAndSpuId(sellerId, spuId);
                        if (!itemR.isSuccess()) {
                            log.error("failed to find item by shopId{} and spuId{}, error error:{}",
                                    shopId, spuId, itemR.getError());
                            continue;
                        }
                        Item item = itemR.getResult();
                        //new sku,首先从defaultItem找到对应outerId的baseSku
                        List<BaseSku> baseSkus = jsonMapper.fromJson(defaultItem.getJsonSkus(), javaType);
                        BaseSku baseSku = null;
                        for (BaseSku bs : baseSkus) {
                            if (Objects.equal(bs.getOuterId(), outerId)) {
                                baseSku = bs;
                                break;
                            }
                        }
                        if (baseSku == null) {
                            log.error("default item don't have baseSku by outId={}, skip it", outerId);
                            continue;
                        }
                        //如果商品不存在，创建商品，价格统一在service层设置
                        if (item == null) {
                            Item newItem = new Item();
                            newItem.setSpuId(spuId);
                            newItem.setShopId(shopId);
                            newItem.setShopName(shop.getName());
                            newItem.setUserId(shop.getUserId());
                            newItem.setTradeType(Item.TradeType.BUY_OUT.toNumber());
                            newItem.setMainImage(defaultItem.getMainImage());
                            newItem.setName(defaultItem.getName());
                            newItem.setQuantity(stock);
                            newItem.setSoldQuantity(0);
                            //如果商品价格为null，商品为下架状态
                            if(newItem.getPrice() == null) {
                                newItem.setStatus(Item.Status.OFF_SHELF.toNumber());
                            }else {
                                if (stock > 0) {
                                    newItem.setStatus(Item.Status.ON_SHELF.toNumber());
                                } else {
                                    newItem.setStatus(Item.Status.OFF_SHELF.toNumber());
                                }
                            }
                            //shop authorize
                            Response<List<Long>> shopAuthR = gridService.authorize(newItem, shopId);
                            if (!shopAuthR.isSuccess()) {
                                log.warn("shop id={} authorize fail, outerCode={} outerId={} " +
                                                "seller don't have the right to create item spiId={}, skip it",
                                        shopId, outShopId, outerId, spuId);
                                continue;
                            }
                            List<Long> resultIds = shopAuthR.getResult();
                            long brandId = resultIds.get(0);
                            List<Long> regionIds = resultIds.subList(1, resultIds.size());
                            newItem.setBrandId(brandId);
                            newItem.setRegion(joiner.join(regionIds));
                            //new item detail
                            ItemDetail itemDetail = new ItemDetail();
                            BeanMapper.copy(defaultItem, itemDetail);
                            //new sku
                            Sku sku = new Sku();
                            BeanMapper.copy(baseSku, sku);
                            sku.setStock(stock);
                            Response<Long> itemCreateR = itemService.create(newItem, itemDetail, Lists.newArrayList(sku), Boolean.TRUE);
                            if (!itemCreateR.isSuccess()) {
                                log.error("fail to create item, error error:{}", itemCreateR.getError());
                            }
                        } else { //商品已存在,更新商品信息,可能需要new sku，或者update sku
                            //首先根据itemId,attributeValue1,attributeValue2查找sku
                            //如果商品是冻结或者删除状态，不应该同步库存自动上下架，跳过
                            if(Objects.equal(item.getStatus(), Item.Status.FROZEN.toNumber())
                                    || Objects.equal(item.getStatus(), Item.Status.DELETED.toNumber())) {
                                continue;
                            }
                            long itemId = item.getId();
                            Integer originQuantity = item.getQuantity();
                            item.setMainImage(defaultItem.getMainImage());
                            item.setName(defaultItem.getName());
                            item.setStatus(Item.Status.OFF_SHELF.toNumber());
                            ItemDetail itemDetail = new ItemDetail();
                            BeanMapper.copy(defaultItem, itemDetail);
                            //如果sku已存在，更新商品，detail，sku
                            Response<Sku> skuR = itemService.findSkuByAttributeValuesAndItemId(itemId, baseSku.getAttributeValue1(), baseSku.getAttributeValue2());
                            if (!skuR.isSuccess()) {
                                log.error("find sku by itemId{},attributeValue1{},attributeValue2{} fail, error code:{}, skip it",
                                        itemId, baseSku.getAttributeValue1(), baseSku.getAttributeValue2(), skuR.getError());
                                continue;
                            }
                            Sku sku = skuR.getResult();
                            if (sku != null) {
                                //商品数量和sku不存在的情况处理逻辑不同
                                item.setQuantity(originQuantity - sku.getStock() + stock);
                                if (item.getQuantity() > 0) {
                                    item.setStatus(Item.Status.ON_SHELF.toNumber());
                                }
                                sku.setStock(stock);

                                //修改的sku替换存在的sku，组成list调用update接口
                                Response<List<Sku>> skusR = itemService.findSkusByItemId(item.getId());
                                if(!skusR.isSuccess()) {
                                    log.error("fail to find skus by itemId={}, error code",
                                            item.getId(), skusR.getError());
                                    continue;
                                }
                                List<Sku> existSkus = skusR.getResult();
                                List<Sku> updateds = Lists.newArrayListWithCapacity(existSkus.size());
                                for(Sku s : existSkus) {
                                    if(Objects.equal(s.getId(), sku.getId())) {
                                        sku.setPrice(s.getPrice()); //价格不用同步
                                        updateds.add(sku);
                                    }else {
                                        updateds.add(s);
                                    }
                                }
                                Response<Boolean> updateR = itemService.update(item, itemDetail, updateds);
                                if (!updateR.isSuccess()) {
                                    log.error("failed to update item when auto release item, error error:{}", updateR.getError());
                                }
                            } else {//如果sku不存在，更新商品，detail，创建sku
                                Sku newSku = new Sku();
                                BeanMapper.copy(baseSku, newSku);
                                newSku.setStock(stock);
                                newSku.setItemId(itemId);
                                item.setQuantity(originQuantity + stock);
                                if (item.getQuantity() > 0) {
                                    item.setStatus(Item.Status.ON_SHELF.toNumber());
                                }
                                Response<Boolean> updateR = itemService.updateItemAndItemDetailAndCreateSku(item, itemDetail, newSku);
                                if (!updateR.isSuccess()) {
                                    log.error("failed to update item and create sku when auto release item, error error:{}",
                                            updateR.getError());
                                }
                            }
                        }
                    }catch (Exception e) {
                        log.error("fail to auto release item by skuAndQuantity {}, spuId={}, skip it, cause:{}",
                                skuAndQuantity, spuId, Throwables.getStackTraceAsString(e));
                    }
                }
            }
        }
        stopwatch.stop();
        log.info("[AUTO_RELEASE_OR_UPDATE_ITEM] auto release or update item finished, cost{} millis, handled {} skus",
                stopwatch.elapsed(TimeUnit.MILLISECONDS), handled);
        result.setResult(Boolean.TRUE);
        return result;
    }
}
