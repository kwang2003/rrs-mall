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
@SuppressWarnings("serial")
public class ShopCoupons implements Serializable {

	@Getter
    @Setter
    private  long id; 
    @Getter
    @Setter
    private  String sellername;  //商家账号
    @Getter
    @Setter
    private  String shopname;//商家名称
    @Getter
    @Setter
    private  String cpname;//商家名称
    @Getter
    @Setter
    private  String  categoryname;//优惠
    @Getter
    @Setter
    private  Date created_at;//创建日期
    @Getter
    @Setter
    private int sendnum;//发券数

    @Getter
    @Setter
    private int couponReceive;//领券数

    @Getter
    @Setter
    private int couponUse;//用券数

    @Getter
    @Setter
    private int STATUS;//优惠券状态：未生效（0）暂停（1）生效（2）失效(3)撤销(4)
 
}
