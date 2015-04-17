package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.model.DeliveryMethod;
import com.aixforce.trade.service.DeliveryMethodService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * Created by yangzefeng on 14-9-3
 */
@Controller @Slf4j
@RequestMapping("/api")
public class DeliveryMethodController {

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private MessageSources messageSources;

    /**
     * 查询配送承诺
     */
    @RequestMapping(value = "/seller/deliveryPromises", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DeliveryMethod> findPromises() {

        Long userId = UserUtil.getUserId();

        Response<Shop> shopR = shopService.findByUserId(userId);
        if(!shopR.isSuccess()) {
            log.error("fail to find shop by userId={}, error code:{}",userId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Shop shop = shopR.getResult();

        //如果不支持配送承诺，返回空
        if (shop.getDeliveryPromise() == null || !shop.getDeliveryPromise()) {
            return Collections.emptyList();
        }
        Response<List<DeliveryMethod>> deliveryMethodR = deliveryMethodService.findBy(
                DeliveryMethod.Status.OK.value(), DeliveryMethod.Type.DELIVER_PROMISE.value());
        if(!deliveryMethodR.isSuccess()) {
            log.error("fail to find delivery method by status={ok}, type={delivery_promise}, error code:{}",
                    deliveryMethodR.getError());
            throw new JsonResponseException(500, messageSources.get(deliveryMethodR.getError()));
        }
        return deliveryMethodR.getResult();


    }

    /**
     * 查询配送时段
     */
    @RequestMapping(value = "/buyer/deliveryTimes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DeliveryMethod> findTimes(@RequestParam("sellerId") Long sellerId) {
        Response<Shop> shopR = shopService.findByUserId(sellerId);
        if(!shopR.isSuccess()) {
            log.error("fail to find shop by userId={}, error code:{}",sellerId, shopR.getError());
            throw new JsonResponseException(500, messageSources.get(shopR.getError()));
        }
        Shop shop = shopR.getResult();

        if(shop.getDeliveryTime() == null || !shop.getDeliveryTime()) {
            return Collections.emptyList();
        }

        Response<List<DeliveryMethod>> deliveryMethodR = deliveryMethodService.findBy(
                DeliveryMethod.Status.OK.value(), DeliveryMethod.Type.DELIVER_TIME.value());
        if(!deliveryMethodR.isSuccess()) {
            log.error("fail to find delivery time by status {od}, type {delivery_time}, error code:{}",
                    deliveryMethodR.getError());
            throw new JsonResponseException(500, messageSources.get(deliveryMethodR.getError()));
        }
        return deliveryMethodR.getResult();
    }

    @RequestMapping(value = "/deliveryMethods", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DeliveryMethod> findByType(@RequestParam(value = "type", defaultValue = "1") Integer type) {
        Response<List<DeliveryMethod>> deliveryMethodR = deliveryMethodService.findBy(
                DeliveryMethod.Status.OK.value(), type);
        if(!deliveryMethodR.isSuccess()) {
            log.error("fail to find delivery method by type={}, status={ok}, error code:{}",
                    type, deliveryMethodR.getError());
            throw new JsonResponseException(500, messageSources.get(deliveryMethodR.getError()));
        }
        return deliveryMethodR.getResult();
    }
}
