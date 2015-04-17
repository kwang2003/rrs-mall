package com.aixforce.web.controller.api.userEvent;

import com.aixforce.api.service.CpsService;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @Description: CPS event<br/>
 * @Author: Benz.Huang@goodaysh.com <br/>
 * @DATE:   2014/10/14 <br/>
 */
@Data
public class CpsEvent {
    protected final HttpServletRequest request;

    protected final Set<Long> ids;

    protected CpsService.EventType eventType;

    /**
     * 构造函数
     * @param request HttpServletRequest
     * @param ids 订单号集合。可以为null，比如PAY
     * @param eventType 事件类型。订单的创建CpsService.EventType.ORDER、取消CpsService.EventType.CANCEL、支付CpsService.EventType.PAY、退款CpsService.EventType.REFUND
     */
    public CpsEvent(HttpServletRequest request,Set<Long> ids,CpsService.EventType eventType){
        this.request = request;
        this.ids = ids;
        this.eventType = eventType;
    }
}
