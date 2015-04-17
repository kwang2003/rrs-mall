package com.aixforce.rrs.buying.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 抢购虚拟订单
 * Created by songrenfei on 14-9-22.
 */
@ToString
@EqualsAndHashCode
public class BuyingTempOrder implements Serializable {


    private static final long serialVersionUID = -1918717448271705350L;

    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long orderId;            //订单id

    @Getter
    @Setter
    private Long buyingActivityId;            //抢购活动定义id

    @Getter
    @Setter
    private Long skuId;            //SKU ID

    @Getter
    @Setter
    private Long buyerId;            //用户 ID

    @Getter
    @Setter
    private Long sellerId;            // 卖家ID

    @Getter
    @Setter
    private Long shopId;            //店铺 ID

    @Getter
    @Setter
    private Integer buyingPrice;      //抢购价

    @Getter
    @Setter
    private Integer skuQuantity;   //商品抢购数量

    @Getter
    @Setter
    private String skuAttributeJson; //sku销售属性

    @Getter
    @Setter
    private String itemImage;   //商品图片

    @Getter
    @Setter
    private String itemName;   //商品名称

    @Getter
    @Setter
    private Long itemId;   //商品id

    @Getter
    @Setter
    private Long tradeInfoId;   //收货信息id

    @Getter
    @Setter
    private Date orderCreatedAt;             //订单创建时间

    @Getter
    @Setter
    private Integer regionId;  //区级别的地址id

    @Getter
    @Setter
    private Date orderStartAt;   //订单开始时间

    @Getter
    @Setter
    private Date orderEndAt;   //订单结束时间

    @Getter
    @Setter
    private Integer payLimit;      //购买时限

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间


    @Getter
    @Setter
    private Integer status;      //状态(0->待下单;1->已下单;-1->已取消,-2下单超时)


    /**
     * no db
     */
    @Getter
    @Setter
    private String buyerName;            //用户 名称 临时使用 不存入数据库

    public static enum Status {

        NOT_ORDER(0, "待下单"),
        IS_ORDER(1, "已下单"),
        CANCEL(-1, "已取消"),
        OUT_DATE(-2, "已过期");

        private final int value;

        private final String description;

        private Status(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return description;
        }
    }



}
