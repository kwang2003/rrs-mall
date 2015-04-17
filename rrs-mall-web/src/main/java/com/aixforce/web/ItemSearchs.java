package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.dto.FacetSearchResult;
import com.aixforce.item.dto.FurnitureSiteItemDto;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemSearchService;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 13-12-31
 */
@Controller @Slf4j
@RequestMapping("/api")
public class ItemSearchs {

    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private CommonConstants commonConstants;

    @Autowired
    private ItemSearchService itemSearchService;

    @Autowired
    private GridService gridService;

    @RequestMapping(value = "/searchBySpuId", method = RequestMethod.GET)
    public String searchBySpuId (@RequestParam(value = "spuId") Long id, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for(Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
        if(!regionR.isSuccess()) {
            log.warn("region can not be null in cookies");
            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
        }
        Map<String, String> params = Maps.newHashMap();
        params.put("rid", String.valueOf(regionR.getResult()));
        params.put("spuId", String.valueOf(id));
        Response<Long> itemCountR = itemSearchService.countItemBy(params);
        if(!itemCountR.isSuccess()) {
            log.error("failed to count item, error code:{}", itemCountR.getError());
            throw new JsonResponseException(500, messageSources.get(itemCountR.getError()));
        }
        long count = itemCountR.getResult();
        if(Objects.equal(count, 0l)) {
            log.error("non item find by spuId={}", id);
            return "redirect:"+commonConstants.getMainSite()+"/item-not-found";
        }
        if(count > 1) {
            return "redirect:" + commonConstants.getMainSite() + "/search?spuId=" + id + "&rid=" + regionR.getResult();
        }else {
            Response<List<Item>> itemR = itemSearchService.searchItemBy(params);
            if(!itemR.isSuccess() || itemR.getResult().isEmpty()) {
                log.error("failed to find item by spuId={}, error code:{}", id, itemR.getError());
                return "redirect:"+commonConstants.getMainSite()+"/item-not-found";
            }
            return "redirect:" + commonConstants.getMainSite() + "/items/" + itemR.getResult().get(0).getId();
        }
    }

    /**
     * 某个前台类目下的热销榜
     * @param fcid  前台类目id
     * @param sort  排列顺序
     * @return      搜索结果，最多返回5件商品
     */
    @RequestMapping(value = "/hot", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FacetSearchResult hot(@RequestParam("fcid") Long fcid,
                                 @RequestParam("sort") String sort,
                                 HttpServletRequest request) {
        Map<String, String> params = Maps.newHashMap();
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for(Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
        if(!regionR.isSuccess()) {
            log.warn("region can not be null in cookies");
            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
        }
        params.put("rid", String.valueOf(regionR.getResult()));
        params.put("fcid", String.valueOf(fcid));
        params.put("sort", sort);
        Response<FacetSearchResult> result = itemSearchService.facetSearchItem(1, 5, params);
        if(!result.isSuccess()) {
            log.error("failed to facet search item,error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    /**
     * 家饰广告位组件,搜商品
     */
    @RequestMapping(value = "/items/furniture/ad", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FurnitureSiteItemDto furnitureAdvertising(@RequestParam("itemIds") String itemIds,
                                                     @RequestParam("order") String order,
                                                     @RequestParam("fcid") Integer fcid,
                                                     @RequestParam("size") Integer size) {
        Response<FurnitureSiteItemDto> result = itemSearchService.recommendItemInSite(itemIds,fcid,order,size);
        if(!result.isSuccess()) {
            log.error("fail to search furniture advertising item by itemIds{},order{},fcid{},size{}",
                   itemIds,order,fcid,size);
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
}
