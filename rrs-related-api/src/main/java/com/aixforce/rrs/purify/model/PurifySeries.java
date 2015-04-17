package com.aixforce.rrs.purify.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 净水系列
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@ToString
@EqualsAndHashCode
public class PurifySeries implements Serializable {
    private static final long serialVersionUID = -306592143280214528L;

    @Getter
    @Setter
    private Long id;                //自赠主键

    @Getter
    @Setter
    private String seriesName;      //系列名称

    @Getter
    @Setter
    private String seriesIntroduce; //净水系列介绍

    @Getter
    @Setter
    private String seriesImage;     //净水系列图片地址

    @Getter
    @Setter
    private Long siteId;            //站点编号

    @Getter
    @Setter
    private Date createdAt;         //创建时间

    @Getter
    @Setter
    private Date updatedAt;         //修改时间
}
