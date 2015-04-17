/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.category.model.AttributeKey;
import com.aixforce.category.model.AttributeValue;
import com.aixforce.category.service.AttributeService;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-03
 */
@Controller
@RequestMapping("/api/admin")
public class Attributes {
    private final static Logger log = LoggerFactory.getLogger(Attributes.class);
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/categories/{categoryId}/keys", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<AttributeKey> loadKeys(@PathVariable("categoryId") Long categoryId) {
        try {
            return attributeService.findCategoryAttributeKeysNoCacheBy(categoryId);
        } catch (Exception e) {
            log.error("failed to query attributeKeys for {},cause:{}", categoryId, e);
            throw new JsonResponseException(500, messageSources.get("attribute.category.key.query.fail"));
        }
    }

    @RequestMapping(value = "/categories/{categoryId}/keys/{attributeKeyId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteCategoryAttributeKey(@PathVariable("categoryId") Long categoryId, @PathVariable("attributeKeyId") Long attributeKeyId) {
        try {
            attributeService.deleteCategoryAttributeKey(categoryId, attributeKeyId);
            return messageSources.get("attribute.category.key.delete.success");
        } catch (Exception e) {
            log.error("failed to delete categoryAttributeKey(categoryId={},attributeKeyId={}),cause:{}",
                    categoryId, attributeKeyId, Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
            throw new JsonResponseException(500, messageSources.get("attribute.category.key.delete.fail"));
        }
    }

    @RequestMapping(value = "/categories/{categoryId}/keys/{attributeKeyId}/values", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<AttributeValue> loadValues(@PathVariable("categoryId") Long categoryId, @PathVariable("attributeKeyId") Long attributeKeyId) {
        try {
            return attributeService.findCategoryAttributeValuesNoCacheBy(categoryId, attributeKeyId);
        } catch (Exception e) {
            log.error("failed to query attributeValues for categoryId={} && attributeKeyId={},cause:{}",
                    categoryId, attributeKeyId, Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
            throw new JsonResponseException(500, messageSources.get("attribute.category.value.query.fail"));
        }
    }

    @RequestMapping(value = "/categories/{categoryId}/keys/{attributeKeyId}/values/{attributeValueId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteCategoryAttributeValue(@PathVariable("categoryId") Long categoryId,
                                               @PathVariable("attributeKeyId") Long attributeKeyId, @PathVariable("attributeValueId") Long attributeValueId) {
        try {
            attributeService.deleteCategoryAttributeValue(categoryId, attributeKeyId, attributeValueId);
            return messageSources.get("attribute.category.value.delete.success");
        } catch (Exception e) {
            log.error("failed to delete categoryAttributeValue(categoryId={},attributeKeyId={},attributeValueId={}),cause:{}",
                    categoryId, attributeKeyId, attributeValueId, Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
            throw new JsonResponseException(500, messageSources.get("attribute.category.value.delete.fail"));
        }
    }


    @RequestMapping(value = "/categories/{categoryId}/keys", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AttributeKey addCategoryAttributeKey(@PathVariable("categoryId") Long categoryId, @RequestParam("name") String attributeKeyName,
                                                @RequestParam(value = "valueType", defaultValue = "1") Integer valueType) {


        Response<AttributeKey> result = attributeService.addCategoryAttributeKey(categoryId, attributeKeyName,
                AttributeKey.ValueType.fromNumber(valueType));
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to add categoryAttributeKeys for categoryId={} && attributeKeyName={},error code:{}",
                    categoryId, attributeKeyName, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

    }

    @RequestMapping(value = "/categories/{categoryId}/keys/{attributeKeyId}/values", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AttributeValue addCategoryAttributeValues(@PathVariable("categoryId") Long categoryId, @PathVariable("attributeKeyId") Long attributeKeyId,
                                                     @RequestParam("data") String attributeValue) {
        Response<AttributeValue> result = attributeService.addCategoryAttributeValue(categoryId, attributeKeyId, attributeValue);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            log.error("failed to add categoryAttributeValues for categoryId={} && attributeKeyId={},error code:{}",
                    categoryId, attributeKeyId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }
}
