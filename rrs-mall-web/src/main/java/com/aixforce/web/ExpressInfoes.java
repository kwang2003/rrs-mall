package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.trade.service.ExpressInfoService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.List;

/**
 * 快递信息控制器
 * Author: haolin
 * On: 9/22/14
 */
@Controller
@RequestMapping("/api/expresses")
public class ExpressInfoes {

    @Autowired
    private ExpressInfoService expressInfoService;

    @Autowired
    private MessageSources messageSources;

    /**
     * 获取所有可用的快递信息
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Collection<ExpressInfo> expresses(){
        Response<Collection<ExpressInfo>> resp = expressInfoService.listEnables(UserUtil.getCurrentUser());
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 获取用户常用的快递列表
     */
    @RequestMapping(value = "/usual", method = RequestMethod.GET)
    @ResponseBody
    public List<ExpressInfo> usual(){
        Response<List<ExpressInfo>> resp = expressInfoService.usual(UserUtil.getCurrentUser());
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 为用户添加常用快递
     * @param id 快递信息id
     */
    @RequestMapping(value = "/usual", method = RequestMethod.POST)
    @ResponseBody
    public Boolean add2Usual(Long id){
        Response<Boolean> resp = expressInfoService.add2Usual(UserUtil.getUserId(), id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 为用户添加常用快递
     * @param id 快递信息id
     */
    @RequestMapping(value = "/usual", method = RequestMethod.DELETE)
    @ResponseBody
    public Boolean rmFromUsual(Long id){
        Response<Boolean> resp = expressInfoService.rmFromUsual(UserUtil.getUserId(), id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }
}
