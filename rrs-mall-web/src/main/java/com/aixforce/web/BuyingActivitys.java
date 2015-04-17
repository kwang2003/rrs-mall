package com.aixforce.web;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.buying.dto.BuyingActivityDto;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.service.BuyingActivityDefinitionService;
import com.aixforce.rrs.buying.service.BuyingItemService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.aixforce.common.utils.Arguments.isNull;

/**
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Controller
@Slf4j
@RequestMapping("/api/seller/buying")
public class BuyingActivitys {
    @Autowired
    private MessageSources messageSources;              //异常消息 placeholder support

    @Autowired
    private BuyingActivityDefinitionService buyingActivityDefinitionService; //抢购活动管理服务接口

    @Autowired
    private BuyingItemService buyingItemService;

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    private final static JavaType javaType= jsonMapper.createCollectionType(
            ArrayList.class, BuyingItem.class);


    /**
     * 新增记录
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String create(@RequestBody BuyingActivityDto buyingActivityDto) {

        Long userId = UserUtil.getUserId();

        validDate(buyingActivityDto);

        Response<Long> createResponse = buyingActivityDefinitionService.create(buyingActivityDto,userId);

        if(!createResponse.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(createResponse.getError()));
        }

        return "ok";

    }

    /**
     * 验证时间
     * @param buyingActivityDto dto对象
     */
    private void validDate(BuyingActivityDto buyingActivityDto){
        if(isNull(buyingActivityDto.getActivityStartAt())){
            throw new JsonResponseException(500, messageSources.get("activity.start.at.can.not.null"));
        }
        if(isNull(buyingActivityDto.getActivityEndAt())){
            throw new JsonResponseException(500, messageSources.get("activity.end.at.can.not.null"));
        }
        if(isNull(buyingActivityDto.getOrderStartAt())){
            throw new JsonResponseException(500, messageSources.get("to.order.start.at.can.not.null"));
        }
        if(isNull(buyingActivityDto.getOrderEndAt())){
            throw new JsonResponseException(500, messageSources.get("to.order.end.at.can.not.null"));
        }
        DateTime activityStartAt = DFT.parseDateTime(buyingActivityDto.getActivityStartAt());
        buyingActivityDto.getBuyingActivityDefinition().setActivityStartAt(activityStartAt.toDate());

        DateTime activityEndAt = DFT.parseDateTime(buyingActivityDto.getActivityEndAt());
        buyingActivityDto.getBuyingActivityDefinition().setActivityEndAt(activityEndAt.toDate());

        DateTime orderStartAt = DFT.parseDateTime(buyingActivityDto.getOrderStartAt());
        buyingActivityDto.getBuyingActivityDefinition().setOrderStartAt(orderStartAt.toDate());

        DateTime orderEndAt = DFT.parseDateTime(buyingActivityDto.getOrderEndAt());
        buyingActivityDto.getBuyingActivityDefinition().setOrderEndAt(orderEndAt.toDate());

        //如果活动结束时间早于当前时间 （不合法）
        if(activityEndAt.isBeforeNow()){
            throw new JsonResponseException(500, messageSources.get("activity.end.at.can.not.before.now"));
        }
        //如果活动结束时间早于活动开始时间 （不合法）
        if(activityEndAt.isBefore(activityStartAt)){
            throw new JsonResponseException(500, messageSources.get("activity.end.at.can.not.before.start.at"));
        }
        //如果下单开始时间早于活动开始时间 （不合法）
        if(orderStartAt.isBefore(activityStartAt)){
            throw new JsonResponseException(500, messageSources.get("to.order.start.at.can.not.before.activity.start.at"));
        }

        //如果下单结束时间早于活动结束时间 （不合法）
        if(orderEndAt.isBefore(activityEndAt)){
            throw new JsonResponseException(500, messageSources.get("to.order.end.at.can.not.before.activity.end.at"));
        }

        //如果下单结束时间早于下单开始时间 （不合法）
        if(orderEndAt.isBefore(orderStartAt)){
            throw new JsonResponseException(500, messageSources.get("to.order.end.at.can.not.before.to.order.start.at"));
        }

    }


    /**
     * 根据自增序列 id 或业务定义 nameId 删除记录
     *
     * @param id        自增序列 ID 号
     * @return          响应结果
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean delete(@PathVariable Long id){

        Response<Boolean> deleteResp = buyingActivityDefinitionService.delete(id);

        if(!deleteResp.isSuccess()){
            throw new JsonResponseException(500, messageSources.get(deleteResp.getError()));
        }

        return deleteResp.getResult();

    }

    /**
     * 更新状态
     *
     * @return          响应结果
     */
    @RequestMapping(value = "/update/{id}",method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateStatus(@PathVariable Long id,@RequestParam Integer status) {
        BuyingActivityDefinition buyingActivity = new BuyingActivityDefinition();
        buyingActivity.setId(id);
        buyingActivity.setStatus(status);
        Response<Boolean> updateResp = buyingActivityDefinitionService.update(buyingActivity);

        if(!updateResp.isSuccess()){

            throw new JsonResponseException(500, messageSources.get(updateResp.getError()));
        }

        return updateResp.getResult();

    }

    /**
     * 更新记录
     *
     * @return          响应结果
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean update(@RequestBody BuyingActivityDto buyingActivityDto) {


        Long userId = UserUtil.getUserId();

        validDate(buyingActivityDto);

        Response<Boolean> updateResp = buyingActivityDefinitionService.update(buyingActivityDto,userId);

        if(!updateResp.isSuccess()){

            throw new JsonResponseException(500, messageSources.get(updateResp.getError()));
        }

        return updateResp.getResult();

    }

    /**
     * 根据唯一ID查询参加活动商品结果
     *
     * @param id    自增序列ID
     * @return      唯一结果集
     */
    @RequestMapping(value = "/items/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BuyingItem> findByActivityId(@PathVariable Long id){

        Response<List<BuyingItem>> byIdResp = buyingItemService.findByActivityId(id);
        if(!byIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byIdResp.getError()));
        return byIdResp.getResult();

    }

    /**
     * 批量更新参与商品的虚拟销量
     *
     * @param data    商品集合json字符串
     * @return      是否更新成功
     */
    @RequestMapping(value = "/items/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateByActivityId(@RequestParam("data") String data){
        if(Strings.isNullOrEmpty(data)){
            throw new JsonResponseException(500, messageSources.get("illegal.param"));
        }

        List<BuyingItem> buyingItemList =  jsonMapper.fromJson(data, javaType);

        Response<Boolean> byIdResp = buyingItemService.updateFakeSoldQuantity(buyingItemList);
        if(!byIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byIdResp.getError()));
        return byIdResp.getResult();

    }


    /**
     * 根据唯一ID查询该活动下的订单
     *
     * @param id    自增序列ID
     * @return      唯一结果集
     */
    @RequestMapping(value = "/order/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BuyingItem> findOrders(@PathVariable Long id){

        Response<List<BuyingItem>> byIdResp = buyingItemService.findByActivityId(id);
        if(!byIdResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(byIdResp.getError()));
        return byIdResp.getResult();

    }
}
