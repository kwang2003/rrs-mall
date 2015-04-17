package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhum01 on 2014/10/21.
 */
@ToString
@EqualsAndHashCode
public class ReserveSmsConfigMall implements Serializable {
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Long province;

    @Getter
    @Setter
    private Long city;

    @Getter
    @Setter
    private Long district;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String phone;

    @Getter
    @Setter
    private String secondLeavelDomain;

    @Getter
    @Setter
    private String weekday;

    @Getter
    @Setter
    private String amStart;

    @Getter
    @Setter
    private String amEnd;



    @Getter
    @Setter
    private String pmStart;


    @Getter
    @Setter
    private String pmEnd;


    @Getter
    @Setter
    private String status;


    @Getter
    @Setter
    private String useYn;


    @Getter
    @Setter
    private String createAt;


    @Getter
    @Setter
    private Date createDate;

    @Getter
    @Setter
    private String updateAt;


    @Getter
    @Setter
    private Date updateDate;
}
