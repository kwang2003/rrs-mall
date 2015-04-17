package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.trade.service.UserTradeInfoService;
import com.aixforce.user.model.Address;
import com.aixforce.user.service.AddressService;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-13
 */

@Controller @Slf4j
@RequestMapping(value = "/api/admin")
public class Addresses {

    @Autowired
    private AddressService addressService;

    @Autowired
    private MessageSources messageSources;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @RequestMapping(value = "/address/provinces", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> provinces() {
        Response<List<Address>> result = addressService.provinces();
        if(!result.isSuccess()) {
            log.error("find provinces failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/address/province/{provinceId}/cities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> cities(@PathVariable("provinceId") Integer provinceId) {
        Response<List<Address>> result = addressService.citiesOf(provinceId);
        if(!result.isSuccess()) {
            log.error("find cities failed, provinceId={}, cause:{}", provinceId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/address/city/{cityId}/districts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Address> district(@PathVariable("cityId") Integer cityId) {
        Response<List<Address>> result = addressService.districtOf(cityId);
        if(!result.isSuccess()) {
            log.error("find cities failed, cityId={}, cause:{}", cityId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/address/trade-info/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserTradeInfo findTradeInfoById(@PathVariable Long id) {
        Response<UserTradeInfo> result = userTradeInfoService.findById(id);
        if (!result.isSuccess()) {
            log.error("failed to find userTradeInfo by id, id={},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }
}
