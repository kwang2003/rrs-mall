package com.aixforce.rrs.purify.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 净水选择类目
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
public class PurifyCategory implements Serializable {
    private static final long serialVersionUID = 7501895715673775744L;
    
    @Getter
    @Setter
    private Long id;                    //自赠主键

    @Getter
    @Setter
    private Long seriesId;              //系列编号

    @Getter
    @Setter
    private Integer stage;              //阶段顺序编号

    @Getter
    @Setter
    private String categoryName;        //组件类目名称

    @Getter
    @Setter
    private String categoryImage;       //组件类目图片地址

    @Getter
    @Setter
    private Date createdAt;             //创建时间

    @Getter
    @Setter
    private Date updatedAt;             //修改时间
}
