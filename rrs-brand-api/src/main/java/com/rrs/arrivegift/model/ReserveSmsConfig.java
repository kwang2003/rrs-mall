package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zf on 2014/10/16.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("serial")
public class ReserveSmsConfig implements Serializable {
    @Getter
    @Setter			
    private Long id;//           bigint(11)

    @Getter
    @Setter
    private Long userId;//       bigint(11)     用户ID
    
    @Getter
    @Setter
    private Long shopId;//       bigint(11)     店铺ID

    @Getter
    @Setter
    private String smsInfo;//     varchar(200)   预约短信内容
    
    @Getter
    @Setter
    private int type;//      int(11)   短信类型 '1 商家 2 体验馆MALL' ,

    @Getter
    @Setter		
    private int userType;//      int(11)   用户类型'1 商家 2 个人' ,

    @Getter
    @Setter
    private int enable;//      varchar(11)   短信状态 '0 启用 1 停用' ,

    @Getter
    @Setter
    private Date createdAt;//   datetime       创建时间
    	
    @Getter
    @Setter
    private Date updatedAt;//   datetime       修改时间
}
