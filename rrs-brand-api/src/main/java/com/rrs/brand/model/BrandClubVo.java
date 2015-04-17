package com.rrs.brand.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by temp on 2014/7/31.
 */

@ToString
@EqualsAndHashCode
public class BrandClubVo implements Serializable {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String brandName;

    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private Date createTime;

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
    private String brandOutId;

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
    private String brandTypeName;
}