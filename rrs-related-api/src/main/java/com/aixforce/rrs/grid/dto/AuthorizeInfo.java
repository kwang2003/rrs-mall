package com.aixforce.rrs.grid.dto;

import com.aixforce.category.model.BackCategory;
import com.aixforce.item.model.Brand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-06-06
 */
@ToString
public class AuthorizeInfo implements Serializable {

    private static final long serialVersionUID = -5666685458223801535L;

    @Getter
    @Setter
    private List<Brand> brands;

    @Getter
    @Setter
    private List<BackCategory> categories;

    @Getter
    @Setter
    List<Map<String, List<Long>>> regions;
}
