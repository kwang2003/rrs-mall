/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.category.dto.RichCategory;
import com.aixforce.category.dto.SpuWithDomain;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.web.misc.MessageSources;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台类目
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-31
 */
@Controller
@RequestMapping("/api/admin/backCategories")
public class BackCategories {

    private final static Logger log = LoggerFactory.getLogger(BackCategories.class);

    @Autowired
    private BackCategoryService backCategoryService;

    @Autowired
    private SpuService spuService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private CommonConstants commonConstants;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichCategory> list() {
        Response<List<RichCategory>> result = backCategoryService.childrenOfNoCache(0L);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load root back categories,error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichCategory> childrenOf(@PathVariable("id") Long categoryId) {
        Response<List<RichCategory>> result = backCategoryService.childrenOfNoCache(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load sub back categories of {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BackCategory newCategory(BackCategory backCategory) {
        Response<Long> result = backCategoryService.create(backCategory);
        if (result.isSuccess()) {
            Long id = result.getResult();
            backCategory.setId(id);
            return backCategory;
        } else {
            log.error("failed to create {},error code: {}", backCategory, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String removeChild(@PathVariable("id") Long id) {

        Response<Boolean> result = backCategoryService.delete(id);
        if (result.isSuccess()) {
            return messageSources.get("category.delete.success");
        } else {
            log.error("failed to delete back category {},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }


    @RequestMapping(value = "/{categoryId}/spus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<SpuWithDomain> findByCategoryId(@PathVariable("categoryId") Long categoryId) {
        Response<List<Spu>> result = spuService.findByCategoryIdNoCache(categoryId);
        if (result.isSuccess()) {
            List<Spu> spus = result.getResult();List<SpuWithDomain> spuWithDomains = Lists.newArrayListWithCapacity(spus.size());
                for(Spu spu : spus) {
                    SpuWithDomain spuWithDomain = new SpuWithDomain();
                    BeanMapper.copy(spu, spuWithDomain);
                    spuWithDomain.setDomain(commonConstants.getHrefProps().getProperty("main"));
                    spuWithDomains.add(spuWithDomain);
                }
            return spuWithDomains;
        } else {
            log.error("failed to find Spus for back categoryId {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateCategory(@PathVariable("id") Long categoryId,
                                 @RequestParam(value = "name") String name) {
        Response<Boolean> result = backCategoryService.update(categoryId, name);
        if (result.isSuccess()) {
            return messageSources.get("category.update.success");
        } else {
            log.error("failed to update back category {}, cause:{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/findByLevel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BackCategory> findByLevel(@RequestParam(value = "level", defaultValue = "2") Integer level) {
        Response<List<BackCategory>> result = backCategoryService.findByLevelNoCache(level);
        if(!result.isSuccess()) {
            log.error("failed to find back category by level={}, error code:{}", level, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
}
