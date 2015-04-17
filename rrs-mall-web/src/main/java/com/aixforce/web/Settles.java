package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 结算
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-26 12:19 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/seller/settle")
public class Settles {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private MessageSources messageSources;


    @RequestMapping(value = "/{id}/confirm", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String done(@PathVariable(value = "id") Long id) {
        Response<Boolean> result = settlementService.confirmed(id, UserUtil.getUserId());
        if (!result.isSuccess() || result.getResult() != Boolean.TRUE) {
            log.error("fail to confirm settlement(id={}),code={}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }


}
