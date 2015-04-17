package com.aixforce.rrs.purify.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:净水组件
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@ToString
@EqualsAndHashCode
public class PurifyAssembly implements Serializable {
    private static final long serialVersionUID = -3805721685372914949L;
    
    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long categoryId;            //系列编号

    @Getter
    @Setter
    private String assemblyName;        //组件名称

    @Getter
    @Setter
    private Integer assemblyTotal;      //组件价格

    @Getter
    @Setter
    private String assemblyIntroduce;   //组件介绍

    @Getter
    @Setter
    private String assemblyImage;       //组件图片地址

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间
}
