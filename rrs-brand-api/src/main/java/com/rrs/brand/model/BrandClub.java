package com.rrs.brand.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by temp on 2014/7/10.
 */

@ToString
@EqualsAndHashCode
public class BrandClub implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String brandName;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Date createTime;
    @Getter
    @Setter
    private Date updateTime;


    @Getter
    @Setter
    private String brandAppNo;

    @Getter
    @Setter
    private String brandEnName;

    @Getter
    @Setter
    private String brandLogo;

    @Getter
    @Setter
    private String brandDesc;

    @Getter
    @Setter
    private String brandQualify;

    @Getter
    @Setter
    private String brandTradeMark;

    @Getter
    @Setter
    private String brandAuthor;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private String status;


    @Getter
    @Setter
    private Long brandOutId;

    @Getter
    @Setter
    private String approReason;

    @Getter
    @Setter
    private Long brandTypeId;

    @Getter
    @Setter
    private String frozenStatus;

    @Getter
    @Setter
    private String brandMainImg;

    @Getter
    @Setter
    private double baozhengFee;

    @Getter
    @Setter
    private double jishuFee;
    @Getter
    @Setter
    private String http2;
}
