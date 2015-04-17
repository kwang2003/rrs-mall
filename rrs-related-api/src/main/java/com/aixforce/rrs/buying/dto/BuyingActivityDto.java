package com.aixforce.rrs.buying.dto;

import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by songrenfei on 14-9-23
 */
@Data
public class BuyingActivityDto implements Serializable {

    private static final long serialVersionUID = -4025305562331580764L;

    private BuyingActivityDefinition buyingActivityDefinition;

    private List<BuyingItem> itemList;


    private String activityStartAt;      //活动开始时间

    private String activityEndAt;   //活动结束时间

    private String orderStartAt;   //订单开始时间

    private String orderEndAt;   //订单结束时间

    private Boolean preview;       //用于表示是编辑还是查看

    private Boolean isEhaier;           //是否是ehaier商家
}
