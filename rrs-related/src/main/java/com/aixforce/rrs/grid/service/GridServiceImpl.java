package com.aixforce.rrs.grid.service;

import com.aixforce.category.model.BackCategory;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.Brand;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.grid.service.ShopAuthorizeInfoService;
import com.aixforce.rrs.grid.dto.AuthorizeInfo;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.trade.dto.FatOrder;
import com.aixforce.user.service.AddressService;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 14-1-18
 */
@Service
@Slf4j
public class GridServiceImpl implements GridService {

    private static final JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private static final Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private final static String requestRegion = "haierRegionId";

    private final static String requestProvince = "haierProvinceId";

    @Autowired
    private SpuService spuService;

    @Autowired
    private BackCategoryService backCategoryService;

    @Autowired
    private ShopAuthorizeInfoService shopAuthorizeInfoService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AddressService addressService;

    @Override
    public Response<List<Long>> authorize(Item item, Long shopId) {
        Response<List<Long>> result = new Response<List<Long>>();

        Response<List<ShopAuthorizeInfo>> shopAuthorizeInfoR = shopAuthorizeInfoService.findByShopId(shopId);
        if(!shopAuthorizeInfoR.isSuccess()) {
            log.error("failed to find shop authorizeInfo by shopId={}, error code:{}",
                    shopId, shopAuthorizeInfoR.getError());
            result.setError("shopAuthorizeInfo.query.fail");
            return result;
        }

        return authorizeBySpuIdAndAuthorizeInfos(item.getSpuId(), shopAuthorizeInfoR.getResult());
    }

    @Override
    public Response<List<Long>> authorizeByInfos(Item item, List<ShopAuthorizeInfo> shopAuthorizeInfos) {
        return authorizeBySpuIdAndAuthorizeInfos(item.getSpuId(), shopAuthorizeInfos);
    }

    private Response<List<Long>> authorizeBySpuIdAndAuthorizeInfos(Long spuId,
                                                                   List<ShopAuthorizeInfo> shopAuthorizeInfos) {
        Response<List<Long>> result = new Response<List<Long>>();
        try {
            Response<Spu> spuR = spuService.findById(spuId);
            if(!spuR.isSuccess()) {
                log.error("query spu fail,spuId={},error code:{}", spuId, spuR.getError());
                result.setError("spu.query.fail");
                return result;
            }
            Spu spu = spuR.getResult();
            long categoryId = spu.getCategoryId();
            Response<List<Long>> categoryAncestorR = backCategoryService.ancestorsOfNoCache(categoryId);
            if(!categoryAncestorR.isSuccess()) {
                log.error("failed to find ancestor by categoryId{},error code:{}",
                        categoryId, categoryAncestorR.getError());
                result.setError("backCategory.query.fail");
                return result;
            }
            List<Long> categoryAncestor = categoryAncestorR.getResult();
            categoryId = categoryAncestor.get(1); //找二级类目
            long brandId = spu.getBrandId();
            List<Long> resultIds = Lists.newArrayList();
            resultIds.add(brandId);

            boolean isAuthorized = false;
            for (ShopAuthorizeInfo sai : shopAuthorizeInfos) {
                AuthorizeInfo authorizeInfo = jsonMapper.fromJson(sai.getJsonAuthorize(), AuthorizeInfo.class);
                //获取所有授权品牌id
                List<Brand> brands = authorizeInfo.getBrands();
                List<Long> brandIds = Lists.transform(brands, new Function<Brand, Long>() {
                    @Override
                    public Long apply(Brand input) {
                        return input.getId();
                    }
                });
                //获取所有授权类目id
                List<BackCategory> backCategories = authorizeInfo.getCategories();
                List<Long> categoryIds = Lists.transform(backCategories, new Function<BackCategory, Long>() {
                    @Override
                    public Long apply(BackCategory input) {
                        return input.getId();
                    }
                });
                //
                if(brandIds.contains(brandId) && categoryIds.contains(categoryId)) {
                    for(Map<String, List<Long>> map : authorizeInfo.getRegions()) {
                        for(String key : map.keySet()) {
                            List<Long> ids = map.get(key);
                            resultIds.addAll(ids);
                        }
                    }
                    isAuthorized = true;
                }
            }
            if(!isAuthorized) {
                log.error("fail to authorize spuId {} for shopAuthorizeInfos={}, need to be authorized by  brandId={} and categoryId={}",
                        spuId, shopAuthorizeInfos, brandId, categoryId);
                result.setError("shop.authorize.fail");
                return result;
            }else {
                result.setResult(resultIds);
                return result;
            }
        }catch (Exception e) {
            log.error("fail to authorize spuId{} for shopAuthorizeInfos={}, cause:{}",spuId, shopAuthorizeInfos, Throwables.getStackTraceAsString(e));
            result.setError("shop.authorize.fail");
            return result;
        }
    }

    @Override
    public Response<List<Long>> authorizeBySpuIdAndShopId(Long spuId, Long shopId) {
        Response<List<Long>> result = new Response<List<Long>>();

        Response<List<ShopAuthorizeInfo>> shopAuthorizeInfoR = shopAuthorizeInfoService.findByShopId(shopId);
        if(!shopAuthorizeInfoR.isSuccess()) {
            log.error("failed to find shop authorizeInfo by shopId={}, error code:{}",
                    shopId, shopAuthorizeInfoR.getError());
            result.setError("shopAuthorizeInfo.query.fail");
            return result;
        }

        return authorizeBySpuIdAndAuthorizeInfos(spuId, shopAuthorizeInfoR.getResult());
    }


    @Override
    public Response<Integer> findRegionFromCookie(Map<String, String> cookies) {
        Response<Integer> result = new Response<Integer>();
        try {
            Integer region = null;
            if(cookies != null) {
                for(String cookieName : cookies.keySet()) {
                    if(Objects.equal(cookieName, requestRegion)) {
                        region = Integer.valueOf(cookies.get(cookieName));
                    }
                }
            }
            if(region == null) {
                log.warn("region can not be null in cookies");
                result.setError("region.not.found");
                return result;
            }
            result.setResult(region);
            return result;
        } catch (Exception e) {
            log.error("failed to find region from cookie, cause:{}", e);
            result.setError("get.region.fail");
            return result;
        }
    }

    @Override
    public Response<Integer> findProvinceFromCookie(Map<String, String> cookies) {
        Response<Integer> result = new Response<Integer>();
        try {
            Integer provinceId = null;
            if(cookies != null) {
                for(String cookieName : cookies.keySet()) {
                    if(Objects.equal(cookieName, requestProvince)) {
                        provinceId = Integer.valueOf(cookies.get(cookieName));
                        break;
                    }
                }
            }

            if(provinceId == null) {
                log.warn("can't find provinceId in cookies");
                result.setError("get.region.fail");
                return result;
            }

            result.setResult(provinceId);
        } catch (Exception e) {
            log.error("failed to find provinceId from cookie, cause:{}", Throwables.getStackTraceAsString(e));
            result.setError("get.region.fail");
            return result;
        }

        return result;
    }


    @Override
    public Response<Boolean> verifyRegionWhenCreateOrder(List<? extends FatOrder> fatOrders, Integer regionId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            for(FatOrder fo : fatOrders) {
                for (Map.Entry<Long, Integer> entry : fo.getSkuIdAndQuantity().entrySet()) {
                    Long skuId = entry.getKey();
                    Response<Sku> skuR = itemService.findSkuById(skuId);
                    if(!skuR.isSuccess()) {
                        log.error("sku not found, skuId={}, error code:{}",skuId, skuR.getError());
                        result.setError(skuR.getError());
                        return result;
                    }
                    Long itemId = skuR.getResult().getItemId();
                    Response<Item> itemR = itemService.findById(itemId);
                    if(!itemR.isSuccess()) {
                        log.error("item not found, itemId={}, error code:{}", itemId, itemR.getError());
                        result.setError(itemR.getError());
                        return result;
                    }
                    Item item = itemR.getResult();
                    List<Integer> regionIds = Lists.transform(splitter.splitToList(item.getRegion()), new Function<String, Integer>() {
                        @Override
                        public Integer apply(String input) {
                            return Integer.valueOf(input);
                        }
                    });
                    List<Integer> ancestors = addressService.ancestorsOf(regionId).getResult();
                    boolean canBuy = false;
                    for(Integer ancestor : ancestors) {
                        if(regionIds.contains(ancestor)) {
                            canBuy = true;
                            break;
                        }
                    }
                    if(!canBuy) {
//                        fo.getSkuIdAndQuantity().remove(skuId);
                        result.setError("item.can.not.buy");
                        return result;
                    }
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("filter item when create order fail, cause:{}", e);
            result.setError("filter.item.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> verifyRegionWhenCreateTempOrder(Long itemId, Integer regionId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
                    Response<Item> itemR = itemService.findById(itemId);
                    if(!itemR.isSuccess()) {
                        log.error("item not found, itemId={}, error code:{}", itemId, itemR.getError());
                        result.setError(itemR.getError());
                        return result;
                    }
                    Item item = itemR.getResult();
                    List<Integer> regionIds = Lists.transform(splitter.splitToList(item.getRegion()), new Function<String, Integer>() {
                        @Override
                        public Integer apply(String input) {
                            return Integer.valueOf(input);
                        }
                    });
                    List<Integer> ancestors = addressService.ancestorsOf(regionId).getResult();
                    boolean canBuy = false;
                    for(Integer ancestor : ancestors) {
                        if(regionIds.contains(ancestor)) {
                            canBuy = true;
                            break;
                        }
                    }
                    if(!canBuy) {
                        result.setError("item.can.not.buy");
                        return result;
                    }
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("filter item when create order fail, cause:{}", e);
            result.setError("filter.item.fail");
            return result;
        }
    }
}
