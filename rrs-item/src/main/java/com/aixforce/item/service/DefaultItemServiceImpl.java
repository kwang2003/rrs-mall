package com.aixforce.item.service;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.dao.redis.DefaultItemRedisDao;
import com.aixforce.item.dto.FullDefaultItem;
import com.aixforce.item.dto.SkuGroup;
import com.aixforce.item.manager.DefaultItemManager;
import com.aixforce.item.model.BaseSku;
import com.aixforce.item.model.DefaultItem;
import com.aixforce.item.model.Item;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 13-12-17
 */
@Slf4j
@Service
public class DefaultItemServiceImpl implements DefaultItemService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultItemServiceImpl.class);

    private static final Splitter splitter = Splitter.on("_").trimResults().omitEmptyStrings();

    private static final JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private static final JavaType javaType = jsonMapper.createCollectionType(ArrayList.class, BaseSku.class);

    @Autowired
    private DefaultItemRedisDao defaultItemDetailRedisDao;

    @Autowired
    private DefaultItemManager defaultItemManager;

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public Response<Boolean> create(DefaultItem defaultItem, List<BaseSku> baseSkus) {
        Response<Boolean> result = new Response<Boolean>();
        if (defaultItem ==null || defaultItem.getSpuId() == null) {
            logger.error("spuId can not be null");
            result.setError("spu.id.null");
            return result;
        }
        if(defaultItem.getPrice() == null) {
            logger.error("defaultItem price can not be null");
            result.setError("defaultItem.price.null");
            return result;
        }
        defaultItem.setJsonSkus(jsonMapper.toJson(baseSkus));
        try {
            defaultItemManager.createOrUpdate(defaultItem, baseSkus);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            logger.error("create {} fail, cause:{}", defaultItem,Throwables.getStackTraceAsString(e));
            result.setError("defaultItem.create.fail");
            return result;
        }
    }

    /**
     * 更新默认商品(模版商品)，这里要求skus的id必须不为空
     *
     * @param defaultItem 默认商品详情
     * @param baseSkus    所有模版sku
     * @return 是否更新成功
     */
    @Override
    public Response<Boolean> update(DefaultItem defaultItem, List<BaseSku> baseSkus) {
        Response<Boolean> result = new Response<Boolean>();
        if (defaultItem.getSpuId() == null) {
            logger.error("spu id can not be null");
            result.setError("spu.id.null");
            return result;
        }
        DefaultItem exist = defaultItemDetailRedisDao.findBySpuId(defaultItem.getSpuId());
        if (exist == null) {
            logger.error("default item not exist");
            result.setError("defaultItem.query.fail");
            return result;
        }
        defaultItem.setId(exist.getId());
        defaultItem.setJsonSkus(jsonMapper.toJson(baseSkus));
        try {
            defaultItemManager.createOrUpdate(defaultItem, baseSkus);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            logger.error("update default item failed spuId={}, cause:{}", defaultItem.getSpuId(), Throwables.getStackTraceAsString(e));
            result.setError("defaultItem.update.fail");
            return result;
        }
    }

    @Override
    public Response<DefaultItem> findDefaultItemBySpuId(Long spuId) {
        Response<DefaultItem> result = new Response<DefaultItem>();
        if (spuId == null) {
            logger.error("spuId can not be null");
            result.setError("spu.id.null");
            return result;
        }
        try {
            DefaultItem defaultItem = defaultItemDetailRedisDao.findBySpuId(spuId);
            if (defaultItem == null){
                logger.warn("spu(id={})'s default item doesn't exist.", spuId);
                result.setError("defaultItem.not.found");
                return result;
            }
            //在前台发商品时，如果没有模版商品就直接返回null
            result.setResult(defaultItem);
            return result;
        } catch (Exception e) {
            logger.error("fail to find default item with spuId={}, cause:{}", spuId, Throwables.getStackTraceAsString(e));
            result.setError("defaultItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<FullDefaultItem> findRichDefaultItemBySpuId(Long spuId) {
        Response<FullDefaultItem> result = new Response<FullDefaultItem>();
        FullDefaultItem fullDefaultItem = new FullDefaultItem();
        Response<DefaultItem> defaultItemR = findDefaultItemBySpuId(spuId);
        if(!defaultItemR.isSuccess()) {
            logger.error("fail to find defaultItem by spuId={}, error code:{}",
                    spuId, defaultItemR.getError());
        }
        DefaultItem defaultItem = defaultItemR.getResult();
        if(defaultItem == null) {
            result.setResult(new FullDefaultItem());
            return result;
        }
        fullDefaultItem.setDefaultItem(defaultItem);
        try {
            String jsonSkus = defaultItem.getJsonSkus();
            List<BaseSku> baseSkus = jsonMapper.fromJson(jsonSkus, javaType);
            if(baseSkus != null && !baseSkus.isEmpty()) {
                fullDefaultItem.setSkus(baseSkus);
                SkuGroup skuGroup = new SkuGroup(baseSkus);
                fullDefaultItem.setSkuGroup(skuGroup);
            }
            result.setResult(fullDefaultItem);
            return result;
        }catch (Exception e) {
            logger.error("fail to find full default item with spuId={}, cause:{}", spuId, Throwables.getStackTraceAsString(e));
            result.setError("defaultItem.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<DefaultItem>> findBySpuIds(String spuIds) {
        Response<List<DefaultItem>> result = new Response<List<DefaultItem>>();
        if(Strings.isNullOrEmpty(spuIds)) {
            result.setResult(Collections.<DefaultItem>emptyList());
            return result;
        }
        List<Long> parsingIds = Lists.transform(splitter.splitToList(spuIds), new Function<String, Long>() {
            @Override
            public Long apply(String input) {
                return Long.parseLong(input);
            }
        });
        try {
            List<DefaultItem> defaultItems = defaultItemDetailRedisDao.findBySpuIds(parsingIds);
            result.setResult(defaultItems);
            return result;
        }catch (Exception e) {
            logger.error("fail to find default items by spuIds={},cause:{}", spuIds,Throwables.getStackTraceAsString(e));
            result.setError("default.item.query.fail");
            return result;
        }
    }


    @Override
    public Response<List<DefaultItem>> findBySpuIdsAndRid(String spuIds,Integer rid) {
        Response<List<DefaultItem>> result = new Response<List<DefaultItem>>();
        if(Strings.isNullOrEmpty(spuIds)) {
            result.setResult(Collections.<DefaultItem>emptyList());
            return result;
        }
        List<Long> parsingIds = Lists.transform(splitter.splitToList(spuIds), new Function<String, Long>() {
            @Override
            public Long apply(String input) {
                return Long.parseLong(input);
            }
        });
        try {
            log.info("test spuIds ={},rid={}",spuIds,rid);
            List<DefaultItem> defaultItems = defaultItemDetailRedisDao.findBySpuIds(parsingIds);
            log.info("end findBySpuIds method");
            log.info("defaultItems size={}",defaultItems.size());
            //进行价格过滤
            for(DefaultItem defaultItem : defaultItems){
                Map<String, String> params = Maps.newHashMap();
                params.put("rid", String.valueOf(rid));
                params.put("spuId", String.valueOf(defaultItem.getSpuId()));
                log.info("start searchItemBy method");
                Response<List<Item>> itemR = itemSearchService.searchItemBy(params);
                log.info("end searchItemBy method");
                if(itemR.isSuccess() &&itemR.getResult()!=null&&!itemR.getResult().isEmpty()&&itemR.getResult().size()>0) {
                    log.info("itemR size={}",itemR.getResult().size());
                    defaultItem.setPrice(itemR.getResult().get(0).getPrice());
                }else {
                    log.error("failed to find item by spuId={}, error code:{}", defaultItem, itemR.getError());
                }
            }
            result.setResult(defaultItems);
            return result;
        }catch (Exception e) {
            logger.error("fail to find default items by spuIds={},cause:{}", spuIds,Throwables.getStackTraceAsString(e));
            result.setError("default.item.query.fail");
            return result;
        }
    }

    @Override
    public Response<DefaultItem> findByOuterId(String outerId) {
        Response<DefaultItem> result = new Response<DefaultItem>();
        if(Strings.isNullOrEmpty(outerId)) {
            logger.error("outerId can not be empty or null");
            result.setError("outerId.null");
        }
        try {
            DefaultItem defaultItem = defaultItemDetailRedisDao.findByOuterId(outerId);
            result.setResult(defaultItem);
            return result;
        }catch (Exception e) {
            logger.error("failed to find defaultItem by outerId={}, cause:{}", outerId, Throwables.getStackTraceAsString(e));
            result.setError("default.item.query.fail");
            return result;
        }
    }
}
