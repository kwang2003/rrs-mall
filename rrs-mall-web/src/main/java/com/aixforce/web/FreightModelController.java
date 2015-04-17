package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.dto.FreightModelDto;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.trade.service.FreightCountService;
import com.aixforce.trade.service.FreightModelService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Desc:模板操作处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-22.
 */
@Slf4j
@Controller
@RequestMapping("/api")
public class FreightModelController {
    @Autowired
    private MessageSources messageSources;

    @Autowired
    private FreightModelService freightModelService;

    @Autowired
    private FreightCountService freightCountService;

    /*
        创建运费模板
     */
    @RequestMapping(value = "/seller/freightModel/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String createFreightModel(@RequestBody FreightModelDto freightModelDto) {
        BaseUser user = UserUtil.getCurrentUser();
        if(user == null){
            log.warn("create freight model need user login.");
            return "user.not.login";
        }

        freightModelDto.setSellerId(user.getId());
        Response<Boolean> result = freightModelService.createModel(freightModelDto);

        if (!result.isSuccess()) {
            log.error("create freight model failed, error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /*
       更新运费模板
    */
    @RequestMapping(value = "/seller/freightModel/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateFreightModel(@RequestBody FreightModelDto freightModelDto) {
        Long userId = UserUtil.getUserId();
        if(userId == null){
            log.warn("update freight model need user login.");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        Response<Boolean> result = freightModelService.updateModel(freightModelDto, userId);

        if (!result.isSuccess()) {
            log.error("update freight model failed, error code={}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    @RequestMapping(value = "/seller/freightModel/{freightModelId}/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void deleteFreightModel(@PathVariable("freightModelId") Long id) {
        Long userId = UserUtil.getUserId();

        if(userId == null) {
            log.error("user not login");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        Response<Boolean> result = freightModelService.deleteModel(id, userId);
        if(!result.isSuccess()) {
            log.error("fail to delete freight model by id={}, current userId={}, error code={}",
                    id, userId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
    }

    /*
       异步删除特殊区域信息(特殊区域编号)
    */
    @RequestMapping(value = "/seller/freightModel/deleteSpecial", method = RequestMethod.DELETE)
    @ResponseBody
    public String deleteLogisticsSpecial(@RequestParam Long specialId) {
        Long userId = UserUtil.getUserId();
        Response<Boolean> result = freightModelService.deleteLogisticsSpecial(specialId, userId);

        if (!result.isSuccess()) {
            log.error("delete logistics special model failed, specialId={}, error code={}", specialId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return "ok";
    }

    /*
       查看运费模板
    */
    @RequestMapping(value = "/seller/freightModel/find/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FreightModelDto findByModelId(@PathVariable("id") Long modelId) {
        Response<FreightModelDto> result = freightModelService.findById(modelId);

        if (!result.isSuccess()) {
            log.error("find freight model failed, modelId={}, error code={}", modelId, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }

    /*
       查寻商家的全部运费模板信息
    */
    @RequestMapping(value = "/seller/freightModel/findBySeller", method = RequestMethod.GET)
    @ResponseBody
    public List<FreightModel> findBySellerId() {
        BaseUser user = UserUtil.getCurrentUser();
        if(user == null){
            log.warn("find freight model need user login.");
            throw new JsonResponseException(500, messageSources.get("user.not.login"));
        }

        Response<List<FreightModel>> result = freightModelService.findBySellerId(user.getId());

        if (!result.isSuccess()) {
            log.error("find freight model failed, sellerId={}, error code={}", user.getId(), result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }

        return result.getResult();
    }

    /*
        通过商品信息&地址信息计算运费(适用与商品详情页&订单页更改数量时异步调用)
     */
    @RequestMapping(value = "/freightModel/count", method = RequestMethod.GET)
    @ResponseBody
    public Integer countDefaultFee(@RequestParam Integer addressId, @RequestParam Long itemId, @RequestParam Integer itemNum){
        if(addressId == null){
            log.error("no addressId is specified for itemId:{}", itemId);
            // 注释的原因 :用户的cookie中可能不存在该服务行政区域信息
//            throw new JsonResponseException(400, messageSources.get("illegal.param"));
            return 0;
        }
        Response<Integer> countFee = freightCountService.countDefaultFee(addressId , itemId, itemNum);

        if (!countFee.isSuccess()) {
            log.error("count freight fee failed, addressId={}, itemId={}, itemNum={}, error code={}", addressId, itemId, itemNum, countFee.getError());
            throw new JsonResponseException(500, messageSources.get(countFee.getError()));
        }

        return countFee.getResult();
    }

    /*
       通过子订单信息&邮寄地址编号计算运费(订单页更改数量时&更改邮寄地址时异步调用)
    */
    @RequestMapping(value = "/freightModel/countOrderFee", method = RequestMethod.POST)
    @ResponseBody
    public Integer countOrderFee(@RequestParam Long userTradeId, @RequestParam Long orderItemId){
        Response<Integer> countFee = freightCountService.countOrderItemFee(userTradeId , orderItemId);

        if (!countFee.isSuccess()) {
            log.error("count freight fee failed, userTradeId={}, orderItemId={}, error code={}", userTradeId, orderItemId, countFee.getError());
            throw new JsonResponseException(500, messageSources.get(countFee.getError()));
        }

        return countFee.getResult();
    }
}
