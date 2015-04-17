package com.rrs.arrivegift.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by zhum01 on 2014/10/20.
 */
public class ArriveSmsInfoDto implements Serializable {

    @Getter
    @Setter
    private Long shopId; //店铺名称

    @Getter
    @Setter
    private String shopName; //店铺名称

    @Getter
    @Setter
    private String address; //店铺地址

    @Getter
    @Setter
    private String phoneNo; //联系电话

    @Getter
    @Setter
    private int reserveType;//类型 1上午 2下午

    @Getter
    @Setter
    private String sendName; //填写名称

    @Getter
    @Setter
    private String sendTele;//填写电话


    @Getter
    @Setter
    private String sendDate;//预约时间

    @Getter
    @Setter
    private String sendTime;//预约时间


    @Getter
    @Setter
    private Long shopConfigId;

    @Getter
    @Setter
    private Long type = 1L;

}
