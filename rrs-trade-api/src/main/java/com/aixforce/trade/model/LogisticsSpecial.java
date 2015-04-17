package com.aixforce.trade.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:特殊地区的收费
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
@ToString
@EqualsAndHashCode
public class LogisticsSpecial implements Serializable{
    private static final long serialVersionUID = 4263516927507715230L;

    @Getter
    @Setter
    private Long id;                //自增主键

    @Getter
    @Setter
    private Long modelId;           //运费模板编号

    @Getter
    @Setter
    private String addressModel;    //存储一个地区编号的json字段(主要是为后期可能使用区域标注->东北区等)

    @Getter
    @Setter
    private Integer firstAmount;    //首批数量在N范围内

    @Getter
    @Setter
    private Integer firstFee;       //首批价格多少

    @Getter
    @Setter
    private Integer addAmount;      //每增加N数量

    @Getter
    @Setter
    private Integer addFee;         //增加N价格

    @Getter
    @Setter
    private Date createdAt;          //创建时间

    @Getter
    @Setter
    private Date updatedAt;          //修改时间
}
