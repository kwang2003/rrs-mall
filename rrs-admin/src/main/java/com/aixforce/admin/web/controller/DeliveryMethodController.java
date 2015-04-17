package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.DeliveryMethod;
import com.aixforce.trade.service.DeliveryMethodService;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by yangzefeng on 14-9-3
 */
@Controller @Slf4j
@RequestMapping("/api/admin")
public class DeliveryMethodController {

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private MessageSources messageSources;


    @RequestMapping(value = "/deliveryMethods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DeliveryMethod> find() {
        Response<List<DeliveryMethod>> result = deliveryMethodService.findBy(null, null);
        if(!result.isSuccess()) {
            log.error("fail to find delivery methods without params, error code:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/deliveryMethod", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(DeliveryMethod deliveryMethod) {
        Response<Long> result = deliveryMethodService.create(deliveryMethod);
        if(!result.isSuccess()) {
            log.error("fail to create delivery method by {}, error code:{}",deliveryMethod, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/deliveryMethod", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(DeliveryMethod deliveryMethod) {
        Response<Boolean> result = deliveryMethodService.update(deliveryMethod);
        if(!result.isSuccess()) {
            log.error("fail to update delivery method by {}, error code:{}", deliveryMethod, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    @RequestMapping(value = "/deliveryMethod/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateStatus(@RequestParam("id") Long id,
                             @RequestParam("status") Integer status) {
        Response<Boolean> result = deliveryMethodService.updateStatus(id, status);
        if(!result.isSuccess()) {
            log.error("fail to update delivery method status by id={}, status={}, error code:{}",
                    id, status, result.getError());
        }
    }
}
