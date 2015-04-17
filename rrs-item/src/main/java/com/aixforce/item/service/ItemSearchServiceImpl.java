/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.category.model.*;
import com.aixforce.category.service.BackCategoryHierarchy;
import com.aixforce.category.service.Forest;
import com.aixforce.category.service.FrontCategoryHierarchy;
import com.aixforce.category.service.FrontCategoryService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.item.dao.mysql.BrandDao;
import com.aixforce.item.dto.*;
import com.aixforce.item.model.Brand;
import com.aixforce.item.model.DefaultItem;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.search.ItemSearchHelper;
import com.aixforce.search.ESClient;
import com.aixforce.search.Pair;
import com.aixforce.search.RawSearchResult;
import com.aixforce.search.SearchFacet;
import com.aixforce.user.service.AddressService;
import com.google.common.base.*;
import com.google.common.collect.*;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-10
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    private final static Logger log = LoggerFactory.getLogger(ItemSearchServiceImpl.class);

    private final static Splitter splitter = Splitter.on('_').trimResults().omitEmptyStrings();
    private final static Joiner joiner = Joiner.on('_').skipNulls();
    public static final String CAT_FACETS = "cat_facets";
    public static final String ATTR_FACETS = "attr_facets";
    public static final String BRAND_FACETS = "brand_facets";
    public static final ImmutableList<Pair> rootCategory = ImmutableList.of(new Pair("所有分类", 0L));
    public static final String ITEM_INDEX_NAME = "items";
    public static final String ITEM_INDEX_TYPE = "item";
    public static final int BRAND_LIMITS = 10;


    @Autowired
    private ESClient esClient;

    @Autowired
    private Forest forest;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private AddressService addressService;
    @Autowired
    private SpuService spuService;

    @Autowired(required = false)
    private BackCategoryHierarchy bch;

    @Autowired(required = false)
    private FrontCategoryHierarchy fch;

    @Autowired(required = false)
    private FrontCategoryService frontCategoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private BrandDao brandDao;


    /**
     * 店铺内搜索,用于搜索店铺内已上架的商品
     *
     * @param pageNo 起始页码
     * @param size   返回条数
     * @param params 搜索参数
     * @return 搜索结果
     */
    @Override
    public Response<ItemsWithTagFacets> searchOnShelfItemsInShop(int pageNo, int size, Map<String, String> params) {
        Response<ItemsWithTagFacets> result = new Response<ItemsWithTagFacets>();
        if (params == null || !params.containsKey("sellerId")) {
            result.setError("sellerId.not.empty");
            return result;
        }
        //transform sq to q
        String q = params.get("sq");
        if (!Strings.isNullOrEmpty(q)) {
            params.put("q", q);
        }
        try {

            params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
            params.put("userId", String.valueOf(params.get("sellerId")));//将卖家id转换为userId
            pageNo = pageNo <= 0 ? 1 : pageNo;
            size = size <= 0 ? 20 : size;

            //find region ancestors
            String regionId = params.get("rid");
            if(!Strings.isNullOrEmpty(regionId)) {
                Response<List<Integer>> addressIdR = addressService.ancestorsOf(Integer.valueOf(regionId));
                if(addressIdR.isSuccess()) {
                    List<Integer> addressIds = addressIdR.getResult();
                    params.put("regions", joiner.join(addressIds));
                }
            }

            SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);
            QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
            requestBuilder.setQuery(queryBuilder);

            String sort = params.get("sort");
            ItemSearchHelper.composeSort(requestBuilder, sort);
            requestBuilder.setFrom((pageNo - 1) * size).setSize(size);
            requestBuilder.addFacet(FacetBuilders.termsFacet("tags").field("tags").size(10));
            RawSearchResult<Item> rawResult = esClient.facetSearchWithIndexType(ITEM_INDEX_TYPE, Item.class, requestBuilder);
            ItemsWithTagFacets items = refineTagFacets(rawResult);
            result.setResult(items);
            return result;
        } catch (Exception e) {
            log.error("failed to search onShelf items of shop(sellerId={}),cause:{}",
                    params.get("sellerId"), Throwables.getStackTraceAsString(e));
            result.setError("item.search.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> recommendItemInShop(Long sellerId, String dataSource, String ids,
                                                    Integer size, String order, Integer region) {
        Response<List<Item>> result = new Response<List<Item>>();
        if (sellerId == null) {
            result.setError("sellerId.not.empty");
            return result;
        }
        try {
            Map<String, String> params = Maps.newHashMap();
            params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
            params.put("userId", String.valueOf(sellerId));//将卖家id转换为userId
            params.put("shopId", String.valueOf(0)); //预售商品店铺id都为0，不能在前天搜索出来
            //find region ancestors
            if(region != null ) {
                Response<List<Integer>> addressIdR = addressService.ancestorsOf(region);
                if(addressIdR.isSuccess()) {
                    List<Integer> addressIds = addressIdR.getResult();
                    params.put("regions", joiner.join(addressIds));
                }
            }
            // 手动推荐
            SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);
            if (Objects.equal(dataSource, "manual")) {
                if (Strings.isNullOrEmpty(ids)) {
                    result.setResult(Lists.<Item>newArrayList());
                    return result;
                }
                params.put("ids", ids);
                QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
                requestBuilder.setQuery(queryBuilder);
                List<String> idList = splitter.splitToList(ids);
                // 最多 12 个
                requestBuilder.setFrom(0).setSize(Math.min(idList.size(), 12));
                List<Item> searchResult = esClient.search(ITEM_INDEX_TYPE, Item.class, requestBuilder).getData();
                List<Item> sortedResult = Lists.newArrayList();
                for (String id : idList) {
                    for (Item item : searchResult) {
                        if (item.getId().toString().equals(id)) {
                            sortedResult.add(item);
                        }
                    }
                }
                result.setResult(sortedResult);
            } else { // 自动推荐
                QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
                requestBuilder.setQuery(queryBuilder);
                if (Objects.equal(order, "hot")) {
                    requestBuilder.addSort("soldQuantity", SortOrder.DESC);
                } else {
                    requestBuilder.addSort("createdAt", SortOrder.DESC);
                }
                // 默认推荐12个
                requestBuilder.setFrom(0).setSize(Objects.firstNonNull(size, 12));
                Paging<Item> searchResult = esClient.search(ITEM_INDEX_TYPE,Item.class, requestBuilder);
                result.setResult(searchResult.getData());
            }
            return result;
        } catch (Exception e) {
            log.error("failed to search recommend items of shop(sellerId={}),cause:{}",
                    sellerId, Throwables.getStackTraceAsString(e));
            result.setError("item.search.fail");
            return result;
        }
    }

    @Override
    public Response<List<RecommendSiteItem>> recommendItemOrDefaultItemInSite(String dateSource,String spuIds,Integer size,String order,
                                                                              Integer brandId, Integer categoryId, Integer region) {
        Response<List<RecommendSiteItem>> result = new Response<List<RecommendSiteItem>>();
        try {
            //手动推荐，spuIds
            if(Objects.equal(dateSource, "manual")) {
                if(Strings.isNullOrEmpty(spuIds)) {
                    result.setResult(Lists.<RecommendSiteItem>newArrayList());
                    return result;
                }
                Response<List<DefaultItem>> defaultItemR = defaultItemService.findBySpuIdsAndRid(spuIds, region); //已下划线分隔
                if(!defaultItemR.isSuccess()) {
                    log.error("failed to find defaultItem, spuIds={}, error code:{}", spuIds, defaultItemR.getError());
                    result.setError("default.item.query.fail");
                    return result;
                }
                List<DefaultItem> defaultItems = defaultItemR.getResult();
                List<RecommendSiteItem> recommendSiteItems = Lists.newArrayListWithCapacity(defaultItems.size());
                for(DefaultItem di : defaultItems) {
                    RecommendSiteItem recommendSiteItem = new RecommendSiteItem();
                    BeanMapper.copy(di, recommendSiteItem);
                    recommendSiteItems.add(recommendSiteItem);
                }
                result.setResult(recommendSiteItems);
                return result;
            }else { //自动推荐,search item
                SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);
                Map<String, String> params = Maps.newHashMap();
                params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
                if(brandId != null) {
                    params.put("bid", String.valueOf(brandId));
                }
                if (categoryId != null) {
                    Iterable<Long> backLeafIds = frontCategoryToBackLeafIds(categoryId);
                    params.put("categoryIds", joiner.join(backLeafIds));
                }
                if(region != null) {
                    Response<List<Integer>> addressIdR = addressService.ancestorsOf(region);
                    if(addressIdR.isSuccess()) {
                        List<Integer> regionIds = addressIdR.getResult();
                        params.put("regions",joiner.join(regionIds));
                    }
                }
                //预售商品shopId都为0，需要剔除
                params.put("shopId", String.valueOf(0));
                QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
                requestBuilder.setQuery(queryBuilder);
                if (Objects.equal(order, "hot")) {
                    requestBuilder.addSort("soldQuantity", SortOrder.DESC);
                } else {
                    requestBuilder.addSort("createdAt", SortOrder.DESC);
                }
                // 默认推荐8个
                requestBuilder.setFrom(0).setSize(Objects.firstNonNull(size, 8));
                Paging<Item> searchResult = esClient.search(ITEM_INDEX_TYPE,Item.class, requestBuilder);
                List<RecommendSiteItem> recommendSiteItems = convertToRecommendSiteItem(searchResult);
                result.setResult(recommendSiteItems);
                return result;
            }
        }catch (Exception e) {
            log.error("failed to search recommend items or defaultItems of site, cause:", e);
            result.setError("item.search.fail");
            return result;
        }
    }

    private List<RecommendSiteItem> convertToRecommendSiteItem(Paging<Item> source) {
        List<RecommendSiteItem> recommendSiteItems = Lists.newArrayListWithCapacity(Integer.valueOf(source.getTotal().toString()));
        for(Item i : source.getData()) {
            RecommendSiteItem recommendSiteItem = new RecommendSiteItem();
            BeanMapper.copy(i, recommendSiteItem);
            recommendSiteItem.setItemId(i.getId());
            recommendSiteItems.add(recommendSiteItem);
        }
        return recommendSiteItems;
    }

    @Override
    public Response<FurnitureSiteItemDto> recommendItemInSite(String itemIds, Integer fcid, String order, Integer size) {
        Response<FurnitureSiteItemDto> result = new Response<FurnitureSiteItemDto>();
        try {
            FurnitureSiteItemDto furnitureSiteItemDto = new FurnitureSiteItemDto();
            SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);
            if (Objects.equal(order, "hot")) {
                requestBuilder.addSort("soldQuantity", SortOrder.DESC);
            } else {
                requestBuilder.addSort("createdAt", SortOrder.DESC);
            }

            if (fcid != null) {
                Map<String, String> paramFcid = Maps.newHashMap();
                paramFcid.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
                Iterable<Long> backLeafIds = frontCategoryToBackLeafIds(fcid);
                paramFcid.put("categoryIds", joiner.join(backLeafIds));
                QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(paramFcid);
                requestBuilder.setQuery(queryBuilder);
                // 默认推荐8个
                requestBuilder.setFrom(0).setSize(Objects.firstNonNull(size, 8));
                Paging<Item> searchResult = esClient.search(ITEM_INDEX_TYPE,Item.class, requestBuilder);
                List<RecommendSiteItem> itemFcid = convertToRecommendSiteItem(searchResult);
                furnitureSiteItemDto.setFcid(itemFcid);
            }
            if(!Strings.isNullOrEmpty(itemIds)) {
                Map<String, String> paramIds = Maps.newHashMap();
                paramIds.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
                paramIds.put("ids",joiner.join(splitter.splitToList(itemIds)));
                QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(paramIds);
                requestBuilder.setQuery(queryBuilder);
                // 默认推荐8个
                requestBuilder.setFrom(0).setSize(Objects.firstNonNull(size, 8));
                Paging<Item> searchResult = esClient.search(ITEM_INDEX_TYPE,Item.class, requestBuilder);
                List<RecommendSiteItem> items = convertToRecommendSiteItem(searchResult);
                furnitureSiteItemDto.setIds(items);
            }
            result.setResult(furnitureSiteItemDto);
            return result;
        }catch (Exception e) {
            log.error("fail to search item by itemIds={},fcid={},order={},cause:{}",
                    itemIds,fcid,order,Throwables.getStackTraceAsString(e));
            result.setError("item.search.fail");
            return result;
        }
    }

    @Override
    public Response<Long> countItemBy(Map<String, String> params) {
        Response<Long> result = new Response<Long>();
        try {
            params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
            //find region ancestors
            String regionId = params.get("rid");
            if(!Strings.isNullOrEmpty(regionId)) {
                Response<List<Integer>> addressIdR = addressService.ancestorsOf(Integer.valueOf(regionId));
                if(addressIdR.isSuccess()) {
                    List<Integer> addressIds = addressIdR.getResult();
                    params.put("regions", joiner.join(addressIds));
                }
            }
            //剔除店铺id为0的预售商品
            params.put("shopId", String.valueOf(0));
            QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
            long count = esClient.count(ITEM_INDEX_NAME, ITEM_INDEX_TYPE, queryBuilder);
            result.setResult(count);
            return result;
        }catch (Exception e) {
            log.error("failed to count item, cause:{]", Throwables.getStackTraceAsString(e));
            result.setError("item.search.count.fail");
            return result;
        }
    }

    @Override
    public Response<List<Item>> searchItemBy(Map<String, String> params) {
        Response<List<Item>> result = new Response<List<Item>>();
        try {
            SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);
            params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber()));
            //find region ancestors
            String regionId = params.get("rid");
            if(!Strings.isNullOrEmpty(regionId)) {
                Response<List<Integer>> addressIdR = addressService.ancestorsOf(Integer.valueOf(regionId));
                if(addressIdR.isSuccess()) {
                    List<Integer> addressIds = addressIdR.getResult();
                    params.put("regions", joiner.join(addressIds));
                }
            }
            //剔除店铺id为0的预售商品
            params.put("shopId", String.valueOf(0));
            QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
            requestBuilder.setQuery(queryBuilder);
            requestBuilder.setFrom(0).setSize(1);//有且只有一个
            Paging<Item> searchResult = esClient.search(ITEM_INDEX_TYPE,Item.class, requestBuilder);
            result.setResult(searchResult.getData());
            return result;
        }catch (Exception e) {
            log.error("failed to search item, cause:{}", Throwables.getStackTraceAsString(e));
            result.setError("item.search.fail");
            return result;
        }
    }

    /**
     * 全网搜索
     *
     * @param pageNo 起始页码
     * @param size   返回条数
     * @param params 搜索参数
     * @return 搜索结果
     */
    @Override
    public Response<FacetSearchResult> facetSearchItem(int pageNo, int size, Map<String, String> params) {

        Response<FacetSearchResult> result = new Response<FacetSearchResult>();
        try {
            pageNo = pageNo <= 0 ? 1 : pageNo;
            size = size <= 0 ? 20 : size;
            if (params == null) {
                params = Maps.newHashMap();
            }
            params.put("status", String.valueOf(Item.Status.ON_SHELF.toNumber())); //主搜只搜上架商品

            final Long categoryId = !Strings.isNullOrEmpty(params.get("cid")) ? Long.valueOf(params.get("cid")) : null;
            final Long brandId = !Strings.isNullOrEmpty(params.get("bid")) ? Long.valueOf(params.get("bid")) : null;

            boolean categoryIdPresent = categoryId != null && !Objects.equal(categoryId, 0L);
            boolean brandIdPresent = brandId != null;

            String pvids = params.get("pvids");//attribute value ids
            Set<Long> attributeIds = null;
            if (!Strings.isNullOrEmpty(pvids)) {
                List<String> parts = splitter.splitToList(pvids);
                attributeIds = Sets.newLinkedHashSetWithExpectedSize(parts.size());
                for (String part : parts) {
                    attributeIds.add(Long.valueOf(part));
                }
            }

            Long frontCategoryId = !Strings.isNullOrEmpty(params.get("fcid")) ? Long.parseLong(params.get("fcid")) : null;
            if (frontCategoryId != null) {
                Iterable<Long> backLeafIds = frontCategoryToBackLeafIds(frontCategoryId);
                params.put("categoryIds", joiner.join(backLeafIds));
            }

            //find region ancestors
            String regionId = params.get("rid");
            if(!Strings.isNullOrEmpty(regionId)) {
                Response<List<Integer>> addressIdR = addressService.ancestorsOf(Integer.valueOf(regionId));
                if(addressIdR.isSuccess()) {
                    List<Integer> addressIds = addressIdR.getResult();
                    params.put("regions", joiner.join(addressIds));
                }
            }

            //预售商品店铺id都为0，在主搜中剔除
            params.put("shopId", String.valueOf(0));

            SearchRequestBuilder requestBuilder = esClient.searchRequestBuilder(ITEM_INDEX_NAME);

            QueryBuilder queryBuilder = ItemSearchHelper.composeQuery(params);
            requestBuilder.setQuery(queryBuilder);

            String sort = params.get("sort");
            ItemSearchHelper.composeSort(requestBuilder, sort);
            requestBuilder.setFrom((pageNo - 1) * size).setSize(size);

            FacetBuilder catFacetBuilder = FacetBuilders.termsFacet(CAT_FACETS).field("categoryIds").size(20);
            FacetBuilder attrFacetBuilder = FacetBuilders.termsFacet(ATTR_FACETS).field("attributeIds").size(100);
            FacetBuilder brandFacetBuilder = FacetBuilders.termsFacet(BRAND_FACETS).field("brandId").size(20);

            requestBuilder.addFacet(catFacetBuilder).addFacet(attrFacetBuilder).addFacet(brandFacetBuilder);

            requestBuilder.addHighlightedField("name");

            RawSearchResult<RichItem> rawResult = esClient.facetSearchWithIndexType(ITEM_INDEX_TYPE, RichItem.class, requestBuilder);

            FacetSearchResult refinedResult = from(rawResult);
            //refine category navigator if necessary
            refineCategoryNavigator(categoryId, categoryIdPresent, refinedResult);

            //refine bread crumbs
            refineBreadCrumbs(categoryId, categoryIdPresent, refinedResult);

            //refine brand
            refineBrandNavigator(brandId, brandIdPresent, refinedResult);

            //refine property navigator
            refineAttributeNavigator(attributeIds, refinedResult);

            //return fcName
            if(frontCategoryId != null) {
                Response<FrontCategory> frontCategoryR = frontCategoryService.findById(frontCategoryId);
                if(!frontCategoryR.isSuccess() || frontCategoryR.getResult() == null) {
                    log.error("fail to find frontCategory by fcid={}, error code:{}, skip",
                            frontCategoryId, frontCategoryR.getError());
                }else {
                    refinedResult.setFcName(frontCategoryR.getResult().getName());
                }
            }
            result.setResult(refinedResult);
            return result;
        } catch (Exception e) {
            log.error("failed to facet search items", e);
            result.setError("item.search.fail");
            return result;
        }
    }




    //if a category has been selected as a query filter, then it should not appears in category navigator
    private void refineCategoryNavigator(final Long categoryId, boolean categoryIdPresent, FacetSearchResult result) {
        if (categoryIdPresent) {
            if (isLeaf(categoryId)) {//if is leaf category,no need to show category navigator any more
                result.setCategories(Collections.<SearchFacet>emptyList());
            }
            Iterables.removeIf(result.getCategories(), new Predicate<SearchFacet>() { //remove selected category
                @Override
                public boolean apply(SearchFacet input) {
                    return Objects.equal(input.getId(), categoryId);
                }
            });
        }
    }

    //if user has selected a category or only one category matches the query ,then use that category as a bread crumbs,
    // else only virtual root shows on bread crumbs
    private void refineBreadCrumbs(Long categoryId, boolean categoryIdPresent, FacetSearchResult result) {
        //if user specified a category or only one category found,then add breadCrumbs
        if (categoryIdPresent || result.getCategories().size() == 1) {
            Long targetId = categoryId != null ? categoryId : result.getCategories().get(0).getId();
            List<BackCategory> ancestors = bch.ancestorsOf(targetId);

            //to make result,we use java.util.ArrayList
            List<Pair> breadCrumbs = Lists.newArrayListWithCapacity(ancestors.size());
            for (BackCategory backCategory : ancestors) {
                breadCrumbs.add(new Pair(backCategory.getName(), backCategory.getId()));
            }

            result.setBreadCrumbs(breadCrumbs);


            //if the leaf category used as bread crumbs, then it should not appears in category navigator
            if (!categoryIdPresent) {
                result.setCategories(Collections.<SearchFacet>emptyList());
            }
        } else { //only add virtual root category
            result.setBreadCrumbs(rootCategory);
        }
    }


    //if a attribute has been selected as a query filter, then it should not appear in attribute navigator.
    //NOTE: we need to return user chosen properties
    private void refineAttributeNavigator(final Set<Long> attributeIds, FacetSearchResult result) {
        if (attributeIds != null) {
            final List<Pair> chosenAttributes = Lists.newArrayListWithCapacity(attributeIds.size());
            Iterables.removeIf(result.getAttributes(), new Predicate<FacetSearchResult.AttributeNavigator>() {
                @Override
                public boolean apply(FacetSearchResult.AttributeNavigator input) {
                    for (SearchFacet searchFacet : input.getValues()) {
                        if (attributeIds.contains(searchFacet.getId())) {
                            chosenAttributes.add(new Pair(input.getKey() + ":" + searchFacet.getName(), searchFacet.getId()));
                            return true;
                        }
                    }
                    return false;
                }
            });
            result.setChosenAttributes(chosenAttributes);
        }
        result.setAttributes(Lists.newArrayList(Iterables.limit(result.getAttributes(), 5)));//return atMost 5 attribute group
    }

    private void refineBrandNavigator(Long brandId, boolean brandIdPresent, FacetSearchResult result) {
        if(brandIdPresent) {
            List<Pair> brands = Lists.newArrayList();
            SearchFacet toRemove = null;
            for(SearchFacet sf : result.getBrands()) {
                if(Objects.equal(brandId, sf.getId())) {
                    brands.add(new Pair(sf.getName(), sf.getId()));
                    toRemove = sf;
                }
            }
            result.setChosenBrands(brands);
            //remove chosen brand
            if(toRemove != null)
                result.getBrands().remove(toRemove);
        }
        result.setBrands(Lists.newArrayList(Iterables.limit(result.getBrands(), 10)));//return atMost 5 brand group
    }


    private boolean isLeaf(Long categoryId) {
        CategoryNode subTree = bch.getSubTreeById(categoryId);
        return subTree != null && subTree.getChildren().isEmpty();
    }

    //accommodate raw search result to com.aixforce.web required format
    private FacetSearchResult from(RawSearchResult<RichItem> rawResult) {
        FacetSearchResult result = new FacetSearchResult();
        Facets facets = rawResult.getFacets();

        //handle category facets
        TermsFacet catFacet = facets.facet(CAT_FACETS);
        List<SearchFacet> leafCategoryFacet = processCategoryFacets(catFacet);

        //handle property facets
        TermsFacet attrFacet = facets.facet(ATTR_FACETS);
        List<FacetSearchResult.AttributeNavigator> attributeNavigators = processAttributeFacets(leafCategoryFacet, attrFacet);

        TermsFacet brandFacet = facets.facet(BRAND_FACETS);
        List<SearchFacet> brandNavigators = processBrandFacet(brandFacet);

        result.setTotal(rawResult.getTotal());
        result.setItems(rawResult.getData());
        result.setCategories(leafCategoryFacet);
        result.setAttributes(attributeNavigators);
        result.setBrands(brandNavigators);
        return result;
    }

    private List<SearchFacet> processBrandFacet(TermsFacet brandFacet) {
        List<SearchFacet> brands = Lists.newArrayListWithCapacity(BRAND_LIMITS);
        int total = 0;
        Iterator<TermsFacet.Entry> it = brandFacet.iterator();
        while (it.hasNext() && total < BRAND_LIMITS) {
            TermsFacet.Entry entry = it.next();
            Long brandId = Long.valueOf(entry.getTerm().string());
            Long count = (long) entry.getCount();
            final Response<Brand> brandR = brandService.findById(brandId);
            if(!brandR.isSuccess()){
                log.error("failed to find brand(id={}), error code:{}", brandId, brandR.getError());
            }else {
                Brand brand = brandR.getResult();
                brands.add(new SearchFacet(brand.getId(), count, brand.getName()));
            }
            total++;

        }
        return brands;
    }


    //find leaf node and fill the category name
    private List<SearchFacet> processCategoryFacets(TermsFacet catFacet) {
        List<SearchFacet> leafCategoryFacet = Lists.newArrayList();
        for (TermsFacet.Entry entry : catFacet) {

            Long categoryId = Long.valueOf(entry.getTerm().string());
            CategoryNode subTreeRootNode = bch.getSubTreeById(categoryId);
            if (subTreeRootNode != null) {
                if (subTreeRootNode.getChildren().isEmpty()) {
                    SearchFacet searchFacet = new SearchFacet(Long.valueOf(entry.getTerm().string()),
                            (long) entry.getCount());
                    searchFacet.setName(subTreeRootNode.getCategory().getName());
                    leafCategoryFacet.add(searchFacet);
                } else {
                    log.debug("skip non-leaf category(id={},name={}) ", categoryId, subTreeRootNode.getCategory().getName());
                }
            } else {
                log.error("failed to find category(id={})", categoryId);
            }
        }
        return leafCategoryFacet;
    }

    //find out attribute key and fill attrFacet name
    private List<FacetSearchResult.AttributeNavigator> processAttributeFacets(List<SearchFacet> leafCategoryFacet,
                                                                              TermsFacet propFacets) {
        Multimap<String, SearchFacet> allAttributes = LinkedHashMultimap.create();
        Map<Long, TermsFacet.Entry> byAttributeId = Maps.uniqueIndex(propFacets, new Function<TermsFacet.Entry, Long>() {
            @Override
            public Long apply(TermsFacet.Entry entry) {
                return Long.valueOf(entry.getTerm().string());
            }
        });
        for (SearchFacet categoryFacet : leafCategoryFacet) {

            Long categoryId = categoryFacet.getId();
            List<AttributeKey> attributeKeys = forest.getAttributeKeys(categoryId);

            for (AttributeKey attributeKey : attributeKeys) {

                Long attributeKeyId = attributeKey.getId();
                List<AttributeValue> attributeValues = forest.getAttributeValues(categoryId, attributeKeyId);

                for (AttributeValue attributeValue : attributeValues) {

                    Long attributeValueId = attributeValue.getId();

                    if (byAttributeId.containsKey(attributeValueId)) {

                        TermsFacet.Entry entry = byAttributeId.get(attributeValueId);
                        SearchFacet searchFacet = new SearchFacet(attributeValueId, (long) entry.getCount());
                        searchFacet.setName(attributeValue.getValue());//fill name
                        allAttributes.put(attributeKey.getName(), searchFacet);

                    }
                }
            }
        }
        List<FacetSearchResult.AttributeNavigator> attributeNavigators = Lists.newArrayListWithCapacity(allAttributes.keySet().size());
        for (String key : allAttributes.keySet()) {
            FacetSearchResult.AttributeNavigator navigator = new FacetSearchResult.AttributeNavigator();
            navigator.setKey(key);

            Set<SearchFacet> values = (Set<SearchFacet>) allAttributes.get(key);

            //make values be serializable
            Set<SearchFacet> nativeVal = Sets.newHashSetWithExpectedSize(values.size());

            //remove duplicate attribute-value name
            Set<String> allAttributeValueNames = Sets.newHashSet();

            for (SearchFacet val : values) {
                if(!allAttributeValueNames.contains(val.getName())) {
                    nativeVal.add(val);
                    allAttributeValueNames.add(val.getName());
                }
            }

            navigator.setValues(nativeVal);
            attributeNavigators.add(navigator);
        }
        return attributeNavigators;
    }


    private ItemsWithTagFacets refineTagFacets(RawSearchResult<Item> rawResult) {
        ItemsWithTagFacets result = new ItemsWithTagFacets();
        result.setTotal(rawResult.getTotal());
        result.setItems(rawResult.getData());
        ImmutableList.Builder<SearchFacet> tags = ImmutableList.builder();
        TermsFacet tagFacets = (TermsFacet) rawResult.getFacets().facetsAsMap().get("tags");
        for (TermsFacet.Entry entry : tagFacets) {
            String tag = entry.getTerm().string();
            Integer count = entry.getCount();
            SearchFacet sf = new SearchFacet(null, (long) count);
            sf.setName(tag);
            tags.add(sf);

        }
        result.setTagFacets(tags.build());
        return result;
    }

    /**
     * 将前台类目转换为后台叶子类目集合便于搜索
     *
     * @param frontCategoryId 前台类目id
     * @return 后台叶子类目集合
     */
    private Iterable<Long> frontCategoryToBackLeafIds(final long frontCategoryId) {
        Set<Long> leafIds = Sets.newHashSet();
        CategoryNode<FrontCategory> tree = fch.getSubTreeById(frontCategoryId);
        if (tree == null) {
            return Collections.emptySet();
        }
        //if is leaf category
        if (tree.getChildren().size() == 0) {
            leafIds.add(tree.getCategory().getId());
        }
        for (CategoryNode<FrontCategory> child : tree.getChildren()) {
            if (child.getChildren().isEmpty()) {
                leafIds.add(child.getCategory().getId());
            }
        }
        List<Long> backCategoryIds = Lists.newArrayList();
        for (Long leafId : leafIds) {
            Response<List<CategoryMapping>> cm = frontCategoryService.findMappingList(leafId);
            if(cm.isSuccess()) {
                List<Long> backIds = Lists.transform(cm.getResult(), new Function<CategoryMapping, Long>() {
                    @Override
                    public Long apply(CategoryMapping input) {
                        return input.getBackCategoryId();
                    }
                });
                backCategoryIds.addAll(backIds);
            }else {
                log.error("failed to find category mapping for front category (id={}),error code:{}, skip it",
                        frontCategoryId, cm.getError());
            }
        }

        return backCategoryIds;
    }


	@Override
	public Response<Map<String, Object>> searchItemsBrandBy(String spuId) {
		Response<Map<String, Object>> resultMap =new Response<Map<String,Object>>();
		Map<String, Object> result =new HashMap<String,Object>();
		result.put("spuId", spuId);
		//result.put("rid", rid);
		//result.put("cid", cid);
		//result.put("pid", pid);
		List<Map<String, Object>> map=brandDao.findItems(result);
				//brandDao.findItems(spuId);
		/*for (Map<String, Object> iterMap : map) {
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("shopId", iterMap.get("shopId"));
			paramMap.put("brandId", iterMap.get("brandId"));
			int count=brandDao.findCount(paramMap);
			if(count>0){
				result.add(iterMap);
			}
		}
		System.out.println(result.toString());*/
        Response<Spu> spuR = spuService.findById(Long.parseLong(spuId));
        Long categoryId = spuR.getResult().getCategoryId();
        BackCategory backCategory=bch.findById(categoryId);
        Map<String, Object> resuMap=new HashMap<String, Object>();
        resuMap.put("total", map.size());
        resuMap.put("name", backCategory.getName());
        resuMap.put("items", map);
        resultMap.setResult(resuMap);
        resultMap.setSuccess(true);
        return resultMap;
    }
}
