package com.aixforce.admin.web.controller;

import com.aixforce.admin.web.jobs.OrderJobs;
import com.aixforce.rrs.predeposit.service.PreDepositService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.trade.service.OrderWriteService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Created by yangzefeng on 14-2-26
 */
@Controller
@RequestMapping("/api/orders/jobs")
public class OrderJobsController {

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private OrderJobs orderJobs;

    @Autowired
    private PreDepositService preDepositService;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @RequestMapping(value = "/orderExpire/{fromDate}", method = RequestMethod.GET)
    @ResponseBody
    public void orderExpire(@PathVariable("fromDate") String fromDate) {
        DateTime dateTime = new DateTime(fromDate);
        Date date = dateTime.toDate();
        orderWriteService.verifyOrderExpire(date);
    }

    @RequestMapping(value = "/orderNotPaidExpire", method = RequestMethod.GET)
    @ResponseBody
    public void orderNotPaidExpire() {
        orderWriteService.verifyOrderNotPaidExpire(new Date());
    }

    @RequestMapping(value = "/orderNotConfirmDeliverExpire", method = RequestMethod.GET)
    @ResponseBody
    public void orderNotConfirmDeliverExpire() {
        orderJobs.orderNotConfirmDeliverExpire();
    }

    @RequestMapping(value = "/orderItemNotConfirmRefundExpire", method = RequestMethod.GET)
    @ResponseBody
    public void orderItemNotConfirmRefundExpire() {
        orderJobs.orderItemNotConfirmRefundExpire();
    }

    @RequestMapping(value = "/preSaleExpire", method = RequestMethod.GET)
    @ResponseBody
    public void preSaleExpire() {
        preSaleService.verifyPreSaleExpire();
    }

    @RequestMapping(value = "/preSaleOrderExpire", method = RequestMethod.GET)
    @ResponseBody
    public void preSaleOrderExpire() {
        preSaleService.verifyPreSaleOrderExpire();
    }

    @RequestMapping(value = "/preDepositExpire", method = RequestMethod.GET)
    @ResponseBody
    public void preDepositExpire() {
        preDepositService.verifyPreDepositExpire();
    }

    @RequestMapping(value = "/orderOnTrialTimeOut", method = RequestMethod.GET)
    @ResponseBody
    public void orderOnTrialTimeOut() {
        orderJobs.orderOnTrialTimeOut();
    }
}
