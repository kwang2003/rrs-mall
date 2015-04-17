package com.aixforce.item.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-17
 */
public class BaseSku implements Serializable {
    private static final long serialVersionUID = -9161545387239480139L;


    @Getter
    @Setter
    protected String outerId; //对应外部的id

    @Getter
    @Setter
    protected String model; //型号

    @Getter
    @Setter
    protected Integer price;

    @Getter
    @Setter
    protected String attributeKey1;

    @Getter
    @Setter
    protected String attributeName1;

    @Getter
    @Setter
    protected String attributeValue1;

    @Getter
    @Setter
    protected String attributeKey2;

    @Getter
    @Setter
    protected String attributeName2;

    @Getter
    @Setter
    protected String attributeValue2;
}
