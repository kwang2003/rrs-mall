package com.aixforce.admin.web.controller;

import com.aixforce.category.service.CategorySyncService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-04-08
 */
@Controller
@RequestMapping("/api/admin/forest")
public class Forests {

    private final static Logger log = LoggerFactory.getLogger(Forests.class);

    private final CategorySyncService categorySyncService;

    @Autowired
    public Forests(CategorySyncService categorySyncService) {
        this.categorySyncService = categorySyncService;
    }

    @RequestMapping(value = "/backSync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String backSync() {
        Response<Boolean> result = categorySyncService.publish("back");
        if (result.isSuccess()) {
            log.info("send backSync change signal");
            return "ok";
        } else {
            log.error("failed to sync back category, error code:{}", result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }

    @RequestMapping(value = "/frontSync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String frontSync() {
        Response<Boolean> result = categorySyncService.publish("front");
        if (result.isSuccess()) {
            log.info("send frontSync change signal");
            return "ok";
        } else {
            log.error("failed to sync front category, error code:{}", result.getError());
            throw new JsonResponseException(500, result.getError());
        }
    }
}
