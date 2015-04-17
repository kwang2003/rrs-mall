package com.rrs.coupons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yea01 on 2014/8/19.
 */
@ToString
@EqualsAndHashCode
public class RrsCou implements Serializable {

    @Getter
    @Setter
    private  long id;
    @Getter
    @Setter
    private  String cpName;
    @Getter
    @Setter
    private  long channelId;
    @Getter
    @Setter
    private Date startTime;
    @Getter
    @Setter
    private  Date endTime;
    @Getter
    @Setter
    private  long area;
    @Getter
    @Setter
    private  int term;
    @Getter
    @Setter
    private  int amount;
    @Getter
    @Setter
    private  int userType;
    @Getter
    @Setter
    private  int type;
    @Getter
    @Setter
    private  int status; //优惠券状态：未生效（0）暂停（1）生效（2）失效(3) 撤销(4)
    @Getter
    @Setter
    private  int sendNum;
    @Getter
    @Setter
    private  int sendType;
    @Getter
    @Setter
    private  Date sendStartTime;
    @Getter
    @Setter
    private  Date sendEndTime;
    @Getter
    @Setter
    private  String sendOrigin;
    @Getter
    @Setter
    private  Date created_at;
    @Getter
    @Setter
    private  Date updated_at;
    @Getter
    @Setter
    private  String  memo;
    @Getter
    @Setter
    private  String costsBear;
    @Getter
    @Setter
    private int useLimit;

    @Getter
    @Setter
    private int couponUse;//int(11) //NULL已使用优惠券数量

    @Getter
    @Setter
    private int couponReceive;//int(11) //NULL已领取优惠券数量

    @Getter
    @Setter
    private String categoryId;//varchar(500)// NULL栏目id组合

    @Getter
    @Setter
    private String mainImages;//var;//char(150) //NULL上传图片地址

    @Getter
    @Setter
    private String categoryName;//varchar(100) //NULL栏目名组合

    @Getter
    @Setter
    private String couponsType;//优惠券类型

    @Getter
    @Setter
    private String couponsCode;//优惠券代码（SP）

    @Getter
    @Setter
    private long sellerId;//商家优惠券创建者

    @Getter
    @Setter
    private String sellerName;//店铺编号

    @Getter
    @Setter
    private String shopName;//店铺编号

    @Getter
    @Setter
    private String shopId;//店铺编号

    @Getter
    @Setter
    private String startTimeStr;
    @Getter
    @Setter
    private String endTimeStr;
    @Getter
    @Setter
    private int amountStr;
}
