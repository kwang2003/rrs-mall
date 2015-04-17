package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.trade.service.ExpressInfoService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 快递信息控制器
 * Author: haolin
 * On: 9/22/14
 */
@Controller
@RequestMapping("/api/admin/expresses")
public class ExpressInfoes {

    @Autowired
    private ExpressInfoService expressInfoService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public Long create(ExpressInfo expressInfo){
        Response<Long> resp = expressInfoService.create(expressInfo);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return expressInfo.getId();
    }

    /**
     * 判断快递名称是否已存在
     * @param name 快递名称
     * @return 存在返回true, 反之false
     */
    @RequestMapping(value = "/exist", method = RequestMethod.GET)
    @ResponseBody
    public Boolean existByName(String name){
        Response<ExpressInfo> resp = expressInfoService.findByName(name);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        ExpressInfo existed = resp.getResult();
        if (existed == null) {
            return Boolean.FALSE;
        }
        // 被逻辑删除
        if (Objects.equal(ExpressInfo.Status.DELETED.value(), existed.getStatus())){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
    public Boolean update(ExpressInfo expressInfo){
        Response<Boolean> resp = expressInfoService.update(expressInfo);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Boolean delete(@PathVariable("id") Long id){
        Response<Boolean> resp = expressInfoService.delete(id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 获取某用户常用的快递列表
     */
    @RequestMapping(value = "/usual/{sellerId}", method = RequestMethod.GET)
    @ResponseBody
    public List<ExpressInfo> usualOfUser(@PathVariable Long sellerId){
        BaseUser user = new BaseUser();
        user.setId(sellerId);
        Response<List<ExpressInfo>> resp = expressInfoService.usual(user);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }
}
