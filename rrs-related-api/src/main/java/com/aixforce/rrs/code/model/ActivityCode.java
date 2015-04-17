package com.aixforce.rrs.code.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wanggen on 14-7-3.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCode implements Serializable {

    private static final long serialVersionUID = -9089608879784151816L;

    private Long id;                //自增主键

    private String code;            //优惠码编码

    private Long activityId;        //优惠活动ID

    private String activityName;    //优惠活动名称

    private Integer activityType;   //活动类别[公开码|渠道码]

    private Integer usage;          //使用次数

    private Date createdAt;         //创建时间

    private Date updatedAt;         //修改时间

}
