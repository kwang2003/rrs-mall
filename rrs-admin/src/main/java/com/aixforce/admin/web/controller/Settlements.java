package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.dto.FatSettlement;
import com.aixforce.rrs.settle.service.SellerSettlementService;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.aixforce.common.utils.Arguments.isEmpty;
import static com.google.common.base.Preconditions.checkState;


/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-16 4:57 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/admin/settlements")
public class Settlements {

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SellerSettlementService sellerSettlementService;
    @Autowired
    private MessageSources messageSources;


    @RequestMapping(value = "/load", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<FatSettlement> load(@RequestParam("sellerName") String sellerName,
                                 @RequestParam("confirmedAt") String confirmedAt,
                                 @RequestParam("pageNo") Integer pageNo,
                                 @RequestParam("size") Integer size) {

        try {
            if (isEmpty(sellerName) || isEmpty(confirmedAt)) {   // 若传入的商户和确认时间为空，则返回空列表
                return Paging.empty(FatSettlement.class);
            }

            Response<Paging<FatSettlement>> result = settlementService
                    .findValidBy(sellerName, null, null, null, null, null, confirmedAt,
                            null, null, null, pageNo, size, UserUtil.getCurrentUser());

            checkState(result.isSuccess(), result.getError());
            return result.getResult();

        } catch (IllegalStateException e) {
            log.error("fail to load settlements with sellerName:{}, confirmedAt:{}, pageNo:{}, size:{}, error:{}",
                    sellerName, confirmedAt, pageNo, size, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to load settlements with sellerName:{}, confirmedAt:{}, pageNo:{}, size:{}, error:{}",
                    sellerName, confirmedAt, pageNo, size, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("settlement.query.fail"));
        }
    }

    @RequestMapping(value = "/{id}/print", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String print(@PathVariable("id") Long id) {

        try {
            Response<Boolean> printResult = sellerSettlementService.printing(id, UserUtil.getCurrentUser());
            checkState(printResult.isSuccess(), printResult.getError());
            return "ok";

        } catch (IllegalStateException e) {
            log.error("fail to print sellerSettlement with id:{}, error:{}", id, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to print sellerSettlement with id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("seller.settlement.print.fail"));
        }
    }

}
