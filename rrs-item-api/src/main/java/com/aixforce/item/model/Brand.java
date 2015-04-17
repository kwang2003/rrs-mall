package com.aixforce.item.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yangzefeng on 14-1-15
 */
@ToString
@EqualsAndHashCode
public class Brand implements Serializable{

    private static final long serialVersionUID = 4201523580987763053L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String englishName;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private Long parentId;

    @Getter
    @Setter
    private Date createdAt;

    @Getter
    @Setter
    private Date updatedAt;
}
