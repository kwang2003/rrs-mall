package com.aixforce.trade.dto;

import com.aixforce.trade.model.OrderExtra;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-28
 */
@ToString
public abstract class RichOrder implements Serializable {
    private static final long serialVersionUID = 8632121021811925786L;
    @Getter
    @Setter
    protected List<RichOrderItem> orderItems;
    @Getter
    @Setter
    private Long orderId;                           // 订单id
    @Getter
    @Setter
    private Date createdAt;                         // 创建时间
    @Getter
    @Setter
    private Integer status;                         // 状态
    @Getter
    @Setter
    private Integer totalFee;                       // 货款
    @Getter
    @Setter
    private Integer deliverFee;                     // 运费
    @Getter
    @Setter
    private Long userTradeInfoId;                   // 配送id
    @Getter
    @Setter
    private OrderExtra orderExtra;                  // 订单补充信息
    @Getter
    @Setter
    private Integer paymentType;                    // 支付方式
    @Getter
    @Setter
    private Integer orderType;                      // 订单类型
    @Getter
    @Setter
    private Integer totalRefundAmount;              // 总退款金额,预售订单才用到这个字段

    @Getter
    @Setter
    private Boolean canPayEarnest;                  // 是否可以支付定金

    @Getter
    @Setter
    private Boolean canPayRemain;                   // 是否可以支付尾款

    @Getter
    @Setter
    private Date earnestPayTime;                    // 预售定金截止时间


    @Getter
    @Setter
    private Date remainFinishAt;                    // 预售付尾款结束时间

    @Getter
    @Setter
    private Date preSaleFinishAt;                   // 预售结束时间

    @Getter
    @Setter
    private Boolean hasComment;                     // 订单是否已评价

    @Getter
    @Setter
    private Boolean isRecordLogistics;              // 是否已经录入物流信息

    @Getter
    @Setter
    private Integer discount;                       // 订单优惠金额

    @Getter
    @Setter
    private String activityName;                    // 活动定义名称

    @Getter
    @Setter
    private Boolean isEhaier;                       // 是否是ehaier商家

    @Getter
    @Setter
    private Boolean canBaskOrder; // 是否能晒单，不持久化

    @Getter
    @Setter
    private Boolean isBuying;                       //是否是抢购订单

    @Getter
    @Setter
    private Boolean buyingCanPay;   //如果是抢购订单是否可以付款

    @Getter
    @Setter
    private Long buyingActivityId;                  //抢购活动id

    @Getter
    @Setter
    private Integer depositType;

    @Getter
    @Setter
    private Integer totalPrice;//预授权押金订单总价格
}
