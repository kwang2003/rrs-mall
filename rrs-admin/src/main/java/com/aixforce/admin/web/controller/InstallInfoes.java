package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.InstallInfo;
import com.aixforce.trade.service.InstallInfoService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 安装信息控制器
 * Author: haolin
 * On: 9/22/14
 */
@Controller
@RequestMapping("/api/admin/installs")
public class InstallInfoes {

    @Autowired
    private InstallInfoService installInfoService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public Long create(InstallInfo installInfo){
        Response<Long> resp = installInfoService.create(installInfo);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return installInfo.getId();
    }

    /**
     * 判断安装名称是否已存在
     * @param name 安装名称
     * @return 存在返回true, 反之false
     */
    @RequestMapping(value = "/exist", method = RequestMethod.GET)
    @ResponseBody
    public Boolean existByName(String name){
        Response<InstallInfo> resp = installInfoService.findByName(name);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        InstallInfo existed = resp.getResult();
        if (existed == null) {
            return Boolean.FALSE;
        }
        // 被逻辑删除
        if (Objects.equal(InstallInfo.Status.DELETED.value(), existed.getStatus())){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
    public Boolean update(InstallInfo installInfo){
        Response<Boolean> resp = installInfoService.update(installInfo);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Boolean delete(@PathVariable("id") Long id){
        Response<Boolean> resp = installInfoService.delete(id);
        if (!resp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 获取启用的安装信息列表
     * @param type 安装类型
     * @return 启用的安装信息列表
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public List<InstallInfo> listEnables(Integer type){
        Response<List<InstallInfo>> installInfoesResp = installInfoService.listEnables(type);
        if (!installInfoesResp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(installInfoesResp.getError()));
        }
        return installInfoesResp.getResult();
    }
}
