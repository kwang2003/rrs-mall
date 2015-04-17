package com.aixforce.rrs.buying.dto;

import com.aixforce.trade.dto.RichOrderItem;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangzefeng on 14-9-25
 */
@Data
public class BuyingPreOrder implements Serializable {
    private static final long serialVersionUID = 6780688847346584662L;

    private String shopName;

    private Long sellerId;

    private Long buyingTempOrderId;     //抢购虚拟订单id

    private Long tradeInfoId;           //收货地址id

    private Long buyingActivityId;      //抢购活动id

    private Boolean isCod;              //是否支持货到付款

    private Boolean eInvoice;           //是否支持电子发票

    private Boolean vatInvoice;         //是否支持增值税发票

    private Boolean isEhaier;           //是否是ehaier商家

    private Integer discount;           //减免金额

    private RichOrderItem richOrderItem;

    private String systemDate; // 当前系统时间
}
