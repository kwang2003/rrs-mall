package com.aixforce.admin.web.controller;

import com.aixforce.category.dto.RichCategory;
import com.aixforce.category.model.CategoryMapping;
import com.aixforce.category.model.FrontCategory;
import com.aixforce.category.service.FrontCategoryService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台类目
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-11
 */
@Controller
@RequestMapping("/api/admin/frontCategories")
public class FrontCategories {
    private final static Logger log = LoggerFactory.getLogger(BackCategories.class);

    @Autowired
    private FrontCategoryService frontCategoryService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichCategory> list() {
        Response<List<RichCategory>> result = frontCategoryService.childrenOfNoCache(0L);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load root front categories,error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<RichCategory> childrenOf(@PathVariable("id") Long categoryId) {
        Response<List<RichCategory>> result = frontCategoryService.childrenOfNoCache(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to load sub front categories of {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FrontCategory newCategory(FrontCategory frontCategory) {
        Response<Long> result = frontCategoryService.create(frontCategory);
        if (result.isSuccess()) {
            Long id = result.getResult();
            frontCategory.setId(id);
            return frontCategory;
        } else {
            log.error("failed to create {},error code: {}", frontCategory, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String removeChild(@PathVariable("id") Long id) {

        Response<Boolean> result = frontCategoryService.delete(id);
        if (result.isSuccess()) {
            return messageSources.get("category.delete.success");
        } else {
            log.error("failed to delete front category {},error code :{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    @RequestMapping(value = "/mapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createMapping(@RequestParam("fid") Long frontCategoryId, @RequestParam("bid") Long backCategoryId,
                                @RequestParam("path") String path) {
        Response<Boolean> result = frontCategoryService.createMapping(frontCategoryId, backCategoryId, path);
        if (result.isSuccess()) {
            return messageSources.get("mapping.create.success");
        } else {
            log.error("failed to create category mapping where frontCategoryId= {},error code:{}", frontCategoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    @RequestMapping(value = "/mapping/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean removeMapping(@RequestParam("fid") Long frontCategoryId, @RequestParam("bid") Long backCategoryId,
                                @RequestParam("path") String path) {
        Response<Boolean> result = frontCategoryService.removeMapping(frontCategoryId, backCategoryId, path);
        if(!result.isSuccess()) {
            log.error("fail to remove mapping by fid={}, bid={}, path={}, error code:{}",
                    frontCategoryId, backCategoryId, path, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/{categoryId}/mapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<CategoryMapping> findByCategoryId(@PathVariable("categoryId") Long categoryId) {

        Response<List<CategoryMapping>> result = frontCategoryService.findMappingList(categoryId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to find category mapping for categoryId {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{categoryId}/mapping", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteByCategoryId(@PathVariable("categoryId") Long categoryId) {

        Response<Boolean> result = frontCategoryService.deleteMapping(categoryId);
        if (result.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to delete category mapping for categoryId {},error code :{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{categoryId}", method = RequestMethod.PUT)
    @ResponseBody
    public String updateCategory(@PathVariable("categoryId") Long categoryId,
                                 @RequestParam("name") String name) {
        Response<Boolean> result = frontCategoryService.update(categoryId, name);
        if (result.isSuccess()) {
            return messageSources.get("category update success");
        } else {
            log.error("failed to update front category{}, cause:{}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }
}
