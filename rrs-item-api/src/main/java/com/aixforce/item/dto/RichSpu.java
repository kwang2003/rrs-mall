package com.aixforce.item.dto;

import com.aixforce.category.model.Spu;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by yangzefeng on 14-1-1
 */
public class RichSpu implements Serializable{
    private static final long serialVersionUID = 4321175174826304460L;

    @Getter
    @Setter
    private Spu spu;

    @Getter
    @Setter
    private String brandName;

    @Getter
    @Setter
    private Map<String, String> attributes;

    @Getter
    @Setter
    private Map<String, String> skus;

    @Getter
    @Setter
    private Boolean isEhaier;//是否是ehaier商家

    @Getter
    @Setter
    private boolean hasService;//是否有服务模版
}
