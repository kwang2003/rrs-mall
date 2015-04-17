package com.aixforce.shop.model;

import com.aixforce.common.model.Indexable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-28
 */
@ToString
public class Shop implements Indexable {
    private static final long serialVersionUID = -2299728283682953287L;

    @Getter
    @Setter
    private Long id;                    // 店铺id

    @Getter
    @Setter
    private Long userId;                // 卖家id

    @Getter
    @Setter
    private String userName;            // 卖家nick

    @Getter
    @Setter
    private String name;                // 店铺名称,要求唯一


    @Getter
    @Setter
    private Integer status;             // 店铺状态  0:待审批 1:正常 -2:审批不通过 -1:冻结


    @Getter
    @Setter
    private String phone;               // 电话

    @Getter
    @Setter
    private String email;               // 电子邮件

    @Getter
    @Setter
    private Long businessId;            // 店铺类目

    @Getter
    @Setter
    private String imageUrl;            // 店铺主图

    @Getter
    @Setter
    private Integer province;           // 店铺所在省份

    @Getter
    @Setter
    private Integer city;               // 店铺所在市

    @Getter
    @Setter
    private Integer region;             // 店铺所在区域

    @Getter
    @Setter
    private String street;              // 店铺所在街道

    @Getter
    @Setter
    private String taxRegisterNo;       // 税务登记号

    @Getter
    @Setter
    private Boolean isCod;              // 是否支持货到付款

    @Getter
    @Setter
    private Boolean eInvoice;           // 是否支持电子发票

    @Getter
    @Setter
    private Boolean vatInvoice;         // 是否支持增值税发票

    @Getter
    @Setter
    private Boolean deliveryTime;       //是否支持配送时段

    @Getter
    @Setter
    private Boolean deliveryPromise;    //是否支持配送承诺

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;             // 修改时间

    @Getter
    @Setter
    private String companyName;  //公司名称

    @Getter
    @Setter
    private String boundStyle; //绑定类型 0未绑定 1 绑定快捷通 2 绑定银行账户

    @Getter
    @Setter
    private String accountId; //绑定快捷通账户或银行账户的id

    @Getter
    @Setter
    private Long isWater;            // 净水businessId子分类，0：默认；1：O2O体验馆;2:体验馆DTD供货商

    public static enum Status {
        INIT(0), OK(1), FAIL(-2), FROZEN(-1);

        private final int value;

        private Status(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Status from(Integer value) {
            for (Status status : Status.values()) {
                if (Objects.equal(status.value, value)) {
                    return status;
                }
            }
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Shop)) {
            return false;
        }
        Shop that = (Shop) o;
        return Objects.equal(this.userId, that.userId);
    }


}
