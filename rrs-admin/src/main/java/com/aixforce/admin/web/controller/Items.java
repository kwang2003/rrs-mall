/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-07
 */
@Controller
@RequestMapping("/api/admin/items")
@SuppressWarnings("unused")
public class Items {
    private final static Logger log = LoggerFactory.getLogger(Items.class);

    private final static Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();

    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageResources;

    @Autowired
    private ShopService shopService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Item> search() {
        return Collections.emptyList();
    }

    /**
     * 运营后台对商品进行冻结，解冻操作,解冻后商品为下架状态
     *
     * @param ids    商品ids
     * @param status 商品待更新状态
     * @return 操作结果
     */
    @RequestMapping(value = "/updateMulti", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateMultiStatus(@RequestParam(value = "ids") String ids,
                                    @RequestParam(value = "status") Integer status) {
        List<String> idStrings = splitter.splitToList(ids);
        List<Long> idLongs = Lists.newArrayListWithCapacity(idStrings.size());
        for(String id : idStrings) {
            idLongs.add(Long.valueOf(id));
        }
        Response<Boolean> result = itemService.updateStatusByIds(idLongs, status);
        log.info("user id={} update items ids={} to status {}", UserUtil.getCurrentUser().getId(),
                ids, Item.Status.fromNumber(status));
        if (!result.isSuccess()) {
            log.error("failed to update multi items status,error code:", result.getError());
            throw new JsonResponseException(500, messageResources.get(result.getError()));
        }
        return "ok";
    }
}
