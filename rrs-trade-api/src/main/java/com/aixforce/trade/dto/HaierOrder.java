package com.aixforce.trade.dto;

import com.aixforce.common.utils.BeanMapper;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-17 11:23 AM  <br>
 * Author: xiao
 */
@ToString
@NoArgsConstructor
public class HaierOrder implements Serializable {

    private static final long serialVersionUID = 1479196508419559249L;
    private static DateTimeFormatter dft = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Getter
    @Setter
    private Long id;                    // RRS订单号

    @Getter
    @Setter
    private Long rusherId;             // 抢券ID

    @Getter
    @Setter
    private Long buyerId;               // 买家id

    @Getter
    @Setter
    private String buyerName;           // 买家名称

    @Getter
    @Setter
    private Long shopId;                // 店铺id

    @Getter
    @Setter
    private Long sellerId;             // 卖家id

    @Getter
    @Setter
    private String outerCode;           // 商家88码

    @Getter
    @Setter
    private Integer fee;                // 订单金额


    @Getter
    @Setter
    private Integer status;             // 订单状态

    @Getter
    @Setter
    private Integer type;               // 订单类型

    @Getter
    @Setter
    private Long business;              // 频道

    @Getter
    @Setter
    private Integer paymentType;        // 支付类型

    @Getter
    @Setter
    private String paidDate;            // 支付时间

    @Getter
    @Setter
    private String deliveredDate;       // 签收时间

    @Getter
    @Setter
    private String doneDate;            // 订单完成时间

    @Getter
    @Setter
    private String canceledDate;        // 订单关闭时间

    @Getter
    @Setter
    private String createdDate;         // 订单创建时间

    @Getter
    @Setter
    private String updatedDate;         // 订单状态更新时间


    // ***** 配送信息 *****
    @Getter
    @Setter
    private String name;                // 收货人名称

    @Getter
    @Setter
    private String phone;               // 收货人手机 或 固话

    @Getter
    @Setter
    private String province;            // 省

    @Getter
    @Setter
    private String city;                // 市

    @Getter
    @Setter
    private String district;            // 区

    @Getter
    @Setter
    private Integer provinceCode;       // 省份编码

    @Getter
    @Setter
    private Integer cityCode;           // 城市编码

    @Getter
    @Setter
    private Integer districtCode;       // 区县编码


    @Getter
    @Setter
    private String street;              // 街道

    @Getter
    @Setter
    private String zip;                 // 邮编




    // ***** 发票与留言 *****

    @Getter
    @Setter
    private String buyerNotes;          // 买家留言

    @Getter
    @Setter
    private String invoice;             // 发票信息

    @Getter
    @Setter
    private String invoiceTitle;        // 发票抬头

    @Getter
    @Setter
    private String invoiceType;         // 1:普通发票 2:增值税发票

    @Getter
    @Setter
    private String deliverTime;         // 送达时段

    @Getter
    @Setter
    private String companyName;             // 公司名称

    @Getter
    @Setter
    private String taxRegisterNo;           // 税务登记号

    @Getter
    @Setter
    private String registerAddress;         // 注册地址

    @Getter
    @Setter
    private String registerPhone;           // 注册电话

    @Getter
    @Setter
    private String registerBank;            // 注册银行

    @Getter
    @Setter
    private String bankAccount;             // 银行帐号

    @Getter
    @Setter
    private String taxCertificate;          // 税务登记证

    @Getter
    @Setter
    private String taxpayerCertificate;     // 一般纳税人证书


    // ***** 收件人信息 *****

    @Getter
    @Setter
    private String receiveName;              // 收件人名称

    @Getter
    @Setter
    private String receiveAddress;          // 收件人地址

    @Getter
    @Setter
    private String receiveZip;              // 收件人邮编

    @Getter
    @Setter
    private String receivePhone;            // 收件人电话

    @Getter
    @Setter
    private String tempReturnId;                    // RRS临时逆向订单号

    @Getter
    @Setter
    protected List<HaierOrderItem> items = Lists.newLinkedList();    // 订单商品明细





    public static HaierOrder transform(Order order, List<OrderItem> orderItems, Long shopId, String outerId) {
        HaierOrder dto = new HaierOrder();
        List<HaierOrderItem> items = BeanMapper.mapList(orderItems, HaierOrderItem.class);
        BeanMapper.copy(order, dto);
        dto.setShopId(shopId);
        dto.setOuterCode(outerId);
        dto.setItems(items);
        transDate(dto, order);
        return dto;
    }


    public static HaierOrder transform(Order order, Long shopId, String outerId) {
        HaierOrder dto = new HaierOrder();
        BeanMapper.copy(order, dto);
        dto.setShopId(shopId);
        dto.setOuterCode(outerId);
        transDate(dto, order);
        return dto;
    }

    private static void transDate(HaierOrder dto, Order order) {
        if (order.getPaidAt() != null)
            dto.setPaidDate(dft.print(new DateTime(order.getPaidAt())));
        if (order.getDeliveredAt() != null)
            dto.setDeliveredDate(dft.print(new DateTime(order.getDeliveredAt())));
        if (order.getDoneAt() != null)
            dto.setDoneDate(dft.print(new DateTime(order.getDoneAt())));
        if (order.getCanceledAt() != null)
            dto.setCanceledDate(dft.print(new DateTime(order.getCanceledAt())));
        if (order.getCreatedAt() != null)
            dto.setUpdatedDate(dft.print(new DateTime(order.getUpdatedAt())));
        if (order.getUpdatedAt() != null)
            dto.setCreatedDate(dft.print(new DateTime(order.getCreatedAt())));
    }

}
