package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.purify.dto.PurifyAssemblyDto;
import com.aixforce.rrs.purify.model.PurifyAssembly;
import com.aixforce.rrs.purify.model.PurifyCategory;
import com.aixforce.rrs.purify.model.PurifySeries;
import com.aixforce.rrs.purify.service.PurifyAssemblyService;
import com.aixforce.rrs.purify.service.PurifyCategoryService;
import com.aixforce.rrs.purify.service.PurifySeriesService;
import com.aixforce.web.misc.MessageSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Desc:定制的管理后台调用（admin or siteUser）
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-11.
 */
@Controller
@RequestMapping("api/purify")
public class PurifyController {
    private final static Logger log = LoggerFactory.getLogger(PurifyController.class);

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private PurifySeriesService purifySeriesService;

    @Autowired
    private PurifyCategoryService purifyCategoryService;

    @Autowired
    private PurifyAssemblyService purifyAssemblyService;

    /**
     * 创建系列信息
     * @param purifySeries  系列对象
     * @return String
     * 返回创建结果
     */
    @RequestMapping(value = "/series/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createSeries(@RequestBody PurifySeries purifySeries){
        //创建系列
        Response<Boolean> result = purifySeriesService.createPurifySeries(purifySeries);

        if (!result.isSuccess()) {
            log.error("fail to create series error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 更新系列信息
     * @param purifySeries  系列对象
     * @return String
     * 返回更新结果
     */
    @RequestMapping(value = "/series/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateSeries(@RequestBody PurifySeries purifySeries){
        Response<Boolean> result = purifySeriesService.updatePurifySeries(purifySeries);

        if (!result.isSuccess()) {
            log.error("fail to update series error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 删除系列信息
     * @param seriesId  系列编号
     * @return String
     * 返回删除结果
     */
    @RequestMapping(value = "/series/{id}/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteSeries(@PathVariable("id") Long seriesId){
        Response<Boolean> result = purifySeriesService.deletePurifySeries(seriesId);

        if (!result.isSuccess()) {
            log.error("fail to delete series error seriesId={} code={}", seriesId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 查询系列信息
     * @param siteId  站点编号
     * @return List
     * 返回查询结果
     */
    @RequestMapping(value = "/series/{id}/find", method = RequestMethod.GET)
    @ResponseBody
    public List<PurifySeries> findSeries(@PathVariable("id") Long siteId){
        Response<List<PurifySeries>> result = purifySeriesService.findSiteSeries(siteId);

        if (!result.isSuccess()) {
            log.error("fail to find series error siteId={} code={}", siteId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }

    /**
     * 创建类目对象信息
     * @param purifyCategory    类目对象
     * @return  String
     * 返回操作结果
     */
    @RequestMapping(value = "/category/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createCategory(@RequestBody PurifyCategory purifyCategory){
        //创建类目
        Response<Boolean> result = purifyCategoryService.createCategory(purifyCategory);

        if(!result.isSuccess()){
            log.error("fail to create category error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 类目对象更改信息
     * @param purifyCategory    类目对象
     * @return  String
     * 返回操作结果
     */
    @RequestMapping(value = "/category/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateCategory(@RequestBody PurifyCategory purifyCategory){
        //更新类目
        Response<Boolean> result = purifyCategoryService.updateCategory(purifyCategory);

        if(!result.isSuccess()){
            log.error("fail to update category error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 删除类目对象信息
     * @param categoryId    类目编号
     * @return  String
     * 返回操作结果
     */
    @RequestMapping(value = "/category/{id}/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteCategory(@PathVariable("id") Long categoryId){
        //删除类目
        Response<Boolean> result = purifyCategoryService.deleteCategory(categoryId);

        if(!result.isSuccess()){
            log.error("fail to delete category categoryId={} error code={}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 查询类目对象信息
     * @param seriesId    系列编号
     * @return  List
     * 返回查询结果
     */
    @RequestMapping(value = "/category/{id}/find", method = RequestMethod.GET)
    @ResponseBody
    public List<PurifyCategory> findCategory(@PathVariable("id") Long seriesId){
        Response<List<PurifyCategory>> result = purifyCategoryService.findBySeriesId(seriesId);

        if(!result.isSuccess()){
            log.error("fail to find category seriesId={} error code={}", seriesId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }

    /**
     * 创建定制组件对象
     * @param purifyAssemblyDto     定制组件对象
     * @return String
     * 返回操作结果
     */
    @RequestMapping(value = "/assembly/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createAssembly(@RequestBody PurifyAssemblyDto purifyAssemblyDto){
        //创建定制组件&绑定组件之间的关系
        Response<Boolean> result = purifyAssemblyService.createAssembly(purifyAssemblyDto.getParentId(),
                purifyAssemblyDto, purifyAssemblyDto.getProductId());

        if (!result.isSuccess()) {
            log.error("fail to create assembly error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 更新定制组件对象
     * @param purifyAssembly     定制组件对象
     * @return String
     * 返回操作结果
     */
    @RequestMapping(value = "/assembly/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateAssembly(@RequestBody PurifyAssembly purifyAssembly){
        //更新定制组件对象
        Response<Boolean> result = purifyAssemblyService.updateAssembly(purifyAssembly);

        if (!result.isSuccess()) {
            log.error("fail to update assembly error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 删除定制组件对象
     * @param assemblyId     定制组件编号
     * @return String
     * 返回操作结果
     */
    @RequestMapping(value = "/assembly/{id}/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteAssembly(@PathVariable("id") Long assemblyId){
        //删除定制组件对象
        Response<Boolean> result = purifyAssemblyService.deleteAssembly(assemblyId);

        if (!result.isSuccess()) {
            log.error("fail to delete assembly assemblyId={} error code={}", assemblyId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /**
     * 查询定制组件对象
     * @param categoryId     类目编号
     * @return List
     * 返回查询结果
     */
    @RequestMapping(value = "/assembly/{id}/find", method = RequestMethod.GET)
    @ResponseBody
    public List<PurifyAssembly> findAssembly(@PathVariable("id") Long categoryId){
        Response<List<PurifyAssembly>> result = purifyAssemblyService.findByCategory(categoryId);

        if (!result.isSuccess()) {
            log.error("fail to find assembly categoryId={} error code={}", categoryId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }
}
