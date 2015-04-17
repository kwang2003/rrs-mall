/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.site.model.Component;
import com.aixforce.site.model.ComponentCategory;
import com.aixforce.site.service.ComponentService;
import com.aixforce.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Desc:
 * Author: dimzfw@gmail.com
 * Date: 8/25/12 4:02 PM
 */
@Controller
@RequestMapping("/api/admin/components")
public class Components {
    private final Logger log = LoggerFactory.getLogger(Components.class);
    @Autowired
    private ComponentService componentService;
    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Component> list() {
        Response<List<Component>> result = componentService.all();
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to list all components,error code:{}", result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));

    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(Component component, @RequestParam("category") String category) {
        component.setCategory(ComponentCategory.valueOf(category.toUpperCase()));
        Response<Long> result = componentService.create(component);
        if (result.isSuccess()) {
            return result.getResult();
        }
        log.error("failed to create {},error code:{}", component, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@PathVariable("id") Long id, Component component) {
        component.setId(id);
        Response<Boolean> result = componentService.update(component);
        if (result.isSuccess()) {
            return "ok";
        }
        log.error("failed to update {},error code:{}", component, result.getError());
        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Component view(@PathVariable("id") Long id) {
        Response<Component> result = componentService.findById(id);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to find component where id= {},error code:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable("id") Long id) {
        Response<Boolean> result = componentService.delete(id);
        if (result.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to delete component where id={},error code:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/{id}/edit", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Component edit(@PathVariable("id") Long id) {
        Response<Component> result = componentService.findById(id);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to find component where id= {},error code:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }
}
