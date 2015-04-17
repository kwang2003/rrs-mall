package com.aixforce.trade.dto;

import com.aixforce.trade.model.UserTradeInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Desc:这个用于和前台关于运费信息和用户物流地址的信息显示
 * Mail:v@terminus.io
 * author:Michael Zhao
 * Date:2014-05-12.
 */
public class UserFreightInfo implements Serializable {
    @Getter
    @Setter
    Map<String , Integer> freightFees;        //用于保存对应的itemId:count计算后得到的价格信息(这个是针对于同一个item的不同sku在同一个order中的情况)

    @Getter
    @Setter
    List<UserTradeInfo> tradeInfoList;      //用于保存用户在当前系统的区域信息中的收货地址信息
}
