package com.aixforce.restful.controller.tool;

import com.aixforce.rrs.jde.Jde;
import com.aixforce.rrs.settle.service.SettlementJobService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-12 1:11 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/admin/tool")
public class Jdes {

    private DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private SettlementJobService settlementJobService;


    @RequestMapping(value = "/data/{type}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String render(@PathVariable Character type) {
        String xml;
        switch (type) {
            case '1':
                Jde sellerEarning = Jde.sellerEarning(112L, "88888888", 10000L, new Date(), 1L);
                xml = sellerEarning.toXml();
                break;
            case '2':
                Jde commissionAndThird = Jde.commissionAndThird(112L, "88888888", 10000L, 500L, new Date(), 1L);
                xml = commissionAndThird.toXml();
                break;
            case '3':
                Jde score = Jde.score(112L, "88888888", 10000L, new Date(), 1L);
                xml = score.toXml();
                break;
            case '4':
                Jde presell = Jde.presell(112L, "88888888", 10000L, 100L, new Date(), 1L);
                xml = presell.toXml();
                break;
            case '5':
                Jde depositRefund = Jde.depositRefund(112L, "88888888", 10000L, new Date(), 1L);
                xml = depositRefund.toXml();
                break;
            case '6':
                Jde paymentRefund = Jde.paymentRefund(112L, "88888888", 10000L, new Date(), 1L);
                xml = paymentRefund.toXml();
                break;
            case '7':
                Jde depositPay = Jde.depositPay(112L, "88888888", 10000L, new Date(), 1L, 2);
                xml = depositPay.toXml();
                break;
            case '8':
                Jde techFeeOrder = Jde.techFeeOrder(112L, "88888888", 10000L, new Date(), 1L, 1);
                xml = techFeeOrder.toXml();
                break;

            case '9':
                Jde techFeeSettlement = Jde.techFeeSettlement(112L, "88888888", 10000L, new Date(), 1L, 2);
                xml = techFeeSettlement.toXml();
                break;

            case 'a':
                Jde alipayCash = Jde.alipayCash(112L, "88888888", 10000L, new Date(), 1L);
                xml = alipayCash.toXml();
                break;
            default:
                return "fail";
        }

        return xml;
    }



    @RequestMapping(value = "/vouch", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String vouch(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.updateVoucher(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/job", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String jobs(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.createJobs(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String summary(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.summary(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/sync", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sync(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.syncToJde(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/fee", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String fee(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.updateAlipayFee(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/cash", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String cash(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.summaryAlipayCash(doneAt);
        return "ok";
    }


    @RequestMapping(value = "/finish", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String finish(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.markedOrderAsFinished(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/settle", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String settle(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.settle(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/rate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateRate(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.updateRate(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/confirm", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String confirm(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.autoConfirmed(doneAt);
        return "ok";
    }


    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateOuterCode(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.updateOuterCode(doneAt);
        return "ok";
    }


    @RequestMapping(value = "/code/full", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateOuterCodeFully(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.updateOuterCodeFully(doneAt);
        return "ok";
    }

    @RequestMapping(value = "/fix", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String fixSettlements(String date) {
        Date doneAt = DFT.parseDateTime(date).toDate();
        settlementJobService.fix(doneAt);
        return "ok";
    }
}
