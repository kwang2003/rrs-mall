package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.LogisticsInfo;
import com.aixforce.trade.service.LogisticsInfoService;
import com.aixforce.trade.service.LogisticsRevertService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Description：
 * Author：Guo Chaopeng
 * Created on 14-4-22-下午2:04
 */
@Controller
@Slf4j
@RequestMapping("/api/logistics")
public class Logistics {

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private LogisticsInfoService logisticsInfoService;

    @Autowired
    private LogisticsRevertService logisticsRevertService;

    /**
     * 录入物流信息
     *
     * @param logisticsInfo 物流信息
     * @return 录入是否成功
     */
    @RequestMapping(value = "/record", method = RequestMethod.POST)
    @ResponseBody
    public String recordLogisticsInfo(LogisticsInfo logisticsInfo) {

        BaseUser baseUser = UserUtil.getCurrentUser();

        if (baseUser == null) {
            log.error("user not login");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        logisticsInfo.setSenderId(baseUser.getId());
        logisticsInfo.setSenderName(baseUser.getName());
        logisticsInfo.setLogisticsStatus(LogisticsInfo.Status.SEND.value());

        Response<Long> result = logisticsInfoService.create(logisticsInfo);

        if (!result.isSuccess()) {
            log.error("failed to create {},error code:{}", logisticsInfo, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }


    /**
     * 根据orderId更新物流信息
     *
     * @param logisticsInfo 要更新的物流信息
     * @return 更新是否成功
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public String update(LogisticsInfo logisticsInfo) {


        BaseUser baseUser = UserUtil.getCurrentUser();

        if (baseUser == null) {
            log.error("user not login");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        Response<Boolean> result = logisticsInfoService.update(logisticsInfo, baseUser.getId());

        if (!result.isSuccess()) {
            log.error("failed to update {},error code:{}", logisticsInfo, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }


    /**
     * 根据orderId获取物流信息
     *
     * @param orderId 订单编号
     * @return 物流信息
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public LogisticsInfo getLogisticsInfo(@RequestParam("orderId") Long orderId) {
        Long userId = UserUtil.getUserId();

        if (userId == null) {
            log.error("user not login");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        Response<LogisticsInfo> result = logisticsInfoService.findByOrderId(orderId, userId);

        if (!result.isSuccess()) {
            log.error("fail to get logistics information where orderId={},error code:{}", orderId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();

    }
}
