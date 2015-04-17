package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.InstallInfo;
import com.aixforce.trade.service.InstallInfoService;
import com.aixforce.web.misc.MessageSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

/**
 * 安装信息控制器
 * Author: haolin
 * On: 9/22/14
 */
@Controller
@RequestMapping("/api/installs")
public class InstallInfoes {

    @Autowired
    private InstallInfoService installInfoService;

    @Autowired
    private MessageSources messageSources;

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
