package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.service.BuyingActivityDefinitionService;
import com.aixforce.rrs.buying.service.BuyingItemService;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Controller
@Slf4j
@RequestMapping("/api/admin/buying")
public class AdminBuyingActivitys {
    @Autowired
    private MessageSources messageSources;              //异常消息 placeholder support

    @Autowired
    private BuyingActivityDefinitionService buyingActivityDefinitionService; //抢购活动管理服务接口

    @Autowired
    private BuyingItemService buyingItemService;


    /**
     * 更新状态 终止活动
     *
     * @return          响应结果
     */
    @RequestMapping(value = "/update",method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateStatus(@RequestParam("id") Long id) {
        BuyingActivityDefinition buyingActivity = new BuyingActivityDefinition();
        buyingActivity.setId(id);			
        buyingActivity.setStatus(BuyingActivityDefinition.Status.FINISHED.value());
        Response<Boolean> updateResp = buyingActivityDefinitionService.update(buyingActivity);

        if(!updateResp.isSuccess()){
        							
            throw new JsonResponseException(500, messageSources.get(updateResp.getError()));
        }

        return updateResp.getResult();

    }

    /**
     * 根据唯一ID查询参加活动商品结果
     *
     * @param id    自增序列ID
     * @return      唯一结果集
     */
    @RequestMapping(value = "/items/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BuyingItem> findByActivityId(@PathVariable Long id){

        Response<List<BuyingItem>> byIdResp = buyingItemService.findByActivityId(id);
        if(!byIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byIdResp.getError()));
        return byIdResp.getResult();

    }
}
