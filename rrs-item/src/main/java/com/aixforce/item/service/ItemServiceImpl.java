/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.model.RichAttribute;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.AttributeService;
import com.aixforce.category.service.BackCategoryHierarchy;
import com.aixforce.category.service.Forest;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.dao.mysql.ItemDao;
import com.aixforce.item.dao.mysql.ItemServiceDao;
import com.aixforce.item.dao.mysql.SkuDao;
import com.aixforce.item.dao.redis.ItemCountDao;
import com.aixforce.item.dao.redis.ItemsTagsDao;
import com.aixforce.item.dto.FullDefaultItem;
import com.aixforce.item.dto.FullItem;
import com.aixforce.item.dto.RichSpu;
import com.aixforce.item.dto.SkuGroup;
import com.aixforce.item.event.DspEvent;
import com.aixforce.item.event.ItemCountEvent;
import com.aixforce.item.event.ItemEventBus;
import com.aixforce.item.manager.ItemManager;
import com.aixforce.item.manager.ItemRealTimeIndexer;
import com.aixforce.item.model.*;
import com.aixforce.search.Pair;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.*;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkState;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-03
 */
@Service
public class ItemServiceImpl implements ItemService {

    private final static Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final static Joiner joiner = Joiner.on(", ").skipNulls();

    private final static List<Sku> EMPTY_SKU_LIST = Collections.emptyList();

    @Autowired
    private ItemsTagsDao itemsTagsDao;

    @Autowired
    private Validator validator;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemRealTimeIndexer realTimeIndexer;

    @Autowired
    private Forest forest;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private SpuService spuService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private BackCategoryHierarchy bch;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ItemCountDao itemCountDao;

    @Autowired
    private ItemEventBus eventBus;

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;


    @Autowired
    private ItemServiceDao itemServiceDao;

    /**
     * 根据id列表批量查找商品
     *
     * @param ids id列表
     * @return 商品列表 ,返回结果按照id逆序
     */
    @Override
    public Response<List<Item>> findByIds(List<Long> ids) {
        Response<List<Item>> result = new Response<List<Item>>();
        if (ids == null||ids.isEmpty()) {
            result.setResult(Collections.<Item>emptyList());
            return result;
        }
        try {
            List<Item> items = itemManager.findByIds(ids);
            result.setResult(items);
            return result;
        } catch (Exception e) {
            log.error("failed to find items for ids {},cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    /**
     * 卖家后台商品列表,用于管理店铺内商品
     *
     * @param baseUser 系统注入的用户
     * @param pageNo   起始页码
     * @param size     返回条数
     * @param params   搜索参数
     * @return 商品列表
     */
    @Override
    public Response<Paging<Item>> sellerItems(BaseUser baseUser, Integer pageNo, Integer size, Map<String, String> params) {
        Response<Paging<Item>> result = new Response<Paging<Item>>();
        if (params == null) {
            params = Collections.emptyMap();
        }
        try {
            Map<String, Object> builder = Maps.newHashMap();
            String keywords = params.get("q");
            if (!Strings.isNullOrEmpty(keywords)) {
                builder.put("name", keywords.trim());
            }

            String priceFrom = params.get("p_f");
            String priceTo = params.get("p_t");
            if (!Strings.isNullOrEmpty(priceFrom)) {
                builder.put("priceFrom", Integer.parseInt(priceFrom) * 100);
            }
            if (!Strings.isNullOrEmpty(priceTo)) {
                builder.put("priceTo", Integer.parseInt(priceTo) * 100);
            }
            String quantityFrom = params.get("q_f");
            String quantityTo = params.get("q_t");
            if (!Strings.isNullOrEmpty(quantityFrom)) {
                builder.put("quantityFrom", Integer.parseInt(quantityFrom));
            }
            if (!Strings.isNullOrEmpty(quantityTo)) {
                builder.put("quantityTo", Integer.parseInt(quantityTo));
            }

            String soldQuantityFrom = params.get("s_f");
            String soldQuantityTo = params.get("s_t");
            if (!Strings.isNullOrEmpty(soldQuantityFrom)) {
                builder.put("soldQuantityFrom", Integer.parseInt(soldQuantityFrom));
            }
            if (!Strings.isNullOrEmpty(soldQuantityTo)) {
                builder.put("soldQuantityTo", Integer.parseInt(soldQuantityTo));
            }
            String status = Objects.firstNonNull(params.get("status"), "0,1,-1,-2");
            if (!Strings.isNullOrEmpty(status)) {//分割状态成为list
                List<String> all = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(status);
                builder.put("status", Lists.transform(all, new Function<String, Integer>() {
                    @Override
                    public Integer apply(String input) {
                        return Integer.parseInt(input);
                    }
                }));
            }
            pageNo = Objects.firstNonNull(pageNo, 1);
            size = Objects.firstNonNull(size, 20);
            size = size > 0 ? size : 20;
            int offset = (pageNo - 1) * size;
            offset = offset > 0 ? offset : 0;
            Paging<Item> items = itemManager.sellerItems(baseUser.getId(), offset, size, builder);
            result.setResult(items);
            return result;
        } catch (Exception e) {
            log.error("failed to query sellerItems,", e);
            result.setError("item.query.fail");
            return result;
        }
    }


    /**
     * 根据tag分页查找归属指定tag的商品信息,商品包含tag信息
     *
     * @param user   卖家
     * @param tag    tag名称
     * @param pageNo 起始页码,从1开始
     * @param size   每页显示条数
     * @return 归属指定tag的商品信息, 商品包含tag信息
     */
    @Override
    public Response<Paging<ItemWithTags>> findItemsOfTag(BaseUser user,
                                                         String tag,
                                                         Integer pageNo,
                                                         Integer size,
                                                         Map<String, String> params) {
        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;
        Long masterId = Objects.firstNonNull(user.getParentId(), user.getId());
        Response<Paging<ItemWithTags>> result = new Response<Paging<ItemWithTags>>();

        try {
            long total = 0;
            List<Item> items;
            if (!Strings.isNullOrEmpty(tag)) {//查找指定tag的商品

                Paging<Long> p = itemsTagsDao.itemsOfTag(masterId, tag.trim(), offset, size);
                if (p.getTotal() > 0) {
                    items = itemManager.findByIds(p.getData());
                    total = p.getTotal();
                } else {
                    items = Collections.emptyList();
                }
            } else { //查找所有的商品
                Response<Paging<Item>> pResult = sellerItems(user, pageNo, size, params);
                if (pResult.isSuccess()) {
                    Paging<Item> pItems = pResult.getResult();
                    items = pItems.getData();
                    total = pItems.getTotal();
                } else {
                    result.setError(pResult.getError());
                    return result;
                }

            }

            if (items.isEmpty()) {
                Paging<ItemWithTags> iwt = new Paging<ItemWithTags>(0L, Collections.<ItemWithTags>emptyList());
                result.setResult(iwt);
                return result;
            }


            ListMultimap<Long, String> tags = itemsTagsDao.tagsOfItems(masterId, Lists.transform(items, new Function<Item, Long>() {
                @Override
                public Long apply(Item input) {
                    return input.getId();
                }
            }));

            List<ItemWithTags> iwts = Lists.newArrayListWithCapacity(items.size());
            for (Item item : items) {
                ItemWithTags iwt = new ItemWithTags();
                iwt.setItemId(item.getId());
                iwt.setImageUrl(item.getMainImage());
                iwt.setItemName(item.getName());
                List<String> t = Lists.newArrayList(tags.get(item.getId())); //f**k dubbo serialization
                iwt.setTags(t);
                iwt.setStatus(item.getStatus());
                iwt.setPrice(item.getPrice());
                iwts.add(iwt);
            }
            Paging<ItemWithTags> piwt = new Paging<ItemWithTags>(total, iwts);
            result.setResult(piwt);
            return result;

        } catch (Exception e) {
            log.error("failed to find items of tag({}) of user (id={}),cause:{}", tag, masterId,
                    Throwables.getStackTraceAsString(e));
            result.setError("item.tags.query.fail");
            return result;
        }
    }

    /**
     * 根据tag分页查找归属指定tag的商品信息,商品包含tag信息
     *
     * @param user   卖家
     * @param tag    tag名称
     * @param pageNo 起始页码,从1开始
     * @param size   每页显示条数
     * @return 归属指定tag的商品信息, 商品包含tag信息
     */
    @Override
    public Response<Paging<ItemWithTags>> findItemsOfTagCoupons(BaseUser user,
                                                         String tag,
                                                         Integer pageNo,
                                                         Integer size,
                                                         Map<String, String> params) {
        pageNo = Objects.firstNonNull(pageNo, 1);
        Map map= new HashMap<String, Object>();
        map.put("userId",user.getId());
        size = Objects.firstNonNull(itemDao.sellerItemCount(map), 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;
        Long masterId = Objects.firstNonNull(user.getParentId(), user.getId());
        Response<Paging<ItemWithTags>> result = new Response<Paging<ItemWithTags>>();
        try {
            long total = 0;
            List<Item> items;
            if (!Strings.isNullOrEmpty(tag)) {//查找指定tag的商品

                Paging<Long> p = itemsTagsDao.itemsOfTag(masterId, tag.trim(), offset, size);
                if (p.getTotal() > 0) {
                    items = itemManager.findByIds(p.getData());
                    total = p.getTotal();
                } else {
                    items = Collections.emptyList();
                }
            } else { //查找所有的商品
                Response<Paging<Item>> pResult = sellerItems(user, pageNo, size, params);
                if (pResult.isSuccess()) {
                    Paging<Item> pItems = pResult.getResult();
                    items = pItems.getData();
                    total = pItems.getTotal();
                } else {
                    result.setError(pResult.getError());
                    return result;
                }

            }

            if (items.isEmpty()) {
                Paging<ItemWithTags> iwt = new Paging<ItemWithTags>(0L, Collections.<ItemWithTags>emptyList());
                result.setResult(iwt);
                return result;
            }


            ListMultimap<Long, String> tags = itemsTagsDao.tagsOfItems(masterId, Lists.transform(items, new Function<Item, Long>() {
                @Override
                public Long apply(Item input) {
                    return input.getId();
                }
            }));

            List<ItemWithTags> iwts = Lists.newArrayListWithCapacity(items.size());
            for (Item item : items) {
                ItemWithTags iwt = new ItemWithTags();
                iwt.setItemId(item.getId());
                iwt.setImageUrl(item.getMainImage());
                iwt.setItemName(item.getName());
                iwt.setQuantity(item.getQuantity());
                iwt.setSoldQuantity(item.getSoldQuantity());
                List<String> t = Lists.newArrayList(tags.get(item.getId())); //f**k dubbo serialization
                if(t.size()>1){
                    Iterator iter = t.iterator();
                    while (iter.hasNext()) {
                       if(iter.next().toString().indexOf("/")< 0){
                           iter.remove();
                       }
                    }
                    iwt.setTags(t);
                }else{
                   // t.add("未分类");
                    iwt.setTags(t);
                }
                iwt.setStatus(item.getStatus());
                iwt.setPrice(item.getPrice());
                if(item.getStatus()==1){
                   iwts.add(iwt);
                }

            }
            Paging<ItemWithTags> piwt = new Paging<ItemWithTags>(total, iwts);
            result.setResult(piwt);
            return result;

        } catch (Exception e) {
            log.error("failed to find items of tag({}) of user (id={}),cause:{}", tag, masterId,
                    Throwables.getStackTraceAsString(e));
            result.setError("item.tags.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateStatusByIds(List<Long> ids, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            itemManager.bulkUpdateStatus(null, status, ids);
            //准实时dump
            realTimeIndexer.index(ids, Item.Status.fromNumber(status));

            //重新计算一把店铺宝贝数,这里的ids可能会包含多个店铺id
            List<Item> items = itemDao.findByIds(ids);
            List<Long> shopIds = Lists.transform(items, new Function<Item, Long>() {
                @Override
                public Long apply(Item item) {
                    return item.getShopId();
                }
            });
            List<Long> filterIds = Lists.newArrayList();
            for(Long shopId : shopIds) {
                if (filterIds.contains(shopId)) {
                    continue;
                }
                filterIds.add(shopId);
            }
            ItemCountEvent event = new ItemCountEvent(filterIds);
            eventBus.post(event);

            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("multi update items status failed itemIds:{} cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    private Response<Boolean> updateStatusBySellerId(Long sellerId, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        if (sellerId == null) {
            log.error("sellerId cannot be null");
            result.setError("sellerId.null.fail");
            return result;
        }
        if (status == null) {
            log.error("status cannot be null");
            result.setError("status.null.fail");
            return result;
        }
        try {
            itemManager.updateStatusBySellerId(sellerId, status);
            List<Long> ids = itemManager.findIdsBySellerId(sellerId);
            //准实时dump
            realTimeIndexer.index(ids, Item.Status.fromNumber(status));
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("update item status by sellerId failed sellerId:{} cause:{}", sellerId, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateStatusBySellerIds(Iterable<Long> sellerIds, Integer status) {
        Response<Boolean> result = new Response<Boolean>();
        if (status == null) {
            log.error("status cannot be null");
            result.setError("status.null.fail");
            return result;
        }
        try {
            for (Long sellerId : sellerIds) {
                updateStatusBySellerId(sellerId, status);
            }
            result.setResult(true);
            return result;
        } catch (Exception e) {
            log.error("update item status by sellerIds failed sellerIds:{} cause:{}", sellerIds, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    /**
     * 根据sellerId或者itemId来查找商品
     *
     * @param pageNo 页数
     * @param params 参数
     * @param size   每页条数
     * @return 商品列表
     */
    @Override
    public Response<Paging<Item>> find(@ParamInfo("pageNo") Integer pageNo,
                                       @ParamInfo("params") Map<String, String> params,
                                       @ParamInfo("size") Integer size) {
        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;
        Paging<Item> items;
        Response<Paging<Item>> result = new Response<Paging<Item>>();
        String status = Objects.firstNonNull(params.get("status"), "0,1,-1,-2");
        List<Integer> concernedStatus = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(status)) {//分割状态成为list
            List<String> all = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(status);

            concernedStatus = Lists.transform(all, new Function<String, Integer>() {
                @Override
                public Integer apply(String input) {
                    return Integer.parseInt(input);
                }
            });
        }
        try {
            String sellerName = params.get("sellerName");
            if (!Strings.isNullOrEmpty(sellerName)) {
                Response<User> ur = accountService.findUserBy(sellerName, LoginType.NAME);
                if (!ur.isSuccess()) {
                    log.error("failed to find user(name={}),error code:{}", sellerName, ur.getError());
                    result.setError(ur.getError());
                    return result;
                }
                User user = ur.getResult();
                items = itemManager.findBySellerId(offset, size, user.getId(), concernedStatus);
                result.setResult(items);
                return result;
            }
            String itemId = params.get("itemId");
            if (!Strings.isNullOrEmpty(itemId)) {
                Item item = itemManager.findById(Long.parseLong(itemId));
                items = new Paging<Item>(1L, Lists.newArrayList(item));
                result.setResult(items);
                return result;
            }
            items = itemManager.findAllItems(offset, size, concernedStatus);
            result.setResult(items);
            return result;
        } catch (Exception e) {
            log.error("find item fail", Throwables.getStackTraceAsString(e));
            result.setError("item.find.fail");
            return result;
        }
    }

    @Override
    public Response<Paging<Item>> findUnclassifiedItems(BaseUser baseUser, Integer pageNo, Integer size) {
        Response<Paging<Item>> result = new Response<Paging<Item>>();
        pageNo = Objects.firstNonNull(pageNo, 1);
        size = Objects.firstNonNull(size, 20);
        size = size > 0 ? size : 20;
        int offset = (pageNo - 1) * size;
        offset = offset > 0 ? offset : 0;
        try {
            Paging<Long> itemIdP = itemsTagsDao.findUnclassifiedItems(baseUser.getId(), offset, size);
            List<Item> items;
            long total;
            if (itemIdP.getTotal() > 0) {
                items = itemManager.findByIds(itemIdP.getData());
                total = itemIdP.getTotal();
            } else {
                items = Collections.emptyList();
                total = 0;
            }
            result.setResult(new Paging<Item>(total, items));
            return result;
        } catch (Exception e) {
            log.error("failed to findUnclassifiedItems", e);
            result.setError("item.not.found");
            return result;
        }
    }

    @Override
    public Response<Integer> countBySpuId(Long spuId) {
        Response<Integer> result = new Response<Integer>();
        if(spuId == null) {
            log.error("spuId can not be null");
            result.setError("bad.params");
            return result;
        }
        try {
            int count = itemDao.countBySpuId(spuId);
            result.setResult(count);
            return result;
        }catch (Exception e) {
            log.error("fail to count by spuId={}, cause:{}", spuId, Throwables.getStackTraceAsString(e));
            result.setError("item.count.fail");
            return result;
        }
    }

    @Override
    public Response<Long> countOnShelfByShopId(Long shopId) {
        Response<Long> result = new Response<Long>();
        if(shopId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            Long count = itemDao.countOnShelfByShopId(shopId);
            result.setResult(count);
            return result;
        }catch (Exception e) {
            log.error("fail to count on shelf by shopId={},cause:{}",shopId, Throwables.getStackTraceAsString(e));
            result.setError("count.shop.item.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> findBySpuId(Long spuId) {
        Response<List<Item>> result = new Response<List<Item>>();
        if(spuId == null) {
            log.error("spuId can not be null");
            result.setError("bad.params");
            return result;
        }
        try {
            List<Item> item = itemDao.findBySpuId(spuId);
            if(item == null) {
                log.error("fail to on-shelf item by spuId={}", spuId);
                result.setError("item.query.fail");
                return result;
            }
            result.setResult(item);
            return result;
        }catch (Exception e) {
            log.error("fail to find unique item by spuId={}, cause:{}", spuId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> findOnShelfBySpuId(Long spuId) {
        Response<List<Item>> result = new Response<List<Item>>();
        if (spuId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Item> items = itemDao.findOnShelfBySpuId(spuId);
            result.setResult(items);
            return result;
        }catch (Exception e) {
            log.error("fail to find items by spuId={}, cause:{}", spuId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<RichSpu> findRichSpuByItemId(Long id) {
        Response<RichSpu> result = new Response<RichSpu>();
        if(id == null) {
            log.error("item id can not be null when find richSpu");
            result.setError("item.params.fail");
            return result;
        }
        try {
            Item item = itemDao.findById(id);
            if (item == null) {
                log.error("item(id={}) not found", id);
                result.setError("item.not.found");
                return result;
            }

            Response<Spu> spuR = spuService.findById(item.getSpuId());
            if(!spuR.isSuccess() || spuR.getResult() == null) {
                log.error("failed to find spu by spuId={}, error code:{}", item.getSpuId(), spuR.getError());
                result.setError("spu.query.fail");
                return result;
            }
            Spu spu = spuR.getResult();
            Response<Brand> brandR = brandService.findById(Long.valueOf(spu.getBrandId()));
            List<RichAttribute> attributes = attributeService.findSpuAttributesBy(spu.getId());
            Map<String, String> map = Maps.newHashMap();
            //refine spu attribute map, not-include sku
            for(RichAttribute ra : attributes) {
                if(!map.containsKey(ra.getAttributeKey())) {
                    map.put(ra.getAttributeKey(), ra.getAttributeValue());
                }else {
                    String update = map.get(ra.getAttributeKey())+", "+ra.getAttributeValue();
                    map.put(ra.getAttributeKey(), update);
                }
            }
            RichSpu richSpu = new RichSpu();
            //ehaier商家
            if(Objects.equal(eHaierSellerId,item.getUserId().toString())){
                richSpu.setIsEhaier(Boolean.TRUE);
            }else {
                richSpu.setIsEhaier(Boolean.FALSE);
            }
            richSpu.setSpu(spuR.getResult());
            if(brandR.isSuccess()) {
                richSpu.setBrandName(brandR.getResult().getName());
            }else{
                log.error("failed to find brand by id={},error code:{}",spu.getBrandId(), spuR.getError());
            }
            richSpu.setAttributes(map);

            boolean hasService = itemServiceDao.countByItemId(id) > 0;
            richSpu.setHasService(hasService);

            result.setResult(richSpu);
            return result;
        }catch (Exception e) {
            log.error("failed to find RichSpu by itemId={},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("richSpu.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Pair>> findBreadCrumbsByItemId(Long id) {
        Response<List<Pair>> result = new Response<List<Pair>>();
        if(id == null) {
            log.error("item id can not be null when find bread crumbs");
            result.setError("params.illegal");
            return result;
        }
        try {
            Response<Item> itemR = findById(id);
            Long spuId = itemR.getResult().getSpuId();
            Response<Spu> spuR = spuService.findById(spuId);
            Long categoryId = spuR.getResult().getCategoryId();
            List<BackCategory> backCategories = bch.ancestorsOf(categoryId);
            List<Pair> pairs = Lists.newArrayListWithCapacity(backCategories.size());
            for (BackCategory bc : backCategories) {
                pairs.add(new Pair(bc.getName(), bc.getId()));
            }
            result.setResult(pairs);
            return result;
        }catch (Exception e) {
            log.error("failed to find bread crumbs by itemId={}, cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("breadCrumbs.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> findByModelId(Long modelId) {
        Response<List<Item>> result = new Response<List<Item>>();

        if(modelId == null){
            log.error("find items need modelId");
            result.setError("freight.model.modelId.null");
            return result;
        }

        try{
            result.setResult(itemDao.findByModelId(modelId));
        }catch(Exception e){
            log.error("find items failed , modelId={}, error code={}" , modelId, e);
            result.setError("item.query.fail");
        }

        return result;
    }

    /*
        绑定运费模板关系
     */
    @Override
    public Response<Boolean> bindFreightModel(Item item) {
        Response<Boolean> result = new Response<Boolean>();

        if(item.getId() == null){
            log.error("Item bind freight model need itemId.");
            result.setError("freight.model.itemId.null");
            return result;
        }

        if(item.getFreightModelId() != null){
            log.error("Item bind freight model need modelId.");
            result.setError("freight.model.modelId.null");
            return result;
        }

        try{
            itemDao.update(item);
            result.setResult(true);
        }catch(Exception e){
            log.error("Item bind freight model failed.");
            result.setError("freight.model.bind.failed");
        }

        return result;
    }

    @Override
    public Response<Item> findBySellerIdAndSpuId(Long sellerId, Long spuId) {
        Response<Item> result = new Response<Item>();
        if(sellerId == null || spuId == null) {
            log.error("shopId,spuId both can not be null when find item");
            result.setError("params.illegal");
            return result;
        }
        try {
            Item item = itemManager.findBySellerIdAndSpuId(sellerId, spuId);
            result.setResult(item);
            return result;
        }catch (Exception e) {
            log.error("failed to find item by shopId{} and spuId{}, cause:{}",
                    sellerId, spuId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<Sku> findSkuByAttributeValuesAndItemId(Long itemId, String attributeValue1, String attributeValue2) {
        Response<Sku> result = new Response<Sku>();
        if(itemId == null || (Strings.isNullOrEmpty(attributeValue1) && Strings.isNullOrEmpty(attributeValue2))) {
            log.error("itemId,attributeValue1,attributeValue2 can not be empty or null");
            result.setError("params.illegal");
            return result;
        }
        try {
            Sku sku = skuDao.findSkuByAttributeValuesAndItemId(itemId, attributeValue1, attributeValue2);
            result.setResult(sku);
            return result;
        }catch (Exception e) {
            log.error("failed to find sku by itemId{}, attributeValue1{}, attributeValue2{}, cause:{}",
                    itemId, attributeValue1, attributeValue2, Throwables.getStackTraceAsString(e));
            result.setError("sku.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateItemAndItemDetailAndCreateSku(Item item, ItemDetail itemDetail, Sku sku) {
        Response<Boolean> result = new Response<Boolean>();
        if(item.getId() == null) {
            log.error("item id can not be null when update");
            result.setError("illegal.param");
            return result;
        }
        Item exist = itemDao.findById(item.getId());
        if(exist == null) {
            log.error("item id={} not exits", item.getId());
            result.setError("item.not.found");
            return result;
        }
        if(sku == null || sku.getPrice() == null) {
            log.error("sku, sku price both can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            List<Sku> existSkus = skuDao.findByItemId(item.getId());

            int lowPrice = sku.getPrice();
            for (Sku existSku : existSkus) {
                //获取sku最低价
                if(existSku.getPrice() == null) {
                    log.error("sku{} price can not be null", sku);
                    result.setError("sku.price.null");
                    return result;
                }
                lowPrice = lowPrice < existSku.getPrice() ? lowPrice : existSku.getPrice();
            }
            item.setPrice(lowPrice);

            //如果有模板商品，要求商品价格不低于模板商品参考价
            Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(item.getSpuId());
            if (defaultItemR.isSuccess() && defaultItemR.getResult() != null) {
                DefaultItem defaultItem = defaultItemR.getResult();
                if(item.getPrice() < defaultItem.getPrice()) {
                    log.error("item {} price can not below defaultItem {} price", item, defaultItem);
                    result.setError("item.price.error");
                    return result;
                }
            }

            itemManager.updateItemAndItemDetailAndCreateSku(item, itemDetail, sku);
            //自动发商品接口调用本接口，商品可能自动上下架，重新计算店铺宝贝数量
            Long shopId = exist.getShopId();
            ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
            eventBus.post(event);

            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("failed to update item{} and itemDetail{},cause:{}",
                    item, itemDetail, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    /**
     * 创建商品,商品的图片信息,以及sku信息
     *
     * @param item       商品
     * @param itemDetail 商品的图片信息
     * @param skus       sku信息
     * @return 新创建商品的id
     */
    @Override
    public Response<Long> create(Item item, ItemDetail itemDetail, List<Sku> skus, Boolean needValidate) {
        Response<Long> result = new Response<Long>();
        if (item.getUserId() == null) {
            log.error("userId can not be null");
            result.setError("user.id.not.null.fail");
            return result;
        }

        if (Strings.isNullOrEmpty(item.getName())) {
            log.error("item name can not be empty");
            result.setError("item.name.empty");
            return result;
        }

        if(skus == null || skus.isEmpty()) {
            log.error("skus can not be null or empty");
            result.setError("illegal.param");
            return result;
        }

        //验证商品体积，体重合法性
        if(itemDetail.getFreightSize()!=null && itemDetail.getFreightSize()<=0) {
            log.error("item freight size can not lower than or same as 0");
            result.setError("item.freight.size.illegal");
            return result;
        }

        if(itemDetail.getFreightWeight()!= null && itemDetail.getFreightWeight() <= 0) {
            log.error("item freight weight can not lower than or same as 0");
            result.setError("item.freight.weight.illegal");
            return result;
        }

        try {

            int lowPrice = skus.get(0).getPrice();
            for (Sku sku : skus) {
                //获取sku最低价
                if(sku.getPrice() == null) {
                    log.error("sku{} price can not be null", sku);
                    result.setError("sku.price.null");
                    return result;
                }
                lowPrice = lowPrice < sku.getPrice() ? lowPrice : sku.getPrice();
            }
            item.setPrice(lowPrice);

            if(needValidate) {
                //如果有模板商品，要求商品价格不低于模板商品参考价
                Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(item.getSpuId());
                if (defaultItemR.isSuccess() && defaultItemR.getResult() != null) {
                    DefaultItem defaultItem = defaultItemR.getResult();
                    if (item.getPrice() < defaultItem.getPrice()) {
                        log.error("item {} price can not below defaultItem {} price", item, defaultItem);
                        result.setError("item.price.error");
                        return result;
                    }
                }
            }

            Set<ConstraintViolation<Item>> violations = validator.validate(item);
            if (!violations.isEmpty()) {

                StringBuilder sb = new StringBuilder();
                for (ConstraintViolation<?> violation : violations) {
                    sb.append(violation.getMessage()).append("\n");
                }
                log.error("illegal item params:{}", sb);
                result.setError("item.params.fail");
                return result;

            }

            //设置一个默认系统运费模板
            item.setFreightModelId(Objects.firstNonNull(item.getFreightModelId() , defaultModelId));
            item.setFreightModelName(Objects.firstNonNull(item.getFreightModelName() , defaultModelName));

            Long id = itemManager.create(item, itemDetail, skus);

            //如果是自动发商品调用本接口，可能商品自动上架，所以统一计算一把店铺宝贝数
            Long shopId = item.getShopId();
            ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
            eventBus.post(event);

            result.setResult(id);
            return result;
        } catch (Exception e) {

            log.error("failed to create item: {} and itemDetail:{}, cause:{}",
                    item, itemDetail, Throwables.getStackTraceAsString(e));
            result.setError("item.create.fail");
            return result;
        }
    }

    /**
     * 更新商品
     *
     * @param item       商品
     * @param itemDetail 商品的图片信息
     * @param skus       sku信息
     * @return 是否更新成功
     */
    @Override
    public Response<Boolean> update(Item item, ItemDetail itemDetail, List<Sku> skus) {
        Response<Boolean> result = new Response<Boolean>();
        if (item.getId() == null) {
            log.error("item id can not be null when updated");
            result.setError("item.id.null");
            return result;
        }
        Item exist = itemDao.findById(item.getId());
        if(exist == null) {
            log.error("item id={} not exist", item.getId());
            result.setError("item.not.found");
            return result;
        }

        if(skus == null || skus.isEmpty()) {
            log.error("skus can not be null or empty");
            result.setError("sku.not.found");
            return result;
        }

        //验证商品体积，体重合法性
        if(itemDetail.getFreightSize()!=null && itemDetail.getFreightSize()<=0) {
            log.error("item freight size can not lower than or same as 0");
            result.setError("item.freight.size.illegal");
            return result;
        }

        if(itemDetail.getFreightWeight()!= null && itemDetail.getFreightWeight() <= 0) {
            log.error("item freight weight can not lower than or same as 0");
            result.setError("item.freight.weight.illegal");
            return result;
        }

        try {
            //取skus最低价
            int lowPrice = skus.get(0).getPrice();
            for (Sku sku : skus) {
                if(sku.getPrice() == null) {
                    log.error("sku{} price can not be null", sku);
                    result.setError("sku.price.null");
                    return result;
                }
                lowPrice = lowPrice < sku.getPrice() ? lowPrice : sku.getPrice();
            }
            item.setPrice(lowPrice);

            //如果有模板商品，要求商品价格不低于模板商品参考价
            Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(item.getSpuId());
            if (defaultItemR.isSuccess() && defaultItemR.getResult() != null) {
                DefaultItem defaultItem = defaultItemR.getResult();
                if(item.getPrice() < defaultItem.getPrice()) {
                    log.error("item {} price can not below defaultItem {} price", item, defaultItem);
                    result.setError("item.price.error");
                    return result;
                }
            }

            itemManager.update(item, itemDetail, skus);

            //准实时dump
            realTimeIndexer.index(item.getId());

            //可能会更新商品的上下架状态，重新计算一把店铺宝贝数
            Long shopId = exist.getShopId();
            ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
            eventBus.post(event);

            result.setResult(Boolean.TRUE);
            return result;

        } catch (Exception e) {
            log.error("failed to update item to {},cause:{}", item, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    /**
     * 减少库存
     *
     * @param skuId    sku id
     * @param itemId   item id
     * @param quantity 数量
     */
    @Override
    public Response<Boolean> decrementStock(Long skuId, Long itemId, Integer quantity) {
        Response<Boolean> result = new Response<Boolean>();
        if (skuId == null) {
            log.error("skuId can not be null");
            result.setError("sku.id.null");
            return result;
        }
        if (quantity == null) {
            log.error("quantity can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemManager.decrementStock(skuId, itemId, quantity);
            result.setResult(Boolean.TRUE);

            //记录库存发生变化的商品，用于feed方式同步
            eventBus.post(new DspEvent(itemId));

            return result;
        } catch (Exception e) {
            log.error("failed to decrement stock for sku(id={}),cause:{}", skuId, Throwables.getStackTraceAsString(e));
            result.setError("stock.decrement.fail");
            return result;
        }

    }

    /**
     * 因为交易或者退货引起的库存和销量的变化
     *
     * @param skuId    sku id
     * @param itemId   商品id
     * @param quantity 变化量,对于卖出商品为负值,对于退货则为正值
     */
    @Override
    public Response<Boolean> changeSoldQuantityAndStock(Long skuId, Long itemId, Integer quantity) {
        Response<Boolean> result = new Response<Boolean>();
        if (skuId == null) {
            log.error("skuId can not be null");
            result.setError("sku.id.null");
            return result;
        }
        if (quantity == null) {
            log.error("quantity can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemManager.changeSoldQuantityAndStock(skuId, itemId, quantity);
            //准实时dump
            realTimeIndexer.index(itemId);

            //如果库存为0，自动下架，重新计算店铺宝贝数
            Item item = itemDao.findById(itemId);
            if(item.getQuantity() <= 0) {
                Long shopId = item.getShopId();
                ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
                eventBus.post(event);
            }

            result.setResult(Boolean.TRUE);

            //记录库存发生变化的商品，用于feed方式同步
            eventBus.post(new DspEvent(itemId));

            return result;
        } catch (Exception e) {
            log.error("failed to changeSoldQuantityAndStock item for sku(id={}),item(id={}),quantity={},cause:{}",
                    skuId, itemId, quantity, Throwables.getStackTraceAsString(e));
            result.setError("stock.decrement.fail");
            return result;
        }
    }

    /**
     * 增加库存
     *
     * @param skuId    sku id
     * @param itemId   item id
     * @param quantity sku id
     */
    @Override
    public Response<Boolean> incrementStock(Long skuId, Long itemId, Integer quantity) {
        Response<Boolean> result = new Response<Boolean>();
        if (skuId == null) {
            log.error("skuId can not be null");
            result.setError("sku.id.null");
            return result;
        }
        if (quantity == null) {
            log.error("quantity can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemManager.incrementStock(skuId, itemId, quantity);
            //准实时dump
            realTimeIndexer.index(itemId);
            result.setResult(Boolean.TRUE);

            //记录库存发生变化的商品，用于feed方式同步
            eventBus.post(new DspEvent(itemId));

            return result;
        } catch (Exception e) {
            log.error("failed to increment stock for sku(id={}),cause:{}", skuId, Throwables.getStackTraceAsString(e));
            result.setError("stock.increment.fail");
            return result;
        }

    }

    /**
     * 批量更新商品状态
     *
     * @param userId 用户id
     * @param status 状态
     * @param ids    商品id列表
     * @return 是否更新成功
     */
    @Override
    public Response<Boolean> bulkUpdateStatus(Long userId, Integer status, List<Long> ids) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("user id can not be null");
            result.setError("user.id.not.null.fail");
            return result;
        }
        if (status == null) {
            log.error("status can not be null");
            result.setError("illegal.param");
            return result;
        }

        if (ids.isEmpty()) {
            log.warn("ids is empty, nothing to be updated,return directly.");
            result.setResult(Boolean.TRUE);
            return result;
        }
        try {
            //过滤不符合要求的商品编号
            ids = filterItemStatus(ids , status);
            if(ids.isEmpty()){
                log.warn("filter ids, nothing can be updated.");
                result.setError("status.update.fail");
                return result;
            }
            itemManager.bulkUpdateStatus(userId, status, ids);

            //准实时dump
            realTimeIndexer.index(ids, Item.Status.fromNumber(status));

            //重新计算item count, 本方法只会被商家后台调用
            Item item = itemDao.findById(ids.get(0));
            Long shopId = item.getShopId();
            ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
            eventBus.post(event);

            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to update status to {} for items {},cause:{}",
                    status, ids, Throwables.getStackTraceAsString(e));
            result.setError("status.update.fail");
            return result;
        }
    }

    /**
     * 根据id查找商品
     *
     * @param id 商品id
     * @return 商品
     */
    @Override
    public Response<Item> findById(Long id) {
        Response<Item> result = new Response<Item>();
        if (id == null) {
            log.error("item id should be specified when find by id");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            Item item = itemManager.findById(id);
            if (item == null) {
                log.error("item(id={}) mot found", id);
                result.setError("item.not.found");
                return result;
            }
            result.setResult(item);
            return result;
        } catch (Exception e) {
            log.error("failed to find item whose id is {},cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    /**
     * 查找商品的其他信息
     *
     * @param itemId 商品id
     * @return 商品的其他图片信息
     */
    @Override
    public Response<ItemDetail> findDetailBy(Long itemId) {
        Response<ItemDetail> result = new Response<ItemDetail>();
        if (itemId == null) {
            log.error("item id can not be null");
            result.setError("item.id.null");
            return result;
        }
        try {
            ItemDetail itemDetail = itemManager.findDetailByItemId(itemId);
            if (itemDetail == null) {
                log.error("no item detail found for item(id={})", itemId);
                result.setError("item.detail.not.found");
                return result;
            }
            result.setResult(itemDetail);
            return result;
        } catch (Exception e) {
            log.error("failed to find itemDetail for item(id={}),cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("item.detail.query.fail");
            return result;
        }
    }

    /**
     * 查找商品的全部信息,包括商品,图片,sku,属性等
     *
     * @param itemId 商品id
     * @return 商品的全部信息
     */
    @Override
    public Response<Map<String, Object>> findWithDetailsById(Long itemId) {

        log.info("in findWithDetailsById, itemId={}", itemId);

        Response<Map<String, Object>> result = new Response<Map<String, Object>>();
        if (itemId == null) {
            log.error("item id can not be null");
            result.setError("item.id.null");
            return result;
        }
        Item item = itemManager.findById(itemId);
        if (item == null) {
            log.error("item(id={}) not found", itemId);
            result.setError("item.not.found");
            return result;
        }


        try {
            List<Sku> skus = itemManager.findSkusByItemId(itemId);
            //RichItem richItem;
            ItemDetail itemDetail = itemManager.findDetailByItemId(itemId);

            FullItem ft = new FullItem();
            ft.setItem(item);
            ft.setItemDetail(itemDetail);
            ft.setSkus(skus);

            SkuGroup skuGroup = new SkuGroup(skus);

            ft.setSkuGroup(skuGroup.getAttributes());
            ft.setAttributes(forest.getRichAttributes(item.getSpuId()));
            Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
            map.put("fullItem", ft);
            //如果有模版商品，返回模版商品信息
            Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(item.getSpuId());
            if (defaultItemR.isSuccess() && defaultItemR.getResult() != null) {
                map.put("defaultItem", defaultItemR.getResult());
            }

            // find service template
            String templateId = itemServiceDao.findTemplateByItemId(itemId);
            log.info("find template by item id={}, template id={}", itemId, templateId);
            map.put("serviceTemplateId", templateId);

            result.setResult(map);
            return result;
        } catch (Exception e) {
            log.error("failed to find full item(id={}),cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    /**
     * 如果商品未售出,则物理删除商品及相关信息,如果商品已经有了交易,则逻辑删除
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @return 是否删除成功
     */
    @Override
    public Response<Boolean> delete(Long userId, Long itemId) {
        Response<Boolean> result = new Response<Boolean>();
        if (itemId == null) {
            log.error("item id should be specified when deleted");
            result.setError("item.id.null");
            return result;
        }
        try {
            itemManager.delete(userId, itemId);

            //准实时dump
            realTimeIndexer.delete(itemId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to delete item(id={}),cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }
    }

    @Override
    public Response<Boolean> bulkDelete(Long userId, List<Long> itemIds) {
        Response<Boolean> result = new Response<Boolean>();
        if (itemIds == null||itemIds.isEmpty()) {
            log.warn("item ids is empty, return directly");
            result.setResult(Boolean.TRUE);
            return result;
        }
        try {
            Item item = itemDao.findById(itemIds.get(0));
            if(item == null){
                log.error("item(id={}) not found", itemIds.get(0));
                result.setError("item.not.found");
                return result;
            }
            for (Long itemId : itemIds) {
                itemManager.delete(userId, itemId);
            }
            realTimeIndexer.delete(itemIds);

            //重新计算一把店铺宝贝数,本方法只会被商家后台调用
            Long shopId = item.getShopId();
            ItemCountEvent event = new ItemCountEvent(Lists.newArrayList(shopId));
            eventBus.post(event);

            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to bulk delete items(ids={}),cause:{}", itemIds, Throwables.getStackTraceAsString(e));
            result.setError("item.delete.fail");
            return result;
        }
    }

    /**
     * 根据skuId查找sku
     *
     * @param skuId sku id
     * @return SKU信息
     */
    @Override
    public Response<Sku> findSkuById(Long skuId) {
        Response<Sku> result = new Response<Sku>();
        if (skuId == null) {
            log.error("sku id should be specified when deleted");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            Sku sku = itemManager.findSkuById(skuId);
            if (sku == null) {
                log.error("sku(id={}) not found", skuId);
                result.setError("sku.not.found");
                return result;
            }
            result.setResult(sku);
            return result;
        } catch (Exception e) {
            log.error("failed to find sku(id={}),cause:{}", skuId, Throwables.getStackTraceAsString(e));
            result.setError("sku.query.fail");
            return result;
        }
    }

    /**
     * 只需spu属性即可
     *
     * @param itemId 商品id
     * @return spu属性列表
     */
    @Override
    public Response<List<RichAttribute>> attributesOf(Long itemId) {
        Response<List<RichAttribute>> result = new Response<List<RichAttribute>>();
        if (itemId == null) {
            log.error("itemId can not be null");
            result.setError("item.id.null");
            return result;
        }

        try {
            Item item = itemManager.findById(itemId);
            if (item == null) {
                log.error("can not find item where id={}", itemId);
                result.setError("item.not.found");
                return result;
            }
            Long spuId = item.getSpuId();
            List<RichAttribute> attributes = forest.getRichAttributes(spuId);
            result.setResult(attributes);
            return result;
        } catch (Exception e) {
            log.error("failed to query rich attributes for item(id={}),cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("attribute.query.fail");
            return result;
        }
    }



    /**
     * 根据sku id 列表获取库存列表
     *
     * @param skuIds 库存id列表
     * @return  库存列表
     */
    public Response<List<Sku>> findSkuByIds(List<Long> skuIds) {
        Response<List<Sku>> result = new Response<List<Sku>>();

        try {
            if (CollectionUtils.isEmpty(skuIds)) {
                result.setResult(EMPTY_SKU_LIST);
                return result;
            }

            List<Sku> skus = skuDao.findByIds(skuIds);
            for (Sku sku : skus) {
                if (isEmpty(sku.getOuterId())) {    // 若没有outerId 则尝试从spu获取
                    fillWithOuterCodeIgnoreFail(sku);
                }
            }

            result.setResult(skus);
        } catch (Exception e) {
            log.error("fail to query sku with skuIds={}", skuIds, e);
            result.setError("sku.query.fail");
        }

        return result;
    }

    /**
     * 填充sku的outerId,如果报错打出警告，不影响查询结果
     *
     * @param sku  库存记录
     */
    private void fillWithOuterCodeIgnoreFail(Sku sku) {
        try {
            Item item = itemDao.findById(sku.getItemId());
            checkState(notNull(item), "item.not.found");

            checkState(notNull(item.getSpuId()), "item.spu.id.empty");

            Response<FullDefaultItem> defaultQueryResult = defaultItemService.findRichDefaultItemBySpuId(item.getSpuId());
            checkState(defaultQueryResult.isSuccess(), defaultQueryResult.getError());

            FullDefaultItem defaultItem = defaultQueryResult.getResult();
            List<BaseSku> baseSkus = defaultItem.getSkus();

            for (BaseSku baseSku : baseSkus) {
                if (equalWith(baseSku.getAttributeValue1(), sku.getAttributeValue1()) &&
                        equalWith(baseSku.getAttributeValue2(), sku.getAttributeValue2())) {
                    sku.setOuterId(baseSku.getOuterId());
                    break;
                }
            }

        } catch (IllegalStateException e) {
            log.warn("fail to find outerId with sku{}, error:{}", sku, e.getMessage());
        } catch (Exception e) {
            log.warn("fail to find outerId with sku{}, cause:{}", sku, Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public Response<List<Item>> findByShopId(Long shopId) {
        Response<List<Item>> result = new Response<List<Item>>();
        if(shopId == null) {
            log.error("shopId can not be null when find items by shop id");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Item> items = itemDao.findByShopId(shopId);
            result.setResult(items);
            return result;
        }catch (Exception e) {
            log.error("failed to find items by shopId{}, cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> findBySellerId(Long sellerId) {
        Response<List<Item>> result = new Response<List<Item>>();
        if(sellerId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Item> items = itemDao.findBySellerIdNoPaging(sellerId);
            result.setResult(items);
            return result;
        }catch (Exception e) {
            log.error("fail to find items by sellerId={},cause:{}", sellerId, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<Sku>> findSkusByItemId(Long itemId) {
        Response<List<Sku>> result = new Response<List<Sku>>();
        if(itemId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Sku> skus = skuDao.findByItemId(itemId);
            result.setResult(skus);
            return result;
        }catch (Exception e) {
            log.error("fail to find skus by itemId={},cause:{}",itemId, Throwables.getStackTraceAsString(e));
            result.setError("sku.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> batchUpdateItemRegion(List<Long> itemIds, String region) {
        Response<Boolean> result = new Response<Boolean>();
        if(itemIds == null) {
            log.error("itemIds can not be null when batch update item region");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemDao.batchUpdateItemRegion(itemIds, region);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("failed to batch update item region by itemIds{}, cause:{}",
                    itemIds, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> removeItemCountByShopIds(List<Long> shopIds) {
        Response<Boolean> result = new Response<Boolean>();
        if (shopIds == null || shopIds.isEmpty()) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemCountDao.deleteItemCount(shopIds);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to remove item count by shopIds={}, cause:{}", shopIds, Throwables.getStackTraceAsString(e));
            result.setError("delete.itemCount.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> setItemCountByShopId(Long shopId, Long count) {
        Response<Boolean> result = new Response<Boolean>();
        if (shopId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemCountDao.setShopItemCount(shopId, count);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to set item count by shopId={},count={},cause:{}",
                    shopId, count, Throwables.getStackTraceAsString(e));
            result.setError("set.itemCount.fail");
            return result;
        }
    }

    @Override
    public Response<Long> maxIdByShopId(Long shopId) {
        Response<Long> result = new Response<Long>();

        if(shopId == null) {
            log.error("shopId can not be null when find max item id by shopId");
            result.setError("illegal.params");
            return result;
        }
        try {
            Long lastId = itemDao.maxIdByShopId(shopId);
            result.setResult(lastId);
            return result;
        }catch (Exception e) {
            log.error("fail to find item max id, cause:{}", e);
            result.setError("maxId.found.error");
            return result;
        }
    }

    @Override
    public Response<List<Item>> findPagingItemByShopId(Long lastId, Long shopId, Integer limit) {
        Response<List<Item>> result = new Response<List<Item>>();

        if(lastId == null || shopId == null) {
            log.error("params incorrect when find paging item by shopId");
            result.setError("illegal.params");
            return result;
        }

        try {
            List<Item> items = itemDao.findPagingByShopId(lastId, shopId, limit);
            result.setResult(items);
            return result;
        }catch (Exception e) {
            log.error("fail to find paging item by shopId={}, lastId={}, limit={}, cause:{}",
                    shopId, lastId, limit, Throwables.getStackTraceAsString(e));
            result.setError("item.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> batchUpdateItemRegions(List<Item> items) {
        Response<Boolean> result = new Response<Boolean>();

        if(items == null || items.isEmpty()) {
            log.warn("params can not be null when batch update item region");
            result.setError("illegal.params");
            return result;
        }

        for(Item i : items) {
            try {
                itemDao.update(i);
            }catch (Exception e) {
                log.error("fail to batchUpdateItemRegions by item={}, cause:{}, skip it",
                        i, Throwables.getStackTraceAsString(e));
            }
        }

        result.setResult(Boolean.TRUE);
        return result;

    }

    @Override
    public Response<Boolean> updateItem(Item item) {
        Response<Boolean> result = new Response<Boolean>();

        if(item == null || item.getId() == null) {
            log.error("params error when update item");
            result.setError("illegal.param");
            return result;
        }

        try {
            itemDao.update(item);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update item {}, cause:{}", item, Throwables.getStackTraceAsString(e));
            result.setError("item.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateSkus(List<Sku> skus) {
        Response<Boolean> result = new Response<Boolean>();

        if(skus == null || skus.isEmpty()) {
            log.error("params error when update skus");
            result.setError("illegal.param");
            return result;
        }

        try {
            skuDao.update(skus);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update skus {}, cause:{}", skus, Throwables.getStackTraceAsString(e));
            result.setError("sku.update.fail");
            return result;
        }
    }

    /**
     * 针对商品上架前必须要绑定运费模板需求在这里进行一把过滤（只有绑定了运费模板的商品才能上架By MichaelZhao）
     * @param itemIds  需要过滤的商品编号
     * @param status   商品需要更改的状态
     * @return List
     * 返回符合要求的商品信息(question:这个现在还没有前台提示信息功能)
     */
    private List<Long> filterUnBind(List<Long> itemIds , Integer status){
        //是否是调整到上架状态
        if(Objects.equal(Item.Status.fromNumber(status), Item.Status.ON_SHELF)){
            //查询实际对象
            List<Item> itemList = itemDao.findByIds(itemIds);
            List<Long> filterIds = Lists.newArrayList();
            for(Item item : itemList){
                if(item.getFreightModelId() != null){
                    //已绑定物流模板
                    filterIds.add(item.getId());
                }
            }

            return filterIds;
        }else{
            return itemIds;
        }
    }

    /**
     * 这个需要处理过滤一些无法批量更改状态的商品编号（这个是针对与商家的过滤机制）
     * @param itemIds   商品编号
     * @param status    需要更改的状态
     */
    private List<Long> filterItemStatus(List<Long> itemIds , Integer status){
        List<Long> filterList = new ArrayList<Long>();

        for(Long itemId : itemIds){
            if(!filterFunction(itemDao.findById(itemId) , status)){
                filterList.add(itemId);
            }
        }

        return filterList;
    }

    /**
     * 判断商品是否过滤
     * @param item      商品信息
     * @param status    更改后的状态(当前批量处理的只存在上架和下架操作)
     * @return  Boolean
     * 返回是否被过滤
     */
    private Boolean filterFunction(Item item , Integer status){
        Boolean filterRes = true;
        final Item.Status fromStatus = Item.Status.fromNumber(item.getStatus());
        switch(Item.Status.fromNumber(status)){
            case INIT:{
                filterRes = true;
                break;
            }
            case ON_SHELF:{
                //只有下架或者初始状态才能迁移到上架状态(并且数量大于0)
                if((fromStatus == Item.Status.OFF_SHELF
                        || fromStatus == Item.Status.INIT )
                        && item.getQuantity() > 0){
                    filterRes = false;
                }
                break;
            }
            case OFF_SHELF:{
                //只有上架状态或未上架才能迁移到下架状态
                if(fromStatus == Item.Status.ON_SHELF ||
                        fromStatus == Item.Status.INIT ){
                    filterRes = false;
                }
                break;
            }
        }

        return filterRes;
    }
}
