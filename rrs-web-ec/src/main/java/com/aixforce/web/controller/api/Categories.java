/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api;

import com.aixforce.category.dto.RichCategory;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-31
 */
@Controller
@RequestMapping("/api/categories")
public class Categories {

    private final static Logger log = LoggerFactory.getLogger(Categories.class);

    @Autowired
    private BackCategoryService backCategoryService;

    @Autowired
    private SpuService spuService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/{id}/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichCategory> childrenOf(@PathVariable("id") Long categoryId) {
        Response<List<RichCategory>> result = backCategoryService.childrenOf(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load sub categories of {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/{categoryId}/spus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Spu> findByCategoryId(@PathVariable("categoryId") Long categoryId) {

        Response<List<Spu>> result = spuService.findByCategoryId(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to find Spus for categoryId {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }
}
