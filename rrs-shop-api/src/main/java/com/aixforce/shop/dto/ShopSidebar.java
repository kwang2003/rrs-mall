package com.aixforce.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 13-12-16
 */
public class ShopSidebar implements Serializable{

    private static final long serialVersionUID = -5961532825344045613L;
    @Getter
    @Setter
    private String shopPhone;

    @Getter
    @Setter
    private Date createAt;

    @Getter
    @Setter
    private Long itemNum;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private String shopName;

    @Getter
    @Setter
    private Long sellerId;

    @Getter
    @Setter
    private Long businessId;

    @Getter
    @Setter
    private String ntalkerId;

    @Getter
    @Setter
    private Integer rExpress = 0;

    @Getter
    @Setter
    private Integer rService = 0;

    @Getter
    @Setter
    private Integer rDescribe = 0;

    @Getter
    @Setter
    private Integer rQuality = 0;

    @Getter
    @Setter
    private Integer rReserve = 0; //  0 不显示  1显示

    @Getter
    @Setter
    private String street; //店铺的地址信息

    @Getter
    @Setter
    private String loginName; //当前登陆用户的用户名

    @Getter
    @Setter
    private String loginMobile; //当前登陆用户的联系电话

    @Getter
    @Setter
    private String arriveStartTime;

    @Getter
    @Setter
    private String arriveEndTime;

    @Getter
    @Setter
    private Long shopConfigId;

    @Getter
    @Setter
    private Long shopType;  //店铺类型1店铺  2mall体验馆

    @Getter
    @Setter
    private Integer isEnable = 0; //0表示无效 1 表示dou有效 2表示上午无效  下午有效

    public static enum shopTypeV {
        SHOP(1L), MALL(2L);
        private final Long value;
        shopTypeV(Long value) {
            this.value = value;
        }

        public Long value() {
            return value;
        }

    }
}
