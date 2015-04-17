package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhum01 on 2014/10/24.
 */
@ToString
@EqualsAndHashCode
public class Experince implements Serializable {
    @Getter
    @Setter
    private Long  id;               //bigint(20)
    @Getter
    @Setter
    private String experiencename;   //varchar(100)    体验馆名称信息
    @Getter
    @Setter
    private Long brandId;          //bigint(20)      对应品牌表(品牌表应该作为基础数据表存在)
    @Getter
    @Setter
    private String serviceTime;      //varchar(500)    营业时间
    @Getter
    @Setter
    private String serviceTel;       //varchar(500)    服务电话
    @Getter
    @Setter
    private String trafficLines;     //varchar(1000)   交通线路
    @Getter
    @Setter
    private String description;    //text            描述和简介
    @Getter
    @Setter
    private String address;        //varchar(500)    详细地址信息
    @Getter
    @Setter
    private String thumImage;   //varchar(127)    列表缩略图
    @Getter
    @Setter
    private String mainImage;        //varchar(127)    详细页面展示图
    @Getter
    @Setter
    private Long province;         //int(11)         区域信息对应区域基础数据表
    @Getter
    @Setter
    private Long city;             //int(11)         区域信息对应区域基础数据表
    @Getter
    @Setter
    private Long region;           //int(11)         区域信息对应区域基础数据表
    @Getter
    @Setter
    private String mapurl;           //varchar(100)    嵌入体验馆地图对应的url地址
    @Getter
    @Setter
    private Date createDate;       //timestamp       创建时间
    @Getter
    @Setter
    private Long creater;          //int(11)         对应用户表的主键信息
    @Getter
    @Setter
    private String locationx;        //varchar(20)     经度
    @Getter
    @Setter
    private String locationy;        //varchar(20)     纬度
    @Getter
    @Setter
    private String provinceName;     //varchar(64)     冗余数据 省名称
    @Getter
    @Setter
    private String cityName;         //varchar(50)     冗余数据 市名称
    @Getter
    @Setter
    private String regionName;       //varchar(50)     冗余数据 区名称
    @Getter
    @Setter
    private String shopurl;          //varchar(150)    店铺URL信息
    @Getter
    @Setter
    private Long experType;       ; //int(11)         体验馆类型 1 旗舰店 2 专卖店
    @Getter
    @Setter
    private Long orderBy;        //int(11)         排序字段
    @Getter
    @Setter
    private Long experUserId;//bigint(20)      体验馆对应用户
    @Getter
    @Setter
    private String taxRegisterNo;    //varchar(32)     税务登记号
    @Getter
    @Setter
    private String licence;        //varchar(137)    营业执照图片
    @Getter
    @Setter
    private String certificate;      //varchar(137)    税务登记证
    @Getter
    @Setter
    private String openinglicenses;  //varchar(137)    开户许可证
    @Getter
    @Setter
    private String orgcertificate;   //varchar(137)    组织机构代码证
    @Getter
    @Setter
    private String frontlicense;     //varchar(137)    法人身份正面照
    @Getter
    @Setter
    private String backlicense;      //varchar(137)    法人身份背面照
    @Getter
    @Setter
    private String contractOne;      //varchar(137)    合同图片一
    @Getter
    @Setter
    private String contractTwo;      //varchar(137)    合同图片二
    @Getter
    @Setter
    private String experCode;        //varchar(100)    体验馆编码 即8码
    @Getter
    @Setter
    private Long rate;             //decimal(9,4)    费率
    @Getter
    @Setter
    private Long rateone;          //decimal(9,4)    费率+1
    @Getter
    @Setter
    private String email;            //varchar(50)     邮箱地址
    @Getter
    @Setter
    private String tele;             //varchar(20)     联系电话
    @Getter
    @Setter
    private String weekday;          //varchar(100)    营业时间（周几）
    @Getter
    @Setter
    private String amStart;          //varchar(50)     上午开始时间
    @Getter
    @Setter
    private String amEnd;            //varchar(50)     上午结束时间
    @Getter
    @Setter
    private String pmStart;          //varchar(50)     下午开始时间
    @Getter
    @Setter
    private String pmEnd;            //varchar(50)     下午结束时间
    @Getter
    @Setter
    private String status;           //varchar(2)      1 已启用 0未启用

}
