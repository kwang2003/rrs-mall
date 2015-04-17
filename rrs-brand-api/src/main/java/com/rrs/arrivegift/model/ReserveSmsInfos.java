package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhum01 on 2014/10/15.
 */
@ToString
@EqualsAndHashCode
public class ReserveSmsInfos implements Serializable {
    @Getter
    @Setter
    private Long id;//           bigint(11)


    @Getter
    @Setter
    private Long shopId;//       bigint(11)     店铺Id

    @Getter
    @Setter
    private Long userId;//       bigint(11)     用户ID

    @Getter
    @Setter
    private String shopName;//     varchar(100)   店铺/体验馆MALL名称

    @Getter
    @Setter
    private String userName;// 填写的用户名

    @Getter
    @Setter
    private String address;//      varchar(200)   地址

    @Getter
    @Setter
    private String phoneNo;//      varchar(200)   联系电话

    @Getter
    @Setter
    private int reserveType;//  varchar(15)    预约类型 1上午 2下午

    @Getter
    @Setter
    private String reserveDate;//  varchar(15)    预约时间

    @Getter
    @Setter
    private String reserveTime;//  varchar(15)    预约时间

    @Getter
    @Setter
    private Long configId;//

    @Getter
    @Setter
    private String smsInfo;//      varchar(200)   预约短信内容

    @Getter
    @Setter
    private Long type;//         int(11)        1 商家 2 体验馆MALL

    @Getter
    @Setter
    private Long userType;//     int(11)        1 商家 2 个人

    @Getter
    @Setter
    private Long state;//        int(11)        0 启用 1 停用
    
    @Getter		
    @Setter
    private Date reserveStart;//   datetime       预约开始时间
        
    @Getter
    @Setter
    private Date reserveEnd;//   datetime       预约截至时间

    @Getter
    @Setter
    private Date created_at;//   datetime       创建时间

    @Getter		
    @Setter
    private Date updated_at;//   datetime       修改时间
}
