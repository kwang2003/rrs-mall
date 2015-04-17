package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.service.OrderAlipayCashService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-02 3:43 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/admin/cashes")
public class Cashes {

    @Autowired
    private OrderAlipayCashService orderAlipayCashService;

    @Autowired
    private MessageSources messageSources;

    private Splitter splitter = Splitter.on(",");


    @RequestMapping(value = "/{id}/cashed", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String orderCashed(@PathVariable("id") Long id) {

        Response<Boolean> result = orderAlipayCashService.cashing(id, UserUtil.getCurrentUser());
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    @RequestMapping(value = "/batch/cashed", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String orderCashed(@RequestParam("ids") String ids) {
        try {
            checkArgument(notEmpty(ids), "ids.can.not.be.empty");
            List<String> idList = splitter.splitToList(ids);
            List<Long> cashingIds = convertToLong(idList);
            Response<Boolean> result = orderAlipayCashService.batchCashing(cashingIds, UserUtil.getCurrentUser());
            checkState(result.isSuccess(), result.getError());
            return "ok";

        } catch (IllegalArgumentException e) {
            log.error("fail to batch cashing with ids:{}, error:{}", ids, e.getMessage());
            throw new JsonResponseException(500, e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to batch cashing with ids:{}, error:{}", ids, e.getMessage());
            throw new JsonResponseException(500, e.getMessage());
        } catch (Exception e) {
            log.error("fail to batch cashing with ids:{}, cause:{}", ids, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "出错了");
        }
    }

    private List<Long> convertToLong(List<String> ids) {
        List<Long> result = Lists.newArrayListWithCapacity(ids.size());
        for (String id : ids) {
            result.add(Long.parseLong(id));
        }
        return result;
    }
}
