package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.UserVatInvoice;
import com.aixforce.trade.service.UserVatInvoiceService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.google.common.base.Preconditions.checkState;

/**
 * 发票相关
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-05 5:58 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/invoice")
public class Invoices {

    @Autowired
    private UserVatInvoiceService userVatInvoiceService;

    @Autowired
    private MessageSources messageSources;

    /**
     * 创建增值税发票定义
     * @param userVatInvoice    增值税发票定义
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(UserVatInvoice userVatInvoice) {

        try {
            Response<Long> result = userVatInvoiceService.create(userVatInvoice, UserUtil.getCurrentUser());
            checkState(result.isSuccess(), result.getError());
            return result.getResult();
        } catch (IllegalStateException e) {
            log.error("fail to create {} , error:{}", userVatInvoice, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to create {} , cause:{}", userVatInvoice, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("user.vat.invoice.create.fail"));
        }
    }


    /**
     * 更新增值税发票定义
     * @param userVatInvoice    增值税发票定义
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean update(UserVatInvoice userVatInvoice) {

        try {
            Response<Boolean> result = userVatInvoiceService.update(userVatInvoice, UserUtil.getCurrentUser());
            checkState(result.isSuccess(), result.getError());
            return result.getResult();
        } catch (IllegalStateException e) {
            log.error("fail to update {} , error:{}", userVatInvoice, e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to update {} , cause:{}", userVatInvoice, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("user.vat.invoice.update.fail"));
        }
    }


    /**
     * 获取当前用户的增值税发票
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserVatInvoice get() {
        try {
            Response<UserVatInvoice> result = userVatInvoiceService.getByUser(UserUtil.getCurrentUser());
            checkState(result.isSuccess(), result.getError());
            return result.getResult();
        } catch (IllegalStateException e) {
            log.error("fail to get by {} , error:{}", UserUtil.getCurrentUser(), e.getMessage());
            throw new JsonResponseException(500, messageSources.get(e.getMessage()));
        } catch (Exception e) {
            log.error("fail to get by {} , cause:{}", UserUtil.getCurrentUser(), Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("user.vat.invoice.query.fail"));
        }
    }

}
