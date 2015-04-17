package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.model.TitleKeyword;
import com.aixforce.item.service.TitleKeywordService;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: Saito
 * Date: 7/10/14
 */
@Controller
@Slf4j
@RequestMapping("/api/admin/tdk")
public class TitleKeywords {
    @Autowired
    private MessageSources messageSources;              //异常消息 placeholder support

    @Autowired
    private TitleKeywordService titleKeywordService;    //页面搜索关键字管理服务接口


    /**
     * 新增记录
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String create(@RequestBody TitleKeyword titleKeyword) {

        Response<Long> createResponse = titleKeywordService.create(titleKeyword);

        if(!createResponse.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(createResponse.getError()));
        }

        return "ok";

    }


    /**
     * 根据自增序列 id 或业务定义 nameId 删除记录
     *
     * @param id        自增序列 ID 号
     * @return          响应结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String delete(@PathVariable Long id){

        Response<Long> deleteResp = titleKeywordService.deleteById(id);

        if(!deleteResp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(deleteResp.getError()));
        }

        return "ok";

    }


    /**
     * 更新记录
     *
     * @return          响应结果
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@RequestBody TitleKeyword titleKeyword) {
        Response<Long> updateResp = titleKeywordService.update(titleKeyword);

        if(!updateResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(updateResp.getError()));

    }


    /**
     * 根据唯一ID查询结果
     *
     * @param id    自增序列ID
     * @return      唯一结果集
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TitleKeyword findById(@PathVariable Long id){

        Response<TitleKeyword> byIdResp = titleKeywordService.findById(id);
        if(!byIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byIdResp.getError()));
        return byIdResp.getResult();

    }


    /**
     * 根据 nameId 查询结果集
     *
     * @param nameId   业务定义 nameId
     * @return         查询结果集
     */
    @RequestMapping(value = "/nameId/{nameId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TitleKeyword findByNameId(@PathVariable Long nameId){

        Response<TitleKeyword> byNameIdResp = titleKeywordService.findById(nameId);
        if(!byNameIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byNameIdResp.getError()));
        return byNameIdResp.getResult();

    }
}
