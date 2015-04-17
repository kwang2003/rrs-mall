package com.aixforce.restful.controller.tool;

import com.aixforce.trade.service.OrderWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-07 4:24 PM  <br>
 * Author: xiao
 */

@Slf4j
@Controller
@RequestMapping("/api/tool/orders")
public class OrderTools {


    @Autowired
    private OrderWriteService orderWriteService;

    @RequestMapping(value = "/{id}/expired", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String items(@PathVariable("id") Long id) {
        try {
            orderWriteService.expireOrder(id);
        } catch (Exception e) {
            log.error("fail to expire order(id:{})", id);
        }

        return "ok";
    }





}
