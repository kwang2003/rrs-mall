package com.aixforce.trade.dto;

import com.aixforce.common.utils.BeanMapper;
import com.aixforce.trade.model.OrderItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 11:44 AM  <br>
 * Author: xiao
 */
@ToString
public class HaierOrderItem implements Serializable {

    private static final long serialVersionUID = 1621460808885468875L;
    @Getter
    @Setter
    private Long id;                // 主键

    @Getter
    @Setter
    private Integer fee;            // 金额: 单位分

    @Getter
    @Setter
    private Integer deliverFee;         //运费

    @Getter
    @Setter
    private Integer originPrice;    // 原始价,即单价: 单位分

    @Getter
    @Setter
    private Integer price;          // 折扣后的价钱

    @Getter
    @Setter
    private Long skuId;             // RRS库存

    @Getter
    @Setter
    private String outerId;         // 海尔库存

    @Getter
    @Setter
    private Long itemId;            // 商品编号

    @Getter
    @Setter
    private String itemName;        // 商品名称

    @Getter
    @Setter
    private Long brandId;           // 品牌id

    @Getter
    @Setter
    private Integer quantity;       // 数量

    @Getter
    @Setter
    private Integer discount;       // 折扣

    @Getter
    @Setter
    private Integer status;         // 订单状态

    @Getter
    @Setter
    private Integer type;           // 子订单类型

    @Getter
    @Setter
    private String createdDate;     // 创建时间

    @Getter
    @Setter
    private Date updatedDate;       // 修改时间

    @Getter
    @Setter
    private String deliveryPromise;     //送达承诺


    public static HaierOrderItem transform(OrderItem item) {
        HaierOrderItem dto = new HaierOrderItem();
        BeanMapper.copy(item, dto);
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
