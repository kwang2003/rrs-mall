package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 结算日汇总
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-21 3:25 PM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode
public class DailySettlement implements Serializable {
    private static final long serialVersionUID = 3792272980467340942L;


    @Getter
    @Setter
    private Long id;                            // 自增主键

    @Getter
    @Setter
    private Long orderCount;                    // 订单数量

    @Getter
    @Setter
    private Long totalEarning;                  // 交易总收入

    @Getter
    @Setter
    private Long totalExpenditure;              // 交易总支出

    @Getter
    @Setter
    private Long sellerEarning;                 // 商家收入

    @Getter
    @Setter
    private Long rrsCommission;                 // 平台佣金收入

    @Getter
    @Setter
    private Long scoreEarning;                  // 积分收入

    @Getter
    @Setter
    private Long presellDeposit;                // 预售定金收入

    @Getter
    @Setter
    private Long presellCommission;             // 营业外收入

    @Getter
    @Setter
    private Long thirdPartyCommission;          // 第三方佣金收入

    @Getter
    @Setter
    private Date confirmedAt;                   // 商家确认时间

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 修改时间


    public static DailySettlement empty() {
        DailySettlement settlement = new DailySettlement();
        settlement.setTotalEarning(0L);
        settlement.setTotalExpenditure(0L);
        settlement.setSellerEarning(0L);
        settlement.setRrsCommission(0L);
        settlement.setScoreEarning(0L);
        settlement.setPresellDeposit(0L);
        settlement.setThirdPartyCommission(0L);
        return settlement;
    }
}
