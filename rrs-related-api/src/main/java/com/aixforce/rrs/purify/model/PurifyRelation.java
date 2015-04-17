package com.aixforce.rrs.purify.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:净水组件关联
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@ToString
public class PurifyRelation implements Serializable {
    private static final long serialVersionUID = -6277977857335858540L;

    @Getter
    @Setter
    private Long id;                //自赠主键

    @Getter
    @Setter
    private Long assemblyParent;    //上级组件编号

    @Getter
    @Setter
    private Long assemblyChild;     //下级组件编号

    @Getter
    @Setter
    private Long productId;         //商品编号(当0编号当前组建没有直接关联商品，n表示关联商品的编号)

    @Getter
    @Setter
    private Date createdAt;         //创建时间

    @Getter
    @Setter
    private Date updatedAt;         //修改时间
}
