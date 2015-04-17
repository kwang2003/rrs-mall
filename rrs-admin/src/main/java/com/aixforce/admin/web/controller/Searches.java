/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.item.service.ItemIndexService;
import com.aixforce.item.service.ItemSearchService;
import com.aixforce.search.ESClient;
import com.aixforce.shop.service.ShopService;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-10
 */
@Controller
@RequestMapping("/api/admin/search")
public class Searches {
    @Autowired
    private ItemIndexService itemIndexService;

    @Autowired
    private ItemSearchService itemSearchService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ESClient esClient;

    @RequestMapping(value = "/items/delta", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    //@RequiresPermissions("search:update")
    public String deltaDumpItems() {
        itemIndexService.deltaDump(15);
        return "delta dump item success";
    }

    @RequestMapping(value = "/items/full", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    // @RequiresPermissions("search:update")
    public String fullDumpItems() {
        itemIndexService.fullDump();
        return "full dump item success";
    }

    @RequestMapping(value = "/shops/delta", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    //@RequiresPermissions("search:update")
    public String deltaDumpShops() {
        shopService.deltaDump(15);
        return "delta dump shop success";
    }

    @RequestMapping(value = "/shops/full", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    // @RequiresPermissions("search:update")
    public String fullDumpShops() {
        shopService.fullDump();
        return "full dump shop success";
    }


    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object search(@RequestParam(value = "type", defaultValue = "item") String type,
                         @RequestParam(value = "p", defaultValue = "1") Integer pageNo,
                         @RequestParam(value = "size", defaultValue = "20") Integer size, WebRequest request) {
        Map<String, String> params = Maps.newHashMap();
        for (String name : request.getParameterMap().keySet()) {
            params.put(name, request.getParameter(name));
        }
        if (Objects.equal(type, "item")) {
            return itemSearchService.facetSearchItem(pageNo, size, params).getResult();
        }
        return Collections.emptyList();
    }

    @RequestMapping(value="/suggest",method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<String> suggest(@RequestParam("t") String indexName,@RequestParam("q")String term){
        return esClient.suggest(indexName,"name",term);
    }
}
