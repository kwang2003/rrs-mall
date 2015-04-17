package com.aixforce.admin.web.controller;

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
@RequestMapping("/api/admin/logistics")
public class AdminLogistics {

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private LogisticsInfoService logisticsInfoService;

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

        Response<LogisticsInfo> result = logisticsInfoService.findByOrderId(orderId);

        if (!result.isSuccess()) {
            log.error("fail to get logistics information where orderId={},error code:{}", orderId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();

    }
}
