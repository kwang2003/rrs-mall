package com.aixforce.shop.model;

import com.aixforce.common.model.Indexable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-1-23
 */
@ToString
@EqualsAndHashCode
public class ShopExtra implements Indexable {
    private static final long serialVersionUID = -5830546081856845273L;

    @Getter
    @Setter
    private Long id;                            // 主键

    @Getter
    @Setter
    private Long shopId;                        // 店铺id

    @Getter
    @Setter
    private String outerCode;                   // 商家编码

    @Getter
    @Setter
    private String ntalkerId;                   // 企业编号，用来调用客服

    @Getter
    @Setter
    private Double rate;                        // 费率

    @Getter
    @Setter
    private Double rateUpdating;                // 待更新的费率

    @Getter
    @Setter
    private Long depositNeed;                   // 应缴纳的保证金

    @Getter
    @Setter
    private Long techFeeNeed;                   // 应缴纳的技术服务费

    @Getter
    @Setter
    private Long rService;                 // 服务评价

    @Getter
    @Setter
    private Long rExpress;                 // 快递评价

    @Getter
    @Setter
    private Long rDescribe;                // 描述评价

    @Getter
    @Setter
    private Long rQuality;                 // 质量评价

    @Getter
    @Setter
    private Long tradeQuantity;            // 交易总量

    @Getter
    @Setter
    private Boolean isStorePay;         // 是否支持到店支付

    @Getter
    @Setter
    private Date createdAt;                     // 创建时间

    @Getter
    @Setter
    private Date updatedAt;                     // 更新时间


    public void addRQuality(Long rQuality) {
        this.rQuality = firstNonNull(this.rQuality, 0l) + firstNonNull(rQuality, 0l);
    }

    public void addRDescribe(Long rDescribe) {
        this.rDescribe = firstNonNull(this.rDescribe, 0l) + firstNonNull(rDescribe, 0l);
    }

    public void addRService(Long rService) {
        this.rService = firstNonNull(this.rService, 0l) + firstNonNull(rService, 0l);
    }

    public void addRExpress(Long rExpress) {
        this.rExpress = firstNonNull(this.rExpress, 0l) + firstNonNull(rExpress, 0l);
    }

    public void addTradeQuantity(Long tradeQuality) {
        this.tradeQuantity = firstNonNull(this.tradeQuantity, 0l) + firstNonNull(tradeQuality, 0l);
    }

    public Integer score(Long score) {
        if (score==null || score <=0 || tradeQuantity==null || tradeQuantity<=0) {
            return 500;
        }

        if (score > 5 * tradeQuantity) {
            // Example: if Score is 20 but quantity is 3,
            // this statement will set actual quantity to 4 before calc score.
            long tq = (long) Math.ceil((score - tradeQuantity*5)/5) + tradeQuantity;
            return (int)(score*100/tq);
        }


        return (int)(score*100/tradeQuantity);
    }

}
