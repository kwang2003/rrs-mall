package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-01 12:11 PM  <br>
 * Author: xiao
 */
@Slf4j
@Controller
@RequestMapping("/api/data")
public class DataFixes {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private SettlementService settlementService;

    private static final DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");



    /**
     * 因买家误操作关闭的订单，需要进行数据补正的操作
     *
     * @param id                订单id
     * @param paymentCode       支付宝交易号
     */
    @RequestMapping(value = "/{id}/fix", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String fixOrder(@PathVariable("id") Long id,
                         @RequestParam("paymentCode") String paymentCode,
                         @RequestParam("paidDate") String paidDate) {
        try {
            checkArgument(notEmpty(paymentCode), "payment.code.can.not.be.empty");

            Response<Order> orderQueryResult = orderQueryService.findById(id);
            checkState(orderQueryResult.isSuccess(), orderQueryResult.getError());

            Order order = orderQueryResult.getResult();

            Order updating = new Order();
            updating.setId(order.getId());
            updating.setStatus(Order.Status.PAID.value());
            updating.setPaymentCode(paymentCode);
            updating.setPaidAt(DFT.parseDateTime(paidDate).toDate());

            Response<List<OrderItem>> orderItemsQueryResult = orderQueryService.findOrderItemByOrderId(id);
            checkState(orderItemsQueryResult.isSuccess(), orderItemsQueryResult.getError());

            List<OrderItem> orderItems = orderItemsQueryResult.getResult();
            List<OrderItem> ois = Lists.newArrayListWithCapacity(orderItems.size());


            for (OrderItem orderItem : orderItems) {
                OrderItem oiUpdating = new OrderItem();
                oiUpdating.setId(orderItem.getId());
                oiUpdating.setStatus(OrderItem.Status.PAYED.value());
                oiUpdating.setPaymentCode(paymentCode);
                ois.add(oiUpdating);
            }

            Response<Boolean> updateResult =  orderWriteService.update4Fix(updating, ois);
            checkState(updateResult.isSuccess(), updateResult.getError());

            Response<Long> settleResult = settlementService.generate(id);
            checkState(settleResult.isSuccess(), settleResult.getError());

            return "ok";
        } catch (IllegalStateException e) {
            log.error("failed to fix data with id:{}, paymentCode:{}, error:{}", id, paymentCode, e.getMessage());
            throw new JsonResponseException(500, e.getMessage());
        } catch (Exception e) {
            log.error("failed to fix data with id:{}, paymentCode:{}, cause:{}", id, paymentCode, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "data.fix.fail");
        }
    }
}
