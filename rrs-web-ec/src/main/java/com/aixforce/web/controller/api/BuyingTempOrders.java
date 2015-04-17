package com.aixforce.web.controller.api;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.buying.service.BuyingTempOrderService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by songrenfei on 14-9-26
 */
@Slf4j
@Controller
@RequestMapping("/api")
public class BuyingTempOrders {

    private final MessageSources messageSources;

    private final BuyingTempOrderService buyingTempOrderService;

    @Autowired
    public BuyingTempOrders(MessageSources messageSources, BuyingTempOrderService buyingTempOrderService) {
        this.messageSources = messageSources;
        this.buyingTempOrderService = buyingTempOrderService;
    }

    /**
     * 取消
     * @param id 订单id
     * @return 是否更新成功
     */
    @RequestMapping(value = "/user/temp/order/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean update(@PathVariable("id") Long id) {
        Long userId = UserUtil.getUserId();
        Response<BuyingTempOrder> result = buyingTempOrderService.findById(id);
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        if (Objects.equal(userId, result.getResult().getBuyerId())) {
            Response<Boolean> ur = buyingTempOrderService.cancelOrder(result.getResult());
            if (ur.isSuccess()) {
                return ur.getResult();
            } else {
                throw new JsonResponseException(500, messageSources.get(ur.getError()));
            }
        } else {
            throw new JsonResponseException(500, messageSources.get("buying.temp.order.not.owner"));
        }
    }

    /**
     * 抢购号校验
     * @param id 抢购id
     * @return 是否是无效的抢购号
     */
    @RequestMapping(value = "/user/temp/order/invalid/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean isInvalidBuyingOrder(@PathVariable("id") Long id) {
        Response<Boolean> result = buyingTempOrderService.checkBuyingOrderId(id);
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

}
