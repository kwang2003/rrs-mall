/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.item.dto.FacetSearchResult;
import com.aixforce.item.dto.FurnitureSiteItemDto;
import com.aixforce.item.dto.ItemsWithTagFacets;
import com.aixforce.item.dto.RecommendSiteItem;
import com.aixforce.item.model.Item;

import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-10
 */
public interface ItemSearchService {

    /**
     * 全网搜索
     *
     * @param pageNo 起始页码
     * @param size   返回条数
     * @param params 搜索参数
     * @return 搜索结果
     */
    Response<FacetSearchResult> facetSearchItem(@ParamInfo("pageNo") int pageNo, @ParamInfo("size") int size,
                                                @ParamInfo("params") Map<String, String> params);


    /**
     * 店铺内搜索,用于搜索店铺内已上架的商品,一个用户只有一个店铺
     *
     * @param pageNo 起始页码
     * @param size   返回条数
     * @param params 搜索参数
     * @return 搜索结果
     */
    Response<ItemsWithTagFacets> searchOnShelfItemsInShop(
            @ParamInfo("pageNo") int pageNo,
            @ParamInfo("size") int size,
            @ParamInfo("params") Map<String, String> params);

    /**
     * 店铺内推荐商品搜索
     * @param sellerId 卖家 id
     * @param dataSource 数据来源，可选 auto, manual ，默认为 auto
     * @param ids 当 dataSource 为 manual 时必选，多个 id 组合成的字符串，空格连接
     * @param size 当 dataSource 为 auto 时可选，默认为 12
     * @param order 当 dataSource 为 auto 时可选 new, hot ，默认为 new
     *               如果 dataSource == auto ，则应该包含 order 和 count ；如果 dataSource == manual ，则应该包含一个 ids 字符串
     * @return 商品 List
     */
    Response<List<Item>> recommendItemInShop(@ParamInfo("sellerId") Long sellerId,
                                             @ParamInfo("dataSource") String dataSource,
                                             @ParamInfo("ids") String ids,
                                             @ParamInfo("size") Integer size,
                                             @ParamInfo("order") String order,
                                             @ParamInfo("rid")Integer region);

    /**
     * 站点首页推荐商品
     * @param dateSource 数据来源，可选 auto, manual ，默认为 auto
     * @param spuIds 当 dataSource 为 manual 时必选，多个 spuId 组合成的字符串，空格连接
     * @param size   商品数量,默认8个
     * @param order  当 dataSource 为 auto 时可选 new, hot ，默认为 new
     * @param brandId 品牌id
     * @param categoryId 前台类目id
     *               如果 dataSource == auto ，则应该包含 order；如果 dataSource == manual ，则应该包含一个 spuIds 字符串
     * @return       站点推荐商品列表
     */
    Response<List<RecommendSiteItem>> recommendItemOrDefaultItemInSite(@ParamInfo("dataSource") String dateSource,
                                                                       @ParamInfo("spuIds") String spuIds,
                                                                       @ParamInfo("size") Integer size,
                                                                       @ParamInfo("order") String order,
                                                                       @ParamInfo("bid") Integer brandId,
                                                                       @ParamInfo("categoryId") Integer categoryId,
                                                                       @ParamInfo("rid") Integer region);

    /**
     * 家饰站点首页推荐商品
     * @param itemIds 商品id列表
     * @param fcid    前台类目id
     * @param order   new, hot ，默认为 new
     * @param size    数量默认8个
     * @return  站点推荐商品列表
     */
    Response<FurnitureSiteItemDto> recommendItemInSite(String itemIds, Integer fcid, String order, Integer size);

    /**
     * 商品计数
     * @param params 参数列表
     * @return  商品数量
     */
    Response<Long> countItemBy(Map<String,String> params);

    Response<List<Item>> searchItemBy(Map<String,String> params);
    
    Response<Map<String, Object>> searchItemsBrandBy(@ParamInfo("spuId") String spuId);
}
