/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.category.dto.AttributeDto;
import com.aixforce.category.dto.SpuWithDomain;
import com.aixforce.category.model.AttributeKey;
import com.aixforce.category.model.RichAttribute;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.AttributeService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.Brand;
import com.aixforce.item.service.BrandService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-10
 */
@Controller
@RequestMapping("/api/admin")
public class Spus {
    private final static Logger log = LoggerFactory.getLogger(Spus.class);

    @Autowired
    private SpuService spuService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private CommonConstants commonConstants;

    @Autowired
    private BrandService brandService;

    @RequestMapping(value = "/spus/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public RichSpu find(@PathVariable("id") Long spuId) {
        Response<Spu> result = spuService.findById(spuId);
        if (!result.isSuccess()) {
            log.error("find spu(id={}) failed,error code:{}", spuId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        Spu spu = result.getResult();
        List<RichAttribute> spuAttributes = attributeService.findSpuAttributesNoCacheBy(spuId);
        List<AttributeKey> skuAttributeKeys = attributeService.findSkuKeysNoCacheBy(spuId);
        List<AttributeDto> attributeDtos = createDtoFrom(spuAttributes, skuAttributeKeys);
        RichSpu richSpu = new RichSpu();
        richSpu.setSpu(spu);
        richSpu.setAttributes(attributeDtos);
        return richSpu;
    }

    //no type needed
    private List<AttributeDto> createDtoFrom(List<RichAttribute> spuAttributes, List<AttributeKey> skuAttributeKeys) {
        List<AttributeDto> attributeDtos = Lists.newArrayListWithCapacity(spuAttributes.size() + skuAttributeKeys.size());
        for (RichAttribute spuAttribute : spuAttributes) {
            AttributeDto attributeDto = new AttributeDto(spuAttribute.getAttributeKeyId(), null, spuAttribute.getAttributeValue(), false, spuAttribute.getAttributeKey());
            attributeDtos.add(attributeDto);
        }
        for (AttributeKey skuAttributeKey : skuAttributeKeys) {
            AttributeDto attributeDto = new AttributeDto(skuAttributeKey.getId(), null, null, true, skuAttributeKey.getName());
            attributeDtos.add(attributeDto);
        }
        return attributeDtos;
    }

    @RequestMapping(value = "/spus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SpuWithDomain create(@RequestParam("data") String data) {
        try {
            RichSpu richSpu = JsonMapper.nonEmptyMapper().fromJson(data, RichSpu.class);
            Spu spu = richSpu.getSpu();
            Response<Spu> result = spuService.create(spu);
            List<AttributeDto> attributes = richSpu.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                attributeService.addForSpu(spu.getId(), attributes);
            }
            SpuWithDomain spuDto = new SpuWithDomain();
            BeanMapper.copy(result.getResult(), spuDto);
            spuDto.setDomain(commonConstants.getHrefProps().getProperty("main"));
            log.info("user id={} create spu by data {}", UserUtil.getCurrentUser().getId(), data);
            return spuDto;
        } catch (Exception e) {
            log.error("failed to create spu (data={}),cause:{}", data, e);
            throw new JsonResponseException(500, messageSources.get("spu.create.fail"));
        }
    }

    @RequestMapping(value = "/spus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String update(@RequestParam("data") String data) {
        RichSpu richSpu = JsonMapper.nonEmptyMapper().fromJson(data, RichSpu.class);
        Spu spu = richSpu.getSpu();
        if (spu.getId() == null) {
            throw new JsonResponseException(500, "spu.id.not.specify");
        }
        Response<Boolean> r = spuService.update(spu.getId(), spu.getName(), spu.getBrandId());
        if (r.isSuccess()) {
            List<AttributeDto> attributes = richSpu.getAttributes();
            attributeService.addForSpu(spu.getId(), attributes);
            log.info("user id={} update spu by data {}", UserUtil.getCurrentUser().getId(), data);
        } else {
            log.error("failed to update spu name where id={},error code : {}", spu.getId(), r.getResult());
            throw new JsonResponseException(500, messageSources.get(r.getError()));
        }
        return "ok";

    }


    @RequestMapping(value = "/spus/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable("id") Long id) {
        try {
            spuService.delete(id);
            log.info("user id={} delete spu id={}", UserUtil.getCurrentUser().getId(), id);
            return messageSources.get("spu.delete.success");
        } catch (Exception e) {
            log.error("failed to delete spu (id={}),cause:{}", id, e);
            throw new JsonResponseException(500, messageSources.get("spu.delete.fail"));
        }
    }

    @RequestMapping(value = "/brands", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Brand> findAllBrands() {
        Response<List<Brand>> result = brandService.findAll();
        if(!result.isSuccess()) {
            log.error("failed to find all brands, error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/brand/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createBrand(Brand brand) {
        Response<Long> result = brandService.create(brand);
        if(!result.isSuccess()) {
            log.error("fail to create brand {}, error code:{}", brand, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/brand/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateBrand(Brand brand) {
        Response<Boolean> result = brandService.update(brand);
        if(!result.isSuccess()) {
            log.error("fail to update brand ({}), error code:{}", brand, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/spus/{spuId}/brand", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Brand findBrandBySpu(@PathVariable("spuId") Long spuId) {
        Response<Spu> spuR = spuService.findById(spuId);
        if(!spuR.isSuccess()) {
            log.error("failed to find spu by id ({}),error code:{}", spuId, spuR.getError());
            throw new JsonResponseException(500, messageSources.get(spuR.getError()));
        }
        Spu spu = spuR.getResult();
        Response<Brand> brandR = brandService.findById(Long.valueOf(spu.getBrandId()));
        if(!brandR.isSuccess()) {
            log.error("failed to  find brand by id ({}), error code:{}", spu.getBrandId(), brandR.getError());
            throw new JsonResponseException(500, messageSources.get(brandR.getError()));
        }
        return brandR.getResult();
    }

    public static class RichSpu {
        @Getter
        @Setter
        private Spu spu;

        @Getter
        @Setter
        private List<AttributeDto> attributes;
    }
}
