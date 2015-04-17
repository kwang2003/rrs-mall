package com.aixforce.item.dto;

import com.aixforce.item.model.BaseSku;
import com.aixforce.item.model.Sku;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-22
 */
@ToString
public class SkuGroup implements Serializable {
    private static final long serialVersionUID = -675328626652187464L;

    @Getter
    private final LinkedHashMap<String,List<Map<String,String>>> attributes;

    public SkuGroup(List<? extends BaseSku> skus) {
        attributes = Maps.newLinkedHashMap();
        for (BaseSku sku : skus) {
            String attributeKey1 = sku.getAttributeKey1();
            if (!Strings.isNullOrEmpty(attributeKey1)) {
                List<Map<String,String>> skuTuples;
                if(attributes.containsKey(attributeKey1)){
                    skuTuples = attributes.get(attributeKey1);

                }else{
                    skuTuples = Lists.newArrayList();
                    attributes.put(attributeKey1,skuTuples);
                }
                Map<String,String> skuTuple = Maps.newHashMapWithExpectedSize(1);
                skuTuple.put(sku.getAttributeName1(), sku.getAttributeValue1());
                if(!skuTuples.contains(skuTuple)){
                    skuTuples.add(skuTuple);
                }
            }

            String attributeKey2 = sku.getAttributeKey2();
            if (!Strings.isNullOrEmpty(attributeKey2)) {
                List<Map<String,String>> skuTuples;
                if(attributes.containsKey(attributeKey2)){
                    skuTuples = attributes.get(attributeKey2);

                }else{
                    skuTuples = Lists.newArrayList();
                    attributes.put(attributeKey2,skuTuples);
                }
                Map<String,String> skuTuple = Maps.newHashMapWithExpectedSize(1);
                skuTuple.put(sku.getAttributeName2(), sku.getAttributeValue2());
                if(!skuTuples.contains(skuTuple)){
                    skuTuples.add(skuTuple);
                }
            }
        }
    }
}
