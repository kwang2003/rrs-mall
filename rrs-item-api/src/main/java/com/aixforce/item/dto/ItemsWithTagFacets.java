package com.aixforce.item.dto;

import com.aixforce.item.model.Item;
import com.aixforce.search.SearchFacet;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-05
 */
public class ItemsWithTagFacets implements Serializable {
    private static final long serialVersionUID = -5336930324387258739L;

    @Getter
    @Setter
    private Long total;

    @Getter
    @Setter
    private List<Item> items = Collections.emptyList();  //for dubbo serialization sake

    @Getter
    @Setter
    private List<SearchFacet> tagFacets = Collections.emptyList(); //for dubbo serialization sake
}
