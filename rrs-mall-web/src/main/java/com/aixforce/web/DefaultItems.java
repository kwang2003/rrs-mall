package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.dto.RecommendSiteItem;
import com.aixforce.item.service.ItemSearchService;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.web.misc.MessageSources;
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
@Controller
@Slf4j
@RequestMapping("/api")
public class DefaultItems {

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private ItemSearchService itemSearchService;

    @Autowired
    private GridService gridService;

    /**
     * 站点推荐商品或者模版商品
     * @param spuIds spuId, 在推荐模版商品时不能为空
     * @param dateSource manual 为推荐模版商品，auto为推荐商品
     * @param size 商品数量
     * @param order 排序
     * @param brandId 品牌id
     * @param categoryId 前台类目id
     * @return  推荐商品列表
     */
    @RequestMapping(value = "/defaultItems", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RecommendSiteItem> findDefaultItems(@RequestParam(value = "spuIds", required = false) String spuIds,
                                              @RequestParam(value = "dataSource") String dateSource,
                                              @RequestParam(value = "size", defaultValue = "8") Integer size,
                                              @RequestParam(value = "order", required = false) String order,
                                              @RequestParam(value = "bid", required = false) Integer brandId,
                                              @RequestParam(value = "fcid", required = false) Integer categoryId,
                                              HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Map<String, String> cookieKVs = Maps.newHashMap();
        for(Cookie cookie : cookies) {
            cookieKVs.put(cookie.getName(), cookie.getValue());
        }
        Response<Integer> regionR = gridService.findRegionFromCookie(cookieKVs);
        if(!regionR.isSuccess()) {
//            log.warn("region not found in cookies");
            throw new JsonResponseException(400, messageSources.get(regionR.getError()));
        }
        Integer region = regionR.getResult();
        Response<List<RecommendSiteItem>> result = itemSearchService.recommendItemOrDefaultItemInSite(dateSource, spuIds, size, order,brandId, categoryId, region);
        if(!result.isSuccess()) {
            log.error("fail to find default item by spuIds={}, error code:{}", spuIds, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
}
