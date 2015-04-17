/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.dto;

import com.aixforce.search.Pair;
import com.aixforce.search.SearchFacet;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-04
 */
public class FacetSearchResult implements Serializable {
    private static final long serialVersionUID = -4823400869781857784L;

    @Getter
    @Setter
    private Long total;
    @Getter
    @Setter
    private List<RichItem> items = Collections.emptyList();   //for dubbo serialization sake
    @Getter
    @Setter
    private List<AttributeNavigator> attributes = Collections.emptyList();  //for dubbo serialization sake
    @Getter
    @Setter
    private List<SearchFacet> categories = Collections.emptyList();   //for dubbo serialization sake
    @Getter
    @Setter
    private List<Pair> breadCrumbs = Collections.emptyList();     //for dubbo serialization sake
    @Getter
    @Setter
    private List<Pair> chosenAttributes = Collections.emptyList();  //for dubbo serialization sake
    @Getter
    @Setter
    private List<SearchFacet> brands = Collections.emptyList();
    @Getter
    @Setter
    private List<Pair> chosenBrands = Collections.emptyList();
    @Getter
    @Setter
    private String fcName; //如果前台传入fcid，后台相应返回fcName

    public static class AttributeNavigator implements Serializable {
        private static final long serialVersionUID = -7690146242950003488L;
        @Getter
        @Setter
        private String key;
        @Getter
        @Setter
        private Set<SearchFacet> values = Collections.emptySet();  //for dubbo serialization sake

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof AttributeNavigator)) {
                return false;
            }
            AttributeNavigator that = (AttributeNavigator) obj;
            return Objects.equal(key, that.getKey());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("key", key).add("values", values).toString();
        }
    }

}
